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
package com.blork.anpod.util;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * <p>
 * This class copies a SQLite database from your application's assets directory
 * to /data/data/<your_application_package>/databases/ so you can access it
 * using the SQLite APIs provided by the Android SDK. Note that
 * {@link SQLHelper#copyDatabaseFile()} checks for the existence of the database
 * and only copies it if needed.
 * </p>
 * <p>
 * {@link SQLHelper#copyDatabaseFile()} calls
 * {@link SQLiteOpenHelper#getReadableDatabase()}, which in turn calls
 * {@link SQLiteOpenHelper#onCreate(SQLiteDatabase)}. Be aware that the
 * implementation of the overridden
 * {@link SQLiteOpenHelper#onCreate(SQLiteDatabase)} must remain empty in order
 * for the copy operation to work correctly.
 * </p>
 * <p>
 * This class includes a constructor
 * {@link SQLHelper#SQLHelper(Context, boolean)} which allows you to control
 * whether the database file should be copied when the class is instantiated.
 * </p>
 * 
 * @see SQLiteOpenHelper
 */
public class SQLHelper extends SQLiteOpenHelper {

	/** The D b_ name. */
	private static String DB_NAME = "apod.db";

	/** The D b_ version. */
	private static int DB_VERSION = 1004;


	/**
	 * Constructor Keeps a reference to the passed context in order to access
	 * the application's assets.
	 * 
	 * @param context
	 *            Context to be used
	 */
	public SQLHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			db.execSQL("CREATE TABLE pictures (" 
					+"Id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					"Credit TEXT," +
					"ImgurUrl TEXT," +
					"Info TEXT," +
					"Title TEXT," +
					"Uid TEXT" +
			");"); 
			db.setTransactionSuccessful();
		} catch (Exception e)  {

		} finally {
			db.endTransaction();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("", "ONUPGRADE " + newVersion + " " +oldVersion);

		db.beginTransaction();

		try {
			db.execSQL("DROP TABLE IF EXISTS apod");
			db.execSQL("DROP TABLE IF EXISTS pictures");
			db.setTransactionSuccessful();
		} catch (Exception e) {

		} finally {
			db.endTransaction();
		}

		onCreate(db);
	}

}
