package com.blork.anpod.adapters;

import java.util.List;

import android.app.Activity;
import android.view.View;

import com.blork.anpod.R;
import com.blork.anpod.activity.SearchActivity;
import com.blork.anpod.model.Picture;
import com.blork.anpod.model.PictureFactory;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

// TODO: Auto-generated Javadoc
/**
 * The Class PictureThumbnailAdapter.
 */
public class SearchAdapter extends EndlessPictureThumbnailAdapter {

	private int page;

	public SearchAdapter(Activity activity, ThumbnailAdapter tAdapter) {
		super(activity, tAdapter);
		page = 1;
	}

	@Override
	protected boolean cacheInBackground() {
		
		try {
			List<Picture> results;

			if (SearchActivity.pictures.isEmpty()) {
				results = PictureFactory.search(SearchActivity.query);
			} else {
				results = PictureFactory.search(SearchActivity.query, page);
			}
			
			page++;

			if (results.isEmpty()) {
				activity.findViewById(R.id.no_results).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.list_progress).setVisibility(View.GONE);

				return false;
			}
			
			//PictureFactory.saveAll(activity, results);
			
			SearchActivity.pictures.addAll(results);		
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
}