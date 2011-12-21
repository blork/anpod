package com.blork.anpod.activity.fragments;

import com.blork.anpod.activity.HomeActivity;
import com.blork.anpod.model.Picture;

import android.os.Bundle;

// TODO: Auto-generated Javadoc
/**
 * This is the secondary fragment, displaying the details of a particular
 * item.
 */

public class TitlesDetailsFragment extends DetailsFragment {
	
	public TitlesDetailsFragment(Picture picture) {
		this.picture = picture;
	}

	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 *
	 * @param index the index
	 * @return the details fragment
	 */
	public static TitlesDetailsFragment newInstance(int index) {
		Picture picture = HomeActivity.pictures.get(index);

		TitlesDetailsFragment f = new TitlesDetailsFragment(picture);
		
		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);
		f.setArguments(args);

		return f;
	}

}