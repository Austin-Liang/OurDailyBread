package com.altujen.android.odb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.altujen.android.odb.DatabaseHandler;
import com.altujen.android.odb.DailyBreadActivity;
import com.altujen.android.odb.odbObject;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity implements OnClickListener, OnCheckedChangeListener, OnTouchListener, OnCompletionListener, OnBufferingUpdateListener, OnPreparedListener, OnErrorListener {
	
	private DatabaseHandler db;
	private odbObject odb;
	
	private LinearLayout layoutPlayer;
	private ImageButton btn_Play;
	private ImageButton btn_Share;
	private ImageButton btn_back;
	private SeekBar seekBarProgress;
	private Animation anim;
	private static final String strBreakLine = System.getProperty("line.separator");
	private static final String strHTMLBreak = "<br />";
	private static final String odb_https = "https://play.google.com/store/apps/details?id=com.altujen.android.odb";
	
	// http://stackoverflow.com/questions/11590538/dpi-value-of-default-large-medium-and-small-text-views-android
	private int sizeXLarge = -1;
	private int sizeLarge = -1;
	private int sizeMedium = -1;
	private int sizeSmall = -1;
	// -1 = sizeSmall, 0 = sizeMedium, 1 = sizeLarge, 2 = sizeXLarge
	private int font_size_status = 0;  // default value : 0
	private ImageButton btn_zoomIn;
	private ImageButton btn_zoomOut;
	
	public RadioGroup rbg;
	public RadioButton rbtn_zht;
	public RadioButton rbtn_zhy;
	public static final int id_rbtn_zht = 0x7f060047;
	public static final int id_rbtn_zhy = 0x7f060048;
	public TextView txt_currentPlayTime;
	public TextView txt_TotalTime;
	public TextView txt_Title;
	public TextView txt_Author;
	public TextView txt_Date;
	public TextView txt_rd_text;
	public TextView txt_annRd_text;
	public TextView txt_Story;
	public TextView txt_Poem;
	public TextView txt_Thought;

	private long currentDuration;
	private MediaPlayer mediaPlayer;
	private boolean isPrepared = false;
	private int mediaFileLengthInMilliseconds; // this value contains the song
												// duration in milliseconds.
												// Look at getDuration() method
												// in MediaPlayer class

	private final Handler handler = new Handler();
	private Runnable notification;
	
	private static DailyBreadSettings user_settings;
	private static EasyTracker easyTracker;
	private boolean ezTracker_NullFlag = false;
	private static final String EZ_Category = "detailActivity_action";
    private static final String EZ_Action = "button_press";

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
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeEzTracker();
		
		// Restore Preference.
		user_settings = (DailyBreadSettings) this.getApplicationContext();
		
		setContentView(R.layout.activity_detail);
		
		// Get the message from the intent
	    Intent intent = getIntent();
	    String odbID = intent.getStringExtra(DailyBreadActivity.EXTRA_MESSAGE);
	    
	    if((odbID != null && !odbID.equals("")) && (Util.isNumeric(odbID))) {
	    	
	    	db = new DatabaseHandler(this);
		    odb = db.getOdb(Integer.valueOf(odbID));
		 	
		    /*sizeXLarge = getResources().getInteger(R.integer.sizeXLarge);
		    sizeLarge = getResources().getInteger(R.integer.sizeLarge);
		    sizeMedium = getResources().getInteger(R.integer.sizeMedium);
		    sizeSmall = getResources().getInteger(R.integer.sizeSmall);*/
		    
		    sizeMedium = user_settings.getFontSize();
		    sizeLarge = (int)Math.round((sizeMedium * 1.2) / 1.0);
		    sizeXLarge = (int)Math.round((sizeMedium * 1.4) / 1.0);
		    sizeSmall = (int)Math.round((sizeMedium * 0.8) / 1.0);
		    
			initView(odb);
			
	    } else {
	    	// odbID is not numeric, finish activity.
	    	this.finish();
	    }
		
	}

	/** This method initialise all the views in project */
	private void initView(odbObject odb) {
		
		layoutPlayer = (LinearLayout) findViewById(R.id.layoutPlayer);
		btn_Play = (ImageButton) findViewById(R.id.btn_Play);
		
		btn_Share = (ImageButton) findViewById(R.id.btn_share);
		btn_Share.setOnClickListener(this);
		
		btn_back = (ImageButton) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		
		btn_zoomIn = (ImageButton) findViewById(R.id.btn_zoomIn);
		btn_zoomIn.setOnClickListener(this);
		btn_zoomOut = (ImageButton) findViewById(R.id.btn_zoomOut);
		btn_zoomOut.setOnClickListener(this);
		
		rbg = (RadioGroup) findViewById(R.id.radiogroup1);
		rbg.setOnCheckedChangeListener(this);
		rbtn_zht = (RadioButton) findViewById(R.id.radioButton1);
		rbtn_zhy = (RadioButton) findViewById(R.id.radioButton2);

		seekBarProgress = (SeekBar) findViewById(R.id.mp3SeekBar);
		
		txt_currentPlayTime = (TextView) findViewById(R.id.txt_currentPlayTime);
		txt_TotalTime = (TextView) findViewById(R.id.txt_TotalTime);
		
		txt_Title = (TextView) findViewById(R.id.txt_Title);
		setText(txt_Title, odb.getTitle());
		
		txt_Author = (TextView) findViewById(R.id.txt_Author);
		txt_Author.setMovementMethod(LinkMovementMethod.getInstance());
		setText(txt_Author, odb.getAuthor_uri(), odb.getAuthor());
		
		txt_Date = (TextView) findViewById(R.id.txt_Date);
		setText(txt_Date, odb.getDate());
		
		txt_rd_text = (TextView) findViewById(R.id.txt_rd_text);
		txt_rd_text.setMovementMethod(LinkMovementMethod.getInstance());
		
		txt_annRd_text = (TextView) findViewById(R.id.txt_annRd_text);
		txt_annRd_text.setMovementMethod(LinkMovementMethod.getInstance());
		
		txt_Story = (TextView) findViewById(R.id.txt_Story);
		{
			List<String[]> lstReplace = new ArrayList<String[]>();
			lstReplace.add(new String[]{"<br \\/>", strBreakLine + strBreakLine});
			setText(txt_Story, odb.getStory(), lstReplace);
		}
		//txt_Story.setMovementMethod(new ScrollingMovementMethod());
		
		txt_Poem = (TextView) findViewById(R.id.txt_Poem);
		
		txt_Thought = (TextView) findViewById(R.id.txt_Thought);
		setText(txt_Thought, odb.getThought());
		if(odb.getThought() != null) {
			//  \n  <-- This one works too.
			txt_Thought.append(strBreakLine);
		}
		//txt_Thought.setMovementMethod(new ScrollingMovementMethod());
		
		
		if(user_settings.getLanguageSettings() == EnumLang.JP) {
			// init view for JP.
			layoutPlayer.setVisibility(View.GONE);
			setText(txt_rd_text, odb.getRd_title());
			setText(txt_annRd_text, odb.getAnnRd_title());
			txt_Poem.setVisibility(View.GONE);
		} else {
			// other Languages.
			{
				if(user_settings.getLanguageSettings() != EnumLang.zh_TW && user_settings.getLanguageSettings() != EnumLang.zh_CN) {
					rbg.setVisibility(View.GONE);
				}
			}
			btn_Play.setOnClickListener(this);
			seekBarProgress.setMax(99); // It means 100% .0-99
			seekBarProgress.setOnTouchListener(this);
			txt_currentPlayTime.setText(milliSecondsToTimer(0000));
			txt_TotalTime.setText("--:--:--");
			setText(txt_rd_text, odb.getRd_uri(), odb.getRd_title());
			setText(txt_annRd_text, odb.getAnnRd_uri(), odb.getAnnRd_title());
			{
				List<String[]> lstReplace = new ArrayList<String[]>();
				lstReplace.add(new String[]{"\r\n", strBreakLine});
				lstReplace.add(new String[]{"<br \\/>", strBreakLine});
				setText(txt_Poem, odb.getPoem(), lstReplace);
			}
			
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnBufferingUpdateListener(this);
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnErrorListener(this);
			
			try {
				
				if(user_settings.getLanguageSettings() == EnumLang.zh_TW || user_settings.getLanguageSettings() == EnumLang.zh_CN) {
					switch(rbg.getCheckedRadioButtonId()) {
					case id_rbtn_zht:
						mediaPlayer.setDataSource(odb.getMp3_uri());
						break;
					case id_rbtn_zhy:
						mediaPlayer.setDataSource(Util.getZhyMp3Uri(odb.getMp3_uri()));
						break;
					}
				} else {
					mediaPlayer.setDataSource(odb.getMp3_uri());
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				// Handle Media Error...
				btn_Play.setEnabled(false);
				seekBarProgress.setEnabled(false);
				txt_currentPlayTime.setText("");
				txt_currentPlayTime.setVisibility(View.INVISIBLE);
				txt_TotalTime.setText(R.string.media_error);
			}
			
			if(Util.isUsingWifi(DetailActivity.this)) {
				try {
					
					btn_Play.setImageResource(R.drawable.av_play);
					btn_Play.setEnabled(false);
					seekBarProgress.setEnabled(false);
					
					mediaPlayer.prepareAsync();
					
				} catch (Exception e) {
					e.printStackTrace();
					// Handle Media Error...
					btn_Play.setEnabled(false);
					seekBarProgress.setEnabled(false);
					txt_currentPlayTime.setText("");
					txt_currentPlayTime.setVisibility(View.INVISIBLE);
					txt_TotalTime.setText(R.string.media_error);
				}
			} else {
				btn_Play.setImageResource(R.drawable.images_rotate_right);
				btn_Play.setEnabled(true);
				seekBarProgress.setEnabled(false);
			}
			
			/*try {
				
				btn_Play.setEnabled(false);
				seekBarProgress.setEnabled(false);
				mediaPlayer.setDataSource(odb.getMp3_uri());
				mediaPlayer.prepareAsync();
				
			} catch (Exception e) {
				e.printStackTrace();
				// Handle Media Error...
				btn_Play.setEnabled(false);
				seekBarProgress.setEnabled(false);
				txt_currentPlayTime.setText("");
				txt_currentPlayTime.setVisibility(View.INVISIBLE);
				txt_TotalTime.setText(R.string.media_error);
			}*/
		}
		
		// Set TextView Size.
		setTextViewSize(null);
		
	}
	
	private void setText(TextView txtView, String strText) {
		if(strText != null) {
			txtView.setText(strText);
		}
	}
	
	private void setText(TextView txtView, String strUriText, String strText) {
		if(strText != null) {
			if(strUriText != null) {
				txtView.setText(Html.fromHtml("<a href=\"" + strUriText + "\">" + strText + "</a>"));
			} else {
				txtView.setText(strText);
			}
		}
	}
	
	private void setText(TextView txtView, String strText, List<String[]> lstReplace) {
		if(strText != null) {
			for(String[] replacement : lstReplace) {
				strText = strText.replaceAll(replacement[0], replacement[1]);
			}
			txtView.setText(strText);
		}
	}

	/**
	 * Method which updates the SeekBar primary progress by current song playing
	 * position
	 */
	private void primarySeekBarProgressUpdater() {
		currentDuration = mediaPlayer.getCurrentPosition();
		int progress = (int) (((float) currentDuration / mediaFileLengthInMilliseconds) * 100);
		//progress = (progress == 99) ? 0 : progress;
		seekBarProgress.setProgress(progress); // This
																				// math
																				// construction
																				// give
																				// a
																				// percentage
																				// of
																				// "was playing"/"song length"
		
		txt_currentPlayTime.setText(milliSecondsToTimer(currentDuration));
		
		if (mediaPlayer.isPlaying()) {
			notification = new Runnable() {
				public void run() {
					primarySeekBarProgressUpdater();
				}
			};
			handler.postDelayed(notification, 1000);
		}
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		
		case R.id.btn_Play:
			
			{
				if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "play_button", null);}
			}
			
			/**
			 * ImageButton onClick event handler. Method which start/pause
			 * mediaplayer playing
			 */
			if(Util.isUsingWifi(DetailActivity.this) && isPrepared) {
				if (!mediaPlayer.isPlaying()) {
					mediaPlayer.start();
					btn_Play.setImageResource(R.drawable.av_pause);
				} else {
					mediaPlayer.pause();
					btn_Play.setImageResource(R.drawable.av_play);
				}
			} else if(!Util.isUsingWifi(DetailActivity.this) && !isPrepared) {
				btn_Play.setImageResource(R.drawable.av_play);
				try {
					
					btn_Play.setEnabled(false);
					seekBarProgress.setEnabled(false);
					
					if(user_settings.getLanguageSettings() == EnumLang.zh_TW || user_settings.getLanguageSettings() == EnumLang.zh_CN) {
						rbtn_zht.setEnabled(false);
						rbtn_zhy.setEnabled(false);
						rbg.setEnabled(false);
					}
					
					/*if(user_settings.getLanguageSettings() == EnumLang.zh_TW || user_settings.getLanguageSettings() == EnumLang.zh_CN) {
						switch(rbg.getCheckedRadioButtonId()) {
						case rbtn_zht:
							mediaPlayer.setDataSource(odb.getMp3_uri());
							break;
						case rbtn_zhy:
							mediaPlayer.setDataSource(Util.getZhyMp3Uri(odb.getMp3_uri()));
							break;
						}
					} else {
						mediaPlayer.setDataSource(odb.getMp3_uri());
					}*/
					
					mediaPlayer.prepareAsync();
					
				} catch (Exception e) {
					e.printStackTrace();
					// Handle Media Error...
					btn_Play.setEnabled(false);
					seekBarProgress.setEnabled(false);
					txt_currentPlayTime.setText("");
					txt_currentPlayTime.setVisibility(View.INVISIBLE);
					txt_TotalTime.setText(R.string.media_error);
				}
			} else if(!Util.isUsingWifi(DetailActivity.this) && isPrepared) {
				if (!mediaPlayer.isPlaying()) {
					mediaPlayer.start();
					btn_Play.setImageResource(R.drawable.av_pause);
				} else {
					mediaPlayer.pause();
					btn_Play.setImageResource(R.drawable.av_play);
				}
			} else {
				Toast.makeText(getBaseContext(), "其他狀況!!...", Toast.LENGTH_SHORT).show();
			}
			
			/*if (!mediaPlayer.isPlaying()) {
				mediaPlayer.start();
				btn_Play.setImageResource(R.drawable.av_pause);
			} else {
				mediaPlayer.pause();
				btn_Play.setImageResource(R.drawable.av_play);
			}*/

			primarySeekBarProgressUpdater();
			
			break;
			
		case R.id.btn_share:
			
			{
				if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "share_button", null);}
			}
			
			{
				final CharSequence[] items = this.getResources().getStringArray(R.array.select_share_items);
				new AlertDialog.Builder(this)
						.setTitle(R.string.plzChoose)
						.setItems(items, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										
										switch(which) {
										case 0:   // Send Email
											{
												if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "shareEmail_button", null);}
											}
											//create the send intent
											Intent shareEmailIntent = new Intent(android.content.Intent.ACTION_SEND);
											shareEmailIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
											
											//set the type
											shareEmailIntent.setType("text/html");
											
											//add a subject
											shareEmailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, 
											 getResources().getString(R.string.app_name) + " - " + odb.getTitle());
											
											//build the body of the message to be shared
											String shareEmail = odb.getDate() + strHTMLBreak + strHTMLBreak + (odb.getStory().replaceAll("<br \\/>", "")).substring(0, MultiLangUtil.getShareEmailLengthByLang(user_settings.getLanguageSettings())) + "..." 
											+ strHTMLBreak + strHTMLBreak
											+ getString(R.string.read_more_at) + strHTMLBreak + odb.getODB_Uri()
											+ strHTMLBreak + strHTMLBreak + strHTMLBreak
											+ getString(R.string.send_from) + "  " + "<a href=\"" + odb_https + "\" title=\"" + getString(R.string.app_name) + "\">" + getString(R.string.app_name) + "</a>";
											
											//add the message
											shareEmailIntent.putExtra(android.content.Intent.EXTRA_TEXT, 
													Html.fromHtml(shareEmail));
											
											//start the chooser for sharing
											startActivity(Intent.createChooser(shareEmailIntent, 
											 getResources().getString(R.string.share_chooser)));
											
											break;
											
										case 1:   // Send Message
											{
												if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "shareMsg_button", null);}
											}
											//create the send intent
											Intent shareMsgIntent = new Intent(android.content.Intent.ACTION_SEND);
											shareMsgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
											
											//set the type
											shareMsgIntent.setType("text/plain");
											
											//add a subject
											shareMsgIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, 
											 getResources().getString(R.string.app_name) + " - " + odb.getTitle());
											
											//build the body of the message to be shared
											String shareMessage = odb.getDate() + strBreakLine + strBreakLine + (odb.getStory().replaceAll("<br \\/>", "")).substring(0, MultiLangUtil.getShareMsgLengthByLang(user_settings.getLanguageSettings())) + "..." 
											+ strBreakLine + strBreakLine
											+ getString(R.string.read_more_at) + strBreakLine + odb.getODB_Uri();
											
											//add the message
											shareMsgIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
											
											//start the chooser for sharing
											startActivity(Intent.createChooser(shareMsgIntent, 
											 getResources().getString(R.string.share_chooser)));
											
											break;
										}
										
									}
								}).show();
			}
			
			break;
			
		case R.id.btn_zoomIn:
			
			{
				if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "zoomIn_button", null);}
			}
			
			anim = AnimationUtils.loadAnimation(this, R.anim.scale_in);
			anim.reset();
			
			font_size_status += 1;
			setTextViewSize(anim);
			
			break;
			
		case R.id.btn_zoomOut:
			
			{
				if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "zoomOut_button", null);}
			}
			
			anim = AnimationUtils.loadAnimation(this, R.anim.scale_out);
			anim.reset();
			
			font_size_status -= 1;
			setTextViewSize(anim);
			
			break;
			
		case R.id.btn_back:
			
			{
				if(!ezTracker_NullFlag) {Util.sendEzTrace(easyTracker, EZ_Category, EZ_Action, "back_button", null);}
			}
			
			this.finish();
			break;
		}
		
	}
	
	private void setTextViewSize(Animation anim) {
		
		// -1 = sizeSmall, 0 = sizeMedium, 1 = sizeLarge, 2 = sizeXLarge
		switch (font_size_status) {
		case -1:
			txt_Story.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSmall);
			txt_Poem.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSmall);
			txt_Thought.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSmall);
			
			btn_zoomOut.setImageResource(R.drawable.btn_minus_disable);
			btn_zoomOut.setEnabled(false);
			break;
		case 0:
			txt_Story.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeMedium);
			txt_Poem.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeMedium);
			txt_Thought.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeMedium);
			
			btn_zoomOut.setImageResource(R.drawable.btn_minus_default);
			btn_zoomOut.setEnabled(true);
			break;
		case 1:
			txt_Story.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeLarge);
			txt_Poem.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeLarge);
			txt_Thought.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeLarge);
			
			btn_zoomIn.setImageResource(R.drawable.btn_plus_default);
			btn_zoomIn.setEnabled(true);
			break;
		case 2:
			txt_Story.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeXLarge);
			txt_Poem.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeXLarge);
			txt_Thought.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeXLarge);
			
			btn_zoomIn.setImageResource(R.drawable.btn_plus_disable);
			btn_zoomIn.setEnabled(false);
			break;
		default:
			txt_Story.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeMedium);
			txt_Poem.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeMedium);
			txt_Thought.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeMedium);
			
			btn_zoomIn.setImageResource(R.drawable.btn_plus_default);
			btn_zoomIn.setEnabled(true);
			btn_zoomOut.setImageResource(R.drawable.btn_minus_default);
			btn_zoomOut.setEnabled(true);
			break;
		}
		
		// Set Animation.
		if (anim != null) {
			txt_Story.clearAnimation();
			txt_Story.startAnimation(anim);
			txt_Poem.clearAnimation();
			txt_Poem.startAnimation(anim);
			txt_Thought.clearAnimation();
			txt_Thought.startAnimation(anim);
		}
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.mp3SeekBar) {
			/**
			 * Seekbar onTouch event handler. Method which seeks MediaPlayer to
			 * seekBar primary progress position
			 */
			if (mediaPlayer.isPlaying()) {
				SeekBar sb = (SeekBar) v;
				int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100)
						* sb.getProgress();
				txt_currentPlayTime.setText(milliSecondsToTimer(playPositionInMillisecconds));
				mediaPlayer.seekTo(playPositionInMillisecconds);
			}
		}
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		/**
		 * MediaPlayer onCompletion event handler. Method which calls then song
		 * playing is complete
		 */
		if(notification != null) {
			handler.removeCallbacksAndMessages(null);
			notification = null;
		}
		if(btn_Play.isEnabled() && isPrepared) {
			seekBarProgress.setProgress(0);
			txt_currentPlayTime.setText(milliSecondsToTimer(0000));
			btn_Play.setImageResource(R.drawable.av_play);
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		/**
		 * Method which updates the SeekBar secondary progress by current song
		 * loading from URL position
		 */
		seekBarProgress.setSecondaryProgress(percent);
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		
		isPrepared = true;
		mediaFileLengthInMilliseconds = mediaPlayer.getDuration();
		btn_Play.setEnabled(true);
		seekBarProgress.setEnabled(true);
		txt_TotalTime.setText(milliSecondsToTimer(mediaFileLengthInMilliseconds));
		
		if(user_settings.getLanguageSettings() == EnumLang.zh_TW || user_settings.getLanguageSettings() == EnumLang.zh_CN) {
			rbtn_zht.setEnabled(true);
			rbtn_zhy.setEnabled(true);
			rbg.setEnabled(true);
		}
		
		// Restore data from SaveInstanceState.
		if(currentDuration > 0 && mediaPlayer != null) {
			txt_currentPlayTime.setText(milliSecondsToTimer(currentDuration));
			mediaPlayer.seekTo((int) currentDuration);
		}
		
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		
		// Media Error...
		btn_Play.setEnabled(false);
		seekBarProgress.setEnabled(false);
		txt_currentPlayTime.setText("");
		txt_currentPlayTime.setVisibility(View.INVISIBLE);
		txt_TotalTime.setText(R.string.media_error);
		
		return false;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(db != null){
			db.close();
		}
		
		if(notification != null || handler != null) {
			// If token is null, all callbacks and messages will be removed.
			handler.removeCallbacksAndMessages(null);
			notification = null;
		}
		
		if(mediaPlayer != null) {
			if(mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
			}
			//mediaPlayer.stop();
			// releasing mediaPlayer.
			mediaPlayer.release();
		}
		
		if(odb != null) {
			odb = null;
		}
		
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		// Restore mp3 play time.
		currentDuration = savedInstanceState.getLong("currentDuration", 0000);  // default = 0000
		// Restore font_size_status.
		font_size_status = savedInstanceState.getInt("font_size_status", 0);  // default = 0
	}

	// http://stackoverflow.com/a/151940/1482579
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// Save mp3 play time.
		outState.putLong("currentDuration", currentDuration);
		// Save font_size_status.
		outState.putInt("font_size_status", font_size_status);
	}
	
	// http://stackoverflow.com/questions/1512045/how-to-disable-orientation-change-in-android
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

	/**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     * */
	//  http://www.androidhive.info/2012/03/android-building-audio-player-tutorial/
	public String milliSecondsToTimer(long milliseconds) {
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there
		if (hours > 0) {
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		// return timer string
		return finalTimerString;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		
		try {
			if(user_settings.getLanguageSettings() == EnumLang.zh_TW || user_settings.getLanguageSettings() == EnumLang.zh_CN) {
				
				if(notification != null) {
					handler.removeCallbacksAndMessages(null);
					notification = null;
				}
				
				txt_currentPlayTime.setText(milliSecondsToTimer(0000));
				txt_TotalTime.setText("--:--:--");
				
				if(mediaPlayer != null) {
					if(mediaPlayer.isPlaying()) {
						mediaPlayer.pause();
					}
					//mediaPlayer.stop();
					mediaPlayer.reset();
				}
				
				switch(rbg.getCheckedRadioButtonId()) {
				case id_rbtn_zht:
					mediaPlayer.setDataSource(odb.getMp3_uri());
					break;
				case id_rbtn_zhy:
					mediaPlayer.setDataSource(Util.getZhyMp3Uri(odb.getMp3_uri()));
					break;
				}
				
				if(Util.isUsingWifi(DetailActivity.this)) {
					
					btn_Play.setEnabled(false);
					seekBarProgress.setEnabled(false);
					btn_Play.setImageResource(R.drawable.av_play);
					mediaPlayer.prepareAsync();
					
				} else {
					
					isPrepared = false;
					btn_Play.setImageResource(R.drawable.images_rotate_right);
					btn_Play.setEnabled(true);
					seekBarProgress.setEnabled(false);
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Handle Media Error...
			btn_Play.setEnabled(false);
			seekBarProgress.setEnabled(false);
			txt_currentPlayTime.setText("");
			txt_currentPlayTime.setVisibility(View.INVISIBLE);
			txt_TotalTime.setText(R.string.media_error);
		}
		
	}
	
	/**
     * @param textView
     *            textView who's text you want to change
     * @param linkThis
     *            a regex of what text to turn into a link
     * @param toThis
     *            the url you want to send them to
     */
	//  http://stackoverflow.com/a/13212160/1482579
	//  http://stackoverflow.com/questions/4746293/android-linkify-textview
    /*public void addLinks(TextView textView, String linkThis, String toThis) {
        Pattern pattern = Pattern.compile(linkThis);
        String scheme = toThis;
        android.text.util.Linkify.addLinks(textView, pattern, scheme, new MatchFilter() {
            @Override
            public boolean acceptMatch(CharSequence s, int start, int end) {
                return true;
            }
        }, new TransformFilter() {

            @Override
            public String transformUrl(Matcher match, String url) {
                return "";
            }
        });
    }*/
	
}
