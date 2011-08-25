package com.blork.anpod.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.blork.anpod.R;
import com.blork.anpod.model.Picture;

// TODO: Auto-generated Javadoc
/*
 * Copyright (C) 2010 The Android Open Source Project
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

/**
 * Demonstration of using fragments to implement different activity layouts.
 * This sample provides a different layout (and activity flow) when run in
 * landscape.
 */
public class HomeActivity extends FragmentActivity {

	/** The pictures. */
	public static List<Picture> pictures = new ArrayList<Picture>();

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		//startActivity(new Intent(this, FragmentStatePagerSupport.class));
		setContentView(R.layout.fragment_layout);

		setProgressBarIndeterminateVisibility(Boolean.FALSE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);	

		try {
			PackageManager pm = getPackageManager();
			pm.getPackageInfo("com.blork.anpodpro", PackageManager.GET_ACTIVITIES);

			boolean pro = settings.getBoolean("pro", false);
			if(!pro){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Thank you!");
				builder.setMessage("Thanks for buying the donate version - I really appreciate it!")
				.setCancelable(false)
				.setNeutralButton("No Problem!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						startActivity(new Intent(HomeActivity.this, HomeActivity.class));
						finish();
					} 
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
			SharedPreferences.Editor pEditor = settings.edit();
			pEditor.putBoolean("pro", true);
			pEditor.commit();
		} catch (NameNotFoundException e1) {
			SharedPreferences.Editor pEditor = settings.edit();
			pEditor.putBoolean("pro", false);
			pEditor.commit();
		}
	}

}
