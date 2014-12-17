package com.altujen.android.odb;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

public class Util {
	
	private static HttpClient httpclient;
	private static HttpGet httpget;
	private static InputStream instream;
	// Set the timeout in milliseconds until a connection is established.
	// The default value is zero, that means the timeout is not used. 
	private static final int timeoutConnection = 3000;
	// Set the default socket timeout (SO_TIMEOUT) 
	// in milliseconds which is the timeout for waiting for data.
	private static final int timeoutSocket = 5000;
	private static final DateFormat monthNameFormat = new SimpleDateFormat("MM yyyy", Locale.US);
	
	public static boolean chkUriExists(String strUri) throws Exception {
		boolean isExists = false;
		HttpURLConnection.setFollowRedirects(false);
		// note : you may also need
		//HttpURLConnection.setInstanceFollowRedirects(false);
		HttpURLConnection conn = null;
		
		if(strUri != null && !"".equals(strUri.trim())) {
			try {
				
				conn = (HttpURLConnection) new URL(strUri).openConnection();
				conn.setInstanceFollowRedirects(false);
				//conn.setRequestMethod("HEAD");
				isExists = (conn.getResponseCode() == HttpURLConnection.HTTP_OK);
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Http Error!");
			} finally {
				if(conn != null){
					conn.disconnect();
				}
			}
		}
		
		return isExists;
	}
	
	public static String getHttpContext(String strUri) throws Exception {
		
		httpget = new HttpGet(strUri);
		
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		
		httpclient = new DefaultHttpClient(httpParameters);
		instream = null;
		String strRtnVal = "";

		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				instream = entity.getContent();
				if(instream == null){
					throw new Exception("Http Error!");
				}
				try {

					strRtnVal = convertStreamToString(instream);
					// System.out.println(strRtnVal);

				} finally {
					if (instream != null) {
						instream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
			throw new Exception("Http Error!");

		} finally {
			// httpget.releaseConnection();
			httpget.abort();
		}

		return strRtnVal;
	}
	
	/**
	 * http://stackoverflow.com/a/5445161/1482579
	 * */
	private static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
	
	public static Date getFirstDay(Calendar cal) {
	    cal.set(Calendar.DAY_OF_MONTH, 1);
	    return cal.getTime();
	}
	
	public static Date getLastDay(Calendar cal) {
	    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
	    return cal.getTime();
	}
	
	public static Date getPickerViewLastDay(Calendar cal) {
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.MONTH, 1);
	    return cal.getTime();
	}
	
	// check network connection. 
	public static boolean isNetworkConnected(Context ctx) {
		
		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			// no connection
			return false;
		} else {
			if(!info.isAvailable()) {
				// connection not available.
				return false;
			} else {
				// true if having connection.
				return true;
			}
		}
		
	}
	
	public static boolean isWifiEnabled(Activity act) {
		// check is wifi opened.
		WifiManager wifiMgr = (WifiManager) act.getSystemService(android.content.Context.WIFI_SERVICE);
		
		return (wifiMgr == null ? false : wifiMgr.isWifiEnabled());
	}
	
	public static boolean isUsingWifi(Context ctx) {
		
		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if (info == null || !info.isConnected()) {
			// no connection
			return false;
		} else {
			if(!info.isAvailable()) {
				// connection not available.
				return false;
			} else {
				// true if having connection.
				return true;
			}
		}
		
	}
	
	public static String removeSymbol(String keyWord) {
    	
    	StringBuilder result = new StringBuilder();
    	for(int i=0; i<keyWord.length(); i++) {
    	    char tmpChar = keyWord.charAt( i );
    	    if (Character.isLetterOrDigit( tmpChar ) || tmpChar == '_' ) {
    	        result.append( tmpChar );
    	    }
    	}
    	
		return result.toString().trim();
    }
	
	// keys must match regex [a-z0-9_-]{1,64}
	public static String keyFilter(String imgKey) {
		
		if (imgKey != null && !imgKey.equals("")) {
			// keys must match regex [a-z0-9_-]{1,64}
			imgKey = (imgKey.replace(":", "").replace(".", "").replaceAll("/", "")).toLowerCase(Locale.ENGLISH);

			if (imgKey.length() > 64) {
				imgKey = imgKey.substring((imgKey.length() - 64));
			}
		}
		
		return imgKey;
	}
	
	public static void sendEzTrace(EasyTracker easyTracker, String category, String action, String label, Long value) {
		// MapBuilder.createEvent().build() returns a Map of event fields and
		// values that are set and sent with the hit.
		easyTracker.send(MapBuilder
			      .createEvent(category,          // Event category (required)
			                   			 action,              // Event action (required)
			                   			 label,                 // Event label
			                   			 value)               // Event value
			      .build()
		);
	}
	
	public static void sendEzCustomMetric(EasyTracker easyTracker, String screenName, int index, String metricValue) {
		// Custom metric value sent is with this screen view.
		easyTracker.send(MapBuilder
		    .createAppView()
		    .set(Fields.SCREEN_NAME, screenName)
		    .set(Fields.customMetric(index), metricValue)
		    .build()
		);
	}
	
	public static boolean isOdd(int Count) {
		
		if (Count % 2 != 0) {
			// odd
			return true;
		} else {
			// even
			return false;
		}
		
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options,
	        int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        // Calculate ratios of height and width to requested height and width
	    	final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);

	        // Choose the smallest ratio as inSampleSize value, this will
	        // guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }

	    return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res,
	        int resId, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    options.inDither=false;                     //Disable Dithering mode
	    options.inPurgeable=true;               //Tell to gc that whether it needs free memory, the Bitmap can be cleared
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static void imageResize(ImageView imgV, Bitmap imgPic, int intendedWidth) {
		
		if(imgPic != null) {
			
			imgV.setImageBitmap(imgPic);
			
			// Gets the width you want it to be
			// Gets the downloaded image dimensions
			int originalWidth = imgPic.getWidth();
			int originalHeight = imgPic.getHeight();

			// Calculates the new dimensions
			float scale = (float) intendedWidth / originalWidth;
			int newHeight = (int) Math.round(originalHeight * scale);
			
			// Resizes m_ImageView. Change "FrameLayout" to whatever layout m_ImageView is located in.
			imgV.getLayoutParams().width = intendedWidth;
			imgV.getLayoutParams().height = newHeight;
			
		}
		
	}
	
	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
	public static String getMonthName(Date date) {
		return (date == null ? "Null Date." : monthNameFormat.format(date));
	}
	
	public static void redraw(final TextView tv, final int progress) {

		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, progress);

	}
	
	public static String getZhyMp3Uri(String zhtUri) {
		
		if(zhtUri != null && !zhtUri.equals("")) {
			return zhtUri.replaceAll("zht", "zhy");
		}
		
		return "";
		
	}
	
	/*public static String appendWildcard(String query) {
		if (TextUtils.isEmpty(query))
			return query;

		final StringBuilder builder = new StringBuilder();
		final String[] splits = TextUtils.split(query, " ");

		for (String split : splits)
			builder.append(" ").append("*").append(split).append("*")
					.append(" ");

		return builder.toString().trim();
	}*/

	/*public static String makePlaceholders(int len) {
		if (len < 1) {
			// It will lead to an invalid query anyway ..
			throw new RuntimeException("No placeholders");
		} else {
			StringBuilder sb = new StringBuilder(len * 2 - 1);
			sb.append("?");
			for (int i = 1; i < len; i++) {
				sb.append(",?");
			}
			return sb.toString();
		}
	}*/
	
}
