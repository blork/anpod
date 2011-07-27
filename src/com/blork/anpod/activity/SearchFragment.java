package com.blork.anpod.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blork.anpod.R;
import com.blork.anpod.adapters.PictureThumbnailAdapter;
import com.blork.anpod.adapters.SearchAdapter;
import com.blork.anpod.model.Picture;
import com.blork.anpod.model.PictureFactory;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

public class SearchFragment extends ResultsFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		return inflater.inflate(R.layout.search_fragment, container, false);
	}
	
	@Override
	public void listSetup() {	
				
		thumbs = new PictureThumbnailAdapter(
				getActivity().getApplicationContext(), 
				R.layout.simple_list_item_checkable_1, 
				SearchActivity.pictures
		);
		
		tAdapter = new ThumbnailAdapter(
				getActivity(), 
				thumbs, 
				cache, 
				IMAGE_IDS
		);
		
		etAdapter = new SearchAdapter(
				getActivity(), tAdapter
		);
		
		setListAdapter(etAdapter);
		
		//new SearchPictureListTask(SearchActivity.query).execute();
	}
	
	/**
	 * The Class UpdatePictureListTask.
	 */
	public class SearchPictureListTask extends AsyncTask<Void, Void, Boolean> {
			
		/** The pictures. */
		List<Picture> pictures = new ArrayList<Picture>();

		private String query;

		/**
		 * Instantiates a new update picture list task.
		 *
		 * @param query the query
		 */
		public SearchPictureListTask(String query) {
			this.query = query;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		public Boolean doInBackground(Void... params) {
			try {
				pictures = PictureFactory.search(query);
				Log.e("!!!", "Loading...");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
		}
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute(Boolean result){
			if (result && !pictures.isEmpty()) {
				Log.e("!!!", pictures.toString());
				SearchActivity.pictures.clear();
				SearchActivity.pictures.addAll(pictures);
				thumbs.notifyDataSetChanged();
			} else {
				getActivity().findViewById(R.id.no_results).setVisibility(View.VISIBLE);
				getActivity().findViewById(R.id.list_progress).setVisibility(View.GONE);
			}
		}

	}

	@Override
	public void showDetails(int index) {
		mCurCheckPosition = index;

        if (mDualPane) {
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            getListView().setItemChecked(index, true);

            // Check what fragment is currently shown, replace if needed.
            SearchDetailsFragment details = (SearchDetailsFragment) getFragmentManager().findFragmentById(R.id.details);
            
            if (details == null || details.getShownIndex() != index) {
                // Make new fragment to show this selection.
                details = SearchDetailsFragment.newInstance(index);

                // Execute a transaction, replacing any existing fragment
                // with this one inside the frame.
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.details, details);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }

        } else {
        	Intent intent = new Intent();
            //intent.setClass(getActivity(), DetailsActivity.class);
        	intent.setClass(getActivity(), SearchDetailsFragmentPagerActivity.class);
            intent.putExtra("index", index);
            startActivity(intent);           
       }
	}
	
}

