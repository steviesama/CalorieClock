package com.calorieclock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.DatePicker;

public class NXDatePicker extends DatePicker {

	public NXDatePicker(Context context) {
		super(context);
	}

	public NXDatePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NXDatePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
	    if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
	        ViewParent p = getParent();
	        if (p != null)
	            p.requestDisallowInterceptTouchEvent(true);
	    }

	    return false;
	}

} //End class NXDatePicker
