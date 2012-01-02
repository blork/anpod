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

import java.util.List;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Window;

import com.blork.anpod.R;
import com.blork.anpod.activity.fragments.HomeDetailFragment;
import com.blork.anpod.model.Picture;
import com.blork.anpod.model.PictureFactory;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class DetailsFragmentPagerActivity extends FragmentActivity {
	private TabPageIndicator mIndicator;

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		setContentView(R.layout.fragment_pager);
		PagerAdapter mAdapter = new PagerAdapter(getSupportFragmentManager());

		ViewPager mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		mIndicator = (TabPageIndicator)findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);

		mIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				HomeActivity.current = position;
				if ((HomeActivity.pictures.size() - position) < 5) {
					new AddMorePicturesTask(getApplicationContext()).execute();
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				
			}
		});
		
		int index = getIntent().getExtras().getInt("index");
		mPager.setCurrentItem(index);
	}

	private class AddMorePicturesTask extends AsyncTask<Integer, Integer, Boolean> {
		private Context context;

		public AddMorePicturesTask(Context context){
			this.context = context;
		}
		
		protected void onPreExecute() {
		}

		protected Boolean doInBackground(Integer... i) {
			try {
				List<Picture> results;

				if (HomeActivity.pictures.isEmpty()) {
					results = PictureFactory.load();
				} else {
					int index = HomeActivity.pictures.size() - 1;
					int pictureId = HomeActivity.pictures.get(index).id;
					results = PictureFactory.load(pictureId);
				}

				if (results.isEmpty()) {
					Log.d("", "load failed?");
					return false;
				}

				PictureFactory.saveAll(context, results);

				HomeActivity.pictures.addAll(results);		
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				mIndicator.notifyDataSetChanged();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onPause();
	}


	private static class PagerAdapter extends FragmentStatePagerAdapter implements TitleProvider {
		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return HomeActivity.pictures.size();
		}

		@Override
		public Fragment getItem(int position) {
			return HomeDetailFragment.newInstance(position);
		}
		
		@Override
		public String getTitle(int position) {
			return HomeActivity.pictures.get(position).title.toUpperCase();
		}
	}

}
