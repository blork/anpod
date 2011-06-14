package com.blork.anpod;

import static android.provider.BaseColumns._ID;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ApodData extends SQLiteOpenHelper{
	
	private static final String DATABASE_NAME = "apod.db"; 
	private static final int DATABASE_VERSION = 49; 
	 
	/** Create a helper object for the Events database */
	public ApodData(Context ctx){
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}
	 
	@Override
	public void onCreate(SQLiteDatabase db){
		db.execSQL("CREATE TABLE apod (" 
				+_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
						"title TEXT," +
						"credit TEXT," +
						"info TEXT," +
						"uri TEXT," +
						"rating TEXT DEFAULT NULL,"+
						"date DATETIME DEFAULT CURRENT_TIMESTAMP," +
						"youtube_url TEXT" +
						");"); 
	}
	  

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL("DROP TABLE IF EXISTS apod");
		onCreate(db);
	}
}