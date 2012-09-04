package com.thesouthisland.android.callblocker.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BlockedCallsDB {
	
	private static final String TAG = BlockedCallsDB.class.getSimpleName();
	
	static final int VERSION = 1;
	static final String DATABASE = "blockedCalls.db";
	static final String TABLE = "blockedCalls";

	public static final String C_ID = "_id";
	public static final String C_USER_ID = "user_id";
	public static final String C_DATE = "created_at";
	public static final String C_NAME = "name";
	public static final String C_NUMBER = "number";
	public static final String C_REVIEWED = "reviewed";

	private static final String GET_ALL_ORDER_BY = C_DATE + " DESC";

	// DbHelper implementations
	class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE, null, VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	    	Log.i(TAG, "Creating database: " + DATABASE);
	    	db.execSQL("create table " + TABLE + " (" + C_ID + " int primary key, "
    			+ C_USER_ID + " text, " + C_DATE + " long, " + C_NAME + " text, " + C_NUMBER + " text, " + C_REVIEWED + " int)");
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    	db.execSQL("drop table " + TABLE);
	    	this.onCreate(db);
	    }
	}

	private final DbHelper dbHelper; // 

	public BlockedCallsDB(Context context) {  // 
		this.dbHelper = new DbHelper(context);
	    Log.i(TAG, "Initialized data");
	}

	public void close() { // 
	    this.dbHelper.close();
	}

	public void insertOrIgnore(ContentValues values) {  // 
		Log.d(TAG, "insertOrIgnore on " + values);
	    SQLiteDatabase db = this.dbHelper.getWritableDatabase();  // 
	    try {
	    	db.insertOrThrow(TABLE, null, values);
	    	//db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);  // 
	    } finally {
	    	db.close(); // 
	    }
	}

	/**
	 *
	 * @return Cursor where the columns are _id, created_at, user, txt
	 */
	public Cursor getBlockedCalls() {  // 
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
	    return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
	}
	
	public void clear()
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		db.delete(TABLE, null, null);
	}

	public int getNumberOfNewBlockedCalls()
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		Cursor cursor = null;
	    try {
	    	cursor = 
	    		db.query
	    		(
					TABLE, 
					null, 
					C_REVIEWED + " = " + 0, 
					null, 
					null, 
					null, 
					null
				);

	    	int count = 0;
	    	if (cursor != null)
	    		count = cursor.getCount();
	    	
    		return count;
    		
	    	} 
	    finally {
	    	if (cursor != null)
	    		cursor.close();
	    	
	    	if (db != null)
	    		db.close();
	    }
	}
	
	public boolean markAllAsRead() 
    {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		
        ContentValues args = new ContentValues();
        args.put(C_REVIEWED, 1);
        
        return db.update(TABLE, args, C_REVIEWED + " = " + 0, null) > 0;
    }
}
