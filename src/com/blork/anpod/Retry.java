package com.blork.anpod;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

 

public class Retry extends Activity {	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		nm.cancel(3);
    	Intent intent = new Intent(this, AnpodService.class);
    	startService(intent);  
        finish();
    }
  

    
}