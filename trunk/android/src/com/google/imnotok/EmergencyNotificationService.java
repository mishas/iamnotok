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
import android.os.IBinder;
import android.telephony.gsm.SmsManager;
import android.util.Log;
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
  private final static String STOP_EMERGENCY = "stopEmergency";
  /** Time allowed for user to cancel the emergency response. */
  private final static int waitForMs = 20000;

  private boolean mEmergencyResponseShouldBeDisabled = false;
  private boolean mServiceRunning = false;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onStart(Intent intent, int startId) {
    Log.d(mLogTag, "onStart() called");

    // Check if the intent was called to stop the emergency.
    boolean stopEmergency = intent.getBooleanExtra(STOP_EMERGENCY, false);
    if (stopEmergency) {
      Log.d(mLogTag, "Disabling the response");
      this.setEmergencyFlagValue(false);
      return;
    }
    if (!mServiceRunning) {
      Log.d(mLogTag, "Starting the service");
      mServiceRunning = true;
      boolean showNotification =
          intent.getBooleanExtra(SHOW_NOTIFICATION_WITH_DISABLE, false);
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
  
  //---sends an SMS message to another device---
  private void sendTextMessage() {
    
//    String[] contacts = getContactNumber();

//    String phoneNumber = contacts[0];
//    String message = contacts[1];
     
    
    String phoneNumber = "5556";
    //---TODO: Include the GPS location in the message.
    String message = "I am not OK!";
    
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
 
    PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
        new Intent(SENT), 0);
    PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
        new Intent(DELIVERED), 0);
 
        //---when the SMS has been sent---
    registerReceiver(new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode())
            {
                case Activity.RESULT_OK:
                    Toast.makeText(getBaseContext(), "SMS sent", 
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(getBaseContext(), "Generic failure", 
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(getBaseContext(), "No service", 
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(getBaseContext(), "Null PDU", 
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(getBaseContext(), "Radio off", 
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));
 
        //---when the SMS has been delivered---
    registerReceiver(new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode())
            {
                case Activity.RESULT_OK:
                    Toast.makeText(getBaseContext(), "SMS delivered", 
                            Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(getBaseContext(), "SMS not delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED));        
 
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);        
    } 
  
//  private String[] getContactNumber(){ 
// // Form an array specifying which columns to return. 
//    String[] projection = new String[] {
//                                People.NAME,
//                                People.NUMBER
//                              };
//
//    // Get the base URI for the People table in the Contacts content provider.
//    Uri contacts =  People.CONTENT_URI;
//
//    // Make the query.
//    ContentResolver contentResolver = this.getContentResolver();
//    Cursor cur = contentResolver.query(contacts,
//                                       projection, // Which columns to return 
//                                       null,       // Which rows to return (all rows)
//                                       null,       // Selection arguments (none)
//                                       //Put the results in ascending order by name
//                                       People.NAME + " ASC");
//    
//    String[] phone_message = new String[3];
//    phone_message[0] = "5556";
//    int i = 0;
//    if (cur.moveToFirst()) {
//        int phoneColumn = cur.getColumnIndex(People.NUMBER);
//        do {
//          ++i;
//            // Get the field values
//            String phoneNumber = cur.getString(phoneColumn);
//            //phone_message[i] = phoneNumber;
//        } while (cur.moveToNext());       
//    }
//    
//    
//    
//    
//    return phone_message;
//}  

  private void invokeEmergencyResponse() {
    Log.d(mLogTag, "Invoking emergency response");
    sendTextMessage();    
    mServiceRunning = false;
  }

  private void showDisableNotificationAndWaitToInvokeResponse() {
    Log.d(mLogTag, "Showing notification and waiting");
    
    // Reset the flag.
    setEmergencyFlagValue(true);
    
    // Show a notification.
    NotificationManager notificationManager =
        (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

    Intent disableEmergencyIntent = new Intent(this, this.getClass());
    disableEmergencyIntent.putExtra(STOP_EMERGENCY, true);
    PendingIntent pendingIntent =
        PendingIntent.getService(this, 0, disableEmergencyIntent, 0);    

    Notification notification = new Notification(android.R.drawable.stat_sys_warning,
        this.getString(R.string.emergency_response_starting),
        System.currentTimeMillis());
    // Notification should be canceled when clicked
    notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
    notification.setLatestEventInfo(this,
        this.getString(R.string.emergency_response_starting),
        this.getString(R.string.click_to_disable), pendingIntent);
    
    notificationManager.notify(0, notification);
    
    // Start the waiting in a separate thread since otherwise the service will
    // not be able to receive the intent for cancelling the emergency response.
    Thread waiterThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(waitForMs);
        } catch (InterruptedException exception) {
          exception.printStackTrace();
        } finally {
          // TODO(vytautas): cancel the notification.
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
}
