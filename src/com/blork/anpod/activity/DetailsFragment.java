package com.blork.anpod.activity;

import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.blork.anpod.R;
import com.blork.anpod.model.Picture;
import com.blork.anpod.util.BitmapUtils;
import com.blork.anpod.util.BitmapUtils.OnFetchCompleteListener;
import com.blork.anpod.util.UIUtils;

// TODO: Auto-generated Javadoc
/**
 * This is the secondary fragment, displaying the details of a particular
 * item.
 */

abstract class DetailsFragment extends Fragment {
	public List<Picture> pictures;
	private Picture picture;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setHasOptionsMenu(true);
	}

	/**
	 * Gets the shown index.
	 *
	 * @return the shown index
	 */
	public int getShownIndex() {
		return getArguments().getInt("index", 0);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		
		

		final View details = inflater.inflate(R.layout.details_fragment, container, false);

		if (!pictures.isEmpty()) {
			
			picture = pictures.get(getShownIndex());
			
			UIUtils.setupActionBar(details, picture.title);

			BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
			decodeOptions.inSampleSize = 2;

			BitmapUtils.fetchImage(
					getActivity(), 
					picture.getFullSizeImageUrl(), 
					picture.title, 
					decodeOptions, 
					null, 
					new OnFetchCompleteListener() {
						@Override
						public void onFetchComplete(Object cookie, final Bitmap result, final Uri uri) {
							try {

								getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

								final ImageView iv = (ImageView)details.findViewById(R.id.main_picture);
								iv.setImageBitmap(result);
								iv.setVisibility(View.VISIBLE);
								details.findViewById(R.id.image_progress).setVisibility(View.GONE);


								picture.uri = uri;
								
							} catch (NullPointerException e) {
								e.printStackTrace();
							}
							
						}


					}
			);

			//TextView handle = (TextView)details.findViewById(R.id.handle);

			final WebView text = (WebView)details.findViewById(R.id.image_desc);
			final View textContainer = details.findViewById(R.id.image_desc_cont);

			String html = "<style>" +
			"* { " +
			"text-align:justify; " +
			"color:#fff; " +
			"line-height:1.5em; " +
			"} " +
			"a { " +
			"color:#EEE; " +
			"} " +
			"</style>" + picture.info.replace("\n", " ");

			text.loadData(html,"text/html", "utf-8");
			text.setBackgroundColor(0);

			ImageButton button = (ImageButton) details.findViewById(R.id.image_details_button);

			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					switch(textContainer.getVisibility()) {
					case View.GONE:
						textContainer.setVisibility(View.VISIBLE);
						break;
					case View.VISIBLE:
						textContainer.setVisibility(View.GONE);
						break;
					}
				}
			});
		}
		return details;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		BitmapUtils.manageCache(this.getActivity().getApplicationContext());
	}

}