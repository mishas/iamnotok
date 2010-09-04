package com.google.imnotok;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Puts the phone to the emergency state and notifies the contacts in the
 * emergency contacts' list about the situation.
 *
 * @author Vytautas
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

  private void invokeEmergencyResponse() {
    Log.d(mLogTag, "Invoking emergency response");
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
