package com.launcher.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBase extends SQLiteOpenHelper
{
	public static final String NAME = "launcher13";
	public static final int VERSION = 1;
	public static final String TABLE_NAME = "movie_info13";
	public static final String TABLE_APP_ADDED_NAME = "app_added_info13";

	public DataBase( Context context, String name, CursorFactory factory, int version )
	{
		super( context, name, factory, version );
	}

	@Override
	public void onCreate( SQLiteDatabase db )
	{
		db.execSQL( "CREATE TABLE IF NOT EXISTS  "
						+ TABLE_NAME
						+ " (_id integer primary key autoincrement,tag text,action text,title text,img_v text,img_h text,img_s text,img_h2 text,img_h3 text,img_full text,app_package_name text,app_id text)" );
		db.execSQL( "CREATE TABLE IF NOT EXISTS "
						+ TABLE_APP_ADDED_NAME
						+ " (_id integer primary key autoincrement,tag text, title text,app_package_name text)" );
	}

	@Override
	public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
	{
		db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME );
		db.execSQL( "DROP TABLE IF EXISTS " + TABLE_APP_ADDED_NAME );

	}
}
