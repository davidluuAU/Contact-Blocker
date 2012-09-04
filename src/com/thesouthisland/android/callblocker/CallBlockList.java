/*
 * Copyright (C) 2011 The South Island
 *
 */

package com.thesouthisland.android.callblocker;

import java.io.InputStream;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import net.londatiga.android.R;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.thesouthisland.android.callblocker.databases.BlockedCallsDB;
import com.thesouthisland.android.callblocker.databases.ContactsDB;
import com.thesouthisland.android.callblocker.datetime.DateTime;

public class CallBlockList extends ListActivity implements OnClickListener {
//    private static final int ACTIVITY_CREATE=0;    
	private static final int ACTIVITY_EDIT=1;
	private static final int ACTIVITY_ABOUT=2;
    private static final int ABOUT_ID = Menu.FIRST;

    private static final int NAME_LABEL = 3;
    private static final int BOLD_FONT = 5;
    
    private static final String TAG = "CALL_BLOCK_LIST";
    
    private Context mContext = null;
    
    QuickAction mQuickAction;
    
    private static final String[] FROM = 
    	{ ContactsDB.KEY_NAME, ContactsDB.KEY_DAY_MON, ContactsDB.KEY_DAY_TUE, ContactsDB.KEY_DAY_WED,
    	ContactsDB.KEY_DAY_THU, ContactsDB.KEY_DAY_FRI, ContactsDB.KEY_DAY_SAT, ContactsDB.KEY_DAY_SUN, ContactsDB.KEY_CONTACT_ID };
    
    private static final int[] TO = 
    	{ R.id.nameLabel, R.id.mondayLabel, R.id.tuesdayLabel, R.id.wednesdayLabel, R.id.thursdayLabel,
    	R.id.fridayLabel, R.id.saturdayLabel, R.id.sundayLabel, R.id.quickContactBadge };
    
    private static final String[] BLOCKED_CALL_LIST_FROM = 
		{ BlockedCallsDB.C_USER_ID, BlockedCallsDB.C_NAME, BlockedCallsDB.C_NUMBER, BlockedCallsDB.C_DATE, BlockedCallsDB.C_REVIEWED};

    private static final int[] BLOCKED_CALL_LIST_TO = 
		{ R.id.blockedContactBadge, R.id.blockedNameLabel, R.id.blockedNumberLabel, R.id.dateLabel, R.id.blockedNameLabel };

    
    private ContactsDB contactsDb;
    
    private BlockedCallsDB blockedCallsDb;
    
    private ListView listBlockedCalls;
    
