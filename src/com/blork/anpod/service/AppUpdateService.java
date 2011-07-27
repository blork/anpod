package com.blork.anpod.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.blork.anpod.util.Utils;


// TODO: Auto-generated Javadoc
/**
 * The Class AppUpdateService.
 */
public class AppUpdateService extends Service implements Runnable {
	   
    /** Called when the activity is first created. */
    @Override
    public void onCreate() {
        
 		if (Utils.isNetworkConnected(this)) {
 			Thread thread = new Thread(this);
 	        thread.start();
 		} else {
 			stopSelf();
 		}

    }

    
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		int newVersion = 0;
		
		try {
			//newVersion = Utils.getCurrentAppVersion();
		} catch (Exception e) {
			e.printStackTrace();
			stopSelf();
		}
		
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);	

		int storedVersion = settings.getInt("version", 3);
        if(storedVersion < newVersion){
     		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
     		
    		int icon = 0; //R.drawable.update;
    		
    		Notification notification = new Notification(icon, "Update for Astronomy Picture of the Day App available!", System.currentTimeMillis());
    		notification.flags = Notification.FLAG_AUTO_CANCEL;
    		
    		Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
    		
    		Uri data;
    		boolean market = settings.getBoolean("market", false);
    		if(market) {
    			data = Uri.parse("market://details?id=com.blork.anpod");
    		} else {
    			data = Uri.parse("http://slideme.org/en/mobileapp/download/0ce62cfe-fe5b-102d-bbbc-6ec07c99d928.apk");

    		}
    		
			notificationIntent.setData(data);

    		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    		notification.setLatestEventInfo(this, "New version of Astronomy Picture of the Day App available!", "Click to download update", contentIntent);
    		nm.cancelAll();
    		nm.notify(1, notification); 		
    		stopSelf();
        }
	}


	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
}