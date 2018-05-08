package com.launcher.apps;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public final class AppInfoUtils
{

	public static List< ResolveInfo > queryAllResolveInfo( Context context )
	{
		PackageManager pm = context.getPackageManager();
		Intent mainIntent = new Intent( Intent.ACTION_MAIN, null );
		mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
		List< ResolveInfo > sAllResolveInfos = pm.queryIntentActivities( mainIntent, 0 );
		return sAllResolveInfos;
	}
}
