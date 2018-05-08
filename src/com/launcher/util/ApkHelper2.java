package com.launcher.util;

import java.util.List;

import com.launcher.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;

public final class ApkHelper2
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
			ApkHelper2.noInstallToast( context, context.getString( R.string.no_message ) );
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
			ApkHelper2.noInstallToast( context, context.getString( R.string.no_message ) );
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

	public static void noInstallToast( Context context, String message )
	{
		Toast.makeText( context, message, Toast.LENGTH_LONG ).show();
	}

	public static void goAppByPkgClass( Context context, String packageName, String className )
	{
		Intent intent = new Intent( Intent.ACTION_MAIN );
		intent.addCategory( Intent.CATEGORY_LAUNCHER );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		ComponentName cn = new ComponentName( packageName, className );
		intent.setComponent( cn );
		context.startActivity( intent );

	}

	public static void goAppByUri( Context context, String uri )
	{
		Intent intent = new Intent();
		intent.setAction( "android.intent.action.VIEW" );
		Uri content_url = Uri.parse( uri );
		intent.setData( content_url );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		context.startActivity( intent );
	}

	/**
	 * 搜索全部应用信息
	 * 
	 * @param context
	 * @return
	 */
	public static List< ResolveInfo > queryAllResolveInfo( Context context )
	{
		PackageManager pm = context.getPackageManager();
		Intent mainIntent = new Intent( Intent.ACTION_MAIN, null );
		mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
		List< ResolveInfo > sAllResolveInfos = pm.queryIntentActivities( mainIntent, 0 );
		return sAllResolveInfos;
	}

	/**
	 * 根据包名获取应用图标
	 * 
	 * @param context
	 * @param pack
	 * @return
	 */
	public static Bitmap getAppIcon( Context context, String pack )
	{
		try
		{
			PackageManager pm = context.getPackageManager();
			ApplicationInfo info = pm.getApplicationInfo( pack, 0 );
			Drawable dw = info.loadIcon( pm );
			return ( ( BitmapDrawable ) dw ).getBitmap();
		}
		catch ( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		return null;

	}
	
	public static Bitmap resource2Bitmap(Context context,int res)
	{
		
		
		return null;
		
	}

}
