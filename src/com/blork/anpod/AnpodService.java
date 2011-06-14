package com.blork.anpod;

import static android.provider.BaseColumns._ID;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.JSONException;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;


public class AnpodService extends Service implements Runnable {
	static final String ACTION_NEW_APOD = "ACTION_NEW_APOD"; 
	
	private boolean notify;
	private boolean weekly;

	private GoogleAnalyticsTracker tracker;
    private static String version = "";
    
    NotificationManager notificationManager;
   
    /** Called when the activity is first created. */
    @Override
    public void onCreate() {
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start("UA-20756745-1", this);
        tracker.trackPageView("/service");
        
    	notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	
    	try { 
    		PackageManager pm = getPackageManager();
			PackageInfo pi = pm.getPackageInfo("com.blork.anpod", PackageManager.GET_ACTIVITIES);
			version = pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	
    	Log.d(Apod.TAG, "Service running.");   	
    	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		notify = prefs.getBoolean("notifications_enabled", true);
		
		Boolean updates = prefs.getBoolean("updates_enabled", true);
		
		weekly = prefs.getBoolean("weekly_enabled", false);
		boolean wifi_only = prefs.getBoolean("wifi", false);
		
		Intent service = new Intent(this, AnpodService.class);
        PendingIntent sender = PendingIntent.getService(this, 0, service, 0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		
		if(updates || weekly){
	    	
	    	if(!IsDataEnabled()){
	    		Log.e(Apod.TAG, "Service | Background data not enabled");
	    		tracker.trackEvent(
	    	            "Error",  // Category
	    	            "Background data disabled",  // Action
	    	            "", // Label
	    	            0);       // Value
	    		
	    		int icon = R.drawable.warning;
				Notification notification = new Notification(icon, "Can't Update APOD", System.currentTimeMillis());
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, null, 0);
				notification.setLatestEventInfo(this, "Can't Update APOD", "You have background data usage turned off!", contentIntent);	
				notificationManager.notify(3, notification);
				
				tracker.dispatch();
	    		stopSelf();
	    	}

		} else {
			am.cancel(sender);
			Log.i(Apod.TAG, "Cancelling update.");
			
			tracker.dispatch();
			stopSelf();
		}

 		if (Apod.IsNetworkConnected(this)) {
 			if(wifi_only && !Apod.IsWiFiConnected(this)){
	 			stopSelf();
	 			Log.e(Apod.TAG, "Service | No wifi connection");
	    		
	 			int icon = R.drawable.warning;
				Notification notification = new Notification(icon, "Can't Update APOD", System.currentTimeMillis());
				Intent notificationIntent = new Intent(this, Retry.class); 
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
				notification.setLatestEventInfo(this, "No WiFi connection", "Click here to run the APOD update again.", contentIntent);
				notificationManager.notify(3, notification);
				sendBroadcast(new Intent(ACTION_NEW_APOD));
	 			stopSelf();
 			}else{
 				Thread thread = new Thread(this);
	 	        thread.start();
 			}   
 		}else{
 			Log.e(Apod.TAG, "Service | No network connection");
    		tracker.trackEvent(
    	            "Error",  // Category
    	            "No network connection",  // Action
    	            "", // Label
    	            0);       // Value
    		
 			int icon = R.drawable.warning;
			Notification notification = new Notification(icon, "Can't Update APOD", System.currentTimeMillis());
			
			Intent notificationIntent = new Intent(this, Retry.class); 
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			notification.setLatestEventInfo(this, "Connection problem", "Click here to run the APOD update again.", contentIntent);
			
			notificationManager.notify(3, notification);
			
			sendBroadcast(new Intent(ACTION_NEW_APOD));
			
			tracker.dispatch();
 			stopSelf();
 			
 			
 		}

    }

    private boolean IsDataEnabled(){
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	return connManager.getBackgroundDataSetting();
    }

    
	public void run() {
		
		try {
			PendingIntent contentIntent = null;
			Notification notification = null;
			if(notify){
				//nm.cancelAll();
				
				int icon = android.R.drawable.stat_notify_sync;
				notification = new Notification(icon, "Checking for new APOD", System.currentTimeMillis());
				Intent syncIntent = new Intent(this, Main.class);
				contentIntent = PendingIntent.getActivity(this, 0, syncIntent, 0);
				notification.setLatestEventInfo(this, "Checking for new APOD", "Checking for new Astronomy Picture of the Day.", contentIntent);
				notification.flags = Notification.FLAG_ONGOING_EVENT^Notification.FLAG_NO_CLEAR;	
				notificationManager.notify(3, notification);
			}
			try {
				ApodData data = new ApodData(this);
				Apod apod = new Apod(weekly, data);
				apod.context = this;
							
				if(apod.isNew() && (apod.youtube == null || apod.youtube.equals("null"))){
					if(notify){
						int icon = android.R.drawable.stat_sys_download;
						notification = new Notification(icon, "Downloading APOD", System.currentTimeMillis());
						Intent syncIntent = new Intent(this, Main.class);
						contentIntent = PendingIntent.getActivity(this, 0, syncIntent, 0);
						notification.setLatestEventInfo(this, "Downloading APOD", "Downloading Astronomy Picture of the Day.", contentIntent);
						notificationManager.notify(3, notification);
					}
					
					WallpaperManager wm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
					
					int wallpaperWidth = wm.getDesiredMinimumWidth();
					
					apod.getImage(wallpaperWidth, weekly);
					
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
					boolean wallpaper = prefs.getBoolean("wallpaper_auto_enabled", true);
					
					boolean sd = prefs.getBoolean("sd_card", true);
					
					apod.save(sd, false);
										
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, apod.imageUri));
					
					if(wallpaper){
						//Log.d(Apod.TAG, "Image size: "+apod.image.getWidth()+"x"+apod.image.getHeight());

						if(notify){
							notification.setLatestEventInfo(this, "Setting APOD as wallpaper", "Setting Astronomy Picture of the Day Wallpaper.", contentIntent);
							notificationManager.notify(3, notification);
						}
						
						Log.d(Apod.TAG, "Setting wallpaper.");
						
						ContentResolver cr = getContentResolver();
						
						InputStream is;
						
						try {
							is = cr.openInputStream(apod.imageUri);
							Log.d(Apod.TAG, "Using image from SD");
						} catch (IOException e) {
							Log.w(Apod.TAG, "SD Card not present; using image from internal storage.");
							is = openFileInput("apod.jpg");
						}
						
						wm.setStream(is);
						
						Log.d(Apod.TAG, "Wallpaper set.");
					}
										
		    		tracker.trackEvent(
		    	            "Update",  // Category
		    	            "Sucessful update",  // Action
		    	            "", // Label
		    	            0);       // Value
					
					if(notify){
						int icon = R.drawable.notify;
						notification = new Notification(icon, "New Picture!", System.currentTimeMillis());
						notification.flags = Notification.FLAG_AUTO_CANCEL;
						Intent notificationIntent = new Intent(this, Today.class); 
						contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
						notification.setLatestEventInfo(this, "New Astronomy Pic of the Day", apod.title, contentIntent);
						notificationManager.cancelAll();
						notificationManager.notify(1, notification); 
					}	
				} else if(apod.isNew() && (apod.youtube != null || !apod.youtube.equals("null"))){
					apod.saveYoutube();
					
					int icon = R.drawable.notify;
					notification = new Notification(icon, "New Video!", System.currentTimeMillis());
					notification.flags = Notification.FLAG_AUTO_CANCEL;
					Intent notificationIntent = new Intent(this, Today.class); 
					contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
					notification.setLatestEventInfo(this, "New Astronomy Video of the Day", apod.title, contentIntent);
					notificationManager.notify(1, notification);
				} else {
					if(notify){
		    			Log.e(Apod.TAG, "Service | No new image");
			    		tracker.trackEvent(
			    	            "Update",  // Category
			    	            "No new image",  // Action
			    	            "", // Label
			    	            0);       // Value
			    		
						Log.i(Apod.TAG, "No new Image");
						int icon = R.drawable.warning;
						notification = new Notification(icon, "No New Picture", System.currentTimeMillis());
						Intent notificationIntent = new Intent(this, Today.class); 
						contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
						notification.setLatestEventInfo(this, "No New Picture", "The APOD has not been updated yet. Try changing the update time.", contentIntent);
						notificationManager.notify(2, notification);
					}
				}
			} catch(IOException e1){
				if(notify){
					e1.printStackTrace();
					
					Log.e(Apod.TAG, "Service | Couldn't connect to server - DNS problem?");
		    		tracker.trackEvent(
		    	            "Error",  // Category
		    	            "Couldn't connect to server",  // Action
		    	            getStackTraceAsString(e1), // Label
		    	            0);       // Value
					
					int icon = R.drawable.warning;
					notification = new Notification(icon, "APOD couldn't find the server", System.currentTimeMillis());
					notification.flags = Notification.FLAG_AUTO_CANCEL;
									
					Intent notificationIntent = new Intent(this, Retry.class); 
					contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
					notification.setLatestEventInfo(this, "Connection problem", "Click here to run the APOD update again.", contentIntent);
					
					notificationManager.notify(2, notification);
				}
			} catch (ClassCastException e1){
				if(notify){
					e1.printStackTrace();
					
					Log.e(Apod.TAG, "Service | Couldn't connect to server - ClassCast");
					tracker.trackEvent(
		    	            "Error",  // Category
		    	            "Couldn't connect to server",  // Action
		    	            getStackTraceAsString(e1), // Label
		    	            0);       // Value
					
					int icon = R.drawable.warning;
					notification = new Notification(icon, "APOD couldn't get the latest info", System.currentTimeMillis());
					notification.flags = Notification.FLAG_AUTO_CANCEL;
									
					Intent notificationIntent = new Intent(this, Retry.class); 
					contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
					notification.setLatestEventInfo(this, "Server problem", "Retrying in 30 minutes, or click here.", contentIntent);
					
					notificationManager.notify(2, notification);
				}
				AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
				Intent service = new Intent(this, AnpodService.class);
		        PendingIntent sender = PendingIntent.getService(this, 0, service, 0);

		        long triggerTime = SystemClock.elapsedRealtime() + 1800000; //Current time + 30 minutes
			    am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, sender);  

			    Log.d(Apod.TAG, "Updates rescheduled.");	
			    
			} catch (JSONException e1){
				if(notify){
					e1.printStackTrace();
					
					Log.e(Apod.TAG, "Service | Couldn't get info - JSON problem?");
					tracker.trackEvent(
		    	            "Error",  // Category
		    	            "Couldn't decode JSON",  // Action
		    	            getStackTraceAsString(e1), // Label
		    	            0);       // Value
					
					
					int icon = R.drawable.warning;
					notification = new Notification(icon, "APOD couldn't get the latest info", System.currentTimeMillis());
					notification.flags = Notification.FLAG_AUTO_CANCEL;
									
					Intent notificationIntent = new Intent(this, Retry.class); 
					contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
					notification.setLatestEventInfo(this, "Server problem", "Retrying in 30 minutes, or click here.", contentIntent);
					
					notificationManager.notify(2, notification);
				}
				AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
				Intent service = new Intent(this, AnpodService.class);
		        PendingIntent sender = PendingIntent.getService(this, 0, service, 0);

		        long triggerTime = SystemClock.elapsedRealtime() + 1800000; //Current time + 30 minutes
			    am.set(AlarmManager.ELAPSED_REALTIME, triggerTime, sender);  

			    Log.d(Apod.TAG, "Updates rescheduled.");	
			    
			} catch (Throwable e1) {	
				e1.printStackTrace();
				
				Log.e(Apod.TAG, "Service | Unknown");
				tracker.trackEvent(
	    	            "Error",  // Category
	    	            "Unknown",  // Action
	    	            getStackTraceAsString(e1), // Label
	    	            0);       // Value
				
				int icon = R.drawable.warning;
				notification = new Notification(icon, "APOD encountered a problem.", System.currentTimeMillis());
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				
				Intent emailIntent = new Intent(Intent.ACTION_SEND); 
				emailIntent.setType("message/rfc822");
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"sam+market@blork.co.uk"}); 
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with APOD Android App"); 
				String stacktrace = getStackTraceAsString(e1);
				emailIntent.putExtra(Intent.EXTRA_TEXT, stacktrace); 
				contentIntent = PendingIntent.getActivity(this, 0, emailIntent, 0);
				notification.setLatestEventInfo(this, "APOD has encountered a problem. Sorry!", "Click here to email me the debug info.", contentIntent);
				notificationManager.notify(2, notification);
				
			}
		} finally{
			sendBroadcast(new Intent(ACTION_NEW_APOD));
    		notificationManager.cancel(3);
    		
    		ApodData data = new ApodData(this);
    		SQLiteDatabase db = data.getReadableDatabase();
    		
    		try {
    			Log.d(Apod.TAG, "Updating widget");

    			Cursor cursor = db.query("apod", new String[] {_ID, "title, credit, date, uri"}, 
    			        null, null, null, null, "date DESC");
    			
    			cursor.moveToFirst(); 

    			String title = (cursor.getString(cursor.getColumnIndex("title")));
    			String credit = (cursor.getString(cursor.getColumnIndex("credit")));
    			
				try {
					RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget);
					ComponentName thisWidget = new ComponentName(this, Widget.class);
					views.setViewVisibility(R.id.content, View.VISIBLE);
					views.setViewVisibility(R.id.loading, View.GONE);
					views.setTextViewText(R.id.title, title);
					views.setTextViewText(R.id.credit, credit);
				
					Intent qIntent = new Intent(this, Today.class);
					PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, qIntent, 0);
					views.setOnClickPendingIntent(R.id.content, pendingIntent);
					AppWidgetManager.getInstance(this).updateAppWidget(thisWidget, views);
				} catch (Exception e) {
					e.printStackTrace();
				} 
				

				try {
					RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.four_by_four_widget);
					ComponentName thisWidget = new ComponentName(this, FourByFourWidget.class);
					views.setViewVisibility(R.id.content, View.VISIBLE);
					views.setViewVisibility(R.id.loading, View.GONE);
					views.setTextViewText(R.id.title, title);
					views.setTextViewText(R.id.credit, credit);
					//views.setTextViewText(R.id.info, info);
					try {
						final String uri = cursor.getString(cursor.getColumnIndex("uri"));
									
						ContentResolver cr = getContentResolver();
						InputStream is = cr.openInputStream(Uri.parse(uri));
						
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inDither = true;
						options.inSampleSize = 2;
						
						Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
						views.setImageViewBitmap(R.id.image, bitmap);

					} catch (Exception e) {
						Log.e(Apod.TAG, "Service | Couldn't set bitmap to imageview");
						tracker.trackEvent(
			    	            "Error",  // Category
			    	            "Couldn't set image to view",  // Action
			    	            getStackTraceAsString(e), // Label
			    	            0);       // Value
						
						e.printStackTrace();
					}
					
					Intent qIntent = new Intent(this, Today.class);
					PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, qIntent, 0);
					views.setOnClickPendingIntent(R.id.content, pendingIntent);
					AppWidgetManager.getInstance(this).updateAppWidget(thisWidget, views);
				} catch (Exception e) {
					e.printStackTrace();
				} 				
				
    		} catch (Exception e) {
    			e.printStackTrace();
    			Log.e(Apod.TAG, "Service | No image downloaded");
    			tracker.trackEvent(
	    	            "Error",  // Category
	    	            "No image downloaded",  // Action
	    	            getStackTraceAsString(e), // Label
	    	            0);       // Value
    			
    		} finally {
    			db.close();
    		}
    		
    		tracker.dispatch();
			stopSelf();
		}	
		
		tracker.dispatch();
		stopSelf();
	}

	public void onDestroy(){
		super.onDestroy();
		tracker.stop();
		Log.i(Apod.TAG, "service finished");
	}
	
	public static String getStackTraceAsString(Throwable t){
		StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true); 
        t.printStackTrace(pw);
        pw.flush();
        sw.flush(); 
        String stacktrace = sw.toString();
        String deviceinfo = 
        	Build.BRAND+" "+
	        Build.MANUFACTURER+" "+
	        Build.DEVICE+" "+
	        Build.MODEL+"\n"+
	        "Android version: "+Build.VERSION.SDK_INT+"\n"+
	        Build.DISPLAY;
        
        return stacktrace+"\n"+deviceinfo+"\n"+"App version: "+version;
    }
	
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}