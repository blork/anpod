package com.blork.anpod;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 

public class About extends Activity implements OnClickListener{	
	BroadcastReceiver updateReceiver;
	private String titleText = "";
	private TextView title;
	private TextView credit;
	private TextView info;
	private TextView play;
	private ImageView image;
	private Apod clickedApod;
	private static int wallpaperWidth;
	Uri imageUri = null;
	public boolean imagedownloaded = false;
	public SharedPreferences settings;
	
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	//private GestureLibrary gestures;
    private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	private GoogleAnalyticsTracker tracker;
	private String youtube_url = "";

	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = getWindow();
		// Eliminates color banding
		window.setFormat(PixelFormat.RGBA_8888);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu); 
        Log.d(Apod.TAG, "menu inflated");
        return true;
    }
    
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem save = menu.findItem(R.id.save);
		MenuItem share = menu.findItem(R.id.share);
		
		save.setEnabled(imagedownloaded);
		share.setEnabled(imagedownloaded);
        return true;
    }
	
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getTitle().equals("Share")){
    		Intent share = new Intent(Intent.ACTION_SEND);
	    	share.setType("text/plain");
	    	share.putExtra(Intent.EXTRA_TITLE, titleText);
   	
	    	if(youtube_url.equals("")){
	    		share.putExtra(Intent.EXTRA_STREAM, imageUri);
	    	} else {
	    		share.putExtra(Intent.EXTRA_TEXT, youtube_url);
	    	}
	    	
	    	startActivity(Intent.createChooser(share, "Share Text"));
    	}else if(item.getTitle().equals("Save image to SD Card")){
    		try{
	    		clickedApod.save();
	    		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, clickedApod.imageUri));
	    		Toast.makeText(this, "Saved to APOD/"+URLEncoder.encode(clickedApod.title), Toast.LENGTH_SHORT).show();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}else if(item.getTitle().equals("Submit your rating")){
    		
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
    	        	   RateThread thread = new RateThread(rating, clickedApod.date);
    	               thread.start();
    	               dialog.cancel();
    	           }
    	       });
    		
    		builder.show();
    	}
    	
        return true;
    }

    
	public void onResume(){
		super.onResume();
		
		youtube_url = "";

		try {
			clickedApod = List.apodList.get(List.position);
	        
			play.setVisibility(View.GONE);
			titleText = clickedApod.title;
			title.setText(titleText);
			
			credit.setText(clickedApod.credit); 
			info.setText(clickedApod.info);
		
		
			try{
				if(!clickedApod.youtube.equals("null")){
					youtube_url = clickedApod.youtube;
					play.setText(clickedApod.youtube);
					play.setVisibility(View.VISIBLE);
					image.setVisibility(View.GONE);
				}else{
					play.setVisibility(View.GONE); 
				}
			}catch (Exception e){
				play.setVisibility(View.GONE);
			}
			
			File dir = this.getFilesDir();
			
			String imageFile = this.clickedApod.date + ".jpg";
				
			File file = new File(dir.getPath()+"/"+imageFile);
			
			try {
				imageUri = Uri.fromFile(file);
				
				final ContentResolver cr = getContentResolver();
				InputStream is = cr.openInputStream(imageUri);
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
				
				
				About.this.clickedApod.image = bitmap;//TODO: TEST
				
				image.setImageBitmap(bitmap);
				image.setVisibility(View.VISIBLE);
				
				imagedownloaded = true;
				
				image.setOnClickListener(new View.OnClickListener() {
				    public void onClick(View v) {
				    	final CharSequence[] items = {"Set Wallpaper", "View image in gallery", "Other actions"};

				    	AlertDialog.Builder builder = new AlertDialog.Builder(About.this);
				    	builder.setTitle("Options");
				    	builder.setItems(items, new DialogInterface.OnClickListener() {
				    	    public void onClick(DialogInterface dialog, int item) {
				    	        switch(item){
				    	        	case 0:
				    	        		WallpaperManager wm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
										try {
											InputStream is2 = cr.openInputStream(imageUri);
											wm.setStream(is2);
											Toast.makeText(About.this, "Wallpaper set.", Toast.LENGTH_LONG).show();
										} catch (IOException e) {
											Toast.makeText(About.this, "Couldn't autoset. Try setting manually.", Toast.LENGTH_LONG).show();
											e.printStackTrace();
									}
				    	        	break;
				    	        	case 1:
								    	Intent intent = new Intent(Intent.ACTION_VIEW);
								    	intent.setDataAndType(imageUri, "image/jpeg");
								    	startActivity(intent);
				    	        	break;
				    	        	case 2:
								    	Intent intent1 = new Intent();
								    	intent1.setDataAndType(imageUri, "image/*");	
								    	startActivity(intent1);
				    	        	break;	
				    	        }
				    	    }
				    	});
				    	builder.show();
				    }
				}); 

				
			} catch(FileNotFoundException e) {
				Log.i(Apod.TAG, "File not cached.");
				if(About.this.clickedApod.image != null){
					image.setImageBitmap(About.this.clickedApod.image);
					image.setVisibility(View.VISIBLE);
				} else {
					new DownloadApodImageTask().execute();
				}
			} catch(Throwable e) {
				e.printStackTrace();
			}
			
	
			CacheThread thread = new CacheThread();
			thread.start();
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			finish();
		} catch(IndexOutOfBoundsException e) {
			if(List.position == -1) {
				finish();
			} else {
		    	try {
		    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		            boolean byRating = prefs.getBoolean("byRating", false);
		            
					List.apodList.addAll(List.getApodList(List.resultsPage, byRating));
					List.adapter.notifyDataSetChanged();
					List.resultsPage++;
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				
				e.printStackTrace();
				
				finish();
				startActivity(getIntent());
			}
		}
		
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



class CacheThread extends Thread {
	@SuppressWarnings("unchecked")
	public void run(){
		File dir = About.this.getFilesDir();
		File[] files = dir.listFiles();
		

	    
		long cacheSize = 0;
		for (File f : files ) {
			if(!f.getName().equals("apod.jpg")) {
				cacheSize = cacheSize + f.length();
			}
		}
		Log.i(Apod.TAG, "Cache size is: "+(cacheSize/1024)+"KB");
		
		
		if(cacheSize > (1024*1024)) {
		    Arrays.sort( files, new Comparator() {
			      public int compare(final Object o1, final Object o2) {
			        return new Long(((File)o1).lastModified()).compareTo
			             (new Long(((File) o2).lastModified()));
			      }
			    }); 
		    
		    int deleted = 0;
		    
			for (File f : files) {
				if(!f.getName().equals("apod.jpg")) {
					f.delete();
					Log.i(Apod.TAG, "Deleting cached file: "+f.getName());
					deleted++;
				}
				
				if(deleted > (files.length/2)) {
					break;
				}

			}
			System.gc();
			Log.i(Apod.TAG, "Removed "+deleted+" files from the cache.");
			
		}
	
	}
}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start("UA-20756745-1", this);
        tracker.trackPageView("/about");
        tracker.dispatch();
        
        //GESTURES!!
//        gestures = GestureLibraries.fromRawResource(this, R.raw.gestures);
//        if (!gestures.load()) {
//        	finish();
//        }
//        
//        GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
//        gestures.addOnGesturePerformedListener(this);
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
        
     
        settings = PreferenceManager.getDefaultSharedPreferences(About.this);	
            	
        TextView titleBarText = (TextView)findViewById(R.id.TitleBarText);
        titleBarText.setText("About the Picture");
        
        updateReceiver = new UpdateReceiver();
        registerReceiver(updateReceiver, new IntentFilter(AnpodService.ACTION_NEW_APOD)); 

		WallpaperManager wm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
		About.wallpaperWidth = wm.getDesiredMinimumWidth();
		
        title = (TextView)findViewById(R.id.image_title);
		credit = (TextView)findViewById(R.id.image_credit);
		info = (TextView)findViewById(R.id.image_info);
		image = (ImageView)findViewById(R.id.image);
		play = (TextView)findViewById(R.id.play);
		
		
		title.setSelected(true);
		credit.setSelected(true);

		ImageButton refreshButton = (ImageButton)this.findViewById(R.id.btn_title_refresh);
		ImageButton homeButton = (ImageButton)this.findViewById(R.id.btn_title_home);
		
		
		homeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(About.this, Main.class));
			}
		});
		
		refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        findViewById(R.id.btn_title_refresh).setVisibility(View.GONE);
		        findViewById(R.id.title_refresh_progress).setVisibility(View.VISIBLE);
	    		Intent intent = new Intent(About.this, AnpodService.class);
	    		intent.putExtra("runonce", true);
	    		startService(intent); 
			}
		});
		View scroll = findViewById(R.id.scroll);
		scroll.setOnClickListener(About.this); 
		scroll.setOnTouchListener(gestureListener);
    }
    
    class RateThread extends Thread {
    	private int rating;
    	private String date;
    	
		public RateThread(int rating, String date) {
			this.rating = rating;
			this.date = date;
		}

		public void run(){

			try {
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(About.this);                             
				String uniqueId = prefs.getString("uniqueId", "");
				  
				if(uniqueId.equals("")){
					throw new Exception();
				}
				  
				HttpGet httpRequest = null;
				URI rateUrl = new URL(Apod.url+"rate?rating="+rating+"&date="+date).toURI();
				
				Log.d(Apod.TAG, "Submitting rating of "+rating+" for "+date+" image");
				
				httpRequest = new HttpGet(rateUrl);
				  
				HttpClient httpclient = new DefaultHttpClient();
				httpclient.execute(httpRequest);
			                                  
			} catch (Exception e) {
				e.printStackTrace(); 
			}
			
		}
	}
    
  private class DownloadApodImageTask extends AsyncTask<Void, Void, Boolean> {
	private RotateAnimation rotate;
	private View throbber;

	protected void onPreExecute(){
		System.gc();
		
		Log.d(Apod.TAG,"Downloading image");
		rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotate.setDuration(4000);
		rotate.setRepeatMode(Animation.RESTART);
		rotate.setRepeatCount(Animation.INFINITE);
		
		throbber = findViewById(R.id.throbber);
		throbber.setVisibility(View.VISIBLE);
		throbber.startAnimation(rotate);
		
     }

	public Boolean doInBackground(Void... params) {
		try {
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = true;
			options.inSampleSize = 2;
			
	
			About.this.clickedApod.image = Apod.getBitmap(About.wallpaperWidth, clickedApod.date);


		} catch (Throwable t) {
			Log.e(Apod.TAG, "Couldn't set bitmap to ImageView");
			t.printStackTrace();
		}
		return true;		
	}
	
	protected void onPostExecute(Boolean result){		
		String imageFile = About.this.clickedApod.date + ".jpg";
		File dir = About.this.getFilesDir();
		File file = new File(dir.getPath()+"/"+imageFile);
				
		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = About.this.openFileOutput(imageFile, Context.MODE_WORLD_READABLE);
			BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
			
			About.this.clickedApod.image.compress(CompressFormat.JPEG, 75, bos);
			
			bos.flush(); 
			bos.close();
			fileOutputStream.flush();
			fileOutputStream.close();

			InputStream is = About.this.openFileInput(imageFile); 
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = true;
			options.inSampleSize = 2; //TODO: memory!!!
			Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
			image.setImageBitmap(bitmap);
			image.setVisibility(View.VISIBLE);
			Animation fadeIn = AnimationUtils.loadAnimation(About.this, R.anim.fadein);
			image.startAnimation(fadeIn); 
			
			imagedownloaded = true;
			
			throbber.clearAnimation();
			throbber.setVisibility(View.GONE);	
			
			final Uri imageUri = Uri.fromFile(file);
			
			final ContentResolver cr = getContentResolver();
			
			image.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View v) {
			    	final CharSequence[] items = {"Set Wallpaper", "View image in gallery", "Other actions"};

			    	AlertDialog.Builder builder = new AlertDialog.Builder(About.this);
			    	builder.setTitle("Options");
			    	builder.setItems(items, new DialogInterface.OnClickListener() {
			    	    public void onClick(DialogInterface dialog, int item) {
			    	        switch(item){
			    	        	case 0:
			    	        		WallpaperManager wm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
									try {
										InputStream is2 = cr.openInputStream(imageUri);
										wm.setStream(is2);
										Toast.makeText(About.this, "Wallpaper set.", Toast.LENGTH_LONG).show();
									} catch (IOException e) {
										Toast.makeText(About.this, "Couldn't autoset. Try setting manually.", Toast.LENGTH_LONG).show();
										e.printStackTrace();
								}
			    	        	break;
			    	        	case 1:
							    	Intent intent = new Intent(Intent.ACTION_VIEW);
							    	intent.setDataAndType(imageUri, "image/jpeg");
							    	startActivity(intent);
			    	        	break;
			    	        	case 2:
							    	Intent intent1 = new Intent();
							    	intent1.setDataAndType(imageUri, "image/*");	
							    	startActivity(intent1);
			    	        	break;	
			    	        }
			    	    }
			    	});
			    	builder.show();
			    }
			});
			
		} catch (NullPointerException e){
			throbber.clearAnimation();
			throbber.setVisibility(View.GONE);	
			play.setText(About.this.clickedApod.youtube);
			play.setVisibility(View.VISIBLE);
		} catch (Throwable t) {
			t.printStackTrace();
			AlertDialog.Builder builder = new AlertDialog.Builder(About.this);
			builder.setTitle("Error");
			builder.setMessage("There was an problem retrieving the image.")
			       .setCancelable(false)
			       .setNeutralButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			    			throbber.clearAnimation();
			    			throbber.setVisibility(View.GONE);	
			           } 
			       });
			AlertDialog alert = builder.create();
			alert.show();
		}

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



  class MyGestureDetector extends SimpleOnGestureListener {
	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	        try {
	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
	                return false;
	            // right to left swipe
	            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	            	List.apodList.get(List.position).image = null;
	            	
					List.position++;
					finish();
					startActivity(getIntent());
					overridePendingTransition(R.anim.slideright, R.anim.slideright2);
					Log.d(Apod.TAG, "slideright");
				} else if(e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
					List.apodList.get(List.position).image = null;
					
					List.position--;
					finish();
					startActivity(getIntent());
					overridePendingTransition(R.anim.slideleft2, R.anim.slideleft);
					Log.d(Apod.TAG, "slideleft");
				}
	        } catch (Exception e) {
	            // nothing
	        }
	        return false;
	    }

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}

}

