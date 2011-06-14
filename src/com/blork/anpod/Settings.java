package com.blork.anpod;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class Settings extends PreferenceActivity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(null);
      addPreferencesFromResource(R.xml.settings);
            
      final CheckBoxPreference daily = (CheckBoxPreference)findPreference("updates_enabled");
      final CheckBoxPreference weekly = (CheckBoxPreference)findPreference("weekly_enabled");
      final Preference cached = findPreference("cache");
      
      File dir = this.getFilesDir();
      final File[] files = dir.listFiles();
		
      long cacheSize = 0;
      for (File f : files ) {
    	  if(!f.getName().equals("apod.jpg")) {
    		  cacheSize = cacheSize + f.length();
    	  }
      }
      
      Log.i(Apod.TAG, "Cache size is: "+(cacheSize/1024)+"KB");
      cached.setSummary((cacheSize/1024)+"KB of 1024KB");
      
      cached.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
    		@Override
    		public boolean onPreferenceClick(Preference preference) {
    			AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
    			builder.setMessage("Clear Image Cache?")
    			       .setCancelable(true)
    			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   
    			        	   for (File f : files) {
    			        			if(!f.getName().equals("apod.jpg")) {
    			        				f.delete();
    			        				Log.i(Apod.TAG, "Deleting cached file: "+f.getName());
    			        				
    			        			}
    			        		}
    			        	   cached.setSummary("0KB of 1024KB");
    			           }
    			       })
    			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			                dialog.cancel();
    			           }
    			       });
    			builder.show();
    			return false;
    		}
    	});
      
    	if(daily.isChecked()){
			weekly.setChecked(false);
			weekly.setEnabled(false);
		}else if(weekly.isChecked()){
			daily.setChecked(false);
			daily.setEnabled(false);
		}
          
    	daily.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			@Override
			public boolean onPreferenceClick(Preference preference) {
			        if(daily.isChecked()){
			                weekly.setChecked(false);
			                weekly.setEnabled(false);
			        }else{
			                weekly.setEnabled(true);
			        }
			        return false;
			}
    	});
          
          weekly.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                                if(weekly.isChecked()){
                                        daily.setChecked(false);
                                        daily.setEnabled(false);
                                }else{
                                        daily.setEnabled(true);
                                }
                                return false;
                        }
                });
     
      
   }  
}