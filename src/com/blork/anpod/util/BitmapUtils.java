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
import android.graphics.Matrix;
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

	/** The Constant TAG. */
	private static final String TAG = "BitmapUtils";

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
		public void onFetchComplete(Object cookie, Bitmap result, Uri uri);
	}

	/**
	 * Only call this method from the main (UI) thread. The {@link OnFetchCompleteListener} callback
	 * be invoked on the UI thread, but image fetching will be done in an {@link AsyncTask}.
	 *
	 * @param context the context
	 * @param url the url
	 * @param name the name
	 * @param callback the callback
	 */
	public static void fetchImage(final Context context, final String url, final String name,
			final OnFetchCompleteListener callback) {
		fetchImage(context, url, name, null, null, callback);
	}

	/**
	 * Only call this method from the main (UI) thread. The {@link OnFetchCompleteListener} callback
	 * be invoked on the UI thread, but image fetching will be done in an {@link AsyncTask}.
	 *
	 * @param context the context
	 * @param url the url
	 * @param name the name
	 * @param decodeOptions the decode options
	 * @param cookie An arbitrary object that will be passed to the callback.
	 * @param callback the callback
	 */
	public static void fetchImage(final Context context, final String url, final String name,
			final BitmapFactory.Options decodeOptions,
			final Object cookie, final OnFetchCompleteListener callback) {
		new AsyncTask<String, Void, Bitmap>() {
			private Uri uri;

			@Override
			protected Bitmap doInBackground(String... params) {
				Bitmap bitmap = null;

				String slug = toSlug(name);
				Log.d("", "Fetching image");

				final String url = params[0];

				// First compute the cache key and cache file path for this URL
				File cacheFile = null;
				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
					Log.d("", "creating cache file");

					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

					if (prefs.getBoolean("archive", false)) {
						cacheFile = new File(
								Environment.getExternalStorageDirectory() 
								+ File.separator + "APOD" 
								+ File.separator + slug + ".jpg");
					} else {
						cacheFile = new File(
								Environment.getExternalStorageDirectory()
								+ File.separator + "Android"
								+ File.separator + "data"
								+ File.separator + "com.blork.anpod"
								+ File.separator + "cache"
								+ File.separator + slug + ".jpg");
					}

					uri = Uri.fromFile(cacheFile);
				} else {
					Log.d("", "SD card not mounted");
					Log.d("", "creating cache file");
					cacheFile = new File(
							context.getCacheDir() + File.separator + slug + ".jpg");
					uri = Uri.fromFile(cacheFile);
				}
				if (cacheFile != null && cacheFile.exists()) {
					Log.d("", "Cache file exists, using it.");
					Log.d("", cacheFile.getAbsolutePath());
					while (bitmap == null) {
						try {
							bitmap = BitmapFactory.decodeFile(cacheFile.toString(), decodeOptions);
						} catch (OutOfMemoryError e) {
							Log.d(Utils.TAG, "Out of memory..."+decodeOptions.inSampleSize);
							if (bitmap != null)
								bitmap.recycle();
							bitmap = null;
							System.gc();
							decodeOptions.inSampleSize += 2;
						}
					}
					return bitmap;
				}

				try {
					BitmapUtils.manageCache(slug, context);

					Log.d("", "Not cached, fetching");

					final HttpClient httpClient = SyncUtils.getHttpClient(
							context.getApplicationContext());
					final HttpResponse resp = httpClient.execute(new HttpGet(url));
					final HttpEntity entity = resp.getEntity();

					final int statusCode = resp.getStatusLine().getStatusCode();
					if (statusCode != HttpStatus.SC_OK || entity == null) {
						return null;
					}

					byte[] respBytes;

					try {
						respBytes = EntityUtils.toByteArray(entity);
					} catch (OutOfMemoryError e) {
						System.gc();
						respBytes = EntityUtils.toByteArray(entity);
					}

					Log.d("APOD", "Writing cache file " + cacheFile.getName());
					try {
						cacheFile.getParentFile().mkdirs();
						cacheFile.createNewFile();
						FileOutputStream fos = new FileOutputStream(cacheFile);
						fos.write(respBytes);
						fos.close();
					} catch (FileNotFoundException e) {
						Log.w(TAG, "Error writing to bitmap cache: " + cacheFile.toString(), e);
					} catch (IOException e) {
						Log.w(TAG, "Error writing to bitmap cache: " + cacheFile.toString(), e);
					}


					Log.d("", "Returning bitmap image");
					// Decode the bytes and return the bitmap.
					while (bitmap == null) {
						try {
							bitmap = BitmapFactory.decodeByteArray(respBytes, 0, respBytes.length, decodeOptions);
						} catch (OutOfMemoryError e) {
							Log.d(Utils.TAG, "Out of memory..."+decodeOptions.inSampleSize);
							if (bitmap != null)
								bitmap.recycle();
							bitmap = null;
							System.gc();
							decodeOptions.inSampleSize += 2;
						}
					}

				} catch (Exception e) {
					Log.w(TAG, "Problem while loading image: " + e.toString(), e);
				}

				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				callback.onFetchComplete(cookie, result, uri);
			}
		}.execute(url);
	}

	public static Bitmap fetchImage(Context context, Picture picture, int desiredWidth, int desiredHeight) {

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
			//uri = Uri.fromFile(cacheFile);
		}
		if (cacheFile != null && cacheFile.exists()) {
			Log.d("APOD", "Cache file exists, using it.");
			Bitmap cachedBitmap = resizeBitmap(cacheFile, desiredWidth, desiredHeight);
			return cachedBitmap;
		}

		try {
			Log.d("APOD", "Not cached, fetching");
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

			Bitmap bitmap = null;
			// Decode the bytes and return the bitmap.
			Log.d("APOD", "Reiszing bitmap image");

			bitmap = resizeBitmap(new ByteArrayInputStream(respBytes), desiredWidth, desiredHeight);
			Log.d("APOD", "Returning bitmap image");
			return bitmap;
		} catch (Exception e) {
			Log.d("APOD", "Problem while loading image: " + e.toString(), e);
		}

		return null;
	}

	public static void manageCache(String slug, Context context) {
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

		int cacheSize = 10;
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

	public static Bitmap resizeBitmap(File file, int desiredWidth, int desiredHeight) {
		try {
			return resizeBitmap(new FileInputStream(file), desiredWidth, desiredHeight);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static Bitmap resizeBitmap(InputStream stream, int desiredWidth, int desiredHeight) {
		try {
			// Get the source image's dimensions
			Log.d("APOD", "Getting dimensions");

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			BufferedInputStream bitmapStream = new BufferedInputStream(stream);
			bitmapStream.mark(Integer.MAX_VALUE);

			Log.d("APOD", "Decoding image (for dimensions)");
			BitmapFactory.decodeStream(bitmapStream, null, options);

			try {
				bitmapStream.reset();
			} catch (IOException e) {
				Log.d("APOD", "Can't reset bitstream", e);
			}

			int srcWidth = options.outWidth;
			int srcHeight = options.outHeight;

			Log.d("APOD", "Image is " + srcWidth + "x" + srcHeight);
			Log.d("APOD", "Desired size is " + desiredWidth + "x" + desiredHeight);


			// Only scale if the source is big enough. This code is just trying to fit a image into a certain width.
			if(desiredWidth > srcWidth) {
				desiredWidth = srcWidth;
			}

			// Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
			// from: http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
			int inSampleSize = 1;
			Log.d("APOD", (srcWidth / 2 > desiredWidth)+"");

			if (desiredWidth > 0) {
				while(srcWidth / 2 > desiredWidth){
					srcWidth /= 2;
					srcHeight /= 2;
					inSampleSize *= 2;
				}
			}

			Log.d("APOD", "Best sample size is " +inSampleSize);

			float desiredScale = (float) desiredWidth / srcWidth;

			Log.d("APOD", "Desired scale is " + desiredScale);


			// Decode with inSampleSize
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inSampleSize = inSampleSize;
			options.inScaled = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			Log.d("APOD", "Decoding stream (for reals)");
			Bitmap sampledSrcBitmap = BitmapFactory.decodeStream(bitmapStream, null, options);

			Log.d("APOD", "Resizing with matrix");
			Bitmap resizedBitmap = matrixResize(sampledSrcBitmap, desiredScale, srcWidth, srcHeight);
			
			sampledSrcBitmap.recycle();
			sampledSrcBitmap = null;
			Log.d("APOD", "Returning scaled bitmap");
			return resizedBitmap;
		} catch (Throwable e) {
			return resizeBitmap(stream, desiredWidth - (desiredWidth/4), desiredHeight - (desiredHeight/4));
		}
	}

	public static Bitmap resizeBitmap(Bitmap bitmap, int desiredWidth, int desiredHeight) {
		try {
			int srcWidth = bitmap.getWidth();
			int srcHeight = bitmap.getHeight();
			float desiredScale = (float) desiredWidth / srcWidth;
			// Resize
			Bitmap resizedBitmap = matrixResize(bitmap, desiredScale, srcWidth, srcHeight);
			bitmap.recycle();
			bitmap = null;
			return resizedBitmap;
		} catch (OutOfMemoryError e) {
			return resizeBitmap(bitmap, desiredWidth - (desiredWidth/4), desiredHeight - (desiredHeight/4));
		}
	}

	private static Bitmap matrixResize(Bitmap bitmap, float desiredScale, int srcWidth, int srcHeight) {
		Matrix matrix = new Matrix();
		matrix.postScale(desiredScale, desiredScale);
		Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, srcWidth, srcHeight, matrix, true);
		
//		bitmap.recycle();
//		bitmap = null;

		return scaledBitmap;
	}
}
