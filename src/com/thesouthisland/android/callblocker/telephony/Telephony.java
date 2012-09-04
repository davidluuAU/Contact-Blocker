package com.thesouthisland.android.callblocker.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class Telephony {

	public static Cursor getContactNumbers(int contactId, Context context)
	{
		ContentResolver cursor = context.getContentResolver();	
		
		Cursor phonesCursor = cursor.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + contactId, null, null);		
		
		return phonesCursor;
		
	}
	
}
