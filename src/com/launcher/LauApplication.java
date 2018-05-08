package com.launcher;

import android.app.Application;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;

import com.fc.util.tool.FCTvDevice;
import com.launcher.provider.FCProviderResolver;

public class LauApplication extends Application
{
	public static final String FC_DVB_VERSION = SystemProperties.get( "fc.dvb.version", "v1.0" );

	public static Boolean isNeedControlDvbVol = true;

	private static Context sContext;

	/**
	 * 是否需要对DVB音量进行控制（目前只有v2.0不需要）
	 * 
	 * @return
	 */
	public static boolean needControlDvbVol()
	{
		Log.i( "test", "FC_DVB_VERSION:" + FC_DVB_VERSION );
		// return FC_DVB_VERSION.equalsIgnoreCase( "v2.0" ) ? false : true;
		return isNeedControlDvbVol;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		sContext = this.getApplicationContext();
		dvbClientVersion = FCTvDevice.getDeviceVersion();
		if ( dvbClientVersion != null )
		{
			dvbClientVersion = dvbClientVersion.toUpperCase();
		}
	}

	/** DVB客户版本 */
	private static String dvbClientVersion;

	/**
	 * 是否为湖南移动版本
	 * 
	 * @return
	 */
	public static boolean isHnMobileVersion()
	{
		if ( dvbClientVersion != null && dvbClientVersion.contains( "HN" ) )
		{
			return true;
		}
		return false;
	}

	public static String getClientVer()
	{
		return dvbClientVersion;
	}
}
