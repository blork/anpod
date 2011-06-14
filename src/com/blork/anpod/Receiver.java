package com.blork.anpod;


import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


public class Receiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	try{
	    	Log.e(Apod.TAG, "Broadcast Received.");
	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    	
			Boolean updates = prefs.getBoolean("updates_enabled", true);
			Boolean weekly = prefs.getBoolean("weekly_enabled", false);
			
			Intent service = new Intent(context, AnpodService.class);
	        PendingIntent sender = PendingIntent.getService(context, 0, service, 0);
			AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			
			if(updates || weekly){
				Log.d(Apod.TAG, "Scheduling Update. (in Receiver)");
				Calendar calendar = Calendar.getInstance();
		
				int updateTime = Integer.parseInt(prefs.getString("update_time", "0"));
	
		        calendar.set(Calendar.HOUR_OF_DAY, updateTime);
		        calendar.set(Calendar.MINUTE, 0);
		        calendar.set(Calendar.SECOND, 0);  
		        
				Calendar rightNow = Calendar.getInstance();
				if(rightNow.compareTo(calendar) == 1){ 
				// 0 if the times are equal, -1 if the time of this Calendar is before the other one, 1 if the time of this Calendar is after the other one
					calendar.add(Calendar.DAY_OF_YEAR, 1);
					Log.w(Apod.TAG, "Update time is in the past, setting for tommorrow.");
				}
		        
		        Log.i(Apod.TAG, "Update is set for "+calendar.getTime().toLocaleString());
		        	     
		        if(weekly){
		        	am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);  
		        } else {
		        	am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), (AlarmManager.INTERVAL_DAY)*7, sender);          	
		        }
		    	
		    	Log.d(Apod.TAG,"Updates scheduled.");	
	
			} else {
				am.cancel(sender);
				Log.i(Apod.TAG, "Cancelling update.");
			}
	    	
	
			
			boolean boot = prefs.getBoolean("boot", true);
			if(boot && (intent.getAction().toString().equals("android.intent.action.BOOT_COMPLETED"))){
		    	Log.d(Apod.TAG,"Starting service on boot");
		    	Intent intent2 = new Intent(context, AnpodService.class);
		    	context.startService(intent2);  
			}
    	} catch (Throwable t) {
    		//pass
    	}
    }
}

