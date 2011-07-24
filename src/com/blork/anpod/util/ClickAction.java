package com.blork.anpod.util;
import android.view.View;
import android.view.View.OnClickListener;

import com.markupartist.android.widget.ActionBar.AbstractAction;

public class ClickAction extends AbstractAction { 

	private OnClickListener clickListener;

	public ClickAction(int drawable, OnClickListener clickListener) {
		super(drawable);
		this.clickListener = clickListener;
	}

	@Override
	public void performAction(View view) {
		view.setOnClickListener(clickListener);
		view.performClick();
	}

}