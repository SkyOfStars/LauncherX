package com.launcher.db;

import com.launcher.db.info.MovieInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager
{
	private Context context;

	public DBManager( Context context )
	{
		super();
		this.context = context;
	}

	public boolean isTagInside( String tag )
	{
		DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
		SQLiteDatabase sd = db.getReadableDatabase();
		Cursor cursor = sd.query( DataBase.TABLE_NAME, new String[] { "_id", "tag" }, "tag=?",
						new String[] { tag }, null, null, null );
		if ( cursor.moveToNext() )
		{
			cursor.close();
			sd.close();
			db.close();
			return true;
		}
		cursor.close();
		sd.close();
		db.close();
		return false;
	}

	public MovieInfo getInfo( String tag )
	{
		MovieInfo info = null;
		DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
		SQLiteDatabase sd = db.getReadableDatabase();

		Cursor cursor = sd.query( DataBase.TABLE_NAME, new String[] { "_id", "tag", "title",
						"action", "img_v", "img_h", "img_s", "img_h2", "img_h3", "img_full",
						"app_package_name", "app_id" }, "tag=?", new String[] { tag }, null, null,
						null );
		if ( cursor.moveToNext() )
		{
			info = new MovieInfo();
			info.setTag( cursor.getString( cursor.getColumnIndex( "tag" ) ) );
			info.setTitle( cursor.getString( cursor.getColumnIndex( "title" ) ) );
			info.setAction( cursor.getString( cursor.getColumnIndex( "action" ) ) );
			info.setImg_v( cursor.getString( cursor.getColumnIndex( "img_v" ) ) );
			info.setImg_h( cursor.getString( cursor.getColumnIndex( "img_h" ) ) );
			info.setImg_s( cursor.getString( cursor.getColumnIndex( "img_s" ) ) );
			info.setImg_h2( cursor.getString( cursor.getColumnIndex( "img_h2" ) ) );
			info.setImg_h3( cursor.getString( cursor.getColumnIndex( "img_h3" ) ) );
			info.setImg_full( cursor.getString( cursor.getColumnIndex( "img_full" ) ) );
			info.setApp_package_name( cursor.getString( cursor.getColumnIndex( "app_package_name" ) ) );
			info.setApp_id( cursor.getString( cursor.getColumnIndex( "app_id" ) ) );
		}
		return info;
	}

	public void insertInfo( MovieInfo info )
	{
		DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
		SQLiteDatabase sd = db.getReadableDatabase();

		ContentValues value = new ContentValues();
		value.put( "tag", info.getTag() );
		value.put( "title", info.getTitle() );
		value.put( "action", info.getAction() );
		value.put( "img_v", info.getImg_v() );
		value.put( "img_h", info.getImg_h() );
		value.put( "img_s", info.getImg_s() );
		value.put( "img_h2", info.getImg_h2() );
		value.put( "img_h3", info.getImg_h3() );
		value.put( "img_full", info.getImg_full() );
		value.put( "app_package_name", info.getApp_package_name() );
		value.put( "app_id", info.getApp_id() );

		sd.insert( DataBase.TABLE_NAME, null, value );
		sd.close();
		db.close();
	}

	public void updateInfo( MovieInfo info )
	{
		DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
		SQLiteDatabase sd = db.getReadableDatabase();

		ContentValues value = new ContentValues();
		value.put( "tag", info.getTag() );
		value.put( "title", info.getTitle() );
		value.put( "action", info.getAction() );
		value.put( "img_v", info.getImg_v() );
		value.put( "img_h", info.getImg_h() );
		value.put( "img_s", info.getImg_s() );
		value.put( "img_h2", info.getImg_h2() );
		value.put( "img_h3", info.getImg_h3() );
		value.put( "img_full", info.getImg_full() );
		value.put( "app_package_name", info.getApp_package_name() );
		value.put( "app_id", info.getApp_id() );

		sd.update( DataBase.TABLE_NAME, value, "tag=?", new String[] { info.getTag() } );
		sd.close();
		db.close();
	}

	public void clearData()
	{
		DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
		SQLiteDatabase sd = db.getReadableDatabase();

		sd.execSQL( "DELETE FROM " + DataBase.TABLE_NAME );
		sd.close();
		db.close();
	}

	// ----------------�����Զ���App���ݿ����-------------------

	public MovieInfo getAddedInfo( String tag )
	{
		MovieInfo info = null;
		DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
		SQLiteDatabase sd = db.getReadableDatabase();

		Cursor cursor = sd.query( DataBase.TABLE_APP_ADDED_NAME, new String[] { "_id", "tag",
						"title", "app_package_name" }, "tag=?", new String[] { tag }, null, null,
						null );
		if ( cursor.moveToNext() )
		{
			info = new MovieInfo();
			info.setTag( cursor.getString( cursor.getColumnIndex( "tag" ) ) );
			info.setTitle( cursor.getString( cursor.getColumnIndex( "title" ) ) );
			info.setApp_package_name( cursor.getString( cursor.getColumnIndex( "app_package_name" ) ) );
		}
		cursor.close();
		sd.close();
		db.close();

		return info;
	}

	public boolean isAddTagInside( String tag )
	{
		DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
		SQLiteDatabase sd = db.getReadableDatabase();
		Cursor cursor = sd.query( DataBase.TABLE_APP_ADDED_NAME, new String[] { "_id", "tag" },
						"tag=?", new String[] { tag }, null, null, null );
		if ( cursor.moveToNext() )
		{
			cursor.close();
			sd.close();
			db.close();
			return true;
		}
		cursor.close();
		sd.close();
		db.close();
		return false;
	}

	public void insertAddInfo( String tag, String title, String packageName )
	{
		DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
		SQLiteDatabase sd = db.getReadableDatabase();

		ContentValues value = new ContentValues();
		value.put( "tag", tag );
		value.put( "title", title );
		value.put( "app_package_name", packageName );

		sd.insert( DataBase.TABLE_APP_ADDED_NAME, null, value );
		sd.close();
		db.close();
	}

	public void updateInfo( String tag, String title, String packageName )
	{
		DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
		SQLiteDatabase sd = db.getReadableDatabase();

		ContentValues value = new ContentValues();
		value.put( "tag", tag );
		value.put( "title", title );
		value.put( "app_package_name", packageName );

		sd.update( DataBase.TABLE_APP_ADDED_NAME, value, "tag=?", new String[] { tag } );
		sd.close();
		db.close();
	}

	public void clearAddedData( String tag )
	{
		DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
		SQLiteDatabase sd = db.getReadableDatabase();
		sd.execSQL( "delete from " + DataBase.TABLE_APP_ADDED_NAME + " where tag = " + tag );
		sd.close();
		db.close();
	}

	public boolean isAddPkgInside( String pkg )
	{
		DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
		SQLiteDatabase sd = db.getReadableDatabase();
		Cursor cursor = sd.query( DataBase.TABLE_APP_ADDED_NAME, new String[] { "_id",
						"app_package_name" }, "app_package_name=?", new String[] { pkg }, null,
						null, null );
		if ( cursor.moveToNext() )
		{
			cursor.close();
			sd.close();
			db.close();
			return true;
		}
		cursor.close();
		sd.close();
		db.close();
		return false;
	}

	public void clearAddedDataByPkg( String packageName )
	{

		if ( isAddPkgInside( packageName ) )
		{
			DataBase db = new DataBase( context, DataBase.NAME, null, DataBase.VERSION );
			SQLiteDatabase sd = db.getReadableDatabase();
			sd.execSQL( "delete from " + DataBase.TABLE_APP_ADDED_NAME
							+ " where app_package_name = " + packageName );
			sd.close();
			db.close();
		}
	}

}
