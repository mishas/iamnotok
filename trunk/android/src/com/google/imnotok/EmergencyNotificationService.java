package com.google.imnotok;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Puts the phone to the emergency state and notifies the contacts in the
 * emergency contacts' list about the situation.
 * 
 * @author Vytautas
 */
public class EmergencyNotificationService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
