package com.google.imnotok;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ApplicationSettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		this.addPreferencesFromResource(R.xml.preferences);
	}
}
