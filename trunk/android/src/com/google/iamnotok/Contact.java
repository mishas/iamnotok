package com.google.iamnotok;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;

public class Contact {

	private static final String[] PROJECTION = new String[] { Data._ID,
			Data.IS_SUPER_PRIMARY, Data.DISPLAY_NAME, Phone.TYPE,
			Phone.NUMBER, Email.DATA, Data.MIMETYPE };

	private static final int COL_NAME = 2;
	private static final int COL_PHONE_TYPE = 3;
	private static final int COL_PHONE_NUMBER = 4;
	private static final int COL_EMAIL = 5;
	
	
	
	private static final String WHERE_CLAUSE = Data.CONTACT_ID + " = ? AND ("
			+ Data.MIMETYPE + " =? OR " + Data.MIMETYPE + " =?)";

	private Context context;

	private String id;
	private String name;
	private String phone;
	private String email;

	public Contact(Context context, String id) {
		this.id = id;
		this.context = context;
	}

	public boolean lookup() {
		ContentResolver cr = context.getContentResolver();
		final String[] whereArgs = new String[] { id,
				CommonDataKinds.Email.CONTENT_ITEM_TYPE,
				CommonDataKinds.Phone.CONTENT_ITEM_TYPE };

		Cursor cur = cr.query(Data.CONTENT_URI, PROJECTION, WHERE_CLAUSE,
				whereArgs, Data.IS_SUPER_PRIMARY + " DESC ");
		if (cur != null) {
			try {
				final int mimetypeCol = cur.getColumnIndex(Data.MIMETYPE);
				while (cur.moveToNext()) {
					if (name == null) {
						name = cur.getString(COL_NAME);
					}
					final String mimetype = cur.getString(mimetypeCol);
					if (mimetype.equals(Phone.CONTENT_ITEM_TYPE)) {
						if (phone == null || cur.getInt(COL_PHONE_TYPE) == Phone.TYPE_MOBILE) {
							phone = cur.getString(COL_PHONE_NUMBER);
						}
					} else if (mimetype.equals(Email.CONTENT_ITEM_TYPE)) {
						if (email == null) {
							email = cur.getString(COL_EMAIL);
						}
					}

				}
				return true;
			} finally {
				cur.close();
			}
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return id + ": " + name + " (" + phone + ") <" + email + ">";
	}
}