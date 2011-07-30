/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.blork.anpod.activity;

import java.io.IOException;
import java.net.URISyntaxException;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.blork.anpod.R;
import com.blork.anpod.model.Picture;
import com.blork.anpod.util.BitmapUtils;
import com.blork.anpod.util.Utils;

public class DetailsFragmentPagerActivity extends FragmentActivity {
	MyAdapter mAdapter;
	ViewPager mPager;
	private int index;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_pager);

		mAdapter = new MyAdapter(getSupportFragmentManager());

		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		index = getIntent().getExtras().getInt("index");

		mPager.setCurrentItem(index);

		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}

			@Override
			public void onPageSelected(int i) {
				index = i;
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.details_menu_items, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Picture picture = HomeActivity.pictures.get(index);
		if(picture.uri != null) {
			menu.findItem(R.id.menu_save).setEnabled(true);
			menu.findItem(R.id.menu_set_wallpaper).setEnabled(true);
			menu.findItem(R.id.menu_fullscreen).setEnabled(true);
		} else {
			menu.findItem(R.id.menu_save).setEnabled(false);
			menu.findItem(R.id.menu_set_wallpaper).setEnabled(false);
			menu.findItem(R.id.menu_fullscreen).setEnabled(false);
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Picture picture = HomeActivity.pictures.get(index);

		switch (item.getItemId()) {
		case R.id.menu_save:
			//Utils.moveToNiceFolder(picture.uri);
			try {
				Utils.copyFileToUserSpace(this, picture.uri);
				Toast.makeText(this, "Saved to sdcard/APOD/" + BitmapUtils.toSlug(picture.title) + ".jpg", Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Toast.makeText(this, "Unable to save image.", Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.menu_set_wallpaper:
			new SetWallpaperTask().execute(picture);
			return true;
		case R.id.menu_fullscreen:
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(picture.uri, "image/jpeg");
			startActivity(intent);
			return true;
		case R.id.menu_website:
			Uri url = Uri.parse("http://apod.nasa.gov/apod/"+ picture.uid +".html");
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(url);
			startActivity(i);
			return true;
		}

		return false;		
	}
	public static class MyAdapter extends FragmentStatePagerAdapter {
		public MyAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return HomeActivity.pictures.size();
		}

		@Override
		public Fragment getItem(int position) {
			return TitlesDetailsFragment.newInstance(position);
		}
	}

	private class SetWallpaperTask extends AsyncTask<Picture, Integer, Boolean> {
		protected void onPreExecute() {
			Toast.makeText(DetailsFragmentPagerActivity.this, "Setting wallpaper...", Toast.LENGTH_LONG).show();
		}
		
		protected Boolean doInBackground(Picture... pictures) {
			Picture picture = pictures[0];

			WallpaperManager wm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);

			int newWidth = wm.getDesiredMinimumWidth();
			int newHeight = wm.getDesiredMinimumHeight();

			try {
				Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(picture.uri));
				Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
				wm.setBitmap(resizedBitmap);
			} catch (Exception e) {
				return false;
			}

			return true;
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(DetailsFragmentPagerActivity.this, "Wallpaper set.", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(DetailsFragmentPagerActivity.this, "Couldn't set the wallpaper.", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
