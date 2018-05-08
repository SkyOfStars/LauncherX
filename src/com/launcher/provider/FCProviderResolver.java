package com.launcher.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class FCProviderResolver
{
	private static class InstanceHolder
	{
		public static FCProviderResolver instance = new FCProviderResolver();
	}

	private FCProviderResolver()
	{

	}

	public static final String PATH_CONFIG = "config";

	public static final String PATH_PARAMS_ONLINE = "params_online";

	private static final String TAG = "FCProviderResolver";

	private static final String AUTHORITY = "com.fc.contentprovider";

	private static final String URI = "content://com.fc.contentprovider";

	private Context mContext;

	private String mPath = PATH_CONFIG;

	public static FCProviderResolver getInstance()
	{
		return InstanceHolder.instance;
	}

	public FCProviderResolver init( Context context )
	{
		mContext = context;
		return this;
	}

	public void setPath( String path )
	{
		mPath = path;
	}

	public String getConfig( String key, String defaultValue )
	{
		Cursor cursor = mContext.getContentResolver().query( Uri.parse( URI + "/" + mPath ), null,
						key, null, null );

		if ( cursor == null )
		{
			return defaultValue;
		}

		while ( cursor.moveToNext() )
		{
			if ( cursor.getString( 0 ).equals( key ) )
			{
				String value = cursor.getString( 1 );
				if ( value == null )
				{
					return defaultValue;
				}

				return value.trim();
			}
		}

		cursor.close();
		return defaultValue;
	}

	public int setLedContent( String content )
	{
		return mContext.getContentResolver().update( Uri.parse( URI + "/" + "led_display" ),
						new ContentValues(), content, new String[] {} );
	}
}
