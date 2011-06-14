package com.blork.anpod;

import static android.provider.BaseColumns._ID;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 

public class Main extends Activity {	
	public Cursor cursor;
	BroadcastReceiver updateReceiver;
	private String titleText = "";
	private String youtube_url = "";
	public Boolean imagedownloaded = true;
	private TextView title;
	private TextView play;
	private ImageView image;
	private ApodData data;
	private SQLiteDatabase db;
	private SharedPreferences settings;
	public boolean refreshing;
	private Button shareButton;
	ImageButton refreshButton;
	PackageManager pm;
	
	GoogleAnalyticsTracker tracker;
	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = getWindow();
		// Eliminates color banding
		window.setFormat(PixelFormat.RGBA_8888);
	}
	  
	public void onResume(){
		super.onResume();
		
		try {
			pm.getPackageInfo("com.blork.anpodpro", PackageManager.GET_ACTIVITIES);
			Log.i(Apod.TAG, "pro is installed");
			boolean pro = settings.getBoolean("pro", false);
			if(!pro){
				AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
				builder.setTitle("Thank you!");
				builder.setMessage("Thanks for buying the donate version - I really appreciate it!")
				       .setCancelable(false)
				       .setNeutralButton("No Problem!", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           } 
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}
			SharedPreferences.Editor pEditor = settings.edit();
        	pEditor.putBoolean("pro", true);
            pEditor.commit();
		} catch (NameNotFoundException e1) {
			Log.i(Apod.TAG, "pro not installed");
			//Show ad?
			SharedPreferences.Editor pEditor = settings.edit();
        	pEditor.putBoolean("pro", false);
            pEditor.commit();
		}
		   
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.cancel(1);
		mNotificationManager.cancel(2);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Boolean updates = prefs.getBoolean("updates_enabled", true);
		Boolean weekly = prefs.getBoolean("weekly_enabled", false);
		
		Intent service = new Intent(this, AnpodService.class);
        PendingIntent sender = PendingIntent.getService(this, 0, service, 0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		
		if(updates){
			Log.d(Apod.TAG, "Scheduling Update. (in Main)");
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
	       	     

	        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 604800000, sender);          	

	    	Log.d(Apod.TAG,"Updates scheduled.");	

		} else if(weekly){
            Log.d(Apod.TAG, "Scheduling Weekly Update");
            Calendar calendar = Calendar.getInstance();

            calendar.add(Calendar.DAY_OF_YEAR, 7);
            
		    Log.i(Apod.TAG, "Update is set for "+calendar.getTime().toGMTString());
		    
		    long WEEK = 604800000;
		    am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), WEEK, sender);  
		    Log.d(Apod.TAG,"Updates scheduled.");   
		    

	    }else {
	    	am.cancel(sender);
	    	Log.i(Apod.TAG, "Cancelling update.");
		}
		
		updateText();
		
		String theme = prefs.getString("theme", "purple");
		View darkGradient = findViewById(R.id.TitleBar);
		View lightGradient = findViewById(R.id.light_gradient_section);
		View titleContainer = findViewById(R.id.title_container);
		View darkSeparator = findViewById(R.id.dark_separator);

		if(theme.equals("black")){
			darkGradient.setBackgroundResource(R.drawable.black_gradient);
			lightGradient.setBackgroundResource(R.drawable.black_light_gradient);
			titleContainer.setBackgroundResource(R.color.darkergrey);
			darkSeparator.setBackgroundResource(R.color.black);
		} else if(theme.equals("green")){
			darkGradient.setBackgroundResource(R.drawable.green_gradient);
			lightGradient.setBackgroundResource(R.drawable.green_light_gradient);
			titleContainer.setBackgroundResource(R.color.lightgreen);
			darkSeparator.setBackgroundResource(R.color.darkergreen);
		} else {
			darkGradient.setBackgroundResource(R.drawable.gradient);
			lightGradient.setBackgroundResource(R.drawable.light_gradient);
			titleContainer.setBackgroundResource(R.color.lightpurple);
			darkSeparator.setBackgroundResource(R.color.purple);
		}
	}

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        settings = PreferenceManager.getDefaultSharedPreferences(Main.this);	
        pm = getPackageManager();
        
        SharedPreferences.Editor pEditor = settings.edit();
		try {
			pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES);
			Log.i(Apod.TAG, "market is installed"); 
        	pEditor.putBoolean("market", true);
            pEditor.commit();
		} catch (NameNotFoundException e1) {
			Log.i(Apod.TAG, "market not installed");
			pEditor.putBoolean("market", false);
            pEditor.commit();
            
            Intent updateIntent = new Intent(Main.this, AppUpdateService.class);
    		startService(updateIntent); 
		}
        		
        
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start("UA-20756745-1", this);
        tracker.trackPageView("/main");
        tracker.dispatch();

        
        
        this.refreshing = false;

                
		try {
			PackageInfo pi = pm.getPackageInfo("com.blork.anpod", PackageManager.GET_ACTIVITIES);
			int version = pi.versionCode;
			
	        int storedVersion = settings.getInt("version", 1);
	        Log.d(Apod.TAG, "Version "+storedVersion);
	        if(storedVersion != version){
	        	SharedPreferences.Editor vEditor = settings.edit();
	        	vEditor.putInt("version", version);
	            vEditor.commit(); 
	            
	    		Intent intent = new Intent(Main.this, Help.class);
	    		startActivity(intent); 
	        }
	        
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		

 
        //ratingbar = (RatingBar)findViewById(R.id.ratingbar);
        title = (TextView)findViewById(R.id.image_title);
		//credit = (TextView)findViewById(R.id.image_credit);
		//info = (TextView)findViewById(R.id.info);
		image = (ImageView)findViewById(R.id.image);

		play = (TextView)findViewById(R.id.play);
		
		data = new ApodData(this);
		db = data.getReadableDatabase();
		
		title.setSelected(true);
		//credit.setSelected(true);

		Button aboutButton = (Button)this.findViewById(R.id.home_btn_about);
		Button moreButton = (Button)this.findViewById(R.id.home_btn_more);
		shareButton = (Button)this.findViewById(R.id.home_btn_share);
		Button optionButton = (Button)this.findViewById(R.id.home_btn_options);
		refreshButton = (ImageButton)this.findViewById(R.id.btn_title_refresh);
		
		aboutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Main.this, Today.class));
			}
		});
		
		moreButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Main.this, List.class));
			}
		});
		
		
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//finish();
			}
		});

		
		optionButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Main.this, Settings.class));
			}
		});
		
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        findViewById(R.id.btn_title_refresh).setVisibility(View.GONE);
		        findViewById(R.id.title_refresh_progress).setVisibility(View.VISIBLE);
	    		Intent intent = new Intent(Main.this, AnpodService.class);
	    		intent.putExtra("runonce", true);
	    		startService(intent); 
			}
		});
		
		
