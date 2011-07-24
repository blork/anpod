///*
// * Copyright 2011 Google Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.blork.anpod.util;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Typeface;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.blork.anpod.R;
//import com.blork.anpod.activity.HomeActivity;
//import com.markupartist.android.widget.ActionBar;
//import com.markupartist.android.widget.ActionBar.AbstractAction;
//
//// TODO: Auto-generated Javadoc
///**
// * A class that handles some common activity-related functionality in the app, such as setting up
// * the action bar. This class provides functioanlity useful for both phones and tablets, and does
// * not require any Android 3.0-specific features.
// */
//public class ActivityHelper {
//
//	/** The m activity. */
//	protected Activity mActivity;
//
//	/** The action bar. */
//	private ActionBar actionBar;
//
//	/**
//	 * Factory method for creating {@link ActivityHelper} objects for a given activity. Depending
//	 * on which device the app is running, either a basic helper or Honeycomb-specific helper will
//	 * be returned.
//	 *
//	 * @param activity the activity
//	 * @return the activity helper
//	 */
//	public static ActivityHelper createInstance(Activity activity) {
//		return UIUtils.isHoneycomb() ?
//				new ActivityHelperHoneycomb(activity) :
//					new ActivityHelper(activity);
//	}
//
//	/**
//	 * Instantiates a new activity helper.
//	 *
//	 * @param activity the activity
//	 */
//	protected ActivityHelper(Activity activity) {
//		mActivity = activity;
//	}
//
//	/**
//	 * On post create.
//	 *
//	 * @param savedInstanceState the saved instance state
//	 */
//	public void onPostCreate(Bundle savedInstanceState) {
//		Log.e("", "onPostCreate");
//
//		// Create the action bar
////		final SimpleMenu menu = new SimpleMenu(mActivity);
////		mActivity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, menu);
//
////		if (this.menuResId != null) {
////			addActionButtonCompat(R.drawable.more, 0,  
////					new OnClickListener() {
////				@Override
////				public void onClick(View view) {
////					Log.e("", "clicked");
////					final QuickAction mQuickAction 	= new QuickAction(view);
////
////					for (int i = 0; i < menu.size(); i++) {
////						final MenuItem item = menu.getItem(i);
////						final ActionItem addAction = new ActionItem();
////						addAction.setTitle(item.getTitle().toString());
////						addAction.setIcon(item.getIcon());
////
////						addAction.setOnClickListener(new OnClickListener() {
////							@Override
////							public void onClick(View v) {
////								mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
////								mQuickAction.dismiss();
////							}
////						});
////
////						mQuickAction.addActionItem(addAction);
////					}
////
////					mQuickAction.setAnimStyle(QuickAction.ANIM_GROW_FROM_RIGHT);
////					mQuickAction.show();
////				}
////			},
////			false);
//		//		} else {
//		
//		addActionButtonCompat(android.R.drawable.ic_menu_search, 0,
//				
//		}, false);
//		
//	}
//
//	/**
//	 * On create options menu.
//	 *
//	 * @param menu the menu
//	 * @return true, if successful
//	 */
//	public boolean onCreateOptionsMenu(Menu menu) {
//		mActivity.getMenuInflater().inflate(R.menu.default_menu_items, menu);
//		return true;	
//	}
//
//	/**
//	 * On options item selected.
//	 *
//	 * @param item the item
//	 * @return true, if successful
//	 */
//	public boolean onOptionsItemSelected(MenuItem item) {
//		Log.e("!", "Menu item selected!");
//		switch (item.getItemId()) {
//		case R.id.menu_search:
//			Log.e("!", "Searching!");
//			goSearch();
//			return true;
//		}
//		return false;
//	}
//
//	/**
//	 * On key down.
//	 *
//	 * @param keyCode the key code
//	 * @param event the event
//	 * @return true, if successful
//	 */
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
////		if (keyCode == KeyEvent.KEYCODE_MENU) {
////			return true;
////		}
//		//TODO: this disables the menu key
//		return false;
//	}
//
//	/**
//	 * On key long press.
//	 *
//	 * @param keyCode the key code
//	 * @param event the event
//	 * @return true, if successful
//	 */
//	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			goHome();
//			return true;
//		}
//		return false;
//	}
//
//	/**
//	 * Method, to be called in <code>onPostCreate</code>, that sets up this activity as the
//	 * home activity for the app.
//	 */
//	public void setupHomeActivity() {
//	}
//
//	/**
//	 * Method, to be called in <code>onPostCreate</code>, that sets up this activity as a
//	 * sub-activity in the app.
//	 */
//	public void setupSubActivity() {
//	}
//
//	/**
//	 * Invoke "home" action, returning to {@link com.google.android.apps.iosched.ui.HomeActivity}.
//	 */
//	public void goHome() {
//		if (mActivity instanceof HomeActivity) {
//			return;
//		}
//
//		final Intent intent = new Intent(mActivity, HomeActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		mActivity.startActivity(intent);
//		//mActivity.overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
//	}
//
//	/**
//	 * Invoke "search" action, triggering a default search.
//	 */
//	public void goSearch() {
//		mActivity.onSearchRequested();
//	}
//
//	/**
//	 * Sets up the action bar with the given title and accent color. If title is null, then
//	 * the app logo will be shown instead of a title. Otherwise, a home button and title are
//	 * visible. If color is null, then the default colorstrip is visible.
//	 *
//	 * @param actionBar the action bar
//	 * @param title the title
//	 * @param color the color
//	 */
//
//
//
//
//	/**
//	 * Sets the action bar title to the given string.
//	 *
//	 * @return the action bar compat
//	 */
//	//    public void setActionBarTitle(CharSequence title) {
//	//        ActionBar actionBar = getActionBarCompat();
//	//        if (actionBar == null) {
//	//            return;
//	//        }
//	//
//	//        TextView titleText = (TextView) actionBar.findViewById(R.id.actionbar_compat_text);
//	//        if (titleText != null) {
//	//            titleText.setText(title);
//	//        }
//	//    }
//
//	/**
//	 * Returns the {@link ViewGroup} for the action bar on phones (compatibility action bar).
//	 * Can return null, and will return null on Honeycomb.
//	 */
//	public ActionBar getActionBarCompat() {
//		return actionBar;
//	}
//
//	/**
//	 * Adds an action bar button to the compatibility action bar (on phones).
//	 *
//	 * @param iconResId the icon res id
//	 * @param textResId the text res id
//	 * @param clickListener the click listener
//	 * @param separatorAfter the separator after
//	 * @return the view
//	 */
//	private View addActionButtonCompat(int iconResId, int textResId,  View.OnClickListener clickListener, boolean separatorAfter) {
//		final ActionBar actionBar = getActionBarCompat();
//		if (actionBar == null) {
//			return null;
//		}
//
//
//
//		return actionBar;
//	}
//
//	/**
//	 * Adds an action button to the compatibility action bar, using menu information from a.
//	 *
//	 * @param item the item
//	 * @return the view
//	 * {@link MenuItem}. If the menu item ID is <code>menu_refresh</code>, the menu item's state
//	 * can be changed to show a loading spinner using
//	 * {@link ActivityHelper#setRefreshActionButtonCompatState(boolean)}.
//	 */
//	private View addActionButtonCompatFromMenuItem(final MenuItem item) {
//		final ActionBar actionBar = getActionBarCompat();
//		if (actionBar == null) {
//			return null;
//		}
//
//		int icon;
//
//		switch (item.getItemId()) {
//		case R.id.menu_search:
//			icon = R.drawable.ic_title_search;
//			break;
//		default:
//			icon = R.drawable.ic_title_today;
//		}
//
//		actionBar.addAction(new MenuAction(icon, item));
//
//		return actionBar;
//	}
//
//
//	/**
//	 * Sets the indeterminate loading state of a refresh button added with
//	 * {@link ActivityHelper#addActionButtonCompatFromMenuItem(android.view.MenuItem)}
//	 * (where the item ID was menu_refresh).
//	 */
//	//    public void setRefreshActionButtonCompatState(boolean refreshing) {
//	//        View refreshButton = mActivity.findViewById(R.id.menu_refresh);
//	//        View refreshIndicator = mActivity.findViewById(R.id.menu_refresh_progress);
//	//
//	//        if (refreshButton != null) {
//	//            refreshButton.setVisibility(refreshing ? View.GONE : View.VISIBLE);
//	//        }
//	//        if (refreshIndicator != null) {
//	//            refreshIndicator.setVisibility(refreshing ? View.VISIBLE : View.GONE);
//	//        }
//	//    }
//}
