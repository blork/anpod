package com.blork.anpod.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.blork.anpod.R;
import com.blork.anpod.activity.HomeActivity;
import com.blork.anpod.model.Picture;
import com.blork.anpod.model.PictureFactory;
import com.blork.anpod.util.BitmapUtils;
import com.blork.anpod.util.UIUtils;
import com.blork.anpod.util.Utils;
import com.blork.anpod.widget.Widget;


// TODO: Auto-generated Javadoc
/**
 * The Class AnpodService.
 */
public class AnpodService extends Service implements Runnable{

	public static final String ACTION_FINISHED_UPDATE = "com.blork.anpod.ACTION_FINISHED_UPDATE";
	private NotificationManager notificationManager;
	private boolean notify;
	private Boolean silent;
	private boolean wallpaper;
	private Handler handler;

	static int INFO = 1;
	static int ERROR = 2;
	static int RUNNING = 3;

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		silent = intent.hasExtra("silent_run");
		return START_STICKY;
	}

	@Override
	public void onCreate() {

		handler = new Handler();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		long time = System.currentTimeMillis();
		Editor editor = prefs.edit();
		editor.putLong("time", time);
		editor.commit();

		if (!Utils.isDataEnabled(this)) {
			finish();
			return;
		}

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		notify = prefs.getBoolean("notifications_enabled", true);
		wallpaper = prefs.getBoolean("set_wallpaper", true);


		boolean updates = prefs.getBoolean("updates_enabled", false);
		boolean wifi_only = prefs.getBoolean("wifi", false);

		if (!updates) {
			finish();
			return;
		}


		if (Utils.isNetworkConnected(this)) {

			if (wifi_only && !Utils.isWiFiConnected(this)) {
				finish();
				return;
			} 

			new Thread(this).start();
		}


	}

	@Override
	public void run() {

		while(silent == null) {
			continue;
		}

		silent = silent && UIUtils.isHoneycombTablet(this);

		if (notify && !silent) {
			int icon = android.R.drawable.stat_notify_sync;
			Notification notification = new Notification(icon, "Checking for new APOD", System.currentTimeMillis());
			Intent syncIntent = new Intent(this, HomeActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, syncIntent, 0);

			notification.setLatestEventInfo(this, "Checking for new APOD", "Checking for new Astronomy Picture of the Day.", contentIntent);
			notification.flags = Notification.FLAG_ONGOING_EVENT^Notification.FLAG_NO_CLEAR;	
			notificationManager.notify(RUNNING, notification);
		}

		List<Picture> pictures = new ArrayList<Picture>();

		try {
			pictures.addAll(PictureFactory.load());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (pictures.isEmpty()) {
			finish();
			return;
		}

		Picture newPicture = pictures.get(0);

		RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget);
		ComponentName thisWidget = new ComponentName(this, Widget.class);
		views.setViewVisibility(R.id.content, View.VISIBLE);
		views.setViewVisibility(R.id.loading, View.GONE);
		views.setTextViewText(R.id.title, newPicture.title);
		views.setTextViewText(R.id.credit, newPicture.credit);

		Intent qIntent = new Intent(this, HomeActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, qIntent, 0);
		views.setOnClickPendingIntent(R.id.content, pendingIntent);
		AppWidgetManager.getInstance(this).updateAppWidget(thisWidget, views);
		
		int count = PictureFactory.saveAll(this, pictures);

		if (count == 0) {
			finish();
			return;
		} else if (count > 10) {
			/*
			 * If there are 10 new pictures, we haven't updated for a while.
			 * Instead of doing a twitter style broken list (hard), just get rid of
			 * all the old pics.
			 */
			PictureFactory.deleteAll(this);
			PictureFactory.saveAll(this, pictures);
		}

		Log.e("", "about to set wallpaper");
		if (wallpaper) {
			Log.e("", "setting wallpaper");
			BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
			decodeOptions.inSampleSize = 2;


			WallpaperManager wm = (WallpaperManager) this.getSystemService(Context.WALLPAPER_SERVICE);

			int newWidth = wm.getDesiredMinimumWidth();
			int newHeight = wm.getDesiredMinimumHeight();

			try {
				Bitmap bitmap = BitmapUtils.fetchImage(this, newPicture, decodeOptions);
				//Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
				wm.setBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(notify){
			int icon = R.drawable.notify;
			Notification notification = new Notification(icon, "New Picture!", System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			Intent notificationIntent = new Intent(this, HomeActivity.class); 
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			notification.setLatestEventInfo(
					this,
					"New Astronomy Pic of the Day",
					newPicture.title, 
					contentIntent
			);
			notificationManager.cancelAll();
			notificationManager.notify(INFO, notification); 
		}

		finish();
	}

	private void finish() {
		notificationManager.cancel(RUNNING);
		sendBroadcast(new Intent(ACTION_FINISHED_UPDATE));
		stopSelf();
	}
}