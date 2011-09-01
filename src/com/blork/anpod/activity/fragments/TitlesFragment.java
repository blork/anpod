package com.blork.anpod.activity.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.blork.anpod.R;
import com.blork.anpod.activity.HomeActivity;
import com.blork.anpod.adapters.AdvertisingAdapter;
import com.blork.anpod.adapters.PictureThumbnailAdapter;
import com.blork.anpod.adapters.TitlesAdapter;
import com.blork.anpod.model.PictureFactory;
import com.blork.anpod.service.AnpodService;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

public class TitlesFragment extends ResultsFragment {

	private com.blork.anpod.activity.fragments.TitlesFragment.UpdateReceiver updateReceiver;
	//private boolean isPro = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.titles_fragment, container, false);

		//		adView = new AdView(getActivity(), AdSize.BANNER, "a14e4bffa8a0175");
		//		LinearLayout layout = (LinearLayout)view.findViewById(R.id.root);
		//		layout.addView(adView);
		//		adView.loadAd(new AdRequest());

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateReceiver = new UpdateReceiver();
		getActivity().registerReceiver(updateReceiver, new IntentFilter(
				AnpodService.ACTION_FINISHED_UPDATE));

		Log.e("", "View_Image");
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null && extras.containsKey("view_image")) {
			showDetails(extras.getInt("view_image"));
			getActivity().getIntent().removeExtra("view_image");
		}
	}


	@Override
	public void listSetup() {

		HomeActivity.pictures = PictureFactory.getLocalPictures(getActivity());

		thumbs = new PictureThumbnailAdapter(
				getActivity(), 
				R.layout.list_item, 
				HomeActivity.pictures
		);

		tAdapter = new ThumbnailAdapter(
				getActivity(), 
				thumbs, 
				cache, 
				IMAGE_IDS
		);

		etAdapter = new TitlesAdapter(
				getActivity(), tAdapter
		);


		setListAdapter(etAdapter);
	}



	class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			HomeActivity.pictures.clear();
			HomeActivity.pictures.addAll(
					PictureFactory.getLocalPictures(
							getActivity().getApplicationContext()
					)
			);
			etAdapter.notifyDataSetChanged();
			getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE);
		}
	}

	public void onDestroy(){
		super.onDestroy();
		getActivity().unregisterReceiver(updateReceiver);
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//		if (!isPro) {
		//			showDetails(position - ((position / AdvertisingAdapter.AD_AMOUNT) + 1));
		//		} else {
		showDetails(position);
		//		}
	}
}
