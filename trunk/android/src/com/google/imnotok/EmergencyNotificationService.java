package com.google.imnotok;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Puts the phone to the emergency state and notifies the contacts in the
 * emergency contacts' list about the situation.
 *
 * @author Vytautas
 * @author Raquel
 */
public class EmergencyNotificationService extends Service {
  private final static String mLogTag = "ImNotOk - EmergencyNotificationService";
  /**
   * Field name for the boolean that should be passed with the intent to start
   * this service. It tells whether the notification in the top bar should be
   * shown. This notification should be against accidental triggering of
   * emergency. It would allow a user to disable the emergency response within
   * 10 seconds.
   */
  public final static String SHOW_NOTIFICATION_WITH_DISABLE = "showNotification";

  public final static int NORMAL_STATE = 0; 
  public final static int WAITING_STATE = 1; 
  public final static int EMERGENCY_STATE = 2; 
  public static int mApplicationState = NORMAL_STATE;
    
  private final static String STOP_EMERGENCY_INTENT = "com.google.imnotok.STOP_EMERGENCY";
  public final static String I_AM_NOW_OK_ACTION = "com.google.imnotok.I_AM_NOW_OK";
  /** Time allowed for user to cancel the emergency response. */
  private static int waitForMs = 10000;  // miliseconds
  private static final String defaultWaitForSeconds = "10";  // seconds

  private boolean mEmergencyResponseShouldBeDisabled = false;
  private boolean mServiceRunning = false;
  private int mNotificationID = 0;
  private Location mLocation;
  private boolean mSMSNotification = true;
  private boolean mEmailNotification = true;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onStart(Intent intent, int startId) {
    Log.d(mLogTag, "onStart() called");
    
    // Check the user preferences.
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    
    // Cancellation time:
    String delay_time = prefs.getString(
        getString(R.string.edittext_cancelation_delay), defaultWaitForSeconds);
    Log.d("Delay Time" + mLogTag, delay_time);
    try {
      waitForMs = Integer.parseInt(delay_time);
      Log.d(mLogTag, delay_time);
    }catch(NumberFormatException e) {
       Log.e("delay_time", "Delay time "+delay_time+" not well formated");
       waitForMs = Integer.parseInt(defaultWaitForSeconds);
    }
    waitForMs *= 1000;
    
    // SMS notification:
    mSMSNotification = prefs.getBoolean(
        getString(R.string.checkbox_sms_notification),mSMSNotification);
    
    // Email notification allowed:
    mEmailNotification = prefs.getBoolean(
        getString(R.string.checkbox_email_notification), mEmailNotification);

    // TODO(raquelmendez): Should we tell the user if no notification method is 
    // active?
    
    if (!mServiceRunning) {
      Log.d(mLogTag, "Starting the service");
      changeState(WAITING_STATE);
      mServiceRunning = true;
      boolean showNotification = intent.getBooleanExtra(SHOW_NOTIFICATION_WITH_DISABLE, false);
      this.setEmergencyFlagValue(true);

      // Start location tracker from here since it takes some time to get
      // the first GPS fix.
      mLocation = null;
      this.startLocationTracker();

      if (showNotification) {
        this.showDisableNotificationAndWaitToInvokeResponse();
      } else {
        this.invokeEmergencyResponse();
      }
      super.onStart(intent, startId);
    } else {
      Log.d(mLogTag, "Service already running");
    }
  }

