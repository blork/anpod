package com.blork.anpod.adapters;

import java.util.List;

import android.app.Activity;
import android.util.Log;

import com.blork.anpod.activity.HomeActivity;
import com.blork.anpod.model.Picture;
import com.blork.anpod.model.PictureFactory;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

// TODO: Auto-generated Javadoc
/**
 * The Class PictureThumbnailAdapter.
 */
public class TitlesAdapter extends EndlessPictureThumbnailAdapter {

	public TitlesAdapter(Activity activity, ThumbnailAdapter tAdapter) {
		super(activity, tAdapter);
	}

	@Override
	protected boolean cacheInBackground() {
		
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
				Log.e("", "load failed?");
				return false;
			}
			
			//PictureFactory.saveAll(activity, results);
			
			HomeActivity.pictures.addAll(results);		
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
}