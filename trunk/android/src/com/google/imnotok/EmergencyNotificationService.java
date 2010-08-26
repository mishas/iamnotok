package com.google.imnotok;

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
  private static String mLogTag = "ImNotOk - EmergencyNotificationService";  

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onStart(Intent intent, int startId) {
    Log.d(mLogTag, "Starting the service");
    super.onStart(intent, startId);
  }
}