//    	if(item.getTitle().equals("Settings")){
//    		
//    	} else if(item.getTitle().equals("Refresh")){
//    		Intent intent = new Intent(this, AnpodService.class);
//    		intent.putExtra("runonce", true);
//    		startService(intent); 
//    	} else if(item.getTitle().equals("About")){
//    		startActivity(new Intent(this, Help.class));
//    	} else if(item.getTitle().equals("Get Today's Picture")){
//    		Intent intent = new Intent(this, AnpodService.class);
//    		startService(intent); 
//     		if (IsNetworkConnected()) {
//     			new DownloadApodImageTask(this).execute();
//     		}
//    	}
 
        
		StartThread s = new StartThread();
		s.start();		
		
		updateText();
    }
    
    class StartThread extends Thread {
    	public void run(){
          
            
            boolean firstRun = settings.getBoolean("firstRun", true);
          
            if(firstRun){
            	Log.i(Apod.TAG, "First run; generating UUID");
            	SharedPreferences.Editor editor = settings.edit();
            	
            	UUID deviceUuid;
            	
    			try {
    				final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

    			    final String tmDevice, tmSerial, androidId;
    			    
    			    tmDevice = "" + tm.getDeviceId();
    			    tmSerial = "" + tm.getSimSerialNumber();
    			    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

    			    deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());

    			} catch (Exception e) {
    				deviceUuid = UUID.randomUUID();
    			}
    			
    			String deviceId = deviceUuid.toString();
    			
            	editor.putString("uniqueId", deviceId);
                editor.putBoolean("firstRun", false);
                
                editor.commit();
            }
            

            updateReceiver = new UpdateReceiver();
            registerReceiver(updateReceiver, new IntentFilter(AnpodService.ACTION_NEW_APOD));    		
    	}
    }
	
    
	public void updateText(){
		
		try {
			Log.d("anpod","reading from db");
			
			cursor = db.query("apod", new String[] {_ID, "title, credit, info, uri, rating, date, youtube_url"}, 
			        null, null, null, null, "date DESC");
			
			cursor.moveToFirst(); 
			play.setVisibility(View.GONE);
			titleText = cursor.getString(cursor.getColumnIndex("title"));
			title.setText(titleText);
			//credit.setText(cursor.getString(cursor.getColumnIndex("credit"))); 
			//info.setText(cursor.getString(cursor.getColumnIndex("info")));
			youtube_url = "";
			try{
				if(!cursor.getString(cursor.getColumnIndex("youtube_url")).equals("null")){
					youtube_url = cursor.getString(cursor.getColumnIndex("youtube_url"));
					play.setText(youtube_url);
					play.setVisibility(View.VISIBLE);
					image.setVisibility(View.GONE);
					//ratingbar.setVisibility(View.GONE);
				}else{
					play.setVisibility(View.GONE); 
					
				}
			}catch (Exception e){
				play.setVisibility(View.GONE);
			}
			
//			try {
//				rating = Integer.parseInt(cursor.getString(cursor.getColumnIndex("rating")));
//				ratingbar.setRating(rating);
//				
//			} catch (Exception e1) {
//				Log.i(Apod.TAG, "No rating set yet.");
//				ratingbar.setRating(3);
//			}
			
			try {
					
				InputStream is;
				String uritemp;
				try{
					uritemp = cursor.getString(cursor.getColumnIndex("uri"));	
					ContentResolver cr = getContentResolver();
					is = cr.openInputStream(Uri.parse(uritemp));
				} catch (FileNotFoundException e){
					File file = new File(this.getFilesDir().getPath()+"/apod.jpg");
					uritemp = Uri.fromFile(file).toString();
					ContentResolver cr = getContentResolver();
					is = cr.openInputStream(Uri.fromFile(file));
				}
				
				final String uri = uritemp;
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither = true;
				options.inSampleSize = 2; //TODO: memory!!!
				
				Bitmap bitmap;
				try{
					bitmap = BitmapFactory.decodeStream(is, null, options);
				} catch(OutOfMemoryError e) {
					options.inSampleSize = 4;
					bitmap = BitmapFactory.decodeStream(is, null, options);
					System.gc();
				}
				
				image.setImageBitmap(bitmap);
				image.setVisibility(View.VISIBLE);
				imagedownloaded = true; 
				
				final ContentResolver cr = getContentResolver();
				
				image.setOnClickListener(new View.OnClickListener() {
				    public void onClick(View v) {
			    	
				    	final CharSequence[] items = {"Set Wallpaper", "View image in gallery", "Other actions"};

				    	AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
				    	builder.setTitle("Options");
				    	builder.setItems(items, new DialogInterface.OnClickListener() {
				    	    public void onClick(DialogInterface dialog, int item) {
				    	        switch(item){
				    	        	case 0:
				    	        		WallpaperManager wm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
										try {
											InputStream is2 = cr.openInputStream(Uri.parse(uri));
											wm.setStream(is2);
											Toast.makeText(Main.this, "Wallpaper set.", Toast.LENGTH_LONG).show();
										} catch (IOException e) {
											Toast.makeText(Main.this, "Couldn't autoset. Try setting manually.", Toast.LENGTH_LONG).show();
											e.printStackTrace();
									}
				    	        	break;
				    	        	case 1:
								    	Intent intent = new Intent(Intent.ACTION_VIEW);
								    	intent.setDataAndType(Uri.parse(uri), "image/jpeg");
								    	startActivity(intent);
				    	        	break;
				    	        	case 2:
								    	Intent intent1 = new Intent();
								    	intent1.setDataAndType(Uri.parse(uri), "image/*");	
								    	startActivity(intent1);
				    	        	break;	
				    	        }
				    	    }
				    	});
				    	builder.show();
				    }
				 });
				
				
				shareButton.setOnClickListener(new View.OnClickListener() {
				    public void onClick(View v) {
				    	Intent share = new Intent(Intent.ACTION_SEND);
				    	share.setType("text/plain");
				    	share.putExtra(Intent.EXTRA_TITLE, titleText);

				    	share.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));

				    	startActivity(Intent.createChooser(share, "Share Text"));
				    }
				});
				
			} catch (Throwable e) {
				Log.e(Apod.TAG, "Couldn't set bitmap to ImageView");
				imagedownloaded = false;
				e.printStackTrace();
				
				if(!youtube_url.equals("")){
					shareButton.setOnClickListener(new View.OnClickListener() {
					    public void onClick(View v) {
					    	Intent share = new Intent(Intent.ACTION_SEND);
					    	share.setType("text/plain");
					    	share.putExtra(Intent.EXTRA_TITLE, titleText);
					    	share.putExtra(Intent.EXTRA_TEXT, youtube_url);
					    	startActivity(Intent.createChooser(share, "Share Text"));
					    }
					});
				}
			}
	        
		} catch (Exception e) {
			e.printStackTrace();
			title.setText("Updating...");
			refreshButton.performClick();
			imagedownloaded = true;
		} finally {
			cursor.close();
		}
	}
	

    public void onDestroy(){
    	super.onDestroy();
    	unregisterReceiver(updateReceiver);
    	db.close();
    	tracker.stop();
    }
    
    
    class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) { 
	          Log.d("anpod", "received broadcast");
	          updateText();
	          findViewById(R.id.btn_title_refresh).setVisibility(View.VISIBLE);
	          findViewById(R.id.title_refresh_progress).setVisibility(View.GONE);
		}
     }

}