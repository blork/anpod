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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.blork.anpod.R;
import com.blork.anpod.activity.HomeActivity;
import com.blork.anpod.model.Picture;
import com.blork.anpod.model.PictureFactory;
import com.blork.anpod.util.BitmapUtils;
import com.blork.anpod.util.BitmapUtils.OnFetchCompleteListener;
import com.blork.anpod.util.Utils;


// TODO: Auto-generated Javadoc
/**
 * The Class AnpodService.
 */
public class AnpodService extends Service implements Runnable{

	public static final String ACTION_FINISHED_UPDATE = "com.blork.anpod.ACTION_FINISHED_UPDATE";
	private NotificationManager notificationManager;
	private boolean notify;
	private boolean silent = false;
	private boolean wallpaper;

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

		if (!Utils.isDataEnabled(this)) {
			finish();
			return;
		}

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		notify = prefs.getBoolean("notifications_enabled", true);
		wallpaper = prefs.getBoolean("set_wallpaper", true);


		boolean updates = prefs.getBoolean("updates_enabled", true);
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

		int count = PictureFactory.saveAll(this, pictures);

		if (count == 0) {
			finish();
			return;
		}

		Picture newPicture = pictures.get(0);

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

		if (wallpaper) {
			BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
			decodeOptions.inSampleSize = 2;

			BitmapUtils.fetchImage(
					this, 
					newPicture.getFullSizeImageUrl(), 
					newPicture.title, 
					decodeOptions, 
					null, 
					new OnFetchCompleteListener() {
						@Override
						public void onFetchComplete(Object cookie, final Bitmap result, final Uri uri) {
							sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

							WallpaperManager wm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);

							int newWidth = wm.getDesiredMinimumWidth();
							int newHeight = wm.getDesiredMinimumHeight();

							try {
								Bitmap resizedBitmap = Bitmap.createScaledBitmap(result, newWidth, newHeight, true);
								wm.setBitmap(resizedBitmap);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
			);
		}


		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());	
		long time = System.currentTimeMillis();
		Editor editor = prefs.edit();
		editor.putLong("time", time);
		editor.commit();
	}

	private void finish() {
		notificationManager.cancel(RUNNING);
		sendBroadcast(new Intent(ACTION_FINISHED_UPDATE));
		stopSelf();
	}
}