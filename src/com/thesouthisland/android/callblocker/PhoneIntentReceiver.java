/*
 * Copyright (C) 2010 The South Island
 *
 */

package com.thesouthisland.android.callblocker;

import java.lang.reflect.Method;
import java.util.Calendar;

import com.android.internal.telephony.ITelephony;
import com.thesouthisland.android.callblocker.databases.BlockedCallsDB;
import com.thesouthisland.android.callblocker.databases.ContactsDB;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneIntentReceiver extends BroadcastReceiver{
	

	private static final String TAG = "PhoneIntentReceiver";
	private TelephonyManager telephonyManager;
	
	private Cursor mContactsCursor;
	
    private ContactsDB databaseHelper;
	
	private Context mContext;
	
	private BlockedCallsDB blockedCallsDb;
	
	public BlockedCallsDB getBlockedCall() {
		return blockedCallsDb;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {

    	telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    	blockedCallsDb = new BlockedCallsDB(context);
		
		mContext = context;
		
    	final Bundle bundle = intent.getExtras();
        
        if (null == bundle)
            return;
        
        Log.d(TAG, "IncomingCallReceiver");
        
        final String state = bundle.getString(TelephonyManager.EXTRA_STATE);
                        
        Log.d(TAG, "IncomingCallReceiver State: "+ state);
        
        if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING))
        {
            final String incomingNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    
            Log.i(TAG, "Incomng Number: " + incomingNumber);
            
            try
            {
				Log.d(TAG, "INCOMING NUMBER: " + incomingNumber);
				databaseHelper = new ContactsDB(mContext);
				databaseHelper.open();
				Cursor mContactsCursor = databaseHelper.fetchAllContacts();
				
				final int numberOfContacts = mContactsCursor.getCount();
				if (numberOfContacts == 0)
				{	
					return;
				}
				
		
				final int contactIdIndex =  mContactsCursor.getColumnIndex(ContactsDB.KEY_CONTACT_ID);
				final int usernameIndex = mContactsCursor.getColumnIndex(ContactsDB.KEY_NAME);
				
				mContactsCursor.moveToFirst();
				
				do
				{
					final String contactId = mContactsCursor.getString(contactIdIndex);
					final String name = mContactsCursor.getString(usernameIndex);
					
					if (incomingNumber != null)
					{
						if (DoesContactHaveThisNumber(contactId, incomingNumber))
						{
							if (BlockContactToday(mContactsCursor))
							{
								Log.d(TAG, "ENDING CALL FROM: " + incomingNumber);
								EndCall(telephonyManager);
								
								ContentValues values = new ContentValues();
								values.put(BlockedCallsDB.C_USER_ID, contactId);
								values.put(BlockedCallsDB.C_NAME, name);
								values.put(BlockedCallsDB.C_NUMBER, incomingNumber);
								values.put(BlockedCallsDB.C_REVIEWED, false);
								values.put(BlockedCallsDB.C_DATE, System.currentTimeMillis());
								getBlockedCall().insertOrIgnore(values);
								
								break;
							}
							
						}
					}
					
				} while(mContactsCursor.moveToNext());
            }
			finally
			{
				if (mContactsCursor != null)
					mContactsCursor.close();
				
				if (databaseHelper != null)
					databaseHelper.close();
				
				if (blockedCallsDb != null)
					blockedCallsDb.close();
			}
        }
	}
        
    private void EndCall(TelephonyManager telephonyManager)
	{
    	try
    	{
    		final Class c = Class.forName(telephonyManager.getClass().getName());
    		final Method m = c.getDeclaredMethod("getITelephony");
    		m.setAccessible(true);
    		
    		com.android.internal.telephony.ITelephony telephonyService = (ITelephony)m.invoke(telephonyManager);
    		
    		Log.d("MY PHONE STATE LISTENER", "ENDING CALL");
    		telephonyService.endCall();
    	}
    	catch (Exception e)
    	{
            e.printStackTrace();
            Log.e(TAG,
                    "FATAL ERROR: could not connect to telephony subsystem");
            Log.e(TAG, "Exception object: " + e);
    	}
    	
    	Log.d("MY PHONE STATE LISTENER", "CALL ENDED SUCCESSFULLY");
	}

	private Boolean DoesContactHaveThisNumber(String contactId, String incomingNumber)
	{
		Boolean result = false;
		
		incomingNumber = incomingNumber.replaceAll("-", "");
		incomingNumber = incomingNumber.replaceAll(" ", "");

		ContentResolver cursor = mContext.getContentResolver();	
		
		Log.d(TAG, "OBTAINING CURSOR with KEY:" + contactId);
	    Cursor phonesCursor = cursor.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + contactId, null, null);		
  
	    //Try an sql statement where Phone.Number = x
	    
        while (phonesCursor.moveToNext()) {
        	
        	String number = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
            
        	number = number.replaceAll("-", "");
        	
            if (number.equalsIgnoreCase(incomingNumber))
            {
            	Log.d(TAG, "BLOCKED NUMBER DETECTED: " + incomingNumber);
            	result = true;
            	break;
            }
            	

        }
        
        if (phonesCursor != null)
        	phonesCursor.close();
		
		return result;
	}
	
	private Boolean BlockContactToday(Cursor contact)
	{
		int dayIndex = 0;

		Calendar calendar = Calendar.getInstance();
		
		switch(calendar.get(Calendar.DAY_OF_WEEK))
		{
		case Calendar.MONDAY:
			dayIndex = contact.getColumnIndex(ContactsDB.KEY_DAY_MON);
			break;
		case Calendar.TUESDAY:
			dayIndex = contact.getColumnIndex(ContactsDB.KEY_DAY_TUE);
			break;
		case Calendar.WEDNESDAY:
			dayIndex = contact.getColumnIndex(ContactsDB.KEY_DAY_WED);
			break;
		case Calendar.THURSDAY:
			dayIndex = contact.getColumnIndex(ContactsDB.KEY_DAY_THU);
			break;
		case Calendar.FRIDAY:
			dayIndex = contact.getColumnIndex(ContactsDB.KEY_DAY_FRI);
			break;
		case Calendar.SATURDAY:
			dayIndex = contact.getColumnIndex(ContactsDB.KEY_DAY_SAT);
			break;
		case Calendar.SUNDAY:
			dayIndex = contact.getColumnIndex(ContactsDB.KEY_DAY_SUN);
			break;
		}

		Integer isBlock = contact.getInt(dayIndex);
		
		if (isBlock == 0)
			return false;
		else
			return true;
		
	}	    
}