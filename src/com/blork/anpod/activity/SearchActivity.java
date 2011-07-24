package com.blork.anpod.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.blork.anpod.R;
import com.blork.anpod.model.Picture;
import com.blork.anpod.util.UIUtils;


// TODO: Auto-generated Javadoc
/**
 * The Class SearchActivity.
 */
public class SearchActivity extends FragmentActivity {

    /** The query. */
    public static String query = null;

	public static List<Picture> pictures = new ArrayList<Picture>();

    
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                		
        if (getIntent().getAction().equals(Intent.ACTION_SEARCH)) {
        	String query = getIntent().getStringExtra(SearchManager.QUERY);
	        SearchActivity.query = query;
        } else {
        	finish();
        }
        
        setContentView(R.layout.search_fragment_layout);
        
        UIUtils.setupActionBar(this, "Searched for:  '" + query + "'");
    }
	
	
	protected void onNewIntent (Intent intent) {
		pictures.clear();
		finish();
		startActivity(intent);
	}


}
