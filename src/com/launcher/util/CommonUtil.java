package com.launcher.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.widget.TextView;

public class CommonUtil
{
	private static Boolean isFirstRun = false;

	public static int getWinWidth( Activity context )
	{
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics( dm );
		return dm.widthPixels;
	}

	public static boolean isNetworkConnected( Context context )
	{
		if ( context != null )
		{
			ConnectivityManager mConnectivityManager = ( ConnectivityManager ) context
							.getSystemService( Context.CONNECTIVITY_SERVICE );
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if ( mNetworkInfo != null )
			{
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public static int dip2px( Context context, float dipValue )
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return ( int ) ( dipValue * scale + 0.5f );
	}

	/**
	 * 暂时保留，直播里的逻辑
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isFirstRun( Context context )
	{
		if ( isFirstRun )
		{
			return true;
		}

		int firstinit = Integer.parseInt( SystemProperties.get( "sys.dvb.live.firstinit", "1" ) );
		if ( firstinit == 1 ) // 当为1，则是第一次Run
		{
			SystemProperties.set( "sys.dvb.live.firstinit", "0" );
			isFirstRun = true;
			return true;
		}

		return false;
	}

	public static Boolean isBootLive( Context ctx )
	{
		try
		{
			String bootPkg = null;
			Context context = ctx.createPackageContext( "com.fc.setting",
							Context.CONTEXT_IGNORE_SECURITY );
			SharedPreferences sharedPreferences = context.getSharedPreferences( "sys_conf", 4 );
			String pkg = sharedPreferences.getString( "setting_boot_app", null );
			if ( pkg != null )
			{
				bootPkg = pkg.split( ";" )[1];
			}
			if ( "com.dvb.live".equals( bootPkg ) )
			{
				return true;
			}

		}
		catch ( NameNotFoundException e )
		{
			e.printStackTrace();
		}

		return false;
	}

	public static void sendKeyCode( final int KeyCode )
	{
		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				Instrumentation inst = new Instrumentation();
				inst.sendKeyDownUpSync( KeyCode );
			}
		} ).start();
	}

	public static void setCompoundDrawable( TextView mTextView, Drawable drawable, int padding )
	{
		drawable.setBounds( 0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight() );
		mTextView.setCompoundDrawables( drawable, null, null, null ); // 设置TextView的drawableleft
		mTextView.setCompoundDrawablePadding( padding ); // 设置图片和text之间的间距
	}

	public static List< String > toMultiLine( List< String > strsList, String str, int len )
	{
		char[] chs = str.toCharArray();
		StringBuffer sb = new StringBuffer();
		for ( int i = 0, sum = 0; i < chs.length; i++ )
		{
			sum += chs[i] < 0xff ? 1 : 2;
			sb.append( chs[i] );
			if ( sum >= len )
			{
				sum = 0;
				strsList.add( sb.toString() );
				sb = new StringBuffer();
			}
		}
		if ( !sb.toString().isEmpty() )
		{
			strsList.add( sb.toString() );
		}

		return strsList;
	}

	public static Properties getFileContent( String REPLACEMENT_PKG_PATH )
	{
		InputStream in = null;
		Properties properties = null;
		try
		{
			in = new FileInputStream( REPLACEMENT_PKG_PATH );
			properties = new Properties();
			properties.load( in );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			if ( in != null )
			{
				try
				{
					in.close();
				}
				catch ( IOException e )
				{
				}
			}
		}
		return properties;
	}

}
