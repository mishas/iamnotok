package com.google.imnotok;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Activity for filling out the emergency information of the user. I think
 * we could try associating the emergency data with the Google account so
 * that if user changes the phones, emergency data will be readily available.
 * http://developer.android.com/reference/android/accounts/AccountManager.html
 * may be worth having a look at for these purposes.
 */
public class ContactPickerActivity extends Activity {
  private static final int CONTACT_PICKER_RESULT = 1001;
  
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.contactpicker);
  
    // Bind the buttons and set the corresponding on-click listeners
    Button ViewContactsButton = (Button) this.findViewById(R.id.ViewContactsButton);
    ViewContactsButton.setOnClickListener(new OnClickListener() {          
        @Override
        public void onClick(View v) {
          // TODO: Show the contacts that are stored as candidates to send sms.
        }
    });
      
    Button AddContactButton = (Button) this.findViewById(R.id.AddContactButton);
    AddContactButton.setOnClickListener(new OnClickListener() {          
        @Override
        public void onClick(View v) {
          //Launch the contact picker to let the user select a contact to inform.
          Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
              Contacts.CONTENT_URI);
         startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
         // TODO: Get the contact id and add it to the shared preferences.
        }
    });
    
    // TODO: Allow delete contacts that have been selected
      

  }
}
