package com.blork.anpod.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.blork.anpod.R;

/**
 * Demonstration of PreferenceActivity to make a top-level preference
 * panel with headers.
 */

public class UserPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_one);
        addPreferencesFromResource(R.xml.preferences_two);
    }
    
    @Override
    protected void onPause() {
    	sendBroadcast(new Intent("com.blork.anpod.SET_UPDATE"));
    	super.onPause();
    }
}

