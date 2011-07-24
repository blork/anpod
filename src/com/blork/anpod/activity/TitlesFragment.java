package com.blork.anpod.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.blork.anpod.R;
import com.blork.anpod.adapters.PictureThumbnailAdapter;
import com.blork.anpod.adapters.TitlesAdapter;
import com.blork.anpod.model.Picture;
import com.blork.anpod.model.PictureFactory;
import com.blork.anpod.util.Utils;
import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

public class TitlesFragment extends ResultsFragment {
		
	@Override
	public void listSetup() {
				
    	HomeActivity.pictures = PictureFactory.getLocalPictures(getActivity());

		thumbs = new PictureThumbnailAdapter(
				getActivity().getApplicationContext(), 
				R.layout.simple_list_item_checkable_1, 
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
		
		new UpdatePictureListTask().execute();
	}
	
	/**
	 * The Class UpdatePictureListTask.
	 */
	public class UpdatePictureListTask extends AsyncTask<Void, Void, Boolean> {
			
		/** The pictures. */
		List<Picture> pictures = new ArrayList<Picture>();

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		public Boolean doInBackground(Void... params) {
			try {
				pictures = PictureFactory.load();
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
				
				for(Picture p: pictures) {
					p.save(getActivity());
				}

				Utils.prependToList(HomeActivity.pictures, pictures);
				etAdapter.notifyDataSetChanged();
				
										
				//PictureFactory.deleteAll(getActivity());
    			//TODO: NPE if rotate
			} else {
				getActivity().findViewById(R.id.no_results).setVisibility(View.VISIBLE);
				getActivity().findViewById(R.id.list_progress).setVisibility(View.GONE);
			}
		}

	}

}
