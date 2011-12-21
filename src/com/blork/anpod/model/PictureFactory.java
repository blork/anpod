package com.blork.anpod.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.blork.anpod.provider.PicturesContentProvider;
import com.blork.anpod.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating Picture objects.
 */
public class PictureFactory {

	/**
	 * Gets the todays picture.
	 *
	 * @param ctx the ctx
	 * @return the todays picture
	 * @throws JSONException the jSON exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 */
	public static final Picture getTodaysPicture(Context ctx) throws JSONException, IOException, URISyntaxException {
		URL url = new URL("http://anpod.heroku.com/latest.json");	
		Picture p = getPictureFromUrl(ctx, url);
		return p;
	}

	/**
	 * Gets the picture.
	 *
	 * @param ctx the ctx
	 * @param id the id
	 * @return the picture
	 * @throws JSONException the jSON exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 */
	public static final Picture getPicture(Context ctx, String id) throws JSONException, IOException, URISyntaxException {
		URL url = new URL("http://anpod.heroku.com/pictures_by_uid/"+ id +".json");	
		return getPictureFromUrl(ctx, url);
	}

	/**
	 * Gets the local pictures.
	 *
	 * @param ctx the ctx
	 * @return the local pictures
	 */
	public static List<Picture> getLocalPictures(Context ctx) {
		List<Picture> pictures = new ArrayList<Picture>();
		
		// Form an array specifying which columns to return. 
		String[] projection = new String[] {
				PicturesContentProvider.ID,
				PicturesContentProvider.TITLE,
				PicturesContentProvider.CREDIT,
				PicturesContentProvider.IMGURURL,
				PicturesContentProvider.INFO,
				PicturesContentProvider.UID,
		};

		Log.d("!!!", "Making query");

		ContentResolver resolver = ctx.getContentResolver();

		// Make the query. 
		Cursor cursor = resolver.query(
				PicturesContentProvider.CONTENT_URI,
				projection, // Which columns to return 
				null,       // Which rows to return (all rows)
				null,       // Selection arguments (none)
				PicturesContentProvider.REVERSE_SORT_ORDER // Put the results in default order
		); 

		Log.d("!!!", "Cursor!");
		if (cursor.moveToFirst()) {
			Log.d("!!!", "Cursor worked!");

			String title;
			String credit;
			String imgur;
			String info;
			String uid;
			int id;

			int titleColumn = cursor.getColumnIndex(PicturesContentProvider.TITLE); 
			int creditColumn = cursor.getColumnIndex(PicturesContentProvider.CREDIT); 
			int imgurColumn = cursor.getColumnIndex(PicturesContentProvider.IMGURURL); 
			int infoColumn = cursor.getColumnIndex(PicturesContentProvider.INFO); 
			int uidColumn = cursor.getColumnIndex(PicturesContentProvider.UID); 
			int idColumn = cursor.getColumnIndex(PicturesContentProvider.ID); 

			do {
				title = cursor.getString(titleColumn);
				credit = cursor.getString(creditColumn);
				imgur = cursor.getString(imgurColumn);
				info = cursor.getString(infoColumn);
				uid = cursor.getString(uidColumn);
				id = cursor.getInt(idColumn);

				pictures.add(new Picture(id, uid, title, credit, info, imgur));

			} while (cursor.moveToNext());
		}


		cursor.close();

		return pictures;
	}


	//	public static Picture getLatestPicture(Context ctx) {
	//		Picture picture = Picture.querySingle(
	//				ctx, 
	//				Picture.class, 
	//				null, 
	//				null, 
	//				"Id DESC"
	//		);
	//		return picture;
	//	}

