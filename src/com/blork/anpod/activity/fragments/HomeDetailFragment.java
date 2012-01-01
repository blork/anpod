package com.blork.anpod.activity.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blork.anpod.R;
import com.blork.anpod.activity.HomeActivity;
import com.blork.anpod.model.Picture;

// TODO: Auto-generated Javadoc
/**
 * This is the secondary fragment, displaying the details of a particular
 * item.
 */

public class HomeDetailFragment extends DetailFragment {
	public HomeDetailFragment() {
		super();
	}
	public HomeDetailFragment(Picture picture) {
		super();
		this.picture = picture;
	}

	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 *
	 * @param index the index
	 * @return the details fragment
	 */
	public static HomeDetailFragment newInstance(int index) {
		HomeDetailFragment f;
		if (!HomeActivity.pictures.isEmpty()) {
			Picture picture = HomeActivity.pictures.get(index);
			f = new HomeDetailFragment(picture);
		} else {
			f = new HomeDetailFragment();
			index = -1;
		}
		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);
		f.setArguments(args);
		return f;
	}

}
