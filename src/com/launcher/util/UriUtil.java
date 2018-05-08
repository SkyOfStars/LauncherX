package com.launcher.util;

import android.util.Log;

import com.fc.util.tool.FCTvDevice;

public class UriUtil
{
	public static final String  TAG = "UriUtil";
	
	public static String version;
	
	public static  String createUrl(String path)
	{
		if ( version==null  )
		{
			 version =	FCTvDevice.getDeviceVersion();
		}
	  String versionPase = version.substring( 0, version.lastIndexOf( "-" ) ).toLowerCase(); 
	  String[] trees = path.split( "/" );
	  String url = ".ottclub.com/";
	  StringBuffer head = new StringBuffer();
	  head.append( trees[1]+"."+versionPase+"."+trees[0]);
	  Log.d( TAG, "http://"+head+url + path );
      return "http://"+head+url + path;
	}


}