	/**
	 * Gets the picture from url.
	 *
	 * @param ctx the ctx
	 * @param url the url
	 * @return the picture from url
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws JSONException the jSON exception
	 */
	private static final Picture getPictureFromUrl(Context ctx, URL url) throws IOException, URISyntaxException, JSONException {

		String jsonString = Utils.getJSON(url);

		JSONTokener jsonTokener = new JSONTokener(jsonString);
		JSONObject json = (JSONObject) jsonTokener.nextValue();
		json = json.getJSONObject("picture");

		int aid = json.getInt("id");
		String uid = json.getString("uid");
		String title = json.getString("title");
		String credit = json.getString("credit");
		String info = json.getString("explanation");
		String imgurId = json.getString("url");

		return new Picture(aid, uid, title, credit, info, imgurId);
	}

	public static List<Picture> load() throws MalformedURLException, IOException, URISyntaxException, JSONException {
		return load(null);
	}

	/**
	 * Load.
	 *
	 * @param ctx the ctx
	 * @param uid the uid
	 * @return the list
	 * @throws MalformedURLException the malformed url exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws JSONException the jSON exception
	 */
	public static List<Picture> load(Integer id) throws MalformedURLException, IOException, URISyntaxException, JSONException {
		URI uri = null;

		if (id == null) {
			uri = new URI(
					"http", 
					"anpod.heroku.com", 
					"/pictures.json",
					null);			
		} else {
			uri = new URI(
					"http", 
					"anpod.heroku.com", 
					"/pictures.json",
					"last="+id,
					null);
		}

		Log.d("", uri.toString());

		String jsonString = Utils.getJSON(uri.toURL());
		JSONTokener jsonTokener = new JSONTokener(jsonString);

		JSONArray json = (JSONArray) jsonTokener.nextValue();
		Log.d("", json.toString());
		return jsonArrayToPictures(json);		
	}

	/**
	 * Json array to pictures.
	 *
	 * @param ctx the ctx
	 * @param json the json
	 * @return the list
	 * @throws JSONException the jSON exception
	 */
	private static List<Picture> jsonArrayToPictures(JSONArray json) throws JSONException {
		ArrayList<Picture> pictures = new ArrayList<Picture>();

		int arrayLength = json.length();

		for (int i = 0; i < arrayLength; i++) {
			JSONObject topJson = (JSONObject) json.get(i);

			JSONObject pictureJson = topJson.getJSONObject("picture");

			int aid = pictureJson.getInt("id");
			String uid = pictureJson.getString("uid");
			String title = pictureJson.getString("title");
			String credit = pictureJson.getString("credit");
			String info = pictureJson.getString("explanation");
			String imgurId = pictureJson.getString("url");

			pictures.add(new Picture(aid, uid, title, credit, info, imgurId));
		}

		return pictures;
	}

	/**
	 * Search.
	 *
	 * @param ctx the ctx
	 * @param query the query
	 * @return the list
	 * @throws MalformedURLException the malformed url exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws JSONException the jSON exception
	 */
	public static List<Picture> search(String query, int page) throws MalformedURLException, IOException, URISyntaxException, JSONException {
		URI uri = new URI(
				"http", 
				"anpod.heroku.com", 
				"/search.json",
				"query="+query+"&page="+page,
				null);

		String jsonString = Utils.getJSON(uri.toURL());
		JSONTokener jsonTokener = new JSONTokener(jsonString);
		JSONArray json = (JSONArray) jsonTokener.nextValue();
		return jsonArrayToPictures(json);
	}

	public static List<Picture> search(String query) throws MalformedURLException, IOException, URISyntaxException, JSONException {
		return search(query, 1);
	}

	/**
	 * Save all. Returns number of new pictures.
	 * 
	 * @param context
	 * @param pictures
	 * @return Number of new pictures added.
	 */
	public static int saveAll(Context context, List<Picture> pictures) {
		int count = 0;
		for (Picture p: pictures) {
			Log.d("saving", p.title);
			Uri uri = p.save(context);
			if (uri != null) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Delete all.
	 *
	 * @param ctx the ctx
	 */
	public static void deleteAll(Context ctx) {
		ContentResolver resolver = ctx.getContentResolver();
		resolver.delete(PicturesContentProvider.CONTENT_URI, null, null);
	}

}
