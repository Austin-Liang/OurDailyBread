package com.altujen.android.odb;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
	 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 4;
 
    // Database Name
    private static final String DATABASE_NAME = "ODBManager";
 
    // ODB table name
    private static final String TABLE_NAME_ODB = "odb_Data";
    
    // Try use FTS3 Virtual Tables in English.
    //private static final String TABLE_ODB_FTS = "odb_Data_FTS";
 
    // ODB Table Columns
    private static final String COL_KEY_ID = "sn";
    private static final String COL_ORDER_WT = "order_wt";
    private static final String COL_ODB_URI = "odb_uri";
    private static final String COL_DATE = "date";
    private static final String COL_TITLE = "title";
    private static final String COL_AUTHOR = "author";
    private static final String COL_AUTHOR_URI = "author_uri";
    private static final String COL_AUTHORIMG_URI = "authorImg_uri";
    private static final String COL_MP3_URI = "mp3_uri";
    private static final String COL_RD_TITLE = "rd_title";
    private static final String COL_RD_URI = "rd_uri";
    private static final String COL_RD_TEXT = "rd_text";
    private static final String COL_ANNRD_TITLE = "annRd_title";
    private static final String COL_ANNRD_URI = "annRd_uri";
    private static final String COL_STORY = "story";
    private static final String COL_POEM = "poem";
    private static final String COL_THOUGHT = "thought";
    private static final String COL_LANGUAGE = "lang";
    
    // ODB Table Indexes
    private static final String IDX_ODB_ORDER_WT = "IDX_ODB_ORDER_WT";
    private static final String IDX_ODB_LANGUAGE = "IDX_ODB_LANGUAGE";
    
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ODB_TABLE = "CREATE TABLE " + TABLE_NAME_ODB + "("
                + COL_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
        		+ COL_ORDER_WT + " INTEGER," 
        		+ COL_ODB_URI + " TEXT," 
        		+ COL_DATE + " TEXT," 
                + COL_TITLE + " TEXT," 
                + COL_AUTHOR + " TEXT," 
                + COL_AUTHOR_URI + " TEXT," 
                + COL_AUTHORIMG_URI + " TEXT," 
                + COL_MP3_URI + " TEXT," 
                + COL_RD_TITLE + " TEXT," 
                + COL_RD_URI + " TEXT," 
                + COL_RD_TEXT + " TEXT," 
                + COL_ANNRD_TITLE + " TEXT," 
                + COL_ANNRD_URI + " TEXT," 
                + COL_STORY + " TEXT," 
                + COL_POEM + " TEXT," 
                + COL_THOUGHT + " TEXT," 
                + COL_LANGUAGE + " INTEGER" + ");";
        db.execSQL(CREATE_ODB_TABLE);
        
        db.execSQL("CREATE INDEX IF NOT EXISTS " + IDX_ODB_ORDER_WT + " ON " + TABLE_NAME_ODB + " (" + COL_ORDER_WT + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS " + IDX_ODB_LANGUAGE + " ON " + TABLE_NAME_ODB + " (" + COL_LANGUAGE + ")");
        
        /*String CREATE_ODB_TABLE_TFS = "CREATE VIRTUAL TABLE " + TABLE_ODB_FTS + " USING fts3( "
                + COL_KEY_ID + ", " 
        		+ COL_ORDER_WT + ", " 
        		+ COL_ODB_URI + ", " 
        		+ COL_DATE + ", " 
                + COL_TITLE + ", " 
                + COL_AUTHOR + ", " 
                + COL_AUTHOR_URI + ", "
                + COL_AUTHORIMG_URI + ", " 
                + COL_MP3_URI + ", " 
                + COL_RD_TITLE + ", " 
                + COL_RD_URI + ", " 
                + COL_RD_TEXT + ", " 
                + COL_ANNRD_TITLE + ", " 
                + COL_ANNRD_URI + ", " 
                + COL_STORY + ", " 
                + COL_POEM + ", " 
                + COL_THOUGHT + ", " 
                + COL_LANGUAGE + ", " 
                + " UNIQUE (" + COL_KEY_ID + "));";
        db.execSQL(CREATE_ODB_TABLE_TFS);*/
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	// Drop index if existed
    	db.execSQL("DROP INDEX IF EXISTS " + IDX_ODB_LANGUAGE);
    	db.execSQL("DROP INDEX IF EXISTS " + IDX_ODB_ORDER_WT);
    	
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ODB);
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_ODB_FTS);
 
        // Create tables again
        onCreate(db);
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
 
    // Adding new odb
	public void addOdb(odbObject odb) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(COL_ORDER_WT, odb.getOrder_WT());
		values.put(COL_ODB_URI, odb.getODB_Uri());
		values.put(COL_DATE, odb.getDate());
		values.put(COL_TITLE, odb.getTitle());
		values.put(COL_AUTHOR, odb.getAuthor());
		values.put(COL_AUTHOR_URI, odb.getAuthor_uri());
		values.put(COL_AUTHORIMG_URI, odb.getAuthorImg_uri());
		values.put(COL_MP3_URI, odb.getMp3_uri());
		values.put(COL_RD_TITLE, odb.getRd_title());
		values.put(COL_RD_URI, odb.getRd_uri());
		values.put(COL_RD_TEXT, odb.getRd_text());
		values.put(COL_ANNRD_TITLE, odb.getAnnRd_title());
		values.put(COL_ANNRD_URI, odb.getAnnRd_uri());
		values.put(COL_STORY, odb.getStory());
		values.put(COL_POEM, odb.getPoem());
		values.put(COL_THOUGHT, odb.getThought());
		values.put(COL_LANGUAGE, odb.getLanguage());

		// Inserting Row
		db.insert(TABLE_NAME_ODB, null, values);
		/*long row_ID;
		row_ID = db.insert(TABLE_ODB, null, values);
		values.put(COL_KEY_ID, String.valueOf(row_ID));
		db.insert(TABLE_ODB_FTS, null, values);*/
		
		// Closing database connection
		db.close();
	}
 
    // Getting single odb
	odbObject getOdb(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_NAME_ODB, new String[] { COL_KEY_ID, COL_ORDER_WT, COL_ODB_URI, 
        		COL_DATE, COL_TITLE, COL_AUTHOR, COL_AUTHOR_URI, 
        		COL_AUTHORIMG_URI, COL_MP3_URI, COL_RD_TITLE, 
        		COL_RD_URI, COL_RD_TEXT, COL_ANNRD_TITLE, 
        		COL_ANNRD_URI, COL_STORY, COL_POEM, COL_THOUGHT, COL_LANGUAGE }, COL_KEY_ID + " = ?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        
        if (cursor != null) {
        	cursor.moveToFirst();
        }
 
        odbObject odb = new odbObject(cursor);
        cursor.close();
        
        return odb;
    }
     
    // Getting All Odbs
    public List<odbObject> getAllOdbs() {
        List<odbObject> odbList = new ArrayList<odbObject>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_ODB + " ORDER BY " + COL_ORDER_WT + " ASC";
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	odbObject odb = new odbObject(cursor);
                //contact.setID(Integer.parseInt(cursor.getString(0)));
            	
                // Adding odb to list
            	odbList.add(odb);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        
        return odbList;
    }
    
    // Getting All Odbs
    public List<odbObject> getAllOdbs(int lang) {
        List<odbObject> odbList = new ArrayList<odbObject>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_ODB + " WHERE " + COL_LANGUAGE + " = " + lang + " ORDER BY " + COL_ORDER_WT + " ASC";
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	odbObject odb = new odbObject(cursor);
                //contact.setID(Integer.parseInt(cursor.getString(0)));
            	
                // Adding odb to list
            	odbList.add(odb);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        
        return odbList;
    }
    
    // Getting All Odbs
    public List<odbObject> getAllOdbsByMonth(String startDate, String endDate) {
        List<odbObject> odbList = new ArrayList<odbObject>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_ODB + " WHERE " + COL_ORDER_WT + " >= " + startDate + " AND " + COL_ORDER_WT + " <= " + endDate + " ORDER BY " + COL_ORDER_WT + " ASC";
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	odbObject odb = new odbObject(cursor);
                //contact.setID(Integer.parseInt(cursor.getString(0)));
            	
                // Adding odb to list
            	odbList.add(odb);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        
        return odbList;
    }
    
    // Getting All Odbs
    public List<odbObject> getAllOdbsByMonth(int lang, String startDate, String endDate) {
        List<odbObject> odbList = new ArrayList<odbObject>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_ODB + " WHERE " + COL_LANGUAGE + " = " + lang + " AND " + "( " + COL_ORDER_WT + " >= " + startDate + " AND " + COL_ORDER_WT + " <= " + endDate + " )" + " ORDER BY " + COL_ORDER_WT + " ASC";
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	odbObject odb = new odbObject(cursor);
                //contact.setID(Integer.parseInt(cursor.getString(0)));
            	
                // Adding odb to list
            	odbList.add(odb);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        
        return odbList;
    }
 
    // Updating single odb
    public int updateOdb(odbObject odb) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(COL_ORDER_WT, odb.getOrder_WT());
        values.put(COL_ODB_URI, odb.getODB_Uri());
		values.put(COL_DATE, odb.getDate());
		values.put(COL_TITLE, odb.getTitle());
		values.put(COL_AUTHOR, odb.getAuthor());
		values.put(COL_AUTHOR_URI, odb.getAuthor_uri());
		values.put(COL_AUTHORIMG_URI, odb.getAuthorImg_uri());
		values.put(COL_MP3_URI, odb.getMp3_uri());
		values.put(COL_RD_TITLE, odb.getRd_title());
		values.put(COL_RD_URI, odb.getRd_uri());
		values.put(COL_RD_TEXT, odb.getRd_text());
		values.put(COL_ANNRD_TITLE, odb.getAnnRd_title());
		values.put(COL_ANNRD_URI, odb.getAnnRd_uri());
		values.put(COL_STORY, odb.getStory());
		values.put(COL_POEM, odb.getPoem());
		values.put(COL_THOUGHT, odb.getThought());
		values.put(COL_LANGUAGE, odb.getLanguage());
		
		// updating FTS row
		//db.update(TABLE_ODB_FTS, values, COL_KEY_ID + " = ?", new String[] { String.valueOf(odb.getID()) });
        // updating row
        return db.update(TABLE_NAME_ODB, values, COL_KEY_ID + " = ?", new String[] { String.valueOf(odb.getID()) });
    }
 
    // Deleting single odb
    public void deleteOdb(odbObject odb) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME_ODB, COL_KEY_ID + " = ?", new String[] { String.valueOf(odb.getID()) });
        //db.delete(TABLE_ODB_FTS, COL_KEY_ID + " = ?", new String[] { String.valueOf(odb.getID()) });
        db.close();
    }
 
 
    // Getting odbs Count
    public int getOdbsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME_ODB;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        
        int intRtnVal = cursor.getCount();
        cursor.close();
        
        return intRtnVal;
    }
    
    // Getting odbs Count by lang.
    public int getOdbsCount(int lang) {
        String countQuery = "SELECT  * FROM " + TABLE_NAME_ODB + " WHERE " + COL_LANGUAGE + " = " + lang + "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        
        int intRtnVal = cursor.getCount();
        cursor.close();
        
        return intRtnVal;
    }
    
    // Getting odbs Count
    public int getOdbsCount(String startDate, String endDate) {
        String countQuery = "SELECT  * FROM " + TABLE_NAME_ODB + " WHERE " + COL_ORDER_WT + " >= " + startDate + " AND " + COL_ORDER_WT + " <= " + endDate;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        
        int intRtnVal = cursor.getCount();
        cursor.close();
        
        return intRtnVal;
    }
    
    // Getting odbs Count by lang.
    public int getOdbsCount(int lang, String startDate, String endDate) {
        String countQuery = "SELECT  * FROM " + TABLE_NAME_ODB + " WHERE " + COL_LANGUAGE + " = " + lang + " AND " + "( " + COL_ORDER_WT + " >= " + startDate + " AND " + COL_ORDER_WT + " <= " + endDate + " )";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        
        int intRtnVal = cursor.getCount();
        cursor.close();
        
        return intRtnVal;
    }
    
    public List<odbObject> getSearchResult(int lang, String strKeywords, String startDate, String endDate) {
    	
    	strKeywords = Util.removeSymbol(strKeywords);
    	List<odbObject> odbList = new ArrayList<odbObject>();
    	
    	// Select All Query
    	String selectQuery = "SELECT * FROM " + TABLE_NAME_ODB + " WHERE " + "(" 
    			+ COL_TITLE + " LIKE " + "'%" + strKeywords + "%'" + " OR " 
    			+ COL_AUTHOR + " LIKE " + "'%" + strKeywords + "%'" + " OR " 
    			+ COL_STORY + " LIKE " + "'%" + strKeywords + "%'" + ")" + " AND "
    			+ COL_LANGUAGE + " = " + lang + " AND "
    			+ "( " + COL_ORDER_WT + " >= " + startDate + " AND " + COL_ORDER_WT + " <= " + endDate + " )"
    			+ " ORDER BY " + COL_ORDER_WT + " ASC";
    	
    	SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	odbObject odb = new odbObject(cursor);
                //contact.setID(Integer.parseInt(cursor.getString(0)));
            	
                // Adding odb to list
            	odbList.add(odb);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        
        return odbList;
    }
    
    // Getting Latest Odb Data by lang.
    public odbObject getLatestOdb(int lang) {
        List<odbObject> odbList = new ArrayList<odbObject>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME_ODB + " WHERE " + COL_LANGUAGE + " = " + lang + " ORDER BY " + COL_ORDER_WT + " DESC LIMIT 1";
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	odbObject odb = new odbObject(cursor);
                //contact.setID(Integer.parseInt(cursor.getString(0)));
            	
                // Adding odb to list
            	odbList.add(odb);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        
        return (odbList.size() > 0 ? odbList.get(0) : null);
    }
    
    /*public List<odbObject> getFullTextSearchResult(int lang, String strKeywords) {
    	*//***
    	 * ****!!!**** FTS3/4 Currently Don't Support Trad-Chinese Word Segmentation.
    	 *                   Thus, Try to use fts3 full-text search in English...
    	 *//*
    	// http://blog.andresteingress.com/2011/09/30/android-quick-tip-using-sqlite-fts-tables/
    	// SELECT * FROM table WHERE table MATCH 'A:cat OR C:cat'
    	strKeywords = appendWildcard(strKeywords);
    	List<odbObject> odbList = new ArrayList<odbObject>();
        // Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_ODB_FTS + " WHERE " + TABLE_ODB_FTS + " MATCH " + "'" + COL_LANGUAGE + ":" + lang + "'"
				+ " INTERSECT "
				+ "SELECT * FROM ("
				+ "SELECT * FROM " + TABLE_ODB_FTS + " WHERE " + TABLE_ODB_FTS + " MATCH " + "'" + COL_TITLE + ":" + strKeywords + "'"
				+ " UNION "
				+ "SELECT * FROM " + TABLE_ODB_FTS + " WHERE " + TABLE_ODB_FTS + " MATCH " + "'" + COL_AUTHOR + ":" + strKeywords + "'"
				+ " UNION "
				+ "SELECT * FROM " + TABLE_ODB_FTS + " WHERE " + TABLE_ODB_FTS + " MATCH " + "'" + COL_STORY + ":" + strKeywords + "'"
				+ ")";
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	odbObject odb = new odbObject(cursor);
                //contact.setID(Integer.parseInt(cursor.getString(0)));
            	
                // Adding odb to list
            	odbList.add(odb);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        
        return odbList;
    }*/
 
}
