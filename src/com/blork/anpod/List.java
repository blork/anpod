package com.blork.anpod;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;


public class List extends ListActivity {	
	public Cursor cursor;
	BroadcastReceiver updateReceiver;
	private GoogleAnalyticsTracker tracker;
	
	public static ApodEndlessAdapter adapter;
	
	public static Integer resultsPage;
	
	public static ArrayList<Apod> apodList = null;

	public static int position;
	
	private boolean byRating = false;
	private ListView lv;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        byRating = prefs.getBoolean("byRating", false);
        
        
        resultsPage = 0;
                
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start("UA-20756745-1", this);
        tracker.trackPageView("/list");
        tracker.dispatch();
        
        updateReceiver = new UpdateReceiver();
        registerReceiver(updateReceiver, new IntentFilter(AnpodService.ACTION_NEW_APOD)); 
		
        ImageButton refreshButton = (ImageButton)this.findViewById(R.id.btn_title_refresh);
		ImageButton homeButton = (ImageButton)this.findViewById(R.id.btn_title_home);
		
		
		homeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(List.this, Main.class));
			}
		}); 
		
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        findViewById(R.id.btn_title_refresh).setVisibility(View.GONE);
		        findViewById(R.id.title_refresh_progress).setVisibility(View.VISIBLE);
	    		Intent intent = new Intent(List.this, AnpodService.class);
	    		intent.putExtra("runonce", true);
	    		startService(intent); 
			}
		});
        
        
        lv = (ListView)getListView(); 
        registerForContextMenu(lv); 
        
		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id) {
		    	try {
			    	List.position = position;
			    	onClick();
		    	} catch (IndexOutOfBoundsException e) {
		    		e.printStackTrace();
		    		//Why?
		    	}
		    }
		});
        
        apodList = new ArrayList<Apod>();
        
        adapter = new ApodEndlessAdapter(apodList);
		setListAdapter(adapter);
    } 
     

    
    public static ArrayList<Apod> getApodList(Integer page, Boolean byRating) throws IOException, URISyntaxException, JSONException{
    	URL jsonUrl;
    	if(byRating){
    		jsonUrl = new URL(Apod.url + "list?by_rating=true&page=" + page.toString());
    	}else{
        	jsonUrl = new URL(Apod.url + "list?page=" + page.toString());
    	}
    	
    	
		String allJson = Apod.getJSON(jsonUrl);
		JSONTokener jsonTokener = new JSONTokener(allJson);
				
		JSONArray list = (JSONArray) jsonTokener.nextValue();
						
		ArrayList<Apod> apodList = new ArrayList<Apod>();
		
		for(int i = 0; i < list.length(); i++){
			Apod apod = new Apod(list.getJSONObject(i));
			apodList.add(apod);
		}
		
    	return apodList;	
    }

	private class ApodAdapter extends ArrayAdapter<Apod> {

    	private ArrayList<Apod> apodList;

    	public ApodAdapter(Context context, int textViewResourceId, ArrayList<Apod> apodList) {
    			super(context, textViewResourceId, apodList);
    			this.apodList = apodList;
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		View v = convertView;
    		if (v == null) {
    			LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			v = vi.inflate(R.layout.list_item, null);
    		}
    		Apod a = apodList.get(position);
    		if (a != null) {
    			TextView tt = (TextView) v.findViewById(R.id.title);
    			TextView tc = (TextView) v.findViewById(R.id.credit);
    			if (tt != null) {
    				tt.setText(a.title);                           
    			}
    			if (tc != null) {
    				tc.setText(a.credit);                           
    			}

    		}
    		return v;
    	}
	}
	
	class ApodEndlessAdapter extends EndlessAdapter { 
		private RotateAnimation rotate=null;
		
		ApodEndlessAdapter(ArrayList<Apod> apodList) {
			super(new ApodAdapter(List.this, R.layout.list_item, apodList));
			
			rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
																	0.5f, Animation.RELATIVE_TO_SELF,
																	0.5f);
			rotate.setDuration(600);
			rotate.setRepeatMode(Animation.RESTART);
			rotate.setRepeatCount(Animation.INFINITE);
		}
		
		protected View getPendingView(ViewGroup parent) {
			View row = getLayoutInflater().inflate(R.layout.list_item, null);
			View child = row.findViewById(R.id.whitedivider);
			child.setVisibility(View.GONE);
			child = row.findViewById(R.id.listcontainer);
			child.setVisibility(View.GONE);
			
			child = row.findViewById(R.id.listloading);
			child.setVisibility(View.VISIBLE);
			child.startAnimation(rotate);
			return(row);
		}
		
		
		protected boolean cacheInBackground() {
			ArrayList<Apod> results = new ArrayList<Apod>();
			
	    	try {
	    		results = getApodList(resultsPage, byRating);
				apodList.addAll(results);
				resultsPage = resultsPage + 1;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//Log.d(Apod.TAG, "List length: "+results.size());
			if(results.size() == 0){
				return(false);
			} else {
				return(true);
			}
		}
		
		protected void appendCachedData() {
//			@SuppressWarnings("unchecked")
//			ArrayAdapter<Apod> a = (ArrayAdapter<Apod>)getWrappedAdapter();
//    		for (Apod apod : apodList) {
//    			a.add(apod);
//    		}
			
		}



	}
	
	public void onClick() {
		Intent d = new Intent(this, About.class);
		startActivity(d);
	}
	
	public void onResume(){
		super.onResume();
		
		lv.setSelection(List.position);
		
		if(!Apod.IsNetworkConnected(this)){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("No network connection.");
			builder.setMessage("To view previous pictures you must have an active internet connection.")
			       .setCancelable(false)
			       .setNegativeButton("Try it anyway", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           } 
			       }).setPositiveButton("Go Back", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   	dialog.cancel();
			                finish();
			           } 
			       });
			AlertDialog alert = builder.create();
			alert.show();
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		String theme = prefs.getString("theme", "purple");
		View darkGradient = findViewById(R.id.TitleBar);

		if(theme.equals("black")){
			darkGradient.setBackgroundResource(R.drawable.black_gradient);
		} else if(theme.equals("green")){
			darkGradient.setBackgroundResource(R.drawable.green_gradient);
		} else {
			darkGradient.setBackgroundResource(R.drawable.gradient);
		}
	}
	
	  class UpdateReceiver extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) { 
		          findViewById(R.id.btn_title_refresh).setVisibility(View.VISIBLE);
		          findViewById(R.id.title_refresh_progress).setVisibility(View.GONE);
			}
	   }
	  
	  public void onDestroy(){
		  	super.onDestroy();
		  	unregisterReceiver(updateReceiver);
		  	tracker.stop();
	  }
	  
	  
	  public boolean onCreateOptionsMenu(Menu menu) {
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.layout.list_menu, menu); 
	        return true;
	    }
	    
		public boolean onPrepareOptionsMenu(Menu menu) {
			super.onPrepareOptionsMenu(menu);
			MenuItem ratingsort = menu.findItem(R.id.ratingsort);
			MenuItem datesort = menu.findItem(R.id.datesort);
			
			if(byRating){
				ratingsort.setVisible(false);
				datesort.setVisible(true);
			}else{
				ratingsort.setVisible(true);
				datesort.setVisible(false);
			}

	        return true;
	    }
		
	    public boolean onOptionsItemSelected(MenuItem item) {
	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(List.this);	
    		SharedPreferences.Editor pEditor = settings.edit();
    		
	    	if(item.getTitle().equals("Sort by rating")){
	    		byRating = true;
	        	pEditor.putBoolean("byRating", true);
	            pEditor.commit();
	    	}else if(item.getTitle().equals("Sort by date")){
	    		byRating = false;
	    		pEditor.putBoolean("byRating", false);
	            pEditor.commit();	
	    	}
	    	List.resultsPage = 0;
	    	List.apodList.clear();
	    	List.adapter.notifyDataSetChanged();
	        return true;
	    }
}