    private SlidingDrawer slidingDrawer;
    private Button contentButton;
    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blocked_contacts);
        
        mContext = this;
        blockedCallsDb = new BlockedCallsDB(this);
        contactsDb = new ContactsDB(this);
        contactsDb.open();
        
        listBlockedCalls = (ListView) findViewById(R.id.listBlockedCalls);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
        contentButton = (Button) findViewById(R.id.contentButton);
        
        loadBlockedContactList();
        registerForContextMenu(getListView());
        
        
		slidingDrawer.setOnDrawerOpenListener(onDrawerOpenListener);
		slidingDrawer.setOnDrawerCloseListener(onDrawerCloseListener);
        
		contentButton.setOnClickListener(this);
		
        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setHomeLogo(R.drawable.ic_logo);
        //actionBar.setTitle(R.string.app_name);
		actionBar.addAction(new IntentAction(this, new Intent(this, AddContact.class), R.drawable.ic_add_contact));
		
    }
    
    @Override
	protected void onResume() {
		super.onResume();		
        loadBlockedCallsList();		
        GetNewNotifications();
	}


	private void loadBlockedCallsList() {
    	Log.d(TAG, "loadBlockedCallsList");
    	
    	try
    	{
	    	// Get all of the rows from the database and create the item list
	        Cursor mBlockedCallsCursor =  blockedCallsDb.getBlockedCalls();
	        startManagingCursor(mBlockedCallsCursor);       
	        
	        // Now create a simple cursor adapter and set it to display        
	        SimpleCursorAdapter blockedCallsAdapter = 
	            new SimpleCursorAdapter(this, R.layout.blocked_calls_row, mBlockedCallsCursor, BLOCKED_CALL_LIST_FROM, BLOCKED_CALL_LIST_TO);
	        
	        blockedCallsAdapter.setViewBinder(VIEW_BINDER);
	        
	        listBlockedCalls.setAdapter(blockedCallsAdapter);
    	}
    	catch (Exception ex)
    	{
    		
    	}
    }
    
    
    private void loadBlockedContactList() {
    	Log.d(TAG, "loadBlockedContactList");
    	
        // Get all of the rows from the database and create the item list
        Cursor mContactsCursor = contactsDb.fetchAllContacts();
        startManagingCursor(mContactsCursor);
        
        // Now create a simple cursor adapter and set it to display        
        SimpleCursorAdapter contacts = 
            new SimpleCursorAdapter(this, R.layout.contacts_row, mContactsCursor, FROM, TO);
        contacts.setViewBinder(VIEW_BINDER);
        
        setListAdapter(contacts);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, ABOUT_ID, 0, R.string.labelAbout);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case ABOUT_ID:
                createAbout();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }    


    @Override
    protected void onListItemClick(ListView l, View v, int position, final long id) {
        super.onListItemClick(l, v, position, id);

        
        ActionItem editAction = new ActionItem();
		editAction.setTitle("Edit");
		editAction.setIcon(getResources().getDrawable(R.drawable.ic_edit_yellow));
        
		ActionItem deleteAction = new ActionItem();
		deleteAction.setTitle("Delete");
		deleteAction.setIcon(getResources().getDrawable(R.drawable.ic_delete_grey));
		
		mQuickAction = new QuickAction(this);
		mQuickAction.addActionItem(editAction);
		mQuickAction.addActionItem(deleteAction);		
		
		//setup the action item click listener
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
			@Override
			public void onItemClick(int pos) {
				
				if (pos == 0) { //Add item selected
			        Intent i = new Intent(mContext, EditContact.class);
			        i.putExtra(ContactsDB.KEY_ROWID, id);
			        startActivityForResult(i, ACTIVITY_EDIT);
				} else if (pos == 1)
				{
					contactsDb.deleteContact(id);
	                loadBlockedContactList();
				}
			}
		});
		
		mQuickAction.show(v);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        loadBlockedContactList();
    }
    
    private void createAbout() {
        Intent i = new Intent(this, About.class);
        startActivityForResult(i, ACTIVITY_ABOUT);
    }
    
    
    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, CallBlockList.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }
    
    private static ViewBinder VIEW_BINDER = new ViewBinder() 
    {
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) { // 
            
        	int day = 0;
        	
            switch (view.getId())
            {
            case R.id.mondayLabel:
            	day = cursor.getInt(columnIndex); 
            	final String monday = (day == 1) ? "Mon " : "";
            	((TextView) view).setText(monday);
            	return true;
            	
            case R.id.tuesdayLabel:
            	day = cursor.getInt(columnIndex); 
            	final String tuesday = (day == 1) ? "Tue " : "";
            	((TextView) view).setText(tuesday);
            	return true;
            case R.id.wednesdayLabel:
            	day = cursor.getInt(columnIndex); 
            	final String wednesday = (day == 1) ? "Wed " : "";
            	((TextView) view).setText(wednesday);
            	return true;
            case R.id.thursdayLabel:
            	day = cursor.getInt(columnIndex); 
            	final String thursday = (day == 1) ? "Thur " : "";
            	((TextView) view).setText(thursday);
            	return true;
            case R.id.fridayLabel:
            	day = cursor.getInt(columnIndex); 
            	final String friday = (day == 1) ? "Fri " : "";
            	((TextView) view).setText(friday);
            	return true;
            case R.id.saturdayLabel:
            	day = cursor.getInt(columnIndex); 
            	final String saturday = (day == 1) ? "Sat " : "";
            	((TextView) view).setText(saturday);
            	return true;
            case R.id.sundayLabel:
            	day = cursor.getInt(columnIndex); 
            	final String sunday = (day == 1) ? "Sun " : "";
            	((TextView) view).setText(sunday);
            	return true;
            case R.id.quickContactBadge:
            case R.id.blockedContactBadge:
            	final int contactId = cursor.getInt(columnIndex);
            	final Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            	
            	ContentResolver resolver = view.getContext().getContentResolver();
            	InputStream photoDataStream = null;
            	try
            	{
            		QuickContactBadge badge = ((QuickContactBadge) view);
            		badge.setMode(ContactsContract.QuickContact.MODE_SMALL);
	        		
            		photoDataStream = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
            		final Bitmap photo = BitmapFactory.decodeStream(photoDataStream);
            		if (photo != null)
            		{
		            	badge.setImageBitmap(photo);
            		}
            		else
            		{
            			badge.setImageResource(R.drawable.icon_watermark);
            		}
            	}
            	catch (Exception e)
            	{
            		Log.e(TAG, e.getMessage());
            	}
            	
            	return true;
            	
            	
            case R.id.blockedNameLabel:
            	switch(columnIndex)
            	{
            	case NAME_LABEL:
            		final String name = cursor.getString(columnIndex);        	
                	((TextView) view).setText(name);
            		break;
            	case BOLD_FONT:
            		final boolean isReviewed = cursor.getInt(columnIndex) != 0; 
            		if (isReviewed == false)
            		{
	            		((TextView) view).setTypeface(null, Typeface.BOLD);
            		}
            		break;
            	}
            	
            	
            	return true;
            	
            case R.id.blockedNumberLabel:
            	
        		final String number = cursor.getString(columnIndex);
            	
            	((TextView) view).setText(number);
            	
            	return true;
            	
            case R.id.dateLabel:
            	final long timeMilliseconds = cursor.getLong(columnIndex);
            	
            	((TextView) view).setText(DateTime.getRelativeTime(timeMilliseconds));
            	
            	return true;
            }
            
            return false;
        }
    };
    

	private void GetNewNotifications()
	{
		if (blockedCallsDb != null)
		{
			int count = blockedCallsDb.getNumberOfNewBlockedCalls();
			TextView numberOfNewNotifications = (TextView) findViewById(R.id.textNumNewNotifications);
			
			if (numberOfNewNotifications != null)
			{
				numberOfNewNotifications.setText(Integer.toString(count));
				
				if (count == 0)
				{
					numberOfNewNotifications.setBackgroundResource(R.drawable.contactblocker_notification_icon_gray);
				}
				else if (count > 0)
				{
					numberOfNewNotifications.setBackgroundResource(R.drawable.contactblocker_notification_icon);				
				}
			}
		}
	}
    
    /*********** CALL BACKS *************/
    
    private OnDrawerOpenListener onDrawerOpenListener = new OnDrawerOpenListener() {

    	@Override
    	public void onDrawerOpened() {
    	
    		if (blockedCallsDb != null)
    			blockedCallsDb.markAllAsRead();
    	}
	};
	
	private OnDrawerCloseListener onDrawerCloseListener = new OnDrawerCloseListener() {

    	@Override
    	public void onDrawerClosed() {
    	
    		GetNewNotifications();
    		
    		loadBlockedCallsList();
    	}
	};


	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
			case R.id.contentButton:
				blockedCallsDb.clear();
												
				slidingDrawer.animateClose();
				
				Toast.makeText(this, R.string.labelHistoryCleared, Toast.LENGTH_LONG).show();
				
				break;
	        
		}
	}
	
}