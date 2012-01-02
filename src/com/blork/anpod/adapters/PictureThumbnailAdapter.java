package com.blork.anpod.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blork.anpod.R;
import com.blork.anpod.model.Picture;

// TODO: Auto-generated Javadoc
/**
 * The Class PictureThumbnailAdapter.
 */
public class PictureThumbnailAdapter extends ArrayAdapter<Picture> {
	
	/** The context. */
	private Context context;

	/**
	 * Instantiates a new picture thumbnail adapter.
	 *
	 * @param context the context
	 * @param textViewResourceId the text view resource id
	 * @param pictureList the picture list
	 */
	public PictureThumbnailAdapter(Context context, int textViewResourceId, List<Picture> pictureList) {
		super(context, textViewResourceId, pictureList);
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	public View getView(int position, View convertView,
			ViewGroup parent) {
		View row=convertView;
		PictureThumbnailWrapper wrapper=null;

		if (row==null) {													
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			row=inflater.inflate(R.layout.list_item, null);
			wrapper=new PictureThumbnailWrapper(row);
			row.setTag(wrapper);
		}
		else {
			wrapper=(PictureThumbnailWrapper)row.getTag();
		}

		wrapper.populateFrom(getItem(position));

		return(row);
	}
}

class PictureThumbnailWrapper {
	private TextView title = null;
	private TextView credit = null;
	private ImageView image = null;
	private View row = null;

	PictureThumbnailWrapper(View row) {
		this.row=row;
	}

	void populateFrom(Picture picture) {
		getTitle().setText(picture.title);
		getCredit().setText(picture.credit);

		if (picture.imgurId!=null) {
			getCover().setImageResource(R.drawable.cover_placeholder);
			getCover().setTag(picture.getThumbnailImageUrl());
		}
	}

	private TextView getTitle() {
		if (title==null) {
			title=(TextView)row.findViewById(R.id.item_name);
		}

		return(title);
	}


	private TextView getCredit() {
		if (credit==null) {
			credit=(TextView)row.findViewById(R.id.item_credit);
		}

		return(credit);
	}

	private ImageView getCover() {
		if (image==null) {
			image=(ImageView)row.findViewById(R.id.item_image);
		}

		return(image);
	}
}