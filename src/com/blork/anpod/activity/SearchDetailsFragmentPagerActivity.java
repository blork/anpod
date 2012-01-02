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



import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.Menu;
import android.support.v4.view.ViewPager;
import android.view.Window;

import com.blork.anpod.R;
import com.blork.anpod.activity.fragments.SearchDetailFragment;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class SearchDetailsFragmentPagerActivity extends FragmentActivity {
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

		int index = getIntent().getExtras().getInt("index");
		mPager.setCurrentItem(index);
	}


	private static class PagerAdapter extends FragmentStatePagerAdapter implements TitleProvider {
		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return SearchActivity.pictures.size();
		}

		@Override
		public Fragment getItem(int position) {
			return SearchDetailFragment.newInstance(position);
		}
		
		@Override
		public String getTitle(int position) {
			return SearchActivity.pictures.get(position).title.toUpperCase();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.details_menu_items, menu);
		return true;	
	}
}
