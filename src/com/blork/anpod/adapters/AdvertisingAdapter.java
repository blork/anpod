//package com.blork.anpod.adapters;
//
//import java.util.Arrays;
//import java.util.HashSet;
//
//import android.app.Activity;
//import android.database.DataSetObserver;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AbsListView;
//import android.widget.BaseAdapter;
//
//import com.google.ads.AdRequest;
//import com.google.ads.AdSize;
//import com.google.ads.AdView;
//
///**
// * List adapter decorator that inserts adverts into the list.
// * @author Daniel Dyer
// */
//public class AdvertisingAdapter extends BaseAdapter
//{
//	private static final String ADMOB_PUBLISHER_ID = "a14e4bffa8a0175";
//
//	private final Activity activity;
//	private final BaseAdapter delegate;
//	public static final int AD_AMOUNT = 8;
//	
//	public static int adsShown = 0;
//
//	public AdvertisingAdapter(Activity activity, BaseAdapter delegate)
//	{
//		this.activity = activity;
//		this.delegate = delegate;
//		delegate.registerDataSetObserver(new DataSetObserver()
//		{
//			@Override
//			public void onChanged()
//			{
//				notifyDataSetChanged();
//			}
//
//			@Override
//			public void onInvalidated()
//			{
//				notifyDataSetInvalidated();
//			}
//		});
//	}
//
//	public int getCount()
//	{
//		return delegate.getCount() + adsShown;
//	}
//
//	public Object getItem(int i)
//	{
//		return delegate.getItem(i - adsShown);
//	}
//
//	public long getItemId(int i)
//	{
//		return delegate.getItemId(i - adsShown);
//	}
//
//	public View getView(int position, View convertView, ViewGroup parent)
//	{
//		if (position % AD_AMOUNT == 0)
//		{
//			if (convertView instanceof AdView)
//			{
//				return convertView;
//			}
//			else
//			{
//				AdView adView = new AdView(activity, AdSize.BANNER, ADMOB_PUBLISHER_ID);
//				// Disable focus for sub-views of the AdView to avoid problems with
//				// trackpad navigation of the list.
//				for (int i = 0; i < adView.getChildCount(); i++)
//				{
//					adView.getChildAt(i).setFocusable(false);
//				}
//				adView.setFocusable(false);
//				// Default layout params have to be converted to ListView compatible
//				// params otherwise there will be a ClassCastException.
//				float density = activity.getResources().getDisplayMetrics().density;
//				int height = Math.round(AdSize.BANNER.getHeight() * density);
//				AbsListView.LayoutParams params
//				= new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,
//						height);
//				adView.setLayoutParams(params);
//				
//				AdRequest request = new AdRequest();
////
////				request.addTestDevice("CF95DC53F383F9A836FD749F3EF439CD");
////				request.addTestDevice(AdRequest.TEST_EMULATOR);
//				
//				String[] keywords = {"nasa", "space", "astronomy", "android"};
//				
//				request.setKeywords(new HashSet<String>(Arrays.asList(keywords)));
//				adView.loadAd(request);
//				
//				adsShown++;
//				
//				return adView;
//			}
//		}
//		else
//		{
//			Log.e("!!!", position + " " + (position/AD_AMOUNT));
//			return delegate.getView(position - ((position / AD_AMOUNT) + 1), convertView, parent);
//		}
//	}
//
//	@Override
//	public int getViewTypeCount()
//	{
//		return delegate.getViewTypeCount() + adsShown;
//	}
//
//	@Override
//	public int getItemViewType(int position)
//	{
//		return position % AD_AMOUNT == 0 ? delegate.getViewTypeCount()
//				: delegate.getItemViewType(position - adsShown);
//	}
//
//	@Override
//	public boolean areAllItemsEnabled()
//	{
//		return false;
//	}
//
//	@Override
//	public boolean isEnabled(int position)
//	{
//		return position % AD_AMOUNT != 0 && delegate.isEnabled(position - adsShown);
//	}
//}