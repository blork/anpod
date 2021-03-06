package com.blork.anpod.activity.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.blork.anpod.R;
import com.blork.anpod.activity.HomeActivity;
import com.blork.anpod.adapters.PictureThumbnailAdapter;
import com.blork.anpod.adapters.TitlesAdapter;
import com.blork.anpod.model.PictureFactory;
import com.blork.anpod.service.AnpodService;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

public class HomeMasterFragment extends MasterFragment {

	private com.blork.anpod.activity.fragments.HomeMasterFragment.UpdateReceiver updateReceiver;
	//private boolean isPro = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.titles_fragment, container, false);	
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateReceiver = new UpdateReceiver();
		getActivity().registerReceiver(updateReceiver, new IntentFilter(
				AnpodService.ACTION_FINISHED_UPDATE));

		Log.d("", "View_Image");
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null && extras.containsKey("view_image")) {
			showDetails(extras.getInt("view_image"));
			getActivity().getIntent().removeExtra("view_image");
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (HomeActivity.current != -1) {
			getListView().setSelection(HomeActivity.current);
			HomeActivity.current = -1;
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



	private class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			HomeActivity.pictures.clear();
			etAdapter.notifyDataSetChanged();
			HomeActivity.pictures.addAll(
					PictureFactory.getLocalPictures(
							getActivity().getApplicationContext()
							)
					);
			etAdapter.notifyDataSetChanged();
			getActivity().setProgressBarIndeterminateVisibility(false);
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
		showDetails(position);
	}
}
