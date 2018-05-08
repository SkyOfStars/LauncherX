package com.launcher.util;

import com.fc.lisence.LisenceInfo;
import com.fc.lisence.LisenceMgr;
import com.fc.util.tool.FCTvDevice;

import android.os.SystemProperties;
import android.os.VPDJni;

public class CommonDeviceUtil
{
	private CommonDeviceUtil()
	{

	}

	public static final String APP_KEY_ID = "10148";
	public static final String SERVER = getServer();
	public static final String VERSION = getVersion();
	public static final String SN = getDecodedSN();
	public static final String DEVICEID = FCTvDevice.getDeviceId();

	public static String getDecodedSN()
	{
		try
		{
			VPDJni jni = ( VPDJni ) Class.forName( "android.os.VPDJni" ).newInstance();
			byte[] b = jni.readDecodedSN();
			if ( null == b )
			{
				return "";
			}
			return new String( b ).trim();
		}
		catch ( Exception e )
		{
			return "";
		}
	}

	public static String getServer()
	{
		String server = "";
		LisenceInfo info = LisenceMgr.getLisenceData();

		if ( info == null || info.getServer() == null || "".equals( info.getServer() ) )
		{
			server = "http://api.ottclub.com";
		}
		else
		{
			server = info.getServer();
		}

		return server;
	}

	public static String getVersion()
	{
		String version = "";
		LisenceInfo info = LisenceMgr.getLisenceData();

		if ( info == null || info.getVersion() == null || "".equals( info.getVersion() ) )
		{
			version = "HAS-FG-V0.0.0.0";
		}
		else
		{
			version = info.getVersion();
		}

		return version;
	}
	
	public static Object getCa()
	{
		String caString = "";
		try
		{
			// caString = FCDvb.getInstance( context
			// ).getCaOptCtrl().getCaBasicInfo().scSerialNumber;
			caString = SystemProperties.get( "persist.sys.ca.id", "" );
			if ( caString == null )
			{
				caString = "";
			}
			else
			{
				caString = caString.replace( " ", "" );
			}
		}
		catch ( Exception e )
		{
		}
		return caString;
	}

}
