package com.altujen.android.odb;

import android.app.Application;
import android.graphics.Bitmap;

public class DailyBreadSettings extends Application {
	
	private DatabaseHandler mDb;
	private DiskLruImageCache mDiskLruCache;
	private Bitmap mMissingAvatar;
	// Application Language Setting.
	private int LANGUAGE_SETTINGS;
	// Font-Size Setting.
	private int FONT_SIZE;
	
	
	public int getLanguageSettings() {
		return LANGUAGE_SETTINGS;
	}
	
	public void setLanguageSettings(int lang) {
		LANGUAGE_SETTINGS = lang;
	}
	
	public int getFontSize() {
		return FONT_SIZE;
	}

	public void setFont_Size(int fontSize) {
		FONT_SIZE = fontSize;
	}

	public Bitmap getMissingAvatar() {
		return mMissingAvatar;
	}
	
	public void setMissingAvatar(Bitmap mMissingAvatar) {
		this.mMissingAvatar = mMissingAvatar;
	}
	
	public void clearMissingAvatar() {
		if(this.mMissingAvatar != null) {
			this.mMissingAvatar.recycle();
			this.mMissingAvatar = null;
		}
	}

	public DiskLruImageCache getDiskLruCache() {
		return mDiskLruCache;
	}

	public void setDiskLruCache(DiskLruImageCache diskLruCache) {
		this.mDiskLruCache = diskLruCache;
	}
	
	public void closeDiskLruImageCache() {
		if(this.mDiskLruCache != null) {
			this.mDiskLruCache.close();
			this.mDiskLruCache = null;
		}
	}

	public DatabaseHandler getDatabaseHandler() {
		return mDb;
	}

	public void setDatabaseHandler(DatabaseHandler db) {
		this.mDb = db;
	}
	
	public void closeDatabaseHandler() {
		if(this.mDb != null) {
			this.mDb.close();
			this.mDb = null;
		}
	}
	
}
