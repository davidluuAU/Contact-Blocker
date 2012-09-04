/*
 * Copyright (C) 2011 The South Island
 *
 */

package com.thesouthisland.android.callblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.thesouthisland.android.callblocker.databases.ContactsDB;
import com.thesouthisland.android.callblocker.telephony.Telephony;

public class AddContact extends Activity implements OnClickListener {
	
	private static final int PICK_CONTACT = 3;
	private static final int NAME_FONT_SIZE_SP = 19;
	
	private static String TAG = "ADD_CONTACT";
	
	private EditText mNameText;
    protected String mContactId;
    
    protected Button mButtonDaysOfTheWeek;
    protected boolean mMonday = true;
    protected boolean mTuesday = true;
    protected boolean mWednesday = true;
    protected boolean mThursday = true;
    protected boolean mFriday = true;
    protected boolean mSaturday = true;
    protected boolean mSunday = true;
    
    protected Long mRowId;

	
    protected Boolean cancelEdit = true;
    private Boolean updateDetails = false;
    
	private String updateName = new String();
	private String updateNumber = new String();
    
    protected ContactsDB contactsListDb;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        contactsListDb = new ContactsDB(this);
        contactsListDb.open();
               
        setContentView(R.layout.add_contact);

        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        //actionBar.setTitle(R.string.addcontact);
        actionBar.setHomeAction(new IntentAction(this, CallBlockList.createIntent(this), R.drawable.ic_logo));
        //actionBar.addAction(new IntentAction(this, createContactsListIntent(), PICK_CONTACT, R.drawable.ic_address_book));

        mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(ContactsDB.KEY_ROWID);
        
        if (mRowId == null)
        {
        	Bundle extras = getIntent().getExtras();
        	mRowId = extras != null ? extras.getLong(ContactsDB.KEY_ROWID) : null;
        }

        
        // Set click listener for name box
        mNameText = (EditText) findViewById(R.id.addNameText);    
        mNameText.setOnClickListener(this);
        
        mButtonDaysOfTheWeek = (Button) findViewById(R.id.buttonDaysOfTheWeek);
        mButtonDaysOfTheWeek.setOnClickListener(this);
        mButtonDaysOfTheWeek.setText(getDaysOfWeekString());
                
        // Set click listener for confirm button
        Button confirmButton = (Button) findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(this);
        
        // Set click listener for cancel button
        Button cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(this);

