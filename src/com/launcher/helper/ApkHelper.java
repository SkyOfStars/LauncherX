package com.launcher.helper;

import com.launcher.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

public final class ApkHelper
{

	public static void goAppByPkg( Context context, String packageName )
	{
		if ( isApkInstalled( context, packageName ) )
		{
			Intent intent = context.getPackageManager().getLaunchIntentForPackage( packageName );
			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			context.startActivity( intent );
		}
		else
		{
			ApkHelper.noInstallToast( context, context.getString( R.string.no_message ) );
		}
	}

	public static void goAppByAction( Context context, String action )
	{
		try
		{
			Intent intent = new Intent( action );
			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			context.startActivity( intent );
		}
		catch ( Exception e )
		{
			ApkHelper.noInstallToast( context, context.getString( R.string.no_message ) );
		}
	}

	public static boolean isApkInstalled( Context context, final String pkgName )
	{
		try
		{
			context.getPackageManager().getPackageInfo( pkgName, 0 );
			return true;
		}
		catch ( NameNotFoundException e )
		{
		}
		return false;
	}

//	public static void showLogo( Context context, String tag, int type, String pkg )
//	{
//		String logStr = type + "|" + tag + "|" + pkg;
//		ReportData reportData = new ReportData( "005001", logStr );
//		DataReportHelper.report( context, reportData );
//	}

	public static void noInstallToast( Context context, String message )
	{
		Toast.makeText( context, message, Toast.LENGTH_LONG ).show();
	}

}
