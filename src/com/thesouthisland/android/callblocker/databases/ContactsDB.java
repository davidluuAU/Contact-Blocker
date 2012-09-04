/*
 * Copyright (C) 2010 The South Island
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.thesouthisland.android.callblocker.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple contacts database access helper class. Gives the ability to list all 
 * contacts as well as retrieve or modify a specific contact.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class ContactsDB {

    public static final String KEY_NAME = "name";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_DAY_MON = "monday";
    public static final String KEY_DAY_TUE = "tuesday";
    public static final String KEY_DAY_WED = "wednesday";
    public static final String KEY_DAY_THU = "thursday";
    public static final String KEY_DAY_FRI = "friday";
    public static final String KEY_DAY_SAT = "saturday";
    public static final String KEY_DAY_SUN = "sunday";
    public static final String KEY_CONTACT_ID = "contact";
    public static final String KEY_ROWID = "_id";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String TAG = "ContactsDbAdapter";
    
    private static String[] ALL_PARAMETERS =
               {KEY_ROWID, KEY_NAME, KEY_NUMBER, KEY_DAY_MON, KEY_DAY_TUE, 
           		KEY_DAY_WED, KEY_DAY_THU, KEY_DAY_FRI, KEY_DAY_SAT, 
           		KEY_DAY_SUN, KEY_CONTACT_ID};
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table contacts (_id integer primary key autoincrement, "
        + "name text not null, number text not null, monday integer, "
		+ "tuesday integer, wednesday integer, thursday integer, "
		+ "friday integer, saturday integer, sunday integer, " +
		"contact text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "contacts";
    private static final int DATABASE_VERSION = 3;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
            
            
            switch(oldVersion) {
            
            case 1:
            	Log.d(TAG, "Upgrading from version 1 to 2");
            case 2:
            	Log.d(TAG, "Upgrading from version 2 to 3");
            	
            	Cursor contactsCursor =  db.query(DATABASE_TABLE, ALL_PARAMETERS, null, null, null, null, KEY_NAME + " ASC"); // Sort by KEY_NAME in ascending order
            	
            	int numberOfContacts = contactsCursor.getCount();
        		if (numberOfContacts != 0)
        		{
        			final int rowIndex = contactsCursor.getColumnIndex(ContactsDB.KEY_ROWID);
        			final int contactIdIndex =  contactsCursor.getColumnIndex(ContactsDB.KEY_CONTACT_ID);	        		
        			final int contactNameIndex = contactsCursor.getColumnIndex(ContactsDB.KEY_NAME);	 
        			final int contactNumberIndex = contactsCursor.getColumnIndex(ContactsDB.KEY_NUMBER);	 
        			final int mondayIndex = contactsCursor.getColumnIndex(ContactsDB.KEY_DAY_MON);	 
        			final int tuesdayIndex = contactsCursor.getColumnIndex(ContactsDB.KEY_DAY_TUE);	 
        			final int wednesdayIndex = contactsCursor.getColumnIndex(ContactsDB.KEY_DAY_WED);	 
        			final int thursdayIndex = contactsCursor.getColumnIndex(ContactsDB.KEY_DAY_THU);	 
        			final int fridayIndex = contactsCursor.getColumnIndex(ContactsDB.KEY_DAY_FRI);	 
        			final int saturdayIndex = contactsCursor.getColumnIndex(ContactsDB.KEY_DAY_SAT);	 
        			final int sundayIndex = contactsCursor.getColumnIndex(ContactsDB.KEY_DAY_SUN);
	        		
	        		contactsCursor.moveToFirst();
	        		do
	        		{
	        			final long rowId = contactsCursor.getLong(rowIndex);
	        			final String contactId = contactsCursor.getString(contactIdIndex);
	        			final String contactName = contactsCursor.getString(contactNameIndex);
	        			final String contactNumber = contactsCursor.getString(contactNumberIndex);
	        			final String monday = contactsCursor.getString(mondayIndex);
	        			final String tuesday = contactsCursor.getString(tuesdayIndex);
	        			final String wednesday = contactsCursor.getString(wednesdayIndex);
	        			final String thursday = contactsCursor.getString(thursdayIndex);
	        			final String friday = contactsCursor.getString(fridayIndex);
	        			final String saturday = contactsCursor.getString(saturdayIndex);
	        			final String sunday = contactsCursor.getString(sundayIndex);
	        			
	        			Log.d(TAG, contactId + contactName + contactNumber + monday + tuesday + wednesday + thursday + friday + saturday + sunday);
	        			
	        			ContentValues args = new ContentValues();
	        	        args.put(KEY_NAME, contactName);
	        	        args.put(KEY_NUMBER, contactNumber);
	        	        args.put(KEY_DAY_MON, (monday.equalsIgnoreCase("Mon") ? 1 : 0));
	        	        args.put(KEY_DAY_TUE, (tuesday.equalsIgnoreCase("Tue") ? 1 : 0));
	        	        args.put(KEY_DAY_WED, (wednesday.equalsIgnoreCase("Wed") ? 1 : 0));
	        	        args.put(KEY_DAY_THU, (thursday.equalsIgnoreCase("Thur") ? 1 : 0));
	        	        args.put(KEY_DAY_FRI, (friday.equalsIgnoreCase("Fri") ? 1 : 0));
	        	        args.put(KEY_DAY_SAT, (saturday.equalsIgnoreCase("Sat") ? 1 : 0));
	        	        args.put(KEY_DAY_SUN, (sunday.equalsIgnoreCase("Sun") ? 1 : 0));        
	        	        args.put(KEY_CONTACT_ID, contactId);

	        	        db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null);
	        			
	        			
	        		} while(contactsCursor.moveToNext());
        		}
        		
        		if (contactsCursor != null)
        			contactsCursor.close();
        		
            }
            
            
            //db.execSQL("DROP TABLE IF EXISTS contacts");
            //onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ContactsDB(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the contacts database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ContactsDB open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new contact using the title and body provided. If the contact is
     * successfully created return the new rowId for that contact, otherwise return
     * a -1 to indicate failure.
     * 
     * @param name the name of the contact
     * @param number the number of the contact
     * @return rowId or -1 if failed
     */
    public long createContact
    (
    	String name, 
    	String number, 
    	Boolean mon,
    	Boolean tue,
    	Boolean wed,
    	Boolean thu,
    	Boolean fri,
    	Boolean sat,
    	Boolean sun,
    	String contactId
	) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_NUMBER, number);
        initialValues.put(KEY_DAY_MON, (mon == true ? 1 : 0));
        initialValues.put(KEY_DAY_TUE, (tue == true ? 1 : 0));
        initialValues.put(KEY_DAY_WED, (wed == true ? 1 : 0));
        initialValues.put(KEY_DAY_THU, (thu == true ? 1 : 0));
        initialValues.put(KEY_DAY_FRI, (fri == true ? 1 : 0));
        initialValues.put(KEY_DAY_SAT, (sat == true ? 1 : 0));
        initialValues.put(KEY_DAY_SUN, (sun == true ? 1 : 0));
        initialValues.put(KEY_CONTACT_ID, contactId);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the contact with the given rowId
     * 
     * @param rowId id of contact to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteContact(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all contacts in the database
     * 
     * @return Cursor over all contacts
     */
    public Cursor fetchAllContacts() {

        return mDb.query(DATABASE_TABLE, ALL_PARAMETERS, null, null, null, null, KEY_NAME + " ASC"); // Sort by KEY_NAME in ascending order
    }

    /**
     * Return a Cursor positioned at the contact that matches the given rowId
     * 
     * @param rowId id of contact to retrieve
     * @return Cursor positioned to matching contact, if found
     * @throws SQLException if contact could not be found/retrieved
     */
    public Cursor fetchContact(long rowId) throws SQLException {

        Cursor mCursor =
    		mDb.query(true, DATABASE_TABLE, ALL_PARAMETERS, KEY_ROWID + "=" + rowId, null, null, null, null, null);
        
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    /**
     * Return a Cursor positioned at the contact that matches the given contactId
     * 
     * @param contactId of contact to retrieve
     * @return Cursor positioned to matching contact, if found
     * @throws SQLException if contact could not be found/retrieved
     */
    public Cursor fetchContact(int contactId) throws SQLException {

        Cursor mCursor =
    		mDb.query(true, DATABASE_TABLE, ALL_PARAMETERS, KEY_CONTACT_ID + "=" + contactId, null, null, null, null, null);
        
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }    

    /**
     * Update the contact using the details provided. The contact to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of contact to update
     * @param name value to set contact name to
     * @param number value to set contact number to
     * @return true if the contact was successfully updated, false otherwise
     */
    public boolean updateContact
    (
    	long rowId, 
    	String name, 
    	String number, 
    	Boolean mon,
    	Boolean tue,
    	Boolean wed,
    	Boolean thu,
    	Boolean fri,
    	Boolean sat,
    	Boolean sun,
    	String contactId
	) 
    {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_NUMBER, number);
        args.put(KEY_DAY_MON, (mon == true ? 1 : 0));
        args.put(KEY_DAY_TUE, (tue == true ? 1 : 0));
        args.put(KEY_DAY_WED, (wed == true ? 1 : 0));
        args.put(KEY_DAY_THU, (thu == true ? 1 : 0));
        args.put(KEY_DAY_FRI, (fri == true ? 1 : 0));
        args.put(KEY_DAY_SAT, (sat == true ? 1 : 0));
        args.put(KEY_DAY_SUN, (sun == true ? 1 : 0));        
        args.put(KEY_CONTACT_ID, contactId);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
