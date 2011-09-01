package com.blork.anpod.widget;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.blork.anpod.service.AnpodService;

// TODO: Auto-generated Javadoc
/**
 * The Class Widget.
 */
public class Widget extends AppWidgetProvider {
   
    /* (non-Javadoc)
     * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
     */
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Log.d("anpod","widget onUpdate."); 
    	Intent intent = new Intent(context, AnpodService.class);
    	intent.putExtra("force_run", true);
    	context.startService(intent);  
    }
}