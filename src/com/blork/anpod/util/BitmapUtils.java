/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blork.anpod.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.blork.anpod.model.Picture;

// TODO: Auto-generated Javadoc
/**
 * Helper class for fetching and disk-caching images from the web.
 */
public class BitmapUtils {

	// TODO: for concurrent connections, DefaultHttpClient isn't great, consider other options
	// that still allow for sharing resources across bitmap fetches.

	/**
	 * The listener interface for receiving onFetchComplete events.
	 * The class that is interested in processing a onFetchComplete
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addOnFetchCompleteListener<code> method. When
	 * the onFetchComplete event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see OnFetchCompleteEvent
	 */
	public static interface OnFetchCompleteListener {

		/**
		 * On fetch complete.
		 *
		 * @param cookie the cookie
		 * @param result the result
		 */
		public void onFetchComplete(BitmapResult result);
	}

	/**
	 * Only call this method from the main (UI) thread. The {@link OnFetchCompleteListener} callback
	 * be invoked on the UI thread, but image fetching will be done in an {@link AsyncTask}.
	 *
	 * @param context the context
	 * @param desiredHeight 
	 * @param desiredWidth 
	 * @param url the url
	 * @param name the name
	 * @param decodeOptions the decode options
	 * @param cookie An arbitrary object that will be passed to the callback.
	 * @param callback the callback
	 */
	public static void fetchImage(final Context context, final Picture picture, final 
			int desiredWidth, final int desiredHeight, final OnFetchCompleteListener callback) {
		new AsyncTask<String, Void, BitmapResult>() {

			@Override
			protected BitmapResult doInBackground(String... params) {
				
				BitmapResult result = fetchImage(context, picture, desiredWidth, desiredHeight);

				return result;
			}

			@Override
			protected void onPostExecute(BitmapResult result) {
				callback.onFetchComplete(result);
			}
		}.execute(picture.getFullSizeImageUrl());
	}

	public static BitmapResult fetchImage(Context context, Picture picture, int desiredWidth, int desiredHeight) {

		// First compute the cache key and cache file path for this URL
		File cacheFile = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Log.d("APOD", "creating cache file");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

			if (prefs.getBoolean("archive", false)) {
				cacheFile = new File(
						Environment.getExternalStorageDirectory() 
						+ File.separator + "APOD" 
						+ File.separator + toSlug(picture.title) + ".jpg");
			} else {
				cacheFile = new File(
						Environment.getExternalStorageDirectory()
						+ File.separator + "Android"
						+ File.separator + "data"
						+ File.separator + "com.blork.anpod"
						+ File.separator + "cache"
						+ File.separator + toSlug(picture.title) + ".jpg");
			}
		} else {
			Log.d("APOD", "SD card not mounted");
			Log.d("APOD", "creating cache file");
			cacheFile = new File(
					context.getCacheDir() + File.separator + toSlug(picture.title) + ".jpg");
		}
				
		if (cacheFile != null && cacheFile.exists()) {
			Log.d("APOD", "Cache file exists, using it.");
			try {
				Bitmap bitmap = decodeStream(new FileInputStream(cacheFile), desiredWidth, desiredHeight);
				return new BitmapResult(bitmap, Uri.fromFile(cacheFile));
			} catch (FileNotFoundException e) {}
		}

		try {
			Log.d("APOD", "Not cached, fetching");
			BitmapUtils.manageCache(toSlug(picture.title), context);
			// TODO: check for HTTP caching headers
			final HttpClient httpClient = SyncUtils.getHttpClient(
					context.getApplicationContext());
			final HttpResponse resp = httpClient.execute(new HttpGet(picture.getFullSizeImageUrl()));
			final HttpEntity entity = resp.getEntity();

			final int statusCode = resp.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK || entity == null) {
				return null;
			}

			final byte[] respBytes = EntityUtils.toByteArray(entity);

			Log.d("APOD", "Writing cache file " + cacheFile.getName());
			try {
				cacheFile.getParentFile().mkdirs();
				cacheFile.createNewFile();
				FileOutputStream fos = new FileOutputStream(cacheFile);
				fos.write(respBytes);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d("APOD", "Error writing to bitmap cache: " + cacheFile.toString(), e);
			} catch (IOException e) {
				Log.d("APOD", "Error writing to bitmap cache: " + cacheFile.toString(), e);
			}

			// Decode the bytes and return the bitmap.
			Log.d("APOD", "Reiszing bitmap image");

			Bitmap bitmap = decodeStream(new ByteArrayInputStream(respBytes), desiredWidth, desiredHeight);
			Log.d("APOD", "Returning bitmap image");
			return new BitmapResult(bitmap, Uri.fromFile(cacheFile));
		} catch (Exception e) {
			Log.d("APOD", "Problem while loading image: " + e.toString(), e);
		}

		return null;
	}

	private static void manageCache(String slug, Context context) {
		String filename = slug + ".jpg";
		File folder;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			folder = new File(
					Environment.getExternalStorageDirectory()
					+ File.separator + "Android"
					+ File.separator + "data"
					+ File.separator + "com.blork.anpod"
					+ File.separator + "cache");
		} else {
			folder = context.getCacheDir();
		}

		int cacheSize = 15;
		Log.w("", "current file: " + filename);
		Log.w("", "Managing cache");
		File[] files = folder.listFiles();
		if (files == null || files.length <= cacheSize) {
			Log.w("", "Cache size is fine");
			return;
		}


		int count = files.length;

		Arrays.sort(files, new Comparator<File>(){
			public int compare(File f1, File f2) {
				return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
			} 
		});

		for (File f : files) {
			if (count > cacheSize && !filename.equals(f.getName())) {
				Log.w("", "Deleting " + f.getName());
				f.delete();
				count--;
			}
		}
	}

	private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
	private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

	public static String toSlug(String input) {
		String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
		String slug = NONLATIN.matcher(nowhitespace).replaceAll("");
		return slug.toLowerCase(Locale.ENGLISH);
	}
	
	private static Bitmap decodeStream(InputStream is, int desiredWidth, int desiredHeight){
	    Bitmap b = null;
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;

	        BufferedInputStream bis = new BufferedInputStream(is);
	        bis.mark(Integer.MAX_VALUE);
	        
	        BitmapFactory.decodeStream(bis, null, o);
	        bis.reset();
	        
	        int scale = 1;
	        if (o.outHeight > desiredHeight || o.outWidth > desiredWidth) {
	            scale = (int)Math.pow(2, (int) Math.round(Math.log(Math.max(desiredHeight, desiredWidth) / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
	        }

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        o2.inDither = true;
	        o2.inPurgeable = true;
	        
	        b = BitmapFactory.decodeStream(bis, null, o2);
	        bis.close();
	        is.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    return b;
	}
	
	public static class BitmapResult {
		private Bitmap bitmap;
		private Uri uri;

		public BitmapResult(Bitmap bitmap, Uri uri) {
			this.bitmap = bitmap;
			this.uri = uri;
		}
		
		public Bitmap getBitmap() {
			return bitmap;
		}

		public Uri getUri() {
			return uri;
		}
		
		public void recycle() {
			bitmap.recycle();
		}
	}
}
