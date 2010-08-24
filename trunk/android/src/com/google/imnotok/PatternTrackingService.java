package com.google.imnotok;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Tracks the events in the system and infers when emergency has happened. When
 * this is the case, sends off an EmergencyIntent to
 * EmergencyNotificationService to invoke the response to the emergency.
 */
public class PatternTrackingService extends Service {
  private final String mLogTag = "ImNotOk - PatternTrackingService";

  // Flag used for the MainActivity to check if the process is running before
  // trying to start off the process again.
  public static boolean mServiceRunning = false;

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    Log.d(mLogTag, "Starting the service");

    if (!this.startTrackingPatterns()) {
      this.informUserAboutProblemTrackingPatterns();
      this.stopSelf();
    } else {
      mServiceRunning = true;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    // TODO Auto-generated method stub
    return null;
  }

  private boolean startTrackingPatterns() {
    Thread trackerThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // Sleep for some time, then invoke the emergency service - just for
          // testing purposes..
          Thread.sleep(50000);

          Intent emergencyIntent =
              new Intent(PatternTrackingService.this, EmergencyNotificationService.class);
          PatternTrackingService.this.startService(emergencyIntent);          
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      }
    });
    trackerThread.start();
    return true;
  }

  private void informUserAboutProblemTrackingPatterns() {
    // Show a notification to the user that we were unable to start the pattern
    // tracker.
    NotificationManager notificationManager =
        (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

    Intent retryLaunchingPatternTrackerIntent = new Intent(this, this.getClass());
    PendingIntent pendingIntent =
        PendingIntent.getService(this, 0, retryLaunchingPatternTrackerIntent, 0);

    Notification notification = new Notification(android.R.drawable.stat_notify_error,
        this.getString(R.string.unable_to_track_actions_for_emergency_title),
        System.currentTimeMillis());
    // Notification should be canceled when clicked
    notification.flags |= Notification.FLAG_AUTO_CANCEL;
    notification.setLatestEventInfo(this,
        this.getString(R.string.unable_to_track_actions_for_emergency_title),
        this.getString(R.string.unable_to_track_actions_for_emergency_msg), pendingIntent);

    notificationManager.notify(0, notification);
  }
}
