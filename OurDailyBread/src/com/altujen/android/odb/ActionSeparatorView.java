package com.altujen.android.odb;

import android.content.Context;
import android.widget.ImageView;

public class ActionSeparatorView extends ImageView {
	public ActionSeparatorView (Context context) {
	    super(context);
	    setImageDrawable(getResources().getDrawable(R.drawable.vertical_separator));
	  }
}
