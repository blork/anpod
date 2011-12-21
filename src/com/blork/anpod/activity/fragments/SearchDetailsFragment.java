package com.blork.anpod.activity.fragments;

import android.os.Bundle;

import com.blork.anpod.activity.SearchActivity;
import com.blork.anpod.model.Picture;

// TODO: Auto-generated Javadoc
/**
 * This is the secondary fragment, displaying the details of a particular
 * item.
 */

public class SearchDetailsFragment extends DetailsFragment {
	public SearchDetailsFragment(Picture picture) {
		this.picture = picture;
	}

	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 *
	 * @param index the index
	 * @return the details fragment
	 */
	public static SearchDetailsFragment newInstance(int index) {		
		Picture picture = SearchActivity.pictures.get(index);

		SearchDetailsFragment f = new SearchDetailsFragment(picture);

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);
		f.setArguments(args);

		return f;
	}

}