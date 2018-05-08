package com.launcher;

import java.util.List;

import com.fc.lisence.LisenceInfo;
import com.fc.lisence.LisenceMgr;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemProperties;
import android.util.DisplayMetrics;

public class CommonUtil
{
	private static Boolean isFirstRun = false;
	private static long lastClickTime;

	private static String version = "";
	private static String server = "";

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

	// public synchronized static boolean isFastClick()
	// {
	// long time = System.currentTimeMillis();
	// if(time - lastC)
	// }

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 
	 * @return
	 */
	public static String getServer()
	{
		if ( server == null || "".equals( server ) )
		{
			LisenceInfo info = LisenceMgr.getLisenceData();

			if ( info == null || info.getServer() == null || "".equals( info.getServer() ) )
			{
				server = "http://api.ottclub.com";
			}
			else
			{
				server = info.getServer();
			}

		}

		return server;
	}

	/**
	 * 
	 * @return
	 */
	public static String getVersion()
	{
		if ( version == null || "".equals( version ) )
		{
			LisenceInfo info = LisenceMgr.getLisenceData();

			if ( info == null || info.getVersion() == null || "".equals( info.getVersion() ) )
			{
				version = "HAS-FG-V0.0.0.0";
			}
			else
			{
				version = info.getVersion();
			}

		}

		return version;
	}
}
