package com.blork.anpod.service;

import java.util.List;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.blork.anpod.R;
import com.blork.anpod.activity.HomeActivity;
import com.blork.anpod.model.Picture;
import com.blork.anpod.model.PictureFactory;
import com.blork.anpod.widget.Widget;


// TODO: Auto-generated Javadoc
/**
 * The Class AnpodService.
 */
public class WidgetService extends Service implements Runnable{



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		System.gc();
		Log.d("APOD", "starting widget update service");
		Log.d("APOD", "about to start main thread");
		new Thread(this).start();

		return START_STICKY;
	}


	@Override
	public void run() {
		Log.d("APOD", "in main thread");

		List<Picture> pictures = PictureFactory.getLocalPictures(this);
		
		if (pictures == null || pictures.size() == 0) {
			stopSelf();
			return;
		}

		Picture latestPicture = pictures.get(0);
		
		Intent notificationIntent = new Intent(this, HomeActivity.class);
		notificationIntent.putExtra("view_image", 0);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		
		Log.d("APOD", "Updating widgets");
		try {
			RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget);
			ComponentName thisWidget = new ComponentName(this, Widget.class);
			views.setViewVisibility(R.id.content, View.VISIBLE);
			views.setViewVisibility(R.id.loading, View.GONE);
			views.setTextViewText(R.id.title, latestPicture.title);
			views.setTextViewText(R.id.credit, latestPicture.credit);

			views.setOnClickPendingIntent(R.id.content, contentIntent);
			AppWidgetManager.getInstance(this).updateAppWidget(thisWidget, views);
		} catch (Exception e) { }


	}


	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}