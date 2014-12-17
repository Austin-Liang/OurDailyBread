package com.altujen.android.odb;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MenuListFragment extends ListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if(view != null) {
			view.setBackgroundResource(R.drawable.bg_light_honeycomb_repeating);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SampleAdapter adapter = new SampleAdapter(getActivity());
		
		adapter.add(new SampleItem(R.string.menu_lastMonth, R.drawable.ic_find_previous_holo_light));
		adapter.add(new SampleItem(R.string.menu_nextMonth, R.drawable.ic_find_next_holo_light));
		adapter.add(new SampleItem(R.string.menu_language, R.drawable.location_web_site));
		adapter.add(new SampleItem(R.string.menu_language, R.drawable.ic_menu_copy_holo_light));
		adapter.add(new SampleItem(R.string.menu_appGuide, R.drawable.action_help));
		adapter.add(new SampleItem(R.string.menu_about, R.drawable.hardware_phone));
		
		setListAdapter(adapter);
	}

	private class SampleItem {
		public int tag;
		public int iconRes;
		public SampleItem(int tag, int iconRes) {
			this.tag = tag; 
			this.iconRes = iconRes;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switchFragment(l, v, position, id);
	}
	
	// the meat of switching the above fragment
	private void switchFragment(ListView l, View v, int position, long id) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof DailyBreadActivity) {
			DailyBreadActivity fca = (DailyBreadActivity) getActivity();
			fca.alertContent(l, v, position, id);
		}
	}

	public class SampleAdapter extends ArrayAdapter<SampleItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);

			return convertView;
		}

	}
}
