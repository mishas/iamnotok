package com.google.imnotok;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/** 
 * Tracks the events in the system and infers when emergency has happened.
 * When this is the case, sends off an EmergencyIntent to
 * EmergencyNotificationService to invoke the response to the emergency.
 */
public class PatternTrackingService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
