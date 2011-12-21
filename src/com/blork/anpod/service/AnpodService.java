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
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		System.gc();

		Log.d("APOD", "starting service");

		if (intent != null && intent.getExtras() != null) {
			forceRun = intent.hasExtra("force_run");
		} else {
			forceRun = false;
		}

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakelock = pm.newWakeLock( 
				PowerManager.FULL_WAKE_LOCK |
				PowerManager.ACQUIRE_CAUSES_WAKEUP |
				PowerManager.ON_AFTER_RELEASE, 
		"apod");
		wakelock.acquire();

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Log.d("APOD", "about to start main thread");
		new Thread(this).start();

		return START_STICKY;
	}


	@Override
	public void run() {
		Log.d("APOD", "in main thread");

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// if background data is off, and this update is in the background, quit.
		if (!Utils.isDataEnabled(this) && !forceRun) {
			Log.d("APOD", "exit point 1");
			finish();
			return;
		} else {
			Log.d("APOD", "completed point 1");
		}

		notify = prefs.getBoolean("notifications_enabled", false);
		wallpaper = prefs.getBoolean("set_wallpaper", false);

		boolean updates = prefs.getBoolean("updates_enabled", false);
		boolean wifi_only = prefs.getBoolean("wifi", false);

		// if updates are disabled, and we are running in the background, quit.
		if (!updates && !forceRun) {
			Log.d("APOD", "exit point 2");
			finish();
			return;
		} else {
			Log.d("APOD", "completed point 2");
		}

		// if no network connection, quit.
		if (!Utils.isNetworkConnected(this)) {
			Log.d("APOD", "exit point 3");
			finish();
			return;
		} else {
			Log.d("APOD", "completed point 3");
		}

		// if wifi only enabled, but not connected to wi-fi, quit.
		if (wifi_only && !Utils.isWiFiConnected(this)) {
			Log.d("APOD", "exit point 4");
			finish();
			return;
		}  else {
			Log.d("APOD", "completed point 4");
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


		Log.d("APOD", "Fetching pictures");

		List<Picture> pictures = new ArrayList<Picture>();

		try {
			Log.d("APOD", "adding pictures");
			pictures.addAll(PictureFactory.load());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (pictures.isEmpty()) {
				Log.d("APOD", "exit point 5");
				finish();
				return;
			} else {
				Log.d("APOD", "completed point 5");
			}
		}

		Log.d("APOD", "Fetching latest picture");
		Picture newPicture = pictures.get(0);

		Intent notificationIntent = new Intent(this, HomeActivity.class);
		notificationIntent.putExtra("view_image", 0);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		WallpaperManager wm = (WallpaperManager) this.getSystemService(Context.WALLPAPER_SERVICE);

		int desiredWidth = wm.getDesiredMinimumWidth();
		int desiredHeight = wm.getDesiredMinimumHeight();

		if (desiredHeight < 0 || desiredWidth < 0) {
			WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE); 
			Display display = window.getDefaultDisplay();
			desiredWidth = display.getWidth() * 2;
			desiredHeight = display.getHeight();
		}

		Log.d("APOD", "Fetching latest picture bitmap");
		Bitmap bitmap = BitmapUtils.fetchImage(this, newPicture, desiredWidth, desiredHeight);

		Log.d("APOD", "Updating widgets");
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

		Log.d("APOD", "Checking for new pics");
		int count = PictureFactory.saveAll(this, pictures);
		Log.d("APOD", count + " new pictures");
		if (count == 0) {
			int icon = android.R.drawable.stat_notify_error;
			Notification notification = new Notification(icon, "No New Picture!", System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.setLatestEventInfo(
					this,
					"No new picture!",
					"There has been no new NASA update today. Try again tomorrow!", 
					contentIntent
			);
			notificationManager.cancelAll();
			notificationManager.notify(INFO, notification); 
			Log.d("APOD", "exit point 6");
			finish();
			return;
		} else {
			PictureFactory.deleteAll(this);
			PictureFactory.saveAll(this, pictures);
			Log.d("APOD", "completed point 6");
		}

		if (wallpaper) {
			Log.d("APOD", "setting wallpaper");

			try {
				wm.setBitmap(bitmap);
			} catch (Exception e) {
				Log.d("APOD", "exit point 7");
				finish();
				return;
			}

			Log.d("APOD", "completed point 7");

		}

		if(notify){
			int icon = R.drawable.ic_stat_notify;
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

		Log.d("APOD", "completed!");
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

	@Override
	public void onDestroy() {
		Log.d("APOD", "destroying service");
		wakelock.release();
		super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}