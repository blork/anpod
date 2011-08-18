package com.blork.anpod.adapters;

import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.blork.anpod.R;
import com.blork.anpod.activity.HomeActivity;
import com.blork.anpod.model.Picture;
import com.blork.anpod.model.PictureFactory;
import com.commonsware.cwac.endless.EndlessAdapter;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

// TODO: Auto-generated Javadoc
/**
 * The Class PictureThumbnailAdapter.
 */
abstract class EndlessPictureThumbnailAdapter extends EndlessAdapter {
	private RotateAnimation rotate=null;
	protected Activity activity;

	public EndlessPictureThumbnailAdapter(Activity activity, ThumbnailAdapter tAdapter) {
		super(tAdapter);
		
		this.activity = activity;
		
		rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotate.setDuration(600);
		rotate.setRepeatMode(Animation.RESTART);
		rotate.setRepeatCount(Animation.INFINITE);
	}

	@Override
	protected View getPendingView(ViewGroup parent) {
		View row = activity.getLayoutInflater().inflate(R.layout.list_item, null);

		View child = row.findViewById(R.id.list_item);

		child.setVisibility(View.GONE);

		child = row.findViewById(R.id.throbber);
		child.setVisibility(View.VISIBLE);
		child.startAnimation(rotate);

		return(row);
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
			
			PictureFactory.saveAll(activity, results);
			
			HomeActivity.pictures.addAll(results);		
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	protected void appendCachedData() {
	}
}