package com.blork.anpod.service;

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
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.blork.anpod.R;
import com.blork.anpod.activity.HomeActivity;
import com.blork.anpod.model.Picture;
import com.blork.anpod.model.PictureFactory;
import com.blork.anpod.util.BitmapUtils;
import com.blork.anpod.util.Utils;
import com.blork.anpod.widget.LargeWidget;
import com.blork.anpod.widget.Widget;


// TODO: Auto-generated Javadoc
/**
 * The Class AnpodService.
 */
public class AnpodService extends Service implements Runnable{

	public static final String ACTION_FINISHED_UPDATE = "com.blork.anpod.ACTION_FINISHED_UPDATE";
	private NotificationManager notificationManager;
	private boolean notify;
	private Boolean forceRun;
	private boolean wallpaper;
	private WakeLock wakelock;

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
		Log.e("", "starting service");

		if (intent != null) {
			forceRun = intent.hasExtra("force_run");
		} else {
			forceRun = false;
		}

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "apod");
		wakelock.acquire();

		new Thread(this).start();

		return START_STICKY;
	}


	@Override
	public void run() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (!Utils.isDataEnabled(this)) {
			finish();
			return;
		}

		notify = prefs.getBoolean("notifications_enabled", false);
		wallpaper = prefs.getBoolean("set_wallpaper", false);


		boolean updates = prefs.getBoolean("updates_enabled", false);
		boolean wifi_only = prefs.getBoolean("wifi", false);

		if (!updates && !forceRun) {
			finish();
			return;
		}


		if (Utils.isNetworkConnected(this)) {
			if (wifi_only && !Utils.isWiFiConnected(this)) {
				finish();
				return;
			} 
		}


		//forceRun = UIUtils.isHoneycombTablet(this);

		if (notify || forceRun) {
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

		Intent notificationIntent = new Intent(this, HomeActivity.class);
		notificationIntent.putExtra("view_image", 0);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		WallpaperManager wm = (WallpaperManager) this.getSystemService(Context.WALLPAPER_SERVICE);

		int newWidth = wm.getDesiredMinimumWidth();
		int newHeight = wm.getDesiredMinimumHeight();

		Bitmap bitmap = BitmapUtils.fetchImage(this, newPicture, newWidth, newHeight);


		try {
			RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget);
			ComponentName thisWidget = new ComponentName(this, Widget.class);
			views.setViewVisibility(R.id.content, View.VISIBLE);
			views.setViewVisibility(R.id.loading, View.GONE);
			views.setTextViewText(R.id.title, newPicture.title);
			views.setTextViewText(R.id.credit, newPicture.credit);

			views.setOnClickPendingIntent(R.id.content, contentIntent);
			AppWidgetManager.getInstance(this).updateAppWidget(thisWidget, views);
		} catch (Exception e) { }

		try {
			RemoteViews views2 = new RemoteViews(this.getPackageName(), R.layout.large_widget);
			ComponentName thisWidget2 = new ComponentName(this, LargeWidget.class);
			views2.setViewVisibility(R.id.content, View.VISIBLE);
			views2.setViewVisibility(R.id.loading, View.GONE);
			views2.setTextViewText(R.id.title, newPicture.title);
			views2.setTextViewText(R.id.credit, newPicture.credit);
			views2.setImageViewBitmap(R.id.image, BitmapUtils.resizeBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2));
			views2.setOnClickPendingIntent(R.id.content, contentIntent);
			AppWidgetManager.getInstance(this).updateAppWidget(thisWidget2, views2);
		} catch (Exception e) { }

		int count = PictureFactory.saveAll(this, pictures);

		if (count == 0) {
			finish();
			return;
		} else {
			PictureFactory.deleteAll(this);
			PictureFactory.saveAll(this, pictures);
		}

		Log.e("", "about to set wallpaper");
		if (wallpaper) {
			Log.e("", "setting wallpaper");

			try {
				wm.setBitmap(bitmap);
			} catch (Exception e) {
				finish();
			}
		}

		if(notify){
			int icon = R.drawable.notify;
			Notification notification = new Notification(icon, "New Picture!", System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;
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
		try {
			notificationManager.cancel(RUNNING);
		} catch (Exception e) { 

		}
		sendBroadcast(new Intent(ACTION_FINISHED_UPDATE));
		stopSelf();
	}

	public void onDestroy() {
		super.onDestroy();
		Log.e("", "finished service");

		wakelock.release();
	}
}