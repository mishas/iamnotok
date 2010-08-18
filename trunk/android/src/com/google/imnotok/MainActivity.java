package com.google.imnotok;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Shows the main screen of the application.
 */
public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Bind the buttons and set the corresponding on-click listeners
        Button editEmergencyInfoButton = (Button) this.findViewById(R.id.EmergencyInfoButton);
        editEmergencyInfoButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// Send of an intent to start off the EmergencyInfoActivity
				Intent intent = new Intent(MainActivity.this, EmergencyInfoActivity.class);
				MainActivity.this.startActivity(intent);
			}
		});
        
        Button changeApplicationSettingsButton = (Button) this.findViewById(R.id.ApplicationSettingsButton);
        changeApplicationSettingsButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// Send of an intent to start off the EmergencyInfoActivity
				Intent intent = new Intent(MainActivity.this, ApplicationSettingsActivity.class);
				MainActivity.this.startActivity(intent);
			}
		});
    }
}