        populateFields();        
    }

    protected boolean IsContactAlreadyBlocked(String mContactId)
    {
    	int numberOfContacts = 0;
    	
    	if (mContactId == null)
    		return false;
    	
    	final int contactId = Integer.parseInt(mContactId);
    	
    	final Cursor mContactsCursor = contactsListDb.fetchContact(contactId);
    	
    	if (mContactsCursor != null)
    	{
	    	numberOfContacts = mContactsCursor.getCount();
	    	
	    	mContactsCursor.close();	    	
    	}
    	
    	if (numberOfContacts > 0)
    		return true;
    	else
    		return false;
    }
    
    
    private void populateFields() {
    	if (mRowId != null)
    	{    		
    		Cursor cursorContact = contactsListDb.fetchContact(mRowId);
    		
    		if (cursorContact != null)
    		{
	    		startManagingCursor(cursorContact);
	    		mNameText.setText(cursorContact.getString(cursorContact.getColumnIndexOrThrow(ContactsDB.KEY_NAME)));
	    		mNameText.setTextSize(NAME_FONT_SIZE_SP);	
	    		
	    		mContactId = cursorContact.getString(cursorContact.getColumnIndexOrThrow(ContactsDB.KEY_CONTACT_ID));
	    		
	    		mMonday = cursorContact.getInt(cursorContact.getColumnIndex(ContactsDB.KEY_DAY_MON)) == 1 ? true : false;
	    		mTuesday = cursorContact.getInt(cursorContact.getColumnIndex(ContactsDB.KEY_DAY_TUE)) == 1 ? true : false;
	    		mWednesday = cursorContact.getInt(cursorContact.getColumnIndex(ContactsDB.KEY_DAY_WED)) == 1 ? true : false;
	    		mThursday = cursorContact.getInt(cursorContact.getColumnIndex(ContactsDB.KEY_DAY_THU)) == 1 ? true : false;
	    		mFriday = cursorContact.getInt(cursorContact.getColumnIndex(ContactsDB.KEY_DAY_FRI)) == 1 ? true : false;
	    		mSaturday = cursorContact.getInt(cursorContact.getColumnIndex(ContactsDB.KEY_DAY_SAT)) == 1 ? true : false;
	    		mSunday = cursorContact.getInt(cursorContact.getColumnIndex(ContactsDB.KEY_DAY_SUN)) == 1 ? true : false;
	    		
	    		if (mButtonDaysOfTheWeek != null)
	    			mButtonDaysOfTheWeek.setText(getDaysOfWeekString());
	    		
	    		cursorContact.close();
    		}
    		
    		
    		if (updateDetails)
    		{
    			mNameText.setText(updateName);
    		}
    	}
    }
    

    protected boolean IsEverydayUnChecked()
    {
    	if 
    	(
			(mMonday == false) &&
			(mTuesday == false) &&
			(mWednesday == false) &&
			(mThursday == false) &&
			(mFriday == false) &&
			(mSaturday == false) &&
			(mSunday == false)
		)
    	{
    		return true;
    	}
    	
		return false; 	
    }    
    
    @Override
	protected void onPause() {
		super.onPause();
		saveState();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	@Override
	protected void onDestroy() {
		
		if (contactsListDb != null)
			contactsListDb.close();
		
		super.onDestroy();
	}	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(ContactsDB.KEY_ROWID, mRowId);
	}
	
	protected void saveState()
	{
		final String name = mNameText.getText().toString();
		
		if (name.length() > 0 && cancelEdit == false)
		{
			if (mRowId == null)
			{
				long id = 
					contactsListDb.createContact(name, "", mMonday, mTuesday, mWednesday, mThursday, mFriday, mSaturday, mSunday, mContactId);
				
				if (id > 0)
				{
					mRowId = id;
				}
			}
			else
			{
				contactsListDb.updateContact(mRowId, name, "", mMonday, mTuesday, mWednesday, mThursday, mFriday, mSaturday, mSunday, mContactId);
			}
		}
		else
		{
			if (mRowId != null)
			{
				contactsListDb.deleteContact(mRowId);
			}
		}
		
		resetState();
	}
    
	protected void resetState()
	{
		cancelEdit = true;
		updateName = "";
		updateNumber = "";
		
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data)
	{
		super.onActivityResult(reqCode, resultCode, data);

		
		if (resultCode == Activity.RESULT_OK)
		{
			switch (reqCode)
			{
			case PICK_CONTACT:

				final Uri contactData = data.getData();
	            
	            Cursor contactCursor = managedQuery(contactData, null, null, null, null);
 
	            if (contactCursor.moveToFirst())
	            {
	            	// other data is available for the Contact.  I have decided  to only get the name of the Contact.
	            	updateName = contactCursor.getString(contactCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

		            final String contactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
		            
		            mContactId = contactId;//contact.getString(contact.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
		            
		            
		            Log.d(TAG, "CONTACT ID: " + contactId);
					//
					//  Get all phone numbers.
					//
		            ContentResolver cr = getContentResolver();
		            Cursor phones = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + contactId, null, null);
		             
		            while (phones.moveToNext()) 
		            {
		            	 
		                final String numbers = phones.getString(phones.getColumnIndex(Phone.NUMBER));
		                Log.d(TAG, "OBTAINED PHONE NUMBER: " + updateNumber);
		                 
		                int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
		                switch (type) 
		                {	 
		                     case Phone.TYPE_HOME:
		                         // do something with the Home number here...
		                         break;
		                     case Phone.TYPE_MOBILE:
		                    	 updateNumber = numbers;
		                         // do something with the Mobile number here...
		                         break;
		                     case Phone.TYPE_WORK:
		                         // do something with the Work number here...
		                         break;
	                     }
		            }
		            
		            if (phones != null)
		            	phones.close();
	            }
	            
	            if (contactCursor != null)
	            	contactCursor.close();
	            
			}
			
			if (mRowId != null)
			{
				updateDetails = true;
			}
			else
			{
				mNameText.setText(updateName);
	    		mNameText.setTextSize(NAME_FONT_SIZE_SP);
	    		mNameText.setTextColor(Color.BLACK);
			}
		}
	}


    protected Intent createContactsListIntent()
    {
    	return new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);    	
    }

	@Override
	public void onClick(View v) 
	{		
		switch (v.getId())
		{
			case R.id.addNameText:
				Intent i = createContactsListIntent();
		        startActivityForResult(i, PICK_CONTACT);
		        break;
		        
			case R.id.buttonDaysOfTheWeek:
				CreatePopupMenu();
				break;
				
			case R.id.confirmButton:
				
				if (mContactId == null)
            	{
            		Toast.makeText(getApplicationContext(), R.string.msgNoContactSelected, Toast.LENGTH_SHORT).show();
            		break;
            	}
				
				if (IsContactAlreadyBlocked(mContactId))
            	{
            		Toast.makeText(getApplicationContext(), R.string.msgAlreadyBlocked, Toast.LENGTH_SHORT).show();
            		break;
            	}
				
				if (IsEverydayUnChecked())
            	{
            		Toast.makeText(getApplicationContext(), R.string.msgEverydayUnchecked, Toast.LENGTH_SHORT).show();
            		break;
            	}
				
				Cursor cursor = Telephony.getContactNumbers(Integer.parseInt(mContactId), getApplicationContext());				
				if (cursor != null)
				{
					int contactNumber = cursor.getCount();
					cursor.close();
					
					if (contactNumber == 0)
					{
						Toast.makeText(getApplicationContext(), R.string.msgContactHasNoNumbers, Toast.LENGTH_SHORT).show();
						break;
					}	
				}

				// Else everything is Ok
				cancelEdit = false;
                setResult(RESULT_OK);
                finish();
            	
                break;
			case R.id.cancel:
				cancelEdit = true;
				setResult(RESULT_CANCELED);
				finish();
				break;
				
		}
		
	}
	
	protected void CreatePopupMenu()
	{
		final CharSequence[] items = 
			{
				getString(R.string.labelMonday), 
				getString(R.string.labelTuesday), 
				getString(R.string.labelWednesday), 
				getString(R.string.labelThursday),
				getString(R.string.labelFriday),
				getString(R.string.labelSaturday),
				getString(R.string.labelSunday)
			};
		
		final boolean[] checkedItems = {mMonday, mTuesday, mWednesday, mThursday, mFriday, mSaturday, mSunday};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Days to block calls");
		
		builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				
				switch (which)
				{
				case 0:
					mMonday = isChecked;
					break;
				case 1:
					mTuesday = isChecked;
					break;
				case 2:
					mWednesday = isChecked;
					break;
				case 3:
					mThursday = isChecked;
					break;
				case 4:
					mFriday = isChecked;
					break;
				case 5:
					mSaturday = isChecked;
					break;
				case 6:
					mSunday = isChecked;
					break;
				}
				
				mButtonDaysOfTheWeek.setText(getDaysOfWeekString());
			}
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private String getDaysOfWeekString()
	{
		String daysOfWeek = "";
		int count = 0;
		
		if (mMonday)
		{
			daysOfWeek += "Mon ";
			count++;
		}
			
		if (mTuesday)
		{
			daysOfWeek += "Tue ";
			count++;
		}
		
		if (mWednesday)
		{
			daysOfWeek += "Wed ";
			count++;
		}
		
		if (mThursday)
		{
			daysOfWeek += "Thu ";
			count++;
		}
		
		if (mFriday)
		{
			daysOfWeek += "Fri ";
			count++;
		}
		
		if (mSaturday)
		{
			daysOfWeek += "Sat ";
			count++;
		}
		
		if (mSunday)
		{
			daysOfWeek += "Sun ";
			count++;
		}
		
		if (count == 7)
			return getString(R.string.labelEveryday);
		else if (count == 0)
			return getString(R.string.labelNoDaysSelected);
		else
			return daysOfWeek;
			
	}
}
