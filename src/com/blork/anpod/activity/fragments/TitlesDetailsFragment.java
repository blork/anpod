package com.blork.anpod.activity.fragments;

import com.blork.anpod.activity.HomeActivity;

import android.os.Bundle;

// TODO: Auto-generated Javadoc
/**
 * This is the secondary fragment, displaying the details of a particular
 * item.
 */

public class TitlesDetailsFragment extends DetailsFragment {
	
	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 *
	 * @param index the index
	 * @return the details fragment
	 */
	public static TitlesDetailsFragment newInstance(int index) {
		TitlesDetailsFragment f = new TitlesDetailsFragment();
		f.pictures = HomeActivity.pictures;
		
		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);
		f.setArguments(args);

		return f;
	}

}