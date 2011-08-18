package com.blork.anpod.activity.fragments;

import java.util.Date;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import com.blork.anpod.R;
import com.blork.anpod.activity.HomeActivity;
import com.blork.anpod.activity.UserPreferenceActivity;
import com.blork.anpod.activity.UserPreferenceActivityV11;
import com.blork.anpod.adapters.PictureThumbnailAdapter;
import com.blork.anpod.adapters.TitlesAdapter;
import com.blork.anpod.model.PictureFactory;
import com.blork.anpod.service.AnpodService;
import com.blork.anpod.util.UIUtils;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;
import com.ocpsoft.pretty.time.PrettyTime;

public class TitlesFragment extends ResultsFragment {

	private ListView lv;
	private com.blork.anpod.activity.fragments.TitlesFragment.UpdateReceiver updateReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.titles_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		lv = getListView();

		updateReceiver = new UpdateReceiver();
		getActivity().registerReceiver(updateReceiver, new IntentFilter(
				AnpodService.ACTION_FINISHED_UPDATE));

	}

	@Override
	public void onResume() {
		super.onResume();
		//lv.setLastUpdated(getLastUpdateTime());
	}

	@Override
	public void listSetup() {

		HomeActivity.pictures = PictureFactory.getLocalPictures(getActivity());

		//		if (HomeActivity.pictures.isEmpty()) {
		//			Intent serviceIntent = new Intent(getActivity(), AnpodService.class);
		//			serviceIntent.putExtra("silent_run", true);
		//			getActivity().startService(serviceIntent);
		//		}
		//		
		thumbs = new PictureThumbnailAdapter(
				getActivity().getApplicationContext(), 
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

		//new UpdatePictureListTask().execute();
	}

	//	/**
	//	 * The Class UpdatePictureListTask.
	//	 */
	//	public class UpdatePictureListTask extends AsyncTask<Void, Void, Boolean> {
	//
	//		/** The pictures. */
	//		List<Picture> pictures = new ArrayList<Picture>();
	//
	//		/* (non-Javadoc)
	//		 * @see android.os.AsyncTask#doInBackground(Params[])
	//		 */
	//		public Boolean doInBackground(Void... params) {
	//			try {
	//				pictures = PictureFactory.load();
	//				return true;
	//			} catch (Exception e) {
	//				e.printStackTrace();
	//				return false;
	//			}
	//		}
	//
	//		/* (non-Javadoc)
	//		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	//		 */
	//		protected void onPostExecute(Boolean result){
	//			if (result && !pictures.isEmpty()) {
	//
	//				for(Picture p: pictures) {
	//					p.save(getActivity());
	//				}
	//
	//				Utils.prependToList(HomeActivity.pictures, pictures);
	//				etAdapter.notifyDataSetChanged();
	//
	//
	//				//PictureFactory.deleteAll(getActivity());
	//				//TODO: NPE if rotate
	//			} else {
	//				getActivity().findViewById(R.id.no_results).setVisibility(View.VISIBLE);
	//				getActivity().findViewById(R.id.list_progress).setVisibility(View.GONE);
	//			}
	//		}
	//
	//	}


	class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			//lv.onRefreshComplete(getLastUpdateTime());

			//			Utils.prependToList(
			//					HomeActivity.pictures, 
			//					PictureFactory.getLocalPictures(getActivity().getApplicationContext())
			//			);

			HomeActivity.pictures.clear();
			HomeActivity.pictures.addAll(
					PictureFactory.getLocalPictures(
							getActivity().getApplicationContext()
					)
			);

			getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE);

			//etAdapter.notifyDataSetChanged();
		}
	}

	public void onDestroy(){
		super.onDestroy();
		getActivity().unregisterReceiver(updateReceiver);
	}

	private String getLastUpdateTime() {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());	
		Date lastUpdate = new Date(prefs.getLong("time", new Date().getTime()));
		PrettyTime pt = new PrettyTime();
		String timeString = "Last updated: " + pt.format(lastUpdate);
		return timeString;
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		showDetails(position);
	}
}
