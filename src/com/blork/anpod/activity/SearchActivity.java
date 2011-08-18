package com.blork.anpod.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.blork.anpod.R;
import com.blork.anpod.model.Picture;


// TODO: Auto-generated Javadoc
/**
 * The Class SearchActivity.
 */
public class SearchActivity extends FragmentActivity {

	/** The query. */
	public static String query = null;

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

		if (getIntent().getAction().equals(Intent.ACTION_SEARCH)) {
			String query = getIntent().getStringExtra(SearchManager.QUERY);
			SearchActivity.query = query;
		} else {
			finish();
		}

		setContentView(R.layout.search_fragment_layout);
	}


	protected void onNewIntent (Intent intent) {
		pictures.clear();
		finish();
		startActivity(intent);
	}


}
