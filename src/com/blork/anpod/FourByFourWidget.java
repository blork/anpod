package com.blork.anpod;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FourByFourWidget extends AppWidgetProvider {
   
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Log.d("anpod","widget onUpdate."); 
    	Intent intent = new Intent(context, AnpodService.class);
    	intent.putExtra("widget", true);
    	context.startService(intent);
    } 
}  