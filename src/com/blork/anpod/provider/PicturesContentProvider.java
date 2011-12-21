/*
 ******************************************************************************
 * Parts of this code sample are licensed under Apache License, Version 2.0   *
 * Copyright (c) 2009, Android Open Handset Alliance. All rights reserved.    *
 *																			  *																			*
 * Except as noted, this code sample is offered under a modified BSD license. *
 * Copyright (C) 2010, Motorola Mobility, Inc. All rights reserved.           *
 * 																			  *
 * For more details, see MOTODEV_Studio_for_Android_LicenseNotices.pdf        * 
 * in your installation folder.                                               *
 ******************************************************************************
 */
package com.blork.anpod.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.blork.anpod.util.SQLHelper;

// TODO: Auto-generated Javadoc
/**
 * The Class PicturesContentProvider.
 */
public class PicturesContentProvider extends ContentProvider {

	/** The db helper. */
	private SQLHelper dbHelper;
	
	/** The PICTURE s_ projectio n_ map. */
	private static HashMap<String, String> PICTURES_PROJECTION_MAP;
	
	/** The Constant TABLE_NAME. */
	public static final String TABLE_NAME = "pictures";
	
	/** The Constant AUTHORITY. */
	private static final String AUTHORITY = "com.blork.anpod.provider.picturescontentprovider";

	/** The Constant CONTENT_URI. */
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_NAME);
	
	/** The Constant ID_FIELD_CONTENT_URI. */
	public static final Uri ID_FIELD_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME.toLowerCase() + "/id");
	
	/** The Constant CREDIT_FIELD_CONTENT_URI. */
	public static final Uri CREDIT_FIELD_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME.toLowerCase() + "/credit");
	
	/** The Constant IMGURURL_FIELD_CONTENT_URI. */
	public static final Uri IMGURURL_FIELD_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME.toLowerCase() + "/imgururl");
	
	/** The Constant INFO_FIELD_CONTENT_URI. */
	public static final Uri INFO_FIELD_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME.toLowerCase() + "/info");
	
	/** The Constant TITLE_FIELD_CONTENT_URI. */
	public static final Uri TITLE_FIELD_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME.toLowerCase() + "/title");
	
	/** The Constant UID_FIELD_CONTENT_URI. */
	public static final Uri UID_FIELD_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME.toLowerCase() + "/uid");
	
	/** The Constant DEFAULT_SORT_ORDER. */
	public static final String DEFAULT_SORT_ORDER = "Id ASC";
	
	/** The Constant REVERSE_SORT_ORDER. */
	public static final String REVERSE_SORT_ORDER = "Id DESC";
	
	/** The Constant URL_MATCHER. */
	private static final UriMatcher URL_MATCHER;

	/** The Constant PICTURES. */
	private static final int PICTURES = 1;
	
	/** The Constant PICTURES_ID. */
	private static final int PICTURES_ID = 2;
	
	/** The Constant PICTURES_CREDIT. */
	private static final int PICTURES_CREDIT = 3;
	
	/** The Constant PICTURES_IMGURURL. */
	private static final int PICTURES_IMGURURL = 4;
	
	/** The Constant PICTURES_INFO. */
	private static final int PICTURES_INFO = 5;
	
	/** The Constant PICTURES_TITLE. */
	private static final int PICTURES_TITLE = 6;
	
	/** The Constant PICTURES_UID. */
	private static final int PICTURES_UID = 7;
	
	// Content values keys (using column names)
	/** The Constant ID. */
	public static final String ID = "Id";
	
	/** The Constant CREDIT. */
	public static final String CREDIT = "Credit";
	
	/** The Constant IMGURURL. */
	public static final String IMGURURL = "ImgurUrl";
	
	/** The Constant INFO. */
	public static final String INFO = "Info";
	
	/** The Constant TITLE. */
	public static final String TITLE = "Title";
	
	/** The Constant UID. */
	public static final String UID = "Uid";

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	public boolean onCreate() {
		Log.d("", "creating content provider");
		dbHelper = new SQLHelper(getContext());
		return (dbHelper == null) ? false : true;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	public Cursor query(Uri url, String[] projection, String selection,
			String[] selectionArgs, String sort) {
		SQLiteDatabase mDB = dbHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (URL_MATCHER.match(url)) {
		case PICTURES:
			qb.setTables(TABLE_NAME);
			qb.setProjectionMap(PICTURES_PROJECTION_MAP);
			break;
		case PICTURES_ID:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("id='" + url.getPathSegments().get(2) + "'");
			break;
		case PICTURES_CREDIT:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("credit='" + url.getPathSegments().get(2) + "'");
			break;
		case PICTURES_IMGURURL:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("imgururl='" + url.getPathSegments().get(2) + "'");
			break;
		case PICTURES_INFO:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("info='" + url.getPathSegments().get(2) + "'");
			break;
		case PICTURES_TITLE:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("title='" + url.getPathSegments().get(2) + "'");
			break;
		case PICTURES_UID:
			qb.setTables(TABLE_NAME);
			qb.appendWhere("uid='" + url.getPathSegments().get(2) + "'");
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
		String orderBy = "";
		if (TextUtils.isEmpty(sort)) {
			orderBy = DEFAULT_SORT_ORDER;
		} else {
			orderBy = sort;
		}
		Cursor c = qb.query(mDB, projection, selection, selectionArgs, null,
				null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), url);
		return c;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	public String getType(Uri url) {
		switch (URL_MATCHER.match(url)) {
		case PICTURES:
			return "vnd.android.cursor.dir/vnd.com.blork.anpod.provider.pictures";
		case PICTURES_ID:
			return "vnd.android.cursor.item/vnd.com.blork.anpod.provider.pictures";
		case PICTURES_CREDIT:
			return "vnd.android.cursor.item/vnd.com.blork.anpod.provider.pictures";
		case PICTURES_IMGURURL:
			return "vnd.android.cursor.item/vnd.com.blork.anpod.provider.pictures";
		case PICTURES_INFO:
			return "vnd.android.cursor.item/vnd.com.blork.anpod.provider.pictures";
		case PICTURES_TITLE:
			return "vnd.android.cursor.item/vnd.com.blork.anpod.provider.pictures";
		case PICTURES_UID:
			return "vnd.android.cursor.item/vnd.com.blork.anpod.provider.pictures";
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	public Uri insert(Uri url, ContentValues initialValues) {
		SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		long rowID;
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		if (URL_MATCHER.match(url) != PICTURES) {
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		rowID = mDB.insert("pictures", "pictures", values);
		if (rowID > 0) {
			Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		throw new SQLException("Failed to insert row into " + url);
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	public int delete(Uri url, String where, String[] whereArgs) {
		SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		int count;
		String segment = "";
		switch (URL_MATCHER.match(url)) {
		case PICTURES:
			count = mDB.delete(TABLE_NAME, where, whereArgs);
			break;
		case PICTURES_ID:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"id="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case PICTURES_CREDIT:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"credit="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case PICTURES_IMGURURL:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"imgururl="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case PICTURES_INFO:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"info="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case PICTURES_TITLE:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"title="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case PICTURES_UID:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.delete(TABLE_NAME,
					"uid="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	public int update(Uri url, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase mDB = dbHelper.getWritableDatabase();
		int count;
		String segment = "";
		switch (URL_MATCHER.match(url)) {
		case PICTURES:
			count = mDB.update(TABLE_NAME, values, where, whereArgs);
			break;
		case PICTURES_ID:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"id="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case PICTURES_CREDIT:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"credit="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case PICTURES_IMGURURL:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"imgururl="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case PICTURES_INFO:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"info="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case PICTURES_TITLE:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"title="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		case PICTURES_UID:
			segment = "'" + url.getPathSegments().get(2) + "'";
			count = mDB.update(TABLE_NAME, values,
					"uid="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	static {
		URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase(), PICTURES);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase() + "/id" + "/*",
				PICTURES_ID);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase() + "/credit"
				+ "/*", PICTURES_CREDIT);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase() + "/imgururl"
				+ "/*", PICTURES_IMGURURL);
		URL_MATCHER.addURI(AUTHORITY,
				TABLE_NAME.toLowerCase() + "/info" + "/*", PICTURES_INFO);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase() + "/title"
				+ "/*", PICTURES_TITLE);
		URL_MATCHER.addURI(AUTHORITY, TABLE_NAME.toLowerCase() + "/uid" + "/*",
				PICTURES_UID);
		
		PICTURES_PROJECTION_MAP = new HashMap<String, String>();
		PICTURES_PROJECTION_MAP.put(ID, "id");
		PICTURES_PROJECTION_MAP.put(CREDIT, "credit");
		PICTURES_PROJECTION_MAP.put(IMGURURL, "imgururl");
		PICTURES_PROJECTION_MAP.put(INFO, "info");
		PICTURES_PROJECTION_MAP.put(TITLE, "title");
		PICTURES_PROJECTION_MAP.put(UID, "uid");
	}
}
