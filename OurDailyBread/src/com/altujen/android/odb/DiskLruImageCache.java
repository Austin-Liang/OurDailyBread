package com.altujen.android.odb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

public class DiskLruImageCache {
	
	private DiskLruCache mDiskCache;
    private static final CompressFormat mCompressFormat = CompressFormat.JPEG;
    private static final int mCompressQuality = 100; // no compress. due to the picture is very small.
    private static final String UNIQUE_NAME = "ODB_AUTHORS";
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    //private static final String TAG = "DiskLruImageCache";
    
	public DiskLruImageCache( Context context ) {
		try {
			final File diskCacheDir = getDiskCacheDir( context, UNIQUE_NAME );
			mDiskCache = DiskLruCache.open( diskCacheDir, APP_VERSION, VALUE_COUNT, DISK_CACHE_SIZE );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

    private boolean writeBitmapToFile( Bitmap bitmap, DiskLruCache.Editor editor )
        throws IOException, FileNotFoundException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream( editor.newOutputStream( 0 ), DiskLruCacheUtils.IO_BUFFER_SIZE );
            return bitmap.compress( mCompressFormat, mCompressQuality, out );
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

	private File getDiskCacheDir(Context context, String uniqueName) {

		File diskCacheDir;
		// Check if media is mounted or storage is built-in, if so, try and use
		// external cache dir
		// otherwise use internal cache dir
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !DiskLruCacheUtils.isExternalStorageRemovable() ? DiskLruCacheUtils
				.getExternalCacheDir(context).getPath() : context.getCacheDir().getPath();
		
		diskCacheDir = new File(cachePath + File.separator + uniqueName);
		
		if(!diskCacheDir.exists()) {
			diskCacheDir.mkdirs();
		}

		return diskCacheDir;
	}

    public void put( String key, Bitmap data ) {

        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit( key );
            if ( editor == null ) {
                return;
            }

            if( writeBitmapToFile( data, editor ) ) {
                mDiskCache.flush();
                editor.commit();
                if ( BuildConfig.DEBUG ) {
                   Log.d( "cache_test_DISK_", "image put on disk cache " + key );
                }
            } else {
                editor.abort();
                if ( BuildConfig.DEBUG ) {
                    Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
                }
            }
        } catch (IOException e) {
            if ( BuildConfig.DEBUG ) {
                Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
            }
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }

    }

    public Bitmap getBitmap( String key ) {

        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {
        	if(this.isClosed()) {
        		return bitmap;
        	}
            snapshot = mDiskCache.get( key );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );
            if ( in != null ) {
                final BufferedInputStream buffIn = 
                new BufferedInputStream( in, DiskLruCacheUtils.IO_BUFFER_SIZE );
                bitmap = BitmapFactory.decodeStream( buffIn );
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        if ( BuildConfig.DEBUG ) {
            Log.d( "cache_test_DISK_", bitmap == null ? "" : "image read from disk " + key);
        }

        return bitmap;

    }

    public boolean containsKey( String key ) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( key );
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return contained;

    }

    public void clearCache() {
        if ( BuildConfig.DEBUG ) {
            Log.d( "cache_test_DISK_", "disk cache CLEARED");
        }
        try {
            mDiskCache.delete();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }
    
    public boolean isClosed() {
		return (mDiskCache == null ? true : mDiskCache.isClosed());
    }
    
    public void close() {
    	if ( BuildConfig.DEBUG ) {
            Log.d( "cache_test_DISK_", "disk cache CLOSED");
        }
    	
    	if(mDiskCache != null && !(mDiskCache.isClosed())) {
    		try {
				mDiskCache.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
	
}
