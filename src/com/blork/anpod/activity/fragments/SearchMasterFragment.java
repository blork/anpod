package com.blork.anpod.activity.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blork.anpod.R;
import com.blork.anpod.activity.SearchActivity;
import com.blork.anpod.activity.SearchDetailsFragmentPagerActivity;
import com.blork.anpod.adapters.PictureThumbnailAdapter;
import com.blork.anpod.adapters.SearchAdapter;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

public class SearchMasterFragment extends MasterFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.search_titles_fragment, container, false);
	}
	
	@Override
	public void listSetup() {	
				
		thumbs = new PictureThumbnailAdapter(
				getActivity().getApplicationContext(), 
				R.layout.list_item, 
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
	}
	

	@Override
	public void showDetails(int index) {
		mCurCheckPosition = index;

        if (isDualPane) {
            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            getListView().setItemChecked(index, true);

            // Check what fragment is currently shown, replace if needed.
            SearchDetailFragment details = (SearchDetailFragment) getFragmentManager().findFragmentById(R.id.details);
            
            if (details == null || details.getShownIndex() != index) {
                // Make new fragment to show this selection.
                details = SearchDetailFragment.newInstance(index);

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

