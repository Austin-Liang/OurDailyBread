package com.altujen.android.odb;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import se.emilsjolander.flipview.FlipView;
import se.emilsjolander.flipview.FlipView.OnFlipListener;
import se.emilsjolander.flipview.FlipView.OnFlipViewSwipeListener;
import se.emilsjolander.flipview.FlipView.OnOverFlipListener;
import se.emilsjolander.flipview.OverFlipMode;
import se.emilsjolander.flipview.SwipeMode;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.altujen.android.odb.CalendarPickerFragment.CalendarPickerDialogListener;
import com.altujen.android.odb.FlipAdapter.FlipAdapterCallback;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseView.ConfigOptions;
import com.espian.showcaseview.ShowcaseView.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseViews;
import com.espian.showcaseview.ShowcaseViews.ItemViewProperties;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class DailyBreadActivity extends SlidingFragmentActivity implements FlipAdapterCallback, OnFlipListener, OnOverFlipListener, OnFlipViewSwipeListener, CalendarPickerDialogListener, OnQueryTextListener, OnShowcaseEventListener {

	public static final String EXTRA_MESSAGE = "com.altujen.android.odb.MESSAGE";
	public static final String EXTRA_CALENDAR = "com.altujen.android.odb.CALENDAR";
	public static final String EXTRA_CALENDAR_PICKER = "com.altujen.android.odb.CalendarPicker";
	
	private CalendarPickerFragment calendarPicker;
	
	private FlipView mFlipView;
	private static FlipAdapter mAdapter;
	private SlidingMenu menu;
	private SearchView mSearchView;
	private MenuItem searchItem;
	private ImageButton ibtn_refresh;
	private boolean isSearched;
	private static Calendar odbCalendar;
	private static int odbPage = -1;
	private int tempUserLang = -1;
	private int Measuredwidth;
	private int Measuredheight;
	
    private static final float SHOWCASE_SMALL_SCALE = 0.5f;
    private ConfigOptions mOptions = new ConfigOptions();
    
    private ShowcaseViews mShowcaseViews;
    private ShowcaseView mShowcaseView;
    private ItemViewProperties ivp_ibtn_refresh;
    private ItemViewProperties ivp_ibtn_search;
    private ItemViewProperties ivp_ibtn_appIcon;
	
	private static Date today;
	private static Calendar cal = new GregorianCalendar();
	
	private static DailyBreadSettings user_settings;
	private static final String PREF = "com.altujen.android.odb_PREF";
    private static final String PREF_LANGUAGE = "odb_Lang";
    private static final String PREF_FONTSIZE = "odb_FontSize";
    private static final String PREF_APPGUIDE = "odb_AppGuide";
    private boolean isAppGuideShown = false;
	
    // time in milliseconds
    private static final long SIMULATE_THREAD_TIME = 700;
	
	private DatabaseHandler db;
	
	// UI Handler
	private final UI_Handler mUI_Handler = new UI_Handler(this);
	// Runnable thread handler
	private final Handler handler = new Handler();
	private Runnable notification;
	
	private Handler mThreadHandler;
	private HandlerThread mThread;
	private DatabaseInitializer dbizer;
	
	private static ProgressDialog progDialog = null;
	
	private static EasyTracker easyTracker;
	private boolean ezTracker_NullFlag = false;
	private static final String EZ_Category = "dailybread_action";
    private static final String EZ_Action = "button_press";
	
	private static final String TAG = "DailyBreadActivity";

	private void initializeEzTracker() {
		// May return null if a EasyTracker has not yet been initialized with a
		// property ID.
		try {
			easyTracker = EasyTracker.getInstance(this);
			ezTracker_NullFlag = (easyTracker == null ? true : false);
		} catch (Exception ignored) {
			ezTracker_NullFlag = true;
		}
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
		initializeEzTracker();
		today = new Date();
		
		// Do something to define each screen size...
		/*boolean isTablet = getResources().getBoolean(R.bool.isTablet);
		String strScreenSize = getResources().getString(R.string.screenSize);
		Log.v(TAG, "isTablet ： " + isTablet);*/
		
		// set the Above View
		setContentView(R.layout.activity_daily_bread);
		
		{   // configure the SlidingMenu
			menu = getSlidingMenu();
			menu.setMenu(R.layout.menu);
			menu.setMode(SlidingMenu.LEFT);
			//menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
			menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			menu.setShadowWidthRes(R.dimen.shadow_width);
			menu.setShadowDrawable(R.drawable.shadow);
			menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
			menu.setFadeDegree(0.35f);
		}

		{   // set the Behind View
			setBehindContentView(R.layout.menu_frame);
			getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new MenuListFragment()).commit();
		}

		{   // configure the SlidingMenu
			setSlidingActionBarEnabled(true);
			getSupportActionBar().setDisplayShowCustomEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		{   // measuring screen width & height
			Point size = new Point();
			WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				wm.getDefaultDisplay().getSize(size);
			    Measuredwidth = size.x;
			    Measuredheight = size.y;
			} else {
			    Display d = wm.getDefaultDisplay();
			    Measuredwidth = d.getWidth();
			    Measuredheight = d.getHeight();
			}
		}
		
		mThread = new HandlerThread("ODBDataThread");
		mThread.start();
		mThreadHandler = new Handler(mThread.getLooper());
		
		user_settings = (DailyBreadSettings) this.getApplicationContext();
		
		// Restore Preference.
		restorePrefs();
				
		db = user_settings.getDatabaseHandler();
		dbizer = new DatabaseInitializer(db, mUI_Handler, user_settings.getLanguageSettings());
		
		
		if(savedInstanceState == null) {
			
			if(db.getOdbsCount(user_settings.getLanguageSettings()) > 0) {
				// has data.
				
				// Check Internet status.
				if (Util.isNetworkConnected(DailyBreadActivity.this)) {
					// internet is ready.
					// do data check in background.
					// *** Should add new method : getInitialDataRunnable without findViews ( initialData Only. )
					// *** Update : Sadly, the UI Thread which inflate include layout xml were too slow, 
					//                        it will cause error when we trying to get ibtn_refresh from layout.
					
					// Show ProgressDialog
					progDialog = ProgressDialog.show(DailyBreadActivity.this, getString(R.string.dialog_title),
							getString(R.string.dialog_msg), true, false);
					mThreadHandler.post(dbizer.getInitialDataRunnable());
					
				} else {
					// internet is not ready.
					// abort data check... go to findViews()
					// *** Update : due to UI Thread inflating program, we have to simulate data checking runnable.
					Message msg = new Message();
	                msg.what = EnumMsg.STOP_SIMULATE_THREAD;
	                // Show ProgressDialog
					progDialog = ProgressDialog.show(DailyBreadActivity.this, getString(R.string.dialog_title),
							getString(R.string.dialog_msg), true, false);
	                mUI_Handler.sendMessageDelayed(msg, SIMULATE_THREAD_TIME);
				}
				
			} else {
				// no data. first time to grab data from http.
				
				if (Util.isNetworkConnected(DailyBreadActivity.this)) {
					// Show ProgressDialog
					progDialog = ProgressDialog.show(DailyBreadActivity.this, getString(R.string.dialog_title),
							getString(R.string.dialog_msg), true, false);
					Log.v(TAG, "progDialog_Start");
					
					mThreadHandler.post(dbizer.getInitialDataRunnable());
					
				} else {
					
					if(!Util.isWifiEnabled(DailyBreadActivity.this) || !Util.isNetworkConnected(DailyBreadActivity.this)) {
						// *** Update : should save status when activity end.
						Builder builder = new Builder(DailyBreadActivity.this);
						builder.setTitle(R.string.dialog_no_wifi_title);
						builder.setMessage(R.string.dialog_no_wifi_msg);
						builder.setCancelable(false);
						builder.setPositiveButton(R.string.dialog_no_wifi_btnText,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										// Open wifi settings.
										// startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
										startActivity(new Intent(Settings.ACTION_SETTINGS));
										DailyBreadActivity.this.finish();
									}
								});
						builder.create();
						builder.show();
					} else {
						// show message dialog.
						AlertDialog.Builder builder = new AlertDialog.Builder(DailyBreadActivity.this);
						builder.setMessage(R.string.dialog_no_internet_msg)
								.setTitle(R.string.dialog_no_internet_title)
								.setPositiveButton(R.string.btnText_ok,
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int id) {
												// User clicked OK button
												dialog.dismiss();
												DailyBreadActivity.this.finish();
											}
										})
								.setCancelable(false)
								.setIcon(R.drawable.ic_launcher);
						
						AlertDialog dialog = builder.create();
						dialog.show();
					}
					
				}
				
			}
			
		}
		
	}
	
    private void restorePrefs() {
    	
    	// 2013-10-14 : Try to use DailyBreadSettings to store user last time usage?
    	// depending on last time usage to determine update process logic.
    	SharedPreferences settings = getSharedPreferences(PREF, MODE_PRIVATE);
    	int pref_lang = settings.getInt(PREF_LANGUAGE, -1);
    	//PREF_FONTSIZE
    	int pref_fontSize = settings.getInt(PREF_FONTSIZE, -1);
    	isAppGuideShown = settings.getBoolean(PREF_APPGUIDE, true);
    	
    	if(pref_lang != -1) {
    		user_settings.setLanguageSettings(MultiLangUtil.getLangCodeByLang(pref_lang));
    	} else {
    		// Get Current Locale
    		Locale current = getResources().getConfiguration().locale;
    		pref_lang = MultiLangUtil.getLangCodeByCountry(current.getCountry());
    		user_settings.setLanguageSettings(pref_lang);
    		settings.edit().putInt(PREF_LANGUAGE, pref_lang).commit();
    	}
    	
    	if(pref_fontSize != -1) {
    		user_settings.setFont_Size(pref_fontSize);
    	} else {
    		// Get Current Locale
    		user_settings.setFont_Size(18);
    		settings.edit().putInt(PREF_FONTSIZE, 18).commit();
    	}
    	
    	// Initialize DatabaseHandler
    	if(user_settings.getDatabaseHandler() == null) {
    		user_settings.setDatabaseHandler(new DatabaseHandler(this.getApplicationContext()));
    	}
		// Initialize mDiskLruCache
    	if(user_settings.getDiskLruCache() == null) {
    		user_settings.setDiskLruCache(new DiskLruImageCache(this.getApplicationContext()));
    	}
		// Initialize MissingAvatar bitmap.
    	if(user_settings.getMissingAvatar() == null) {
    		user_settings.setMissingAvatar(Util.decodeSampledBitmapFromResource(this.getResources(),R.drawable.missing_avatar, (77 * 3), (90 * 3)));
    	}
    	
    }
    
    @Override
    public void onStart() {
      super.onStart();
      // https://developers.google.com/analytics/devguides/collection/android/v3/
      if(!ezTracker_NullFlag) {easyTracker.activityStart(this);}
    }
    
    @Override
    public void onStop() {
      super.onStop();
      // https://developers.google.com/analytics/devguides/collection/android/v3/
      if(!ezTracker_NullFlag) {easyTracker.activityStop(this);}
    }
    
    @Override
    protected void onPause() {
    	
    	super.onPause();
    	Log.v(TAG, "onPause");
    	
    	// Save user preference. Use Editor object to make changes.
    	SharedPreferences settings = getSharedPreferences(PREF, MODE_PRIVATE);
    	settings.edit().putInt(PREF_LANGUAGE, user_settings.getLanguageSettings()).commit();
    	settings.edit().putInt(PREF_FONTSIZE, user_settings.getFontSize()).commit();
    	settings.edit().putBoolean(PREF_APPGUIDE, isAppGuideShown).commit();
    	
    }
	
	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if(savedInstanceState != null) {
			// abort data check... go to findViews()
			// *** Update : due to UI Thread inflating program, we have to simulate data checking runnable.
			Message msg = new Message();
			msg.what = EnumMsg.STOP_SIMULATE_THREAD_ONPOSTCREATE;
			msg.obj = savedInstanceState;
			// Show ProgressDialog
			progDialog = ProgressDialog.show(DailyBreadActivity.this, getString(R.string.dialog_title),
					getString(R.string.dialog_msg), true, false);
			mUI_Handler.sendMessageDelayed(msg, SIMULATE_THREAD_TIME);
		}
	}
	
	private void findViews(Calendar cal) {
		
		isSearched = false;
		
		{   // initial image button.
			ibtn_refresh = (ImageButton) findViewById(R.id.ibtn_refresh);
			ibtn_refresh.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					{
						if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "refresh_button", null);}
					}
					
					if(Util.isWifiEnabled(DailyBreadActivity.this) || Util.isNetworkConnected(DailyBreadActivity.this)) {
						// Set fade-in animation.
						Animation anim = AnimationUtils.loadAnimation(ibtn_refresh.getContext(), R.anim.rotate_360);
						anim.reset();
						
						// once clicked, set click-able to false.
						ibtn_refresh.setClickable(false);
						
						// do data check in background thread.
						// in this moment, the image button is already initialized, no null pointer error to be worried.
						mThreadHandler.post(dbizer.getChkDataManualRunnable());
						
				        ibtn_refresh.startAnimation(anim);
				        
					} else {
						Toast.makeText(getBaseContext(), R.string.internet_is_not_ready, Toast.LENGTH_SHORT).show();
					}
					
				}

			});
		}
		
		mFlipView = (FlipView) findViewById(R.id.flip_view);
		mAdapter = new FlipAdapter(this, db, cal, user_settings.getLanguageSettings());
		mAdapter.setCallback(this);
		mFlipView.setAdapter(mAdapter);
		mFlipView.setOnFlipListener(this);
		mFlipView.peakNext(false, true);
		mFlipView.setOverFlipMode(OverFlipMode.GLOW);
		mFlipView.setEmptyView(findViewById(R.id.empty_view));
		mFlipView.setOnOverFlipListener(this);
		mFlipView.setOnFlipViewSwipeListener(this);
		
		calendarPicker = CalendarPickerFragment.newInstance(cal);
		
		mOptions.block = true;
        mOptions.hideOnClickOutside = false;
        mOptions.shotType = ShowcaseView.TYPE_NO_LIMIT;
        
		ivp_ibtn_refresh = new ShowcaseViews.ItemViewProperties(R.id.ibtn_refresh,
                R.string.guide_refresh_title,
                R.string.guide_refresh_message,
                SHOWCASE_SMALL_SCALE,
                mOptions);
		ivp_ibtn_search = new ShowcaseViews.ItemViewProperties(R.id.menu_search,
                R.string.guide_search_title,
                R.string.guide_search_message,
                SHOWCASE_SMALL_SCALE,
                mOptions);
		ivp_ibtn_appIcon = new ShowcaseViews.ItemViewProperties(android.R.id.home,
		        R.string.guide_appIcon_title,
		        R.string.guide_appIcon_message,
		        SHOWCASE_SMALL_SCALE,
		        mOptions);
		
		mShowcaseViews = new ShowcaseViews(this,
				R.layout.showcase_view_template,
				new ShowcaseViews.OnShowcaseAcknowledged() {
					@Override
					public void onShowCaseAcknowledged(ShowcaseView showcaseView) {
						if (isAppGuideShown) {
							if(isSearched) {
								isSearched = false;
								mAdapter.reloadItems(user_settings.getLanguageSettings(), null, true);
							}
							mOptions.block = true;
							mOptions.hideOnClickOutside = false;
							mOptions.shotType = ShowcaseView.TYPE_NO_LIMIT;
							mOptions.showcaseId = 1;
							mShowcaseView = ShowcaseView.insertShowcaseView((Measuredwidth / 2), ((Measuredheight / 2) - (Measuredheight / 5)), DailyBreadActivity.this, R.string.guide_calendar_title, R.string.guide_calendar_message, mOptions);
							mShowcaseView.setOnShowcaseEventListener(DailyBreadActivity.this);
							mShowcaseView.animateGesture(300, 0, -300, 0);
						}
					}
				});
		
		if(isAppGuideShown) {
			showAppGuide();
		}
		
	}
	
	private void showAppGuide() {
		{   // ShowcaseView.
			mShowcaseViews.addView(ivp_ibtn_refresh);
			mShowcaseViews.addView(ivp_ibtn_search);
			mShowcaseViews.addView(ivp_ibtn_appIcon);
			mShowcaseViews.show();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mThread != null) {
			mThread.quit();
		}
		
		if(mThreadHandler != null) {
			//mThreadHandler.removeCallbacks(dbizer.getInitialDataRunnable());
			// If token is null, all callbacks and messages will be removed.
			mThreadHandler.removeCallbacksAndMessages(null);
		}
		
		if(mUI_Handler != null) {
			// If token is null, all callbacks and messages will be removed.
			mUI_Handler.removeCallbacksAndMessages(null);
		}
		
		if(notification != null || handler != null) {
			// If token is null, all callbacks and messages will be removed.
			handler.removeCallbacksAndMessages(null);
			notification = null;
		}
		
		if(mAdapter != null) {
			mAdapter.clearTasks();
		}
		
		if(user_settings.getMissingAvatar() != null) {
			user_settings.clearMissingAvatar();
		}
		
		if(user_settings.getDiskLruCache() != null) {
			user_settings.closeDiskLruImageCache();
		}
		
		if(user_settings.getDatabaseHandler() != null) {
			user_settings.closeDatabaseHandler();
		}
		
		if(progDialog != null && progDialog.isShowing()) {
			progDialog.dismiss();
		}
		
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.daily_bread, menu);
		
		searchItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint(getString(R.string.search_hint));
        setupSearchView(searchItem);
        
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
		switch (item.getItemId()) {
		case android.R.id.home:
			{
				if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "menu_button", null);}
			}
			// toggle sliding menu.
			menu.toggle();
			if(isAppGuideShown) {
				handler.removeCallbacksAndMessages(null);
				notification = new Runnable() {
					public void run() {
						menu.toggle();
					}
				};
				handler.postDelayed(notification, 500);
			}

			return true;
		}
        
        return super.onOptionsItemSelected(item);
    }
    
    private void setupSearchView(MenuItem searchItem) {
    	 
        if (isAlwaysExpanded()) {
            mSearchView.setIconifiedByDefault(false);
        } else {
            searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
        mSearchView.setOnQueryTextListener(this);
        
    }
    
    protected boolean isAlwaysExpanded() {
        return false;
    }
	
	@Override
	public void onDetailRequested(int odbID) {
		
		if(mShowcaseView == null || !mShowcaseView.isShown()) {
			Intent intent = new Intent(this, DetailActivity.class);
			intent.putExtra(EXTRA_MESSAGE, odbID + "");
		    startActivity(intent);
		}
	    
	}

	@Override
	public void onFlippedToPage(FlipView v, int position, long id) {
		//Toast.makeText(getBaseContext(), "Page: "+position, Toast.LENGTH_SHORT).show();
		if(position > mFlipView.getPageCount()-3 && mFlipView.getPageCount()<30){
			//mAdapter.addItems(5);
		}
	}

	@Override
	public void onOverFlip(FlipView v, OverFlipMode mode,
			boolean overFlippingPrevious, float overFlipDistance,
			float flipDistancePerPage) {
		Log.i("overflip", "overFlipDistance = "+overFlipDistance);
		Log.i("overflip", "flipDistancePerPage = "+flipDistancePerPage);
	}
	
	// http://stackoverflow.com/questions/1512045/how-to-disable-orientation-change-in-android
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	@Override
    public void onLowMemory() {
		
        //clear all memory cached images when system is in low memory
        //note that you can configure the max image cache count, see CONFIGURATION
		Log.v(TAG, "onLowMemory");
		
    }

	@Override
	public boolean onQueryTextSubmit(String query) {
		
		isSearched = true;
		mSearchView.setIconified(true);
		mSearchView.clearFocus();
		searchItem.collapseActionView();
		
		{
			if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "search_button", null);}
		}
		
		mAdapter.reloadSearchResultItems(user_settings.getLanguageSettings(), query);
		
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			// When BACK button is clicked.
			
			if(menu != null && menu.isMenuShowing()) {
				menu.toggle();
			} else {
				if(isSearched) {
					
					isSearched = false;
					mAdapter.reloadItems(user_settings.getLanguageSettings(), null, true);
					
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage(R.string.exitMsg)
							.setTitle(R.string.plzChoose)
							.setPositiveButton(R.string.btnText_ok,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											// User clicked OK button
											dialog.dismiss();
											DailyBreadActivity.this.finish();
										}
									})
							.setNegativeButton(R.string.btnText_cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											// User cancelled the dialog
											dialog.dismiss();
										}
									})
							.setCancelable(true)
							.setIcon(R.drawable.ic_launcher);
					
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
			
			return true;
			
		case KeyEvent.KEYCODE_MENU:
			
			if(menu != null) {
				menu.toggle();
			}
			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public void alertContent(ListView l, View v, int position, long id) {

		switch (position) {
		case 2: // Languages.
			
			{
				if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "setting_button", null);}
			}
			
			// http://www.yogeshblogspot.com/android-alert-dialog-with-radio-button/
			final CharSequence[] items = this.getResources().getStringArray(R.array.select_language_items);
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.location_web_site)
					.setTitle(R.string.menu_language)
					.setPositiveButton(R.string.btnText_ok,
							new DialogInterface.OnClickListener() {
						
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
									if(tempUserLang != -1 && tempUserLang != user_settings.getLanguageSettings()) {
										// commit changes.
										user_settings.setLanguageSettings(MultiLangUtil.getLangCodeByLang(tempUserLang));
										tempUserLang = -1;
										
										// dismiss.
										dialog.dismiss();
										
										// show dialog.
										progDialog = ProgressDialog.show(DailyBreadActivity.this,
												getString(R.string.dialog_title), getString(R.string.dialog_msg), true, false);
										Log.v(TAG, "progDialog_Start");

										mThreadHandler.post(dbizer.getNewInitializeRunnable(db, mUI_Handler, user_settings.getLanguageSettings()));
									} else {
										// dismiss.
										dialog.dismiss();
									}
									
								}
								
							})
					.setNegativeButton(R.string.btnText_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									// User cancelled the dialog
									tempUserLang = -1;
									dialog.dismiss();
								}
							})
					.setSingleChoiceItems(items, user_settings.getLanguageSettings(), new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									
									switch(which) {
									case EnumLang.zh_TW:
										tempUserLang = EnumLang.zh_TW;
										break;
										
									case EnumLang.zh_CN:
										tempUserLang = EnumLang.zh_CN;
										break;
										
									case EnumLang.EN:
										tempUserLang = EnumLang.EN;
										break;
										
									case EnumLang.JP:
										tempUserLang = EnumLang.JP;
										break;
										
									}
									
								}
							}).show();

			break;
			
		case 3: // font-size setting.
			
			{
				if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "fontSize_button", null);}
			}
			
			// retrieve display dimensions
			Rect displayRectangle = new Rect();
			Window window = this.getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
			
			final View checkBoxView = View.inflate(this, R.layout.dialog_font_size, null);
			checkBoxView.setMinimumWidth((int)(displayRectangle.width() * 0.85f));
			checkBoxView.setMinimumHeight((int)(displayRectangle.height() * 0.8f));
			final TextView tv = (TextView) checkBoxView.findViewById(R.id.txt_TestText);
			redraw(tv, user_settings.getFontSize());
			final SeekBar seek = (SeekBar) checkBoxView.findViewById(R.id.seekBar1);
			seek.setProgress((user_settings.getFontSize() - 14));
			seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// TODO Auto-generated method stub
					redraw(tv, (progress + 14));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}});
			new AlertDialog.Builder(this)
			.setView(checkBoxView)
			.setIcon(R.drawable.ic_menu_settings_holo_light)
			.setTitle(R.string.menu_language)
			.setPositiveButton(R.string.btnText_ok,
					new DialogInterface.OnClickListener() {
				
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							// commit changes.
							user_settings.setFont_Size((seek.getProgress() + 14));
							{
								if(!ezTracker_NullFlag) {Util.sendEzCustomMetric(easyTracker, "fontSize", 1, String.valueOf((seek.getProgress() + 14)));}
							}
							dialog.dismiss();
							
						}
						
					})
			.setNegativeButton(R.string.btnText_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).show();
			
			break;
			
		case 4: // show app guide.
			
			{
				if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "appGuide_button", null);}
			}
			
			handler.removeCallbacksAndMessages(null);
			notification = new Runnable() {
				public void run() {
					isAppGuideShown = true;
					showAppGuide();
				}
			};
			handler.postDelayed(notification, 500);
			
			break;
			
		case 5: // show about dialog.
			
			{
				if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "about_button", null);}
			}
			
			String versionName = null;
			try {
				versionName = getPackageManager()
				.getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				versionName = null;
			}
			
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.hardware_phone)
					.setTitle(
							getString(R.string.about)
									+ " - "
									+ getString(R.string.app_name)
									+ (versionName == null ? "" : "(" + "v"
											+ versionName + ")"))
					.setMessage(Html.fromHtml(getString(R.string.about_msg)))
					.setPositiveButton(R.string.like_app,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									{
										if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "rateApp_button", null);}
									}
									Uri uri = Uri.parse("market://details?id="
											+ getPackageName());
									Intent goToMarket = new Intent(
											Intent.ACTION_VIEW, uri);
									dialog.dismiss();
									startActivity(goToMarket);
								}

							})
					.setNegativeButton(R.string.btnText_close,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();

			break;

		case 0: // Last Month.
		case 1: // Next Month.
			
			{
				if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, (position == 0 ? "lastMonth_button" : "nextMonth_button"), null);}
			}
			
			Calendar cal = mAdapter.getCalendar();
			
			if (progDialog != null) {
				// not null.
				if (!(progDialog.isShowing())) {
					switch (position) {
					case 0:
						cal.add(Calendar.MONTH, -1);
						break;
					case 1:
						cal.add(Calendar.MONTH, 1);
						break;
					}

					// Show ProgressDialog
					progDialog = ProgressDialog.show(DailyBreadActivity.this,
							getString(R.string.dialog_title), getString(R.string.dialog_msg), true, false);
					Log.v(TAG, "progDialog_Start");

					mThreadHandler.post(dbizer.getGetDataRunnable(db, cal,	mUI_Handler, user_settings.getLanguageSettings()));
				}
			} else {
				// null.

				switch (position) {
				case 0:
					cal.add(Calendar.MONTH, -1);
					break;
				case 1:
					cal.add(Calendar.MONTH, 1);
					break;
				}

				// Show ProgressDialog
				progDialog = ProgressDialog.show(DailyBreadActivity.this,
						getString(R.string.dialog_title), getString(R.string.dialog_msg), true, false);
				Log.v(TAG, "progDialog_Start");

				mThreadHandler.post(dbizer.getGetDataRunnable(db, cal,	mUI_Handler, user_settings.getLanguageSettings()));

			}

			break;
		}
		// toggle menu back.
		menu.showContent();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.v(TAG, "onRestoreInstanceState");
		
		if(savedInstanceState.containsKey("odbCalendar")) {
			// Restore current calendar.
			odbCalendar = new GregorianCalendar();
			odbCalendar.setTimeInMillis(savedInstanceState.getLong("odbCalendar", 0000));  // default = 0000
		}
		
		if(savedInstanceState.containsKey("odbPage")) {
			// Restore current page.
			odbPage = savedInstanceState.getInt("odbPage", -1);  // default = -1
		}
		
	}
	
	private static void redraw(final TextView tv, final int progress){
		
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, progress);
		
		/*// The gesture threshold expressed in dip
		final float GESTURE_THRESHOLD_DIP = 16.0f;
		final float scale = iv.getContext().getResources().getDisplayMetrics().density;
		int mGestureThreshold = (int) (GESTURE_THRESHOLD_DIP * size + 0.5f);
		
		Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(10);
        paint.setAntiAlias(true);
        paint.setTextSize(mGestureThreshold);
        
        float blackTextWidth = paint.measureText("位在愛爾蘭戈爾韋的聖尼古拉斯教堂，有著悠久的歷史，至今仍十分活躍。那是愛爾蘭最古老的教堂，而且教堂還以一種非常實際的方式提供引導。原來這座教堂乃城鎮中最高的建築物，高聳的尖塔常被船長們用作導航指引，讓船隻安全地航入戈爾韋海灣。幾世紀以來，這間教堂...");
        
        Bitmap bmp = Bitmap.createBitmap((int)blackTextWidth, (int)blackTextWidth, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bmp);
        //Draw the image bitmap into the cavas
        canvas.drawBitmap(bmp, 0, 0, null);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.save();
        
        float startPositionX = (canvas.getWidth() - blackTextWidth) / 2;
        
        //int xPos = (canvas.getWidth() / 2);
        //int xPos = (int) ((canvas.getWidth() - paint.getTextSize() * Math.abs(1 / 2)) / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ; 
        
        canvas.drawText("位在愛爾蘭戈爾韋的聖尼古拉斯教堂，有著悠久的歷史，至今仍十分活躍。那是愛爾蘭最古老的教堂，而且教堂還以一種非常實際的方式提供引導。原來這座教堂乃城鎮中最高的建築物，高聳的尖塔常被船長們用作導航指引，讓船隻安全地航入戈爾韋海灣。幾世紀以來，這間教堂...", startPositionX, yPos, paint);
        canvas.restore();
        
        iv.setImageBitmap(bmp);*/
	} 

	// http://stackoverflow.com/a/151940/1482579
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.v(TAG, "onSaveInstanceState");
		if(mAdapter != null) {
			outState.putLong("odbCalendar", mAdapter.getCalendar().getTimeInMillis());
			outState.putInt("odbPage", mAdapter.getPage());
		}
	}
	
	private void onPageRequested(int page) {
		mFlipView.smoothFlipTo(page);
	}
	
	@Override
	public void onFlipViewSwipe(FlipView v, SwipeMode mode) {
		
		switch(mode) {
		case onSwipeTop:
			Log.v(TAG, "onSwipe : Top");
			break;
		case onSwipeRight:
			Log.v(TAG, "onSwipe : Right");
			break;
		case onSwipeLeft:
			if(!isSearched) {
				{
					if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, "swipe_left", "calendar_menu", null);}
				}
				Bundle bundle = new Bundle(1);
			    bundle.putLong(DailyBreadActivity.EXTRA_CALENDAR, mAdapter.getCalendar().getTimeInMillis());
			    calendarPicker.setArguments(bundle);
				calendarPicker.show(getSupportFragmentManager(), DailyBreadActivity.EXTRA_CALENDAR_PICKER);
			}
			Log.v(TAG, "onSwipe : Left");
			break;
		case onSwipeBottom:
			Log.v(TAG, "onSwipe : Bottom");
			break;
		case onNeutral:
			Log.v(TAG, "onSwipe : Neutral");
			break;
		}
		
	}

	@Override
	public void onFinishPickerDialog(int dayOfMonth) {
		onPageRequested(((int)Math.ceil((dayOfMonth/2.0) - 1)));
	}
	
	@Override
	public void onShowcaseViewHide(ShowcaseView showcaseView) {
		switch (mOptions.showcaseId) {
		case 1:
			mOptions.showcaseId = 2;
			int finalPage = (int)Math.ceil((mAdapter.getCount()/2.0) - 1);
			if(mAdapter.getPage() == finalPage) {
				// at finalPage.
				mShowcaseView = ShowcaseView.insertShowcaseView((Measuredwidth / 2), ((Measuredheight / 2) - (Measuredheight / 5)), DailyBreadActivity.this, R.string.guide_flip_title, R.string.guide_flip_message, mOptions);
		        mShowcaseView.animateGesture(0, 0, 0, 400);
			} else {
				mShowcaseView = ShowcaseView.insertShowcaseView((Measuredwidth / 2), ((Measuredheight / 2) + (Measuredheight / 4)), DailyBreadActivity.this, R.string.guide_flip_title, R.string.guide_flip_message, mOptions);
		        mShowcaseView.animateGesture(0, 0, 0, -400);
			}
	        isAppGuideShown = false;
			break;
		case 2:
			isAppGuideShown = false;
			break;
		}
	}

	@Override
	public void onShowcaseViewShow(ShowcaseView showcaseView) {
		// Do nothing.
	}
	
	/**
	 * Instances of static inner classes do not hold an implicit reference to
	 * their outer class.
	 * http://www.androiddesignpatterns.com/2013/01/inner-class-handler-memory-leak.html
	 * http://stackoverflow.com/questions/17899328/this-handler-class-should-be-static-or-leaks-might-occur-com-test-test3-ui-main
	 */
	private static class UI_Handler extends Handler {
		private final WeakReference<DailyBreadActivity> mActivity;

		public UI_Handler(DailyBreadActivity activity) {
			mActivity = new WeakReference<DailyBreadActivity>(activity);
		}

		// http://stackoverflow.com/questions/7298731/when-to-call-activity-context-or-application-context
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			DailyBreadActivity activity = mActivity.get();
			if (activity != null) {
				// handle messages from here...
				switch (msg.what) {
				case EnumMsg.STOP_SIMULATE_THREAD:   // simulate data checking process.
					
					if(progDialog != null && progDialog.isShowing()) {
						progDialog.dismiss();
					}
					activity.findViews(null);
					cal.setTime(today);
					if(mAdapter.getCalendar().get(GregorianCalendar.MONTH) == cal.get(GregorianCalendar.MONTH)) {
						activity.onPageRequested(((int)Math.ceil((cal.get(GregorianCalendar.DAY_OF_MONTH)/2.0) - 1)));
					}
					
					break;
					
				case EnumMsg.STOP_SIMULATE_THREAD_ONPOSTCREATE:   // simulate data checking process.
					
					if(progDialog != null && progDialog.isShowing()) {
						progDialog.dismiss();
					}
					
					Bundle savedInstanceState = (Bundle) msg.obj;
					if(savedInstanceState != null) {
						activity.findViews(odbCalendar);
						if(odbPage != -1) {
							activity.onPageRequested(odbPage);
						}
					}
					
					break;
					
				case EnumMsg.DB_INITIALIZE_TASK_OK:	// db_initialize_task.

					if(progDialog != null && progDialog.isShowing()) {
						Calendar latest_cal = (Calendar) msg.obj;
						activity.findViews(latest_cal);
						cal.setTime(today);
						if(latest_cal.get(GregorianCalendar.MONTH) == cal.get(GregorianCalendar.MONTH)) {
							activity.onPageRequested(((int)Math.ceil((cal.get(GregorianCalendar.DAY_OF_MONTH)/2.0) - 1)));
						}
						progDialog.dismiss();
					}

					break;
					
				case EnumMsg.DB_NEWINITIALIZE_TASK_OK:
					
					if(progDialog != null && progDialog.isShowing()) {
						Calendar latest_cal = (Calendar) msg.obj;
						mAdapter.reloadItems(user_settings.getLanguageSettings(), latest_cal, false);
						cal.setTime(today);
						if(latest_cal.get(GregorianCalendar.MONTH) == cal.get(GregorianCalendar.MONTH)) {
							activity.onPageRequested(((int)Math.ceil((cal.get(GregorianCalendar.DAY_OF_MONTH)/2.0) - 1)));
						}
						progDialog.dismiss();
					}
					
					break;
					
				case EnumMsg.DB_INITIALIZE_TASK_NO_DATA:
					
					if(progDialog != null && progDialog.isShowing()) {
						Calendar latest_cal = (Calendar) msg.obj;
						activity.findViews(latest_cal);
						cal.setTime(today);
						if(latest_cal.get(GregorianCalendar.MONTH) == cal.get(GregorianCalendar.MONTH)) {
							activity.onPageRequested(((int)Math.ceil((cal.get(GregorianCalendar.DAY_OF_MONTH)/2.0) - 1)));
						}
						progDialog.dismiss();
					}
					
					break;
					
				case EnumMsg.DB_NEWINITIALIZE_TASK_NO_DATA:
					
					Calendar latest_cal = (Calendar) msg.obj;
					mAdapter.reloadItems(user_settings.getLanguageSettings(), latest_cal, false);
					cal.setTime(today);
					if(latest_cal.get(GregorianCalendar.MONTH) == cal.get(GregorianCalendar.MONTH)) {
						activity.onPageRequested(((int)Math.ceil((cal.get(GregorianCalendar.DAY_OF_MONTH)/2.0) - 1)));
					}
					if(progDialog != null && progDialog.isShowing()) {
						progDialog.dismiss();
					}
					Toast.makeText(activity, R.string.no_more_data, Toast.LENGTH_SHORT).show();
					
					break;

				case EnumMsg.DB_GETDATA_NO_DATA:	// don't have data from http.
					
					if(progDialog != null && progDialog.isShowing()) {
						progDialog.dismiss();
					}
					Toast.makeText(activity, R.string.no_more_data, Toast.LENGTH_SHORT).show();

					break;
					
				case EnumMsg.DB_GETDATA_UPDATED:	// update data to current db.
				case EnumMsg.DB_GETDATA_ALREADY_HAS_DATA:	// already have data.
					
					Calendar current_cal = (Calendar) msg.obj;
					cal.setTime(today);
					mAdapter.reloadItems(user_settings.getLanguageSettings(), current_cal, false);
					if(current_cal.get(GregorianCalendar.MONTH) == cal.get(GregorianCalendar.MONTH)) {
						activity.onPageRequested(((int)Math.ceil((cal.get(GregorianCalendar.DAY_OF_MONTH)/2.0) - 1)));
					}
					if(progDialog != null && progDialog.isShowing()) {
						progDialog.dismiss();
					}

					break;
					
				case EnumMsg.DB_CHKDATA_MANUAL_OK:
					
					if(activity.ibtn_refresh != null) {
						activity.ibtn_refresh.clearAnimation();
						activity.ibtn_refresh.setClickable(true);
					}
					// jump to latest month.
					
					Toast.makeText(activity, R.string.data_updated, Toast.LENGTH_SHORT).show();
					// flip to newest data maybe?
					
					break;
					
				case EnumMsg.DB_CHKDATA_MANUAL_NO_DATA:
					
					if(activity.ibtn_refresh != null) {
						activity.ibtn_refresh.clearAnimation();
						activity.ibtn_refresh.setClickable(true);
					}
					Toast.makeText(activity, R.string.no_more_data, Toast.LENGTH_SHORT).show();
					
					break;
					
				case EnumMsg.DB_INITIALIZE_TASK_INTERRUPTED:
					
					if(progDialog != null && progDialog.isShowing()) {
						activity.findViews(null);
						progDialog.dismiss();
					}
					Toast.makeText(activity, R.string.connection_interrupted, Toast.LENGTH_SHORT).show();
					
					break;
					
				case EnumMsg.DB_NEWINITIALIZE_TASK_INTERRUPTED:
					
					mAdapter.reloadItems(user_settings.getLanguageSettings(), null, false);
					if(progDialog != null && progDialog.isShowing()) {
						progDialog.dismiss();
					}
					Toast.makeText(activity, R.string.connection_interrupted, Toast.LENGTH_SHORT).show();
					
					break;
					
				case EnumMsg.DB_CHKDATA_MANUAL_TASK_INTERRUPTED:
					
					if(activity.ibtn_refresh != null) {
						activity.ibtn_refresh.clearAnimation();
						activity.ibtn_refresh.setClickable(true);
					}
					Toast.makeText(activity, R.string.connection_interrupted, Toast.LENGTH_SHORT).show();
					
					break;
					
				case EnumMsg.DB_GETDATA_TASK_INTERRUPTED:

					if(progDialog != null && progDialog.isShowing()) {
						progDialog.dismiss();
					}
					Toast.makeText(activity, R.string.connection_interrupted, Toast.LENGTH_SHORT).show();

					break;
				}
			}
		}
	}
    
}
