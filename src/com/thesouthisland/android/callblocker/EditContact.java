/*
 * Copyright (C) 2011 The South Island
 *
 */

package com.thesouthisland.android.callblocker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class EditContact extends AddContact
{
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
        //ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        //actionBar.setTitle(R.string.editcontact);
		
        Button confirmButton = (Button) findViewById(R.id.confirmButton);
        confirmButton.setText("Update");
        
     // Set click listener for name box
        EditText addNameText = (EditText) findViewById(R.id.addNameText);    
        addNameText.setEnabled(false);
             
        ImageView arrowImage = (ImageView) findViewById(R.id.addNameArrow);
        arrowImage.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void onClick(View v) 
	{		
		switch (v.getId())
		{
			case R.id.buttonDaysOfTheWeek:
				CreatePopupMenu();
				break;
				
			case R.id.confirmButton:		    	
				if (this.IsEverydayUnChecked())
		    	{
		    		Toast toast = Toast.makeText(getApplicationContext(), R.string.msgEverydayUnchecked, Toast.LENGTH_SHORT);
		    		toast.show();
		    	}
		    	else
		    	{
		    		Toast.makeText(getApplicationContext(), R.string.msgContactUpdated, Toast.LENGTH_SHORT).show();
		    		
					this.cancelEdit = false;
		            setResult(RESULT_OK);
		            finish();
		    	}
				break;
			case R.id.cancel:
				cancelEdit = true;
				setResult(RESULT_CANCELED);
				finish();
				break;
				
		}
		
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data)
	{
		return;
	}
	
	@Override
	protected void saveState()
	{
		String name = ((EditText) findViewById(R.id.addNameText)).getText().toString();
		
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
			/*
			 * This is where this method differs from the super class
			 */
			if (mRowId != null && cancelEdit == false)
			{
				contactsListDb.deleteContact(mRowId);
			}
		}
		
		resetState();
	}
}
	