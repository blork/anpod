package com.blork.anpod;

import static android.provider.BaseColumns._ID;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 

public class Today extends Activity {
	public Cursor cursor;
	BroadcastReceiver updateReceiver;
	private String titleText = "";
	public Boolean imagedownloaded = true;
	private TextView title;
	private TextView credit;
	private TextView info;
	private TextView play;
	private ImageView image;
	private ApodData data;
	private SQLiteDatabase db;
	public SharedPreferences settings;
	public boolean refreshing;
	private GoogleAnalyticsTracker tracker;

	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = getWindow();
		// Eliminates color banding
		window.setFormat(PixelFormat.RGBA_8888);
	}
	  
	public void onResume(){
		super.onResume();
				
		String ns = Context.NOTIFICATION_SERVICE; 
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.cancel(1);
		mNotificationManager.cancel(2);
		 

		updateText();
		
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
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
        setContentView(R.layout.about);

        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start("UA-20756745-1", this);
        tracker.trackPageView("/today");
        tracker.dispatch();
        
        settings = PreferenceManager.getDefaultSharedPreferences(Today.this);	
        
 
        //ratingbar = (RatingBar)findViewById(R.id.ratingbar);
        title = (TextView)findViewById(R.id.image_title);
		credit = (TextView)findViewById(R.id.image_credit);
		info = (TextView)findViewById(R.id.image_info);
		image = (ImageView)findViewById(R.id.image);
		play = (TextView)findViewById(R.id.play);
		
		data = new ApodData(this);
		db = data.getReadableDatabase();
		
		title.setSelected(true);
		credit.setSelected(true);

		ImageButton refreshButton = (ImageButton)this.findViewById(R.id.btn_title_refresh);
		ImageButton homeButton = (ImageButton)this.findViewById(R.id.btn_title_home);
		
		
		homeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Today.this, Main.class));
			}
		});
		
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        findViewById(R.id.btn_title_refresh).setVisibility(View.GONE);
		        findViewById(R.id.title_refresh_progress).setVisibility(View.VISIBLE);
	    		Intent intent = new Intent(Today.this, AnpodService.class);
	    		intent.putExtra("runonce", true);
	    		startService(intent); 
			}
		});
		
        
		StartThread s = new StartThread();
		s.start();		
		
		updateText();
		
    }
    
    class StartThread extends Thread {
    	public void run(){
          
            updateReceiver = new UpdateReceiver();
            registerReceiver(updateReceiver, new IntentFilter(AnpodService.ACTION_NEW_APOD));    		
    	}
    }
	
    
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.today_menu, menu); 
        return true;
    }
    

	
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getTitle().equals("Submit your rating")){
    		
    		AlertDialog.Builder builder;

    		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
    		View layout = inflater.inflate(R.layout.rating_dialog,
    		                               (ViewGroup) findViewById(R.id.layout_root));

    		final RatingBar ratingbar = (RatingBar) layout.findViewById(R.id.rating);
    		
    		builder = new AlertDialog.Builder(this);
    		builder.setView(layout);
    		
    		builder.setTitle("Your rating")
    	       .setCancelable(true)
    	       .setNeutralButton("Submit rating", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   int rating = (int) ratingbar.getRating();
    	        	   RateThread thread = new RateThread(rating);
    	               thread.start();
    	               dialog.cancel();
    	           }
    	       });
    		
    		builder.show();
    	}
        return true;
    }

    class RateThread extends Thread {
    	private int rating;
    	
		public RateThread(int rating) {
			this.rating = rating;
		}

		public void run(){

			try {
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Today.this);                             
				String uniqueId = prefs.getString("uniqueId", "");
				  
				if(uniqueId.equals("")){
					throw new Exception();
				}
				  
				HttpGet httpRequest = null;
				URI rateUrl = new URL(Apod.url+"rate?rating="+rating+"&uid="+uniqueId).toURI();
				
				Log.d(Apod.TAG, "Submitting rating of "+rating+" for today's image");
				
				httpRequest = new HttpGet(rateUrl);
				  
				HttpClient httpclient = new DefaultHttpClient();
				httpclient.execute(httpRequest);
			                                  
			} catch (Exception e) {
				e.printStackTrace(); 
			}
			
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
			credit.setText(cursor.getString(cursor.getColumnIndex("credit"))); 
			info.setText(cursor.getString(cursor.getColumnIndex("info")));
			
			try{
				if(!cursor.getString(cursor.getColumnIndex("youtube_url")).equals("null")){
					play.setText(cursor.getString(cursor.getColumnIndex("youtube_url")));
					play.setVisibility(View.VISIBLE);
					image.setVisibility(View.GONE);
					//ratingbar.setVisibility(View.GONE);
				}else{
					play.setVisibility(View.GONE); 
				}
			}catch (Exception e){
				play.setVisibility(View.GONE);
			}
			
			try {
				InputStream is;
				String uritemp;
				final ContentResolver cr = getContentResolver();
				try{
					uritemp = cursor.getString(cursor.getColumnIndex("uri"));	
					is = cr.openInputStream(Uri.parse(uritemp));
				} catch (FileNotFoundException e){
					File file = new File(this.getFilesDir().getPath()+"/apod.jpg");
					uritemp = Uri.fromFile(file).toString();
					is = cr.openInputStream(Uri.fromFile(file));
				}
							
				final String uri = uritemp;
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither = true;
				options.inSampleSize = 2;
				
				final Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
				is.close();
				
				image.setImageBitmap(bitmap);
				image.setVisibility(View.VISIBLE);

				imagedownloaded = true;
					
				image.setOnClickListener(new View.OnClickListener() {
				    public void onClick(View v) {
			    	
				    	final CharSequence[] items = {"Set Wallpaper", "View image in gallery", "Other actions"};

				    	AlertDialog.Builder builder = new AlertDialog.Builder(Today.this);
				    	builder.setTitle("Options");
				    	builder.setItems(items, new DialogInterface.OnClickListener() {
				    	    public void onClick(DialogInterface dialog, int item) {
				    	        switch(item){
				    	        	case 0:
				    	        		WallpaperManager wm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
										try {
											InputStream is2 = cr.openInputStream(Uri.parse(uri));
											wm.setStream(is2);
											Toast.makeText(Today.this, "Wallpaper set.", Toast.LENGTH_LONG).show();
										} catch (IOException e) {
											Toast.makeText(Today.this, "Couldn't autoset. Try setting manually.", Toast.LENGTH_LONG).show();
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
								
				
			} catch (Exception e) {
				Log.e(Apod.TAG, "Couldn't set bitmap to ImageView");
				imagedownloaded = false;
				e.printStackTrace();
			}
	        
		} catch (Exception e) {
			e.printStackTrace();
			title.setText("No image downloaded - Please refresh.");
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
	          Log.d(Apod.TAG, "received broadcast");
	          updateText();
	          findViewById(R.id.btn_title_refresh).setVisibility(View.VISIBLE);
	          findViewById(R.id.title_refresh_progress).setVisibility(View.GONE);
		}
     }
    
    
    
}