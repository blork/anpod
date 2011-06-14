package com.blork.anpod;

import static android.provider.BaseColumns._ID;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class Apod {
	public static final String TAG = "anpod";
	
	public static final String url = "http://astronomypictureoftheday.appspot.com/";
	//public static final String url = "http://10.0.2.2:8080/"; 
	public String title, credit, info;
	public Context context;
	private ApodData data;
	public Bitmap image;
	public Uri imageUri = Uri.parse("");
	public String youtube;
	public String date;
	private Boolean weekly;
	
	public Apod(Boolean weekly, ApodData data) throws MalformedURLException, ClassCastException, JSONException, IOException, URISyntaxException, InterruptedException{
		this.data = data;
		this.weekly = weekly;
		this.getText();
	}
	
	public Apod(String title, ApodData data) { 
		this.data = data;
		this.title = title;
	}
	

	public Apod(JSONObject apodJson) throws JSONException {

		this.title = apodJson.getString("title");
		this.credit = apodJson.getString("credit");
		this.info = apodJson.getString("info");
		this.date = apodJson.getString("date");
		
		try{
			this.youtube = apodJson.getString("youtube");
		}catch(Throwable t){}
		
		//Log.d(Apod.TAG, title);
	}

	public void getText() throws MalformedURLException, JSONException, IOException, URISyntaxException, ClassCastException, InterruptedException{
		String jsonString;
		
		if(weekly){
			jsonString = Apod.url+"bestJson";
		} else {
			jsonString = Apod.url+"json";
		}
		
		URL jsonUrl = new URL(jsonString);
		
		JSONObject json;
		
		
		String allJson = Apod.getJSON(jsonUrl);
		JSONTokener jsonTokener = new JSONTokener(allJson);
		json = (JSONObject) jsonTokener.nextValue();

        Log.i(Apod.TAG, "Parsing JSON.");
		this.title = json.getString("title");
		this.credit = json.getString("credit");
		this.info = json.getString("info");
		try {
			this.youtube = json.getString("youtube");
		} catch (JSONException e) {
			this.youtube = "null";
		}
		Log.d(Apod.TAG, this.youtube);
	}
	
	public void getImage(int wallpaperWidth, boolean weekly) throws OutOfMemoryError, MalformedURLException, IOException{
		Log.i(Apod.TAG, "Image download size: "+wallpaperWidth);
		this.image = Apod.getBitmap(wallpaperWidth, weekly);
	}
	

	
	public Boolean isNew(){
		Log.i(Apod.TAG,"checking if new");
		
		SQLiteDatabase db = null;
		
		int tries = 0;
		while(db == null || tries  < 5){
			db = this.data.getReadableDatabase();
			tries++;
		}
		
		String oldTitle = "";
		
		Cursor cursor = null;
		try {
			cursor = db.query("apod", new String[] {_ID, "title, date"}, 
			        null, null, null, null, "date DESC");	
			cursor.moveToFirst(); 
			oldTitle = cursor.getString(cursor.getColumnIndex("title"));
		} catch (Exception e) {
			return true;
		} finally {
			db.close();
			cursor.close();
		}
		
		if(this.title.equals(oldTitle)){
			return false;
		} else {
			return true;
		}
		
	}
	public void saveYoutube(){
		Log.d(Apod.TAG, "Saving info.");

		SQLiteDatabase db = this.data.getWritableDatabase();
		
		ContentValues values = new ContentValues();

		values.put("title", this.title);
		values.put("credit", this.credit);
		values.put("info", this.info);
		values.put("youtube_url", this.youtube);
		
		db.insertOrThrow("apod", null, values);
		
		db.close();
		
	}	
	
	public void save(){
		save(false, false, true, false);
	}
	
	public void save(Boolean sd, Boolean update){
		save(true, true, sd, update);
	}
	
	public void save(Boolean db, Boolean internal, Boolean sd, Boolean update){
		Log.d(Apod.TAG, "Saving info.");

		boolean sdAvailable = false;
		boolean sdWriteable = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			sdAvailable = sdWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			sdAvailable = true;
			sdWriteable = false;
		} else {
		    sdAvailable = sdWriteable = false;
		}
		
		if(internal){
			try{		
				Log.i(Apod.TAG, "Saving image to internal storage");
				
				String fileName = "apod.jpg";
				
				FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
				BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
				this.image.compress(CompressFormat.JPEG, 75, bos);
				bos.flush();
				bos.close();
				
				File file = new File(this.context.getFilesDir().getPath()+"/"+fileName);
							
				this.imageUri = Uri.fromFile(file);
	
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		if(sdAvailable && sdWriteable && sd){
			Log.i(Apod.TAG, "SD card is writable");
			
			try{			
				File dir = new File(Environment.getExternalStorageDirectory().toString()+"/APOD");
				dir.mkdir();
				
				File imageFile = new File(dir.toString()+"/"+URLEncoder.encode(title)+".jpg");
				
				Log.d(Apod.TAG, imageFile.toString());
				
				FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
				BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
				
				Log.d(Apod.TAG, image.toString());
				
				this.image.compress(CompressFormat.JPEG, 75, bos);
				bos.flush();
				bos.close();
				
				this.imageUri = Uri.fromFile(imageFile);

			}catch(Exception e){
				e.printStackTrace();
			}
	
		}else{
			Log.i(Apod.TAG,"SD is not writable!");
		}
		 
		
		if(db){
			saveToDB(update);
		}
	}
	

	private void saveToDB(Boolean update) {
		SQLiteDatabase db = this.data.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		
		if(update){
			values.put("uri", this.imageUri.toString());
			db.update("apod", values, "title = \""+this.title+"\"", null);
		}else{
			values.put("title", this.title);
			values.put("credit", this.credit);
			values.put("info", this.info);
			values.put("uri", this.imageUri.toString());
			
			Boolean saved = false;
			while(!saved){
				try {
					db.insertOrThrow("apod", null, values);
					saved = true;
				} catch (SQLException e) {
					e.printStackTrace();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			} 
		}
		db.close();
	}

	public static String getJSON(URL url) throws IOException, URISyntaxException {
		InputStream instream = Apod.getStream(url);
		        
        return Apod.streamToString(instream);
    }
	
	public static int getCurrentAppVersion() throws IOException, URISyntaxException {
		InputStream instream = Apod.getStream(new URL(Apod.url+"version"));
		return Integer.parseInt(Apod.streamToString(instream));
    }

	public static String streamToString(InputStream instream) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
		final StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
	}
	
    public static Bitmap getBitmap(int wallpaperWidth, boolean weekly) throws IOException, OutOfMemoryError{
    	return Apod.getBitmap(wallpaperWidth, null, weekly);
    }
    
    public static Bitmap getBitmap(int wallpaperWidth, String date) throws IOException, OutOfMemoryError{
    	return Apod.getBitmap(wallpaperWidth, date, false);
    }
    
    public static Bitmap getBitmap(int wallpaperWidth, String date, boolean weekly) throws IOException, OutOfMemoryError{
    	Log.i(Apod.TAG, "Desired wallpaper size = "+wallpaperWidth);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = true;
				
		Log.i(Apod.TAG, "Desired wallpaper size = "+wallpaperWidth);
		
		String size = "large";
		
		if(wallpaperWidth < 500){
			size = "small";
		}else if(wallpaperWidth < 700){
			size = "normal";
		}else{
			size = "large";
		}
    	
		URL url;
		String urlString = Apod.url;
		
		if(weekly){
			urlString = urlString+"best";
		}else{
			urlString = urlString+"image";
		}
		
		urlString = urlString+"?size="+size;
		
		if(date != null){
			urlString = urlString+"&date="+date;
		}
		
		url = new URL(urlString);
        	
     	Bitmap bitmap;
     	InputStream instream = null;
		try {
			instream = Apod.getStream(url);
			bitmap = BitmapFactory.decodeStream(instream, null, options);
			return bitmap;
		} catch (OutOfMemoryError t) {
			System.gc();

			
			t.printStackTrace();
			try {
				instream.close();
			} catch (Exception e) {
				//pass
			}
			instream = Apod.getStream(url);
			options.inSampleSize = 4;
			bitmap = BitmapFactory.decodeStream(instream, null, options);
			return bitmap;
		}
    }
    
    private static InputStream getStream(URL url) throws ClientProtocolException, IOException, OutOfMemoryError{
    	 HttpGet httpRequest = null;

         try {
         	httpRequest = new HttpGet(url.toURI());
         	httpRequest.removeHeaders("User-Agent");
         	httpRequest.setHeader("Accept-Encoding", "gzip");
         	httpRequest.setHeader( "Pragma", "no-cache" );
         	httpRequest.setHeader( "Cache-Control", "no-cache" );
         	httpRequest.setHeader( "Expires", "0" );
         } catch (URISyntaxException e) {
         	e.printStackTrace();
         }

 		HttpClient httpclient = new DefaultHttpClient();
 		HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
 		
 		HttpEntity entity = response.getEntity();
 		BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity); 
 		InputStream instream = bufHttpEntity.getContent();
 		
 		Header contentEncoding = response.getFirstHeader("Content-Encoding");
 		
 		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
 		    instream = new GZIPInputStream(instream);
 		    Log.d(Apod.TAG, "Gzipped");
 		}
 		
 		return instream;
    }
    
    public static boolean IsNetworkConnected(Context context) {
	  	boolean result = false;
	  	ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	  	NetworkInfo info = connManager.getActiveNetworkInfo();
	  	if (info == null || !info.isConnected()) {
	  		result = false;
	  	} else {
	  		if (!info.isAvailable()) {
	  			result = false;
	  		} else {
	  			result = true;
	  		}
	  	}
	  	return result;
  	}
    
    public static boolean IsWiFiConnected(Context context) {
	  	ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	  	NetworkInfo info = connManager.getActiveNetworkInfo();
	  	
	  	int netType = info.getType();
	  	if (netType == ConnectivityManager.TYPE_WIFI) {
	  	    return info.isConnected();
	  	} else {
	  		return false;
	  	}
  	}
}



