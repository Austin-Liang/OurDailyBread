package com.altujen.android.odb;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.squareup.timessquare.CalendarPickerView;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class CalendarPickerFragment extends DialogFragment implements OnClickListener {
	
	public interface CalendarPickerDialogListener {
        void onFinishPickerDialog(int dayOfMonth);
    }
	
	//private static final String TAG = "CalendarPicker";
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;
    
	private CalendarPickerView calPickerView;
	private Calendar cal = new GregorianCalendar();
	private Button done_button;
	
	public CalendarPickerFragment() {
        // Empty constructor required for DialogFragment
    }
	
	public static final CalendarPickerFragment newInstance(Calendar cal) {
		if(cal == null) {
			cal = Calendar.getInstance();
		}
		CalendarPickerFragment fragment = new CalendarPickerFragment();
	    Bundle bundle = new Bundle(1);
	    bundle.putLong(DailyBreadActivity.EXTRA_CALENDAR, cal.getTimeInMillis());
	    fragment.setArguments(bundle);
	    
	    return fragment ;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		long millis = getArguments().getLong(DailyBreadActivity.EXTRA_CALENDAR, Calendar.getInstance().getTimeInMillis());
		this.cal.setTimeInMillis(millis);
		
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.calendar_picker, container);
        
        if(cal == null) {
        	cal = Calendar.getInstance();
        }
        
        calPickerView = (CalendarPickerView) view.findViewById(R.id.calendar_view);
        calPickerView.init(Util.getFirstDay((Calendar)cal.clone()), Util.getPickerViewLastDay((Calendar)cal.clone()));
        
        Date today = new Date();
        
        if(Util.getMonthName(today).equals(Util.getMonthName(cal.getTime()))) {
        	calPickerView.selectDate(today);
        }
        
        done_button = (Button) view.findViewById(R.id.done_button);
        done_button.setOnClickListener(this);
        
        // Gesture detection
        gestureDetector = new GestureDetector(getActivity(), new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        
        // Set Gesture Listener
        calPickerView.setOnTouchListener(gestureListener);

        return view;
    }

	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		getDialog().getWindow()
	    .getAttributes().windowAnimations = R.style.DialogAnimation;
		
		// code below will set dialog background to transparent.
		/*getDialog().getWindow()
		.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));*/
	}
	
	private class MyGestureDetector extends SimpleOnGestureListener {
		
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	onSwipeLeft();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	onSwipeRight();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
        
        public void onSwipeRight() {
        	if (getActivity() == null)
    			return;
        	((DialogFragment) getActivity()
        			.getSupportFragmentManager()
        			.findFragmentByTag(DailyBreadActivity.EXTRA_CALENDAR_PICKER))
        			.dismiss();
        }

        public void onSwipeLeft() {
        	// Do nothing.
        }

    }

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.done_button) {
			CalendarPickerDialogListener activity = (CalendarPickerDialogListener) getActivity();
			if(calPickerView.getSelectedDate() != null) {
				activity.onFinishPickerDialog(calPickerView.getSelectedDate().getDate());
				this.dismiss();
			} else {
				Toast.makeText(getActivity(), R.string.pick_date, Toast.LENGTH_SHORT).show();
			}
		}
	}
    
}