  /**
   * Sends a sms to another device
   **/
  private void sendTextMessage() {
    Thread messageSender = new Thread(new Runnable() {
      @Override
      public void run() {
        Log.d(mLogTag, "Sending sms");
        String phoneNumber = "5556";
        Location loc = getLocation();
        Log.d(mLogTag, "Sending the location - latitude: " + loc.getLatitude() + ", longitude: "
            + loc.getLongitude());
        String message =
            "I am not OK! My current location is: latitude " + loc.getLatitude() + ", longitude "
                + loc.getLongitude();

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI =
            PendingIntent.getBroadcast(EmergencyNotificationService.this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(
            EmergencyNotificationService.this, 0, new Intent(DELIVERED), 0);

        // ---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver() {
          @Override
          public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
              case Activity.RESULT_OK:
                Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                break;
              case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                break;
              case SmsManager.RESULT_ERROR_NO_SERVICE:
                Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                break;
              case SmsManager.RESULT_ERROR_NULL_PDU:
                Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                break;
              case SmsManager.RESULT_ERROR_RADIO_OFF:
                Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                break;
            }
          }
        }, new IntentFilter(SENT));

        // ---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver() {
          @Override
          public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
              case Activity.RESULT_OK:
                Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                break;
              case Activity.RESULT_CANCELED:
                Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                break;
            }
          }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

      }
    });
    messageSender.start();
  }


  /**
   * Sends an email
   */
  private void sendEmail() {
    // TODO(raquelmendez): chose the correct contact to send the email to
    String contact = "raquelmendez@google.com";

    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    emailIntent.setType("plain/text");
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, contact);
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.i_am_not_ok_start);
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, R.string.i_am_not_ok_start);
    Intent.createChooser(emailIntent, "Send mail...");
  }

  private void invokeEmergencyResponse() {
    Log.d(mLogTag, "Invoking emergency response");

    // Check the user preferences:
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    // If the user has enable the sms notifications, send them:

    if(mSMSNotification) {
      Log.d(mLogTag, "Sending sms...");
      sendTextMessage(); 
    }  
    if(mEmailNotification) {
      Log.d(mLogTag, "Sending email...");
      sendEmail(); 
    } 

    mServiceRunning = false;
  }

  private void showDisableNotificationAndWaitToInvokeResponse() {
    Log.d(mLogTag, "Showing notification and waiting");

    // Reset the flag.
    setEmergencyFlagValue(true);

    // Show a notification.
    final NotificationManager notificationManager =
        (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

    Intent disableEmergencyIntent = new Intent(STOP_EMERGENCY_INTENT);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, disableEmergencyIntent, 0);

    Notification notification =
        new Notification(android.R.drawable.stat_sys_warning,
            this.getString(R.string.emergency_response_starting), System.currentTimeMillis());
    // Notification should be canceled when clicked
    notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
    notification.setLatestEventInfo(this, this.getString(R.string.emergency_response_starting),
        this.getString(R.string.click_to_disable), pendingIntent);

    notificationManager.notify(mNotificationID, notification);

    // Register a receiver that can receive the cancellation intents.
    BroadcastReceiver cancellationReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(mLogTag, "Received cancellation intent...");
        // TODO(vytautas): check the state of the service when continuous mode
        // is introduced.
        EmergencyNotificationService.this.setEmergencyFlagValue(false);
        // TODO(vytautas): make this multi-thread friendly.
        EmergencyNotificationService.this.mServiceRunning = false;
      }
    };
    IntentFilter intentFilter = new IntentFilter(STOP_EMERGENCY_INTENT);
    this.registerReceiver(cancellationReceiver, intentFilter);

    // Start the waiting in a separate thread since otherwise the service will
    // not be able to receive the intent for cancelling the emergency response.
    Thread waiterThread = new Thread(new Runnable() {
      @Override
      public void run() {
        
        
        try {
          Thread.sleep(waitForMs);
          changeState(EMERGENCY_STATE);
        } catch (InterruptedException exception) {
          exception.printStackTrace();
        } finally {
          notificationManager.cancel(mNotificationID++);
          if (EmergencyNotificationService.this.getEmergencyFlagValue()) {
            invokeEmergencyResponse();
          }
        }
      }
    });
    waiterThread.start();
  }

  private synchronized void setEmergencyFlagValue(boolean newValue) {
    mEmergencyResponseShouldBeDisabled = newValue;
  }

  private synchronized boolean getEmergencyFlagValue() {
    return mEmergencyResponseShouldBeDisabled;
  }

  private synchronized void setLocation(Location location) {
    mLocation = location;
    this.notifyAll();
  }

  /**
   * Should be called from a separate thread since may block waiting for
   * location.
   */
  @SuppressWarnings("unused")
  private synchronized Location getLocation() {
    // Construct a copy of the current location.
    while (mLocation == null) {
      try {
        Log.d(mLogTag, "Waiting for location");
        this.wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return new Location(mLocation);
  }

  private void startLocationTracker() {
    // TODO(vytautas): get permission to change settings and make sure we
    // can turn the GPS on even if the user has disabled it.
    LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    LocationListener mlocListener = new UserLocationListener();
    mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
  }

  private void changeState(int new_state) {
    mApplicationState = new_state;
    RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.emergency_button_widget);
    EmergencyButtonWidgetProvider.setupViews(this, views);    
  }
  
  private class UserLocationListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {
      Log.d(mLogTag, "Location has changed");
      if (EmergencyNotificationService.this.mLocation != null) {
        if (location.getAccuracy() < EmergencyNotificationService.this.mLocation.getAccuracy()) {
          EmergencyNotificationService.this.setLocation(location);
        }
      } else {
        EmergencyNotificationService.this.setLocation(location);
      }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

  }
}
