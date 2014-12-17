package com.altujen.android.odb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.os.AsyncTask;
import android.os.Build;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FlipAdapter extends BaseAdapter implements OnClickListener {
	
	public interface FlipAdapterCallback{
		public void onDetailRequested(int odbID);
	}
	
	private LayoutInflater inflater;
	private FlipAdapterCallback flipAdapterCallback;
	private List<odbObject> items = new ArrayList<odbObject>();
	// set to false, or it will cause error. (FlipView.java : 200 || 242)
	private final boolean hasStableIds = false;
	private int page;
	private Calendar cal = new GregorianCalendar();
	private DatabaseHandler db;
	private DiskLruImageCache DiskLruCache;
	private static DailyBreadSettings user_settings;
	private ArrayList<DownloadImageTask> lst_Tasks = new ArrayList<DownloadImageTask>();
	
	private static final int imgBorder = 1;
	private static final int marginLeft = 10;
	private static final int marginRight = 10;
	private static final int paddingLeft = (4 + imgBorder);
	private static final int paddingRight = (4 + imgBorder);
	private static final double widthScale = 3.5;
	
	private static final int txt_marginLeft = 2;
	private static final int txt_marginRight = 2;
	private static final int txt_marginBuffer = 35;
	
	private int Measuredwidth = 0;
	//private int Measuredheight = 0;
	
	private static String readMore;
	
	private String strScreenSize;
	
	private final DateFormat df_Default = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
	//private static final String TAG = "FlipAdapter";
	
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public FlipAdapter(Context context, DatabaseHandler db, Calendar odbCalendar, int lang) {
		this.inflater = LayoutInflater.from(context);
		
		{
			user_settings = (DailyBreadSettings) context.getApplicationContext();
			readMore = context.getResources().getString(R.string.read_more);
		}
		
		{
			Point size = new Point();
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				wm.getDefaultDisplay().getSize(size);
			    Measuredwidth = size.x;
			    //Measuredheight = size.y;
			} else {
			    Display d = wm.getDefaultDisplay();
			    Measuredwidth = d.getWidth();
			    //Measuredheight = d.getHeight();
			}
			strScreenSize = context.getResources().getString(R.string.screenSize);
		}
		
		this.DiskLruCache = user_settings.getDiskLruCache();
		this.db = db;
		
		if(odbCalendar != null) {
			this.cal = odbCalendar;
		} else {
			// we need to find latest available month.
			odbObject latest_odb = db.getLatestOdb(lang);
			if(latest_odb != null) {
				this.cal.set(Integer.parseInt(latest_odb.getOrder_WT().substring(0, 4)), ((Integer.parseInt(latest_odb.getOrder_WT().substring(4, 6))) - 1), 1);
			} else {
				this.cal.setTime(new Date());
			}
		}
		
		List<odbObject> odbs = db.getAllOdbsByMonth(lang, df_Default.format(Util.getFirstDay(cal)), df_Default.format(Util.getLastDay(cal)));
		items = odbs;
		
	}

	public void setCallback(FlipAdapterCallback flipAdapterCallback) {
		this.flipAdapterCallback = flipAdapterCallback;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return Long.parseLong(items.get(position).getID());
	}
	
	@Override
	public boolean hasStableIds() {
		return this.hasStableIds;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		this.page = position;
		
		ViewHolder holder;
		
		int finalPage = (int)Math.ceil((getCount()/2.0) - 1);
		boolean isOdd = Util.isOdd(this.getCount());
		
		if(convertView == null){
			
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.page, parent, false);
			
			holder.intendedWidth = (int) Math.floor((Measuredwidth - marginLeft - marginRight - paddingLeft - paddingRight) / widthScale);
			
			if(strScreenSize.equals("values-xlarge")) {
				holder.substringLine = 7;
			} else if(strScreenSize.equals("values-large")) {
				holder.substringLine = 6;
			} else if(strScreenSize.equals("values-normal")) {
				holder.substringLine = 5;
			} else if(strScreenSize.equals("values-small")) {
				holder.substringLine = 2;
			} else if(strScreenSize.equals("values-sw600dp")) {
				holder.substringLine = 6;
			} else if(strScreenSize.equals("values-sw720dp")) {
				// sw720dp = ASUS TF101 、 Samsung Tab 10.1(GT-P7510) ...
				holder.substringLine = 7;
			} else {
				// something we missed can go here.
				holder.substringLine = 4;
			}
			
			//Toast.makeText(convertView.getContext(), strScreenSize, Toast.LENGTH_SHORT).show();
			
			holder.layoutTop = (LinearLayout) convertView.findViewById(R.id.layoutTop);
			holder.layoutBottom = (LinearLayout) convertView.findViewById(R.id.layoutBottom);
			holder.imgAuthor_Top = (ImageView) convertView.findViewById(R.id.imgAuthor_Top);
			holder.imgAuthor_Bottom = (ImageView) convertView.findViewById(R.id.imgAuthor_Bottom);
			holder.textAuthor_Top = (TextView) convertView.findViewById(R.id.textAuthor_Top);
			holder.textAuthor_Bottom = (TextView) convertView.findViewById(R.id.textAuthor_Bottom);
			holder.textTitle_Top = (TextView) convertView.findViewById(R.id.textTitle_Top);
			holder.textTitle_Bottom = (TextView) convertView.findViewById(R.id.textTitle_Bottom);
			holder.textDate_Top = (TextView) convertView.findViewById(R.id.textDate_Top);
			holder.textDate_Bottom = (TextView) convertView.findViewById(R.id.textDate_Bottom);
			
			holder.textTop = (TextView) convertView.findViewById(R.id.textTop);
			holder.textBottom = (TextView) convertView.findViewById(R.id.textBottom);
			
			holder.layoutTop.setOnClickListener(this);
			holder.layoutBottom.setOnClickListener(this);
			
			convertView.setTag(holder);
			
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		
		if(position == finalPage) {
			if(isOdd) {
				
				setAuthorImage(holder.imgAuthor_Top, holder.intendedWidth, items.get(position * 2).getAuthorImg_uri());
				holder.textAuthor_Top.setText("  - " + items.get(position * 2).getAuthor());
				holder.textTitle_Top.setText(items.get(position * 2).getTitle());
				holder.textDate_Top.setText(items.get(position * 2).getDate());
				setText(holder.textTop, items.get(position * 2).getStory().replaceAll("<br \\/>", ""), holder.substringLine);
				holder.layoutTop.setTag(items.get(position * 2).getID());
				
				holder.imgAuthor_Bottom.setImageBitmap(null);
				holder.imgAuthor_Bottom.setVisibility(View.INVISIBLE);
				holder.textAuthor_Bottom.setText("");
				holder.textTitle_Bottom.setText("");
				holder.textDate_Bottom.setText("");
				holder.textBottom.setText("");
				holder.layoutBottom.setTag(null);
				
			} else {
				
				setAuthorImage(holder.imgAuthor_Top, holder.intendedWidth, items.get(position * 2).getAuthorImg_uri());
				holder.textAuthor_Top.setText("  - " + items.get(position * 2).getAuthor());
				holder.textTitle_Top.setText(items.get(position * 2).getTitle());
				holder.textDate_Top.setText(items.get(position * 2).getDate());
				setText(holder.textTop, items.get(position * 2).getStory().replaceAll("<br \\/>", ""), holder.substringLine);
				holder.layoutTop.setTag(items.get(position * 2).getID());
				
				
				setAuthorImage(holder.imgAuthor_Bottom, holder.intendedWidth, items.get((position * 2) + 1).getAuthorImg_uri());
				holder.imgAuthor_Bottom.setVisibility(View.VISIBLE);
				holder.textAuthor_Bottom.setText("  - " + items.get((position * 2) + 1).getAuthor());
				holder.textTitle_Bottom.setText(items.get((position * 2) + 1).getTitle());
				holder.textDate_Bottom.setText(items.get((position * 2) + 1).getDate());
				setText(holder.textBottom, items.get((position * 2) + 1).getStory().replaceAll("<br \\/>", ""), holder.substringLine);
				holder.layoutBottom.setTag(items.get((position * 2) + 1).getID());
				
			}
		} else {
			
			setAuthorImage(holder.imgAuthor_Top, holder.intendedWidth, items.get(position * 2).getAuthorImg_uri());
			holder.textAuthor_Top.setText("  - " + items.get(position * 2).getAuthor());
			holder.textTitle_Top.setText(items.get(position * 2).getTitle());
			holder.textDate_Top.setText(items.get(position * 2).getDate());
			setText(holder.textTop, items.get(position * 2).getStory().replaceAll("<br \\/>", ""), holder.substringLine);
			holder.layoutTop.setTag(items.get(position * 2).getID());
			
			
			setAuthorImage(holder.imgAuthor_Bottom, holder.intendedWidth, items.get((position * 2) + 1).getAuthorImg_uri());
			holder.imgAuthor_Bottom.setVisibility(View.VISIBLE);
			holder.textAuthor_Bottom.setText("  - " + items.get((position * 2) + 1).getAuthor());
			holder.textTitle_Bottom.setText(items.get((position * 2) + 1).getTitle());
			holder.textDate_Bottom.setText(items.get((position * 2) + 1).getDate());
			setText(holder.textBottom, items.get((position * 2) + 1).getStory().replaceAll("<br \\/>", ""), holder.substringLine);
			holder.layoutBottom.setTag(items.get((position * 2) + 1).getID());
			
		}
		
		return convertView;
	}

	static class ViewHolder {
		int intendedWidth;
		int substringLine;
		LinearLayout layoutTop;
		LinearLayout layoutBottom;
		ImageView imgAuthor_Top;
		ImageView imgAuthor_Bottom;
		TextView textAuthor_Top;
		TextView textAuthor_Bottom;
		TextView textTitle_Top;
		TextView textTitle_Bottom;
		TextView textDate_Top;
		TextView textDate_Bottom;
		TextView textTop;
		TextView textBottom;
		TextView textMonth;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.layoutTop:
		case R.id.layoutBottom:
			if (flipAdapterCallback != null && v.getTag() != null) {
				flipAdapterCallback.onDetailRequested(Integer.valueOf((String) v.getTag()));
			}
			break;
		}
	}
	
	private void setText(TextView mTextView, String mText, int mLine) {
		// http://stackoverflow.com/questions/11497241/get-width-of-textview-in-characters
		mTextView.setText(null);
		
		if(mText != null && !mText.trim().equals("")) {
			for(int i = 1; i <= mLine; i++) {
			int totalCharstoFit = mTextView.getPaint().breakText(mText, 0,	mText.length(), true, (Measuredwidth - txt_marginLeft - txt_marginRight - txt_marginBuffer), null);
			String subString = mText.substring(0, totalCharstoFit);
			mTextView.append(subString);
			mText = mText.substring(subString.length(), mText.length());
			}
			mTextView.append(readMore);
		}
		
	}
	
	private void setAuthorImage(ImageView imgV, int intendedWidth, String imgRawUri) {
		
		if(imgRawUri == null) {
			imgRawUri = "";
		}
		
		// set image visibility to invisible.
		imgV.setVisibility(View.INVISIBLE);
		
		// keys must match regex [a-z0-9_-]{1,64}
		String imgKey = Util.keyFilter(imgRawUri);
		
		if((this.DiskLruCache != null && !(this.DiskLruCache.isClosed())) && (imgKey != null && !(imgKey.equals("")))) {
			// key is okay.
			{   // DiskLruImageCache
				if(this.DiskLruCache.containsKey(imgKey)) {
					// already cached.
					Bitmap mAuthor = this.DiskLruCache.getBitmap(imgKey);
					// resize image.
					Util.imageResize(imgV, mAuthor, intendedWidth);
					
					// Set fade-in animation.
					Animation anim = AnimationUtils.loadAnimation(imgV.getContext(), R.anim.fade_in);
					anim.reset();
					// set image visibility to visible.
					imgV.setVisibility(View.VISIBLE);
					imgV.startAnimation(anim);
					
				} else {
					// not cached.
					// new a DownloadImageTask.
					DownloadImageTask imgTask = new DownloadImageTask(imgV, this.DiskLruCache, intendedWidth);
					imgTask.execute(imgRawUri, imgKey);
					lst_Tasks.add(imgTask);
				}
			}
		} else {
			// use default image.
			Util.imageResize(imgV, user_settings.getMissingAvatar(), intendedWidth);
			// Set fade-in animation.
			Animation anim = AnimationUtils.loadAnimation(imgV.getContext(), R.anim.fade_in);
			anim.reset();
			// set image visibility to visible.
			imgV.setVisibility(View.VISIBLE);
			imgV.startAnimation(anim);
		}
		
	}
	
	public void clearTasks() {
		
		for(DownloadImageTask imgTask : lst_Tasks) {
			if(!imgTask.isCancelled()) {
				imgTask.cancel(true);
			}
		}
		
		lst_Tasks.clear();
		
	}
	
	public void reloadItems(final int lang, final Calendar cal, final boolean isFollow) {
		
		if(!isFollow) {
			if(cal != null) {
				this.cal = cal;
			} else {
				// we need to find latest available month.
				odbObject latest_odb = db.getLatestOdb(lang);
				if(latest_odb != null) {
					this.cal.set(Integer.parseInt(latest_odb.getOrder_WT().substring(0, 4)), ((Integer.parseInt(latest_odb.getOrder_WT().substring(4, 6))) - 1), 1);
				} else {
					this.cal.setTime(new Date());
				}
			}
		}
		
		List<odbObject> odbs = db.getAllOdbsByMonth(lang, df_Default.format(Util.getFirstDay(this.cal)), df_Default.format(Util.getLastDay(this.cal)));
		items.clear();
		items = odbs;
		this.clearTasks();
		
		notifyDataSetChanged();
	}
	
	public void reloadSearchResultItems(final int lang, String keyWord) {
		
		List<odbObject> odbs = db.getSearchResult(lang, keyWord, df_Default.format(Util.getFirstDay(this.cal)), df_Default.format(Util.getLastDay(this.cal)));
		items.clear();
		items = odbs;
		this.clearTasks();
		
		notifyDataSetChanged();
	}
	
	public Calendar getCalendar() {
		return (Calendar) this.cal.clone();
	}
	
	public int getPage() {
		return this.page;
	}
	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		
		private ImageView m_ImageView;
		private int m_IntendedWidth;
		private DiskLruImageCache m_DiskLruCache;

		public DownloadImageTask(ImageView authorImgv, DiskLruImageCache diskLruCache, int intendedWidth) {
			this.m_ImageView = authorImgv;
			this.m_DiskLruCache = diskLruCache;
			this.m_IntendedWidth = intendedWidth;
		}

		protected Bitmap doInBackground(String... args) {
			
			String imgRawUri = args[0];
			String imgKey = args[1];
			
			URL url = null;
			InputStream is = null;
			Bitmap mAuthor = null;
			
			{   // DiskLruImageCache
				if(m_DiskLruCache != null && !(m_DiskLruCache.isClosed())) {
					if(m_DiskLruCache.containsKey(imgKey)) {
						// already cached.
						mAuthor = m_DiskLruCache.getBitmap(imgKey);
						
					} else {
						// not cached.
						try {

							url = new URL(imgRawUri);
							is = url.openStream();
							mAuthor = BitmapFactory.decodeStream(is);
							
							if((m_DiskLruCache != null && !(m_DiskLruCache.isClosed())) && (mAuthor != null)) {
								m_DiskLruCache.put(imgKey, mAuthor);
							}

						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (is != null) {
								try {
									is.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}

					}
				}
			}
			
			return mAuthor;
		}

		@Override
		protected void onPostExecute(Bitmap result) {

			if(result == null) {
				result = user_settings.getMissingAvatar();
			}
			
			if(result != null) {
				// resize image.
				Util.imageResize(m_ImageView, result, m_IntendedWidth);
				
				// Set fade-in animation.
				Animation anim = AnimationUtils.loadAnimation(m_ImageView.getContext(), R.anim.fade_in);
				anim.reset();
				// set image visibility to visible.
				m_ImageView.setVisibility(View.VISIBLE);
				m_ImageView.startAnimation(anim);
			}
			
			super.onPostExecute(result);
			
		}
		
	}
	
}
