package com.blork.anpod.service;


import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.blork.anpod.util.Utils;

/**
 * The Class Receiver.
 */
public class Receiver extends BroadcastReceiver
{

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		try{
			Log.d(Utils.TAG, "Broadcast Received.");

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

			Boolean updates = prefs.getBoolean("updates_enabled", true);

			Intent service = new Intent(context, AnpodService.class);
			PendingIntent sender = PendingIntent.getService(context, 0, service, 0);
			AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

			if(updates){
				Log.d(Utils.TAG, "Scheduling Update. (in Receiver)");
				Calendar calendar = Calendar.getInstance();

				String updateTime = prefs.getString("update_time", "0:0");
				String[] splitTime = updateTime.split(":");
				
				int hours = Integer.parseInt(splitTime[0]);
				int minutes = Integer.parseInt(splitTime[1]);

				calendar.set(Calendar.HOUR_OF_DAY, hours);
				calendar.set(Calendar.MINUTE, minutes);
				calendar.set(Calendar.SECOND, 0);  

				Calendar rightNow = Calendar.getInstance();
				if(rightNow.compareTo(calendar) == 1){ 
					// 0 if the times are equal, -1 if the time of this Calendar is before the other one, 1 if the time of this Calendar is after the other one
					calendar.add(Calendar.DAY_OF_YEAR, 1);
					Log.w(Utils.TAG, "Update time is in the past, setting for tommorrow.");
				}


				Log.i(Utils.TAG, "Update is set for "+calendar.getTime().toLocaleString());

				am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);  

				Log.d(Utils.TAG,"Updates scheduled.");	

			} else {
				am.cancel(sender);
				Log.i(Utils.TAG, "Cancelling update.");
			}
		} catch (Throwable t) {
			//pass
		}
	}
}

