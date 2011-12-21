package com.blork.anpod.activity.fragments;

import java.util.List;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.blork.anpod.R;
import com.blork.anpod.model.Picture;
import com.blork.anpod.util.BitmapUtils;
import com.blork.anpod.util.BitmapUtils.OnFetchCompleteListener;
import com.blork.anpod.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * This is the secondary fragment, displaying the details of a particular
 * item.
 */

abstract class DetailsFragment extends Fragment {
	public List<Picture> pictures;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
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

		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		View detailsFrame = getActivity().findViewById(R.id.details);
		boolean isDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
		if (isDualPane) {
			setHasOptionsMenu(true);
		}

		final View details = inflater.inflate(R.layout.details_fragment, container, false);

		if (pictures != null && !pictures.isEmpty()) {

			final Picture picture = pictures.get(getShownIndex());

			if (!isDualPane) {
				//				getActivity().getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
				//				getActivity().getSupportActionBar().setTitle(picture.title);
				//				getActivity().getSupportActionBar().setSubtitle(picture.credit);
				getSupportActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			}

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
								//details.findViewById(R.id.image_progress).setVisibility(View.GONE);

								iv.setOnLongClickListener(new OnLongClickListener() {
									@Override
									public boolean onLongClick(View v) {
										Intent intent2 = new Intent();
										intent2.setDataAndType(picture.uri, "image/jpeg");
										Intent intent3 = Intent.createChooser(intent2, "Open image with...");
										startActivity(intent3);
										return true;
									}
								});

								picture.uri = uri;

							} catch (NullPointerException e) {
								e.printStackTrace();
							}

						}


					}
			);

			//TextView handle = (TextView)details.findViewById(R.id.handle);

			final WebView text = (WebView)details.findViewById(R.id.image_desc);
			text.setWebViewClient(new EmbeddedWebViewClient());


			String html = "<style>" +
			"* { " +
			"text-align:justify; " +
			"color:#fff; " +
			"line-height:1.5em; " +
			"} " +
			"a { " +
			"color:#EEE; " +
			"} " +
			"h3, h4 {" +
			"text-align:center;" +
			"}" +
			"</style>" + "<h3>" + picture.title + "</h3>" + picture.info.replace("\n", " ");

			text.loadData(html,"text/html", "utf-8");
			text.setBackgroundColor(0);

			WebSettings settings = text.getSettings();
			settings.setTextSize(WebSettings.TextSize.NORMAL);
		} else if (isDualPane) {
			details.findViewById(R.id.image_select_one).setVisibility(View.VISIBLE);
			details.findViewById(R.id.image_progress).setVisibility(View.GONE);
		}

		return details;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		BitmapUtils.manageCache(this.getActivity().getApplicationContext());
	}


	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		if (menu.findItem(R.id.menu_info) == null) {
			inflater.inflate(R.menu.details_menu_items, menu);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if(pictures.size() < getShownIndex())
			return false;
		
		final Picture picture = pictures.get(getShownIndex());
		switch (item.getItemId()) {
		case R.id.menu_save:
			//Utils.moveToNiceFolder(picture.uri);
			try {
				Utils.copyFileToUserSpace(getActivity(), picture.uri);
				Toast.makeText(getActivity(), "Saved to sdcard/APOD/" + BitmapUtils.toSlug(picture.title) + ".jpg", Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Toast.makeText(getActivity(), "Unable to save image.", Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.menu_open:
			Intent intent2 = new Intent();
			intent2.setDataAndType(picture.uri, "image/jpeg");
			Intent intent3 = Intent.createChooser(intent2, "Open image with...");
			startActivity(intent3);
			return true;
		case R.id.menu_set_wallpaper:
			new SetWallpaperTask().execute(picture);
			return true;
		case R.id.menu_fullscreen:
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(picture.uri, "image/jpeg");
			startActivity(intent);
			return true;
		case R.id.menu_website:
			Uri url = Uri.parse("http://apod.nasa.gov/apod/"+ picture.uid +".html");
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(url);
			startActivity(i);
			return true;
		case R.id.menu_info:
			final View textContainer = getView().findViewById(R.id.image_desc_cont);
			switch(textContainer.getVisibility()) {
			case View.GONE:
				textContainer.setVisibility(View.VISIBLE);
				break;
			case View.VISIBLE:
				textContainer.setVisibility(View.GONE);
				break;
			}
			return true;
		case android.R.id.home:
			Utils.goHome(getActivity());
			return true;
		}
		return false;		
	}


	public void onPrepareOptionsMenu(Menu menu) {
		if (pictures == null || pictures.isEmpty()) {
			return;
		}

		final Picture picture = pictures.get(getShownIndex());

		if(picture != null && picture.uri != null) {
			menu.findItem(R.id.menu_save).setEnabled(true);
			menu.findItem(R.id.menu_set_wallpaper).setEnabled(true);
			menu.findItem(R.id.menu_fullscreen).setEnabled(true);
			menu.findItem(R.id.menu_open).setEnabled(true);
		} else {
			menu.findItem(R.id.menu_save).setEnabled(false);
			menu.findItem(R.id.menu_set_wallpaper).setEnabled(false);
			menu.findItem(R.id.menu_fullscreen).setEnabled(false);
			menu.findItem(R.id.menu_open).setEnabled(false);
		}
	}

	private class SetWallpaperTask extends AsyncTask<Picture, Integer, Boolean> {
		private Picture settingPicture;

		protected void onPreExecute() {
			Toast.makeText(getActivity(), "Setting wallpaper...", Toast.LENGTH_LONG).show();
		}

		protected Boolean doInBackground(Picture... settingPictures) {
			settingPicture = settingPictures[0];

			WallpaperManager wm = (WallpaperManager) getActivity().getSystemService(Context.WALLPAPER_SERVICE);

			int desiredWidth = wm.getDesiredMinimumWidth();
			int desiredHeight = wm.getDesiredMinimumHeight();


			if (desiredHeight < 0 || desiredWidth < 0) {
				WindowManager window = getActivity().getWindowManager();
				Display display = window.getDefaultDisplay();
				desiredWidth = display.getWidth() * 2;
				desiredHeight = display.getHeight();
			}
			
			Log.d("APOD", desiredWidth+""+desiredHeight);
			
			try {
				wm.setBitmap(
						BitmapUtils.resizeBitmap(
								getActivity().getContentResolver().openInputStream(settingPicture.uri), 
								desiredWidth, 
								desiredHeight
						)
				);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		protected void onPostExecute(Boolean result) {
			if (result && settingPicture != null && getActivity() != null) {
				Toast.makeText(getActivity(), "Picture set as wallpaper.", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity(), "Couldn't set the wallpaper.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class EmbeddedWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Uri a = Uri.parse(url);

			if(a.isRelative()) {
				a = Uri.parse("http://apod.nasa.gov/apod/"+ url);
			}

			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(a);
			startActivity(i);
			return true;
		}
	}
}