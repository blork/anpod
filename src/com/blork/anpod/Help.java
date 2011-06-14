package com.blork.anpod;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Help extends Activity {	
	public Cursor cursor;
	BroadcastReceiver updateReceiver;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        
    	Button close = (Button)findViewById(R.id.close);        
    	close.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Help.this.finish();
			} 
	     
	    });
    	Button market = (Button)findViewById(R.id.market);        
    	market.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.blork.anpodpro"));
				startActivity(intent); 
			} 
	     
	    }); 
    	
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean pro = settings.getBoolean("pro", false);
        if(pro){
        	market.setVisibility(View.GONE);
        }
    	
    	Button otherApps = (Button)findViewById(R.id.otherapps);        
    	otherApps.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=Sam%20Oakley"));
				startActivity(intent); 
			} 
	     
	    });
    	
		PackageManager pm = getPackageManager();
		PackageInfo pi;
		String versionName = "New Version";
		try {
			pi = pm.getPackageInfo("com.blork.anpod", PackageManager.GET_ACTIVITIES);
			versionName  = pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	TextView version = (TextView)findViewById(R.id.version);
    	version.setText(versionName+" Changelog:");

    }


}