/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blork.anpod.util;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.blork.anpod.R;
import com.markupartist.android.widget.ActionBar;

// TODO: Auto-generated Javadoc
/**
 * An assortment of UI helpers.
 */
public class UIUtils {

	/**
	 * Populate the given {@link TextView} with the requested text, formatting
	 * through {@link Html#fromHtml(String)} when applicable. Also sets
	 *
	 * @param view the view
	 * @param text the text
	 * {@link TextView#setMovementMethod} so inline links are handled.
	 */
	public static void setTextMaybeHtml(TextView view, String text) {
		if (TextUtils.isEmpty(text)) {
			view.setText("");
			return;
		}
		if (text.contains("<") && text.contains(">")) {
			view.setText(Html.fromHtml(text));
			view.setMovementMethod(LinkMovementMethod.getInstance());
		} else {
			view.setText(text);
		}
	}


	/** The Constant BRIGHTNESS_THRESHOLD. */
	private static final int BRIGHTNESS_THRESHOLD = 130;

	/**
	 * Calculate whether a color is light or dark, based on a commonly known
	 * brightness formula.
	 *
	 * @param color the color
	 * @return true, if is color dark
	 * @see {@literal http://en.wikipedia.org/wiki/HSV_color_space%23Lightness}
	 */
	public static boolean isColorDark(int color) {
		return ((30 * Color.red(color) +
				59 * Color.green(color) +
				11 * Color.blue(color)) / 100) <= BRIGHTNESS_THRESHOLD;
	}

	/**
	 * Checks if is honeycomb.
	 *
	 * @return true, if is honeycomb
	 */
	public static boolean isHoneycomb() {
		// Can use static final constants like HONEYCOMB, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed behavior.
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	/**
	 * Checks if is honeycomb tablet.
	 *
	 * @param context the context
	 * @return true, if is honeycomb tablet
	 */
	public static boolean isHoneycombTablet(Context context) {
		// Can use static final constants like HONEYCOMB, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed behavior.
		return isHoneycomb() && (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK)
				== Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}



	/**
	 * Gets the icon for intent.
	 *
	 * @param context the context
	 * @param i the i
	 * @return the icon for intent
	 */
	public static Drawable getIconForIntent(final Context context, Intent i) {
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> infos = pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
		if (infos.size() > 0) {
			return infos.get(0).loadIcon(pm);
		}
		return null;
	}


	public static void setupActionBar(View v, String t) {
		if(UIUtils.isHoneycomb()) {
			return;
		}
		
		ActionBar actionBar = (ActionBar)v.findViewById(R.id.actionbar);
		
		if (actionBar == null) {
			return;
		}
		
		actionBar.setTitle(t);
	}
	
	public static void setupActionBar(Activity activity) {
		setupActionBar(activity, null);
	}
	

	public static void setupActionBar(final Activity activity, String title) {
		if(UIUtils.isHoneycomb()) {
			return;
		}
		
		ActionBar actionBar = (ActionBar)activity.findViewById(R.id.actionbar);

		if (actionBar == null) {
			return;
		}

		if (title == null) {
			TextView tv = (TextView)actionBar.findViewById(R.id.actionbar_title);
			Typeface face = Typeface.createFromAsset(activity.getAssets(), "fonts/FFFFORWA.TTF"); 
			tv.setTypeface(face);
			tv.setTextSize(12f);
			actionBar.setTitle(activity.getResources().getString(R.string.app_name));
		} else {
			actionBar.setTitle(title);
		}

		actionBar.addAction(new ClickAction(android.R.drawable.ic_menu_search, new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onSearchRequested();
			}
		}));
	}




	/**
	 * The Class MenuAction.
	 */
//	private class MenuAction extends AbstractAction { 
//
//		/** The item. */
//		MenuItem item;
//
//		/**
//		 * Instantiates a new menu action.
//		 *
//		 * @param drawable the drawable
//		 * @param item the item
//		 */
//		public MenuAction(int drawable, MenuItem item) {
//			super(drawable);	
//			this.item = item;
//		}
//
//		/* (non-Javadoc)
//		 * @see com.markupartist.android.widget.ActionBar.Action#performAction(android.view.View)
//		 */
//		@Override
//		public void performAction(View view) {
//			Log.e("!", "Performing action...");
//			mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
//		}
//
//	}
	
}
