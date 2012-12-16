package com.google.iamnotok;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class Notification {
	
	public static final String LOG = "Notification";
	public static final long NO_ID = 0;
	public static final String TYPE_SMS = "SMS";
	public static final String TYPE_EMAIL = "EMAIL";
	
	public final long id;
	public final String type;
	public final String target;
	public final String label;
	
	private boolean enabled;
	private boolean dirty;
	
	public static boolean containsTarget(List<Notification> notifications, String target) {
		for (Notification n : notifications) {
			if (n.target.equals(target))
				return true;
		}
		return false;
	}
	
	public static boolean containsEnabled(List<Notification> notifications) {
		for (Notification n : notifications) {
			if (n.isEnabled())
				return true;
		}
		return false;
	}
	
	public static boolean containsDirty(List<Notification> notifications) {
		for (Notification n : notifications) {
			if (n.isDirty())
				return true;
		}
		return false;
	}
	
	public static List<String> enabledTargets(List<Notification> notifications) {
		List<String> enabled = new ArrayList<String>();
		for (Notification n : notifications) {
			if (n.isEnabled())
				enabled.add(n.target);
		}
		return enabled;
	}

	// For creating from system contacts database
	public Notification(String type, String target, String label) {
		this(NO_ID, type, target, label, false);
	}

	// For creating from application database
	public Notification(long id, String type, String target, String label, boolean enabled) {
		this.id = id;
		this.type = type;
		this.target = target;
		this.label = label;
		this.enabled = enabled;
		this.dirty = (id == NO_ID); // Not stored in database yet
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			this.enabled = enabled;
			this.dirty = true;
			Log.d(LOG, (enabled ? "enabled " : "disabled ") + this);
		}
	}
	
	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	@Override
	public String toString() {
		return "<Notifiction id: " + id 
				+ " type: " + type 
				+ " target: " + target 
				+ " label: " + label 
				+ " enabled: " + enabled + ">"; 
	}

}

