package com.launcher.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BootToLiveManager
{
	private static String TAG = BootToLiveManager.class.getSimpleName();
	private static Handler handler = new Handler( Looper.getMainLooper() );
	private static String mPackageName;
	private static Context mContext;

	/**
	 * 读取配置，如果是延时启动直播模式则调用此方法
	 */
	public static void delayStartPkg( final Context context, String packageName, int durion )
	{
		mPackageName = packageName;
		mContext = context;
		handler.postDelayed( r, durion );
	}

	/**
	 * 中断启动直播的任务
	 */
	public static void breakStartLive()
	{
		Log.i( "Launcher", "breakStartLive" );
		handler.removeCallbacks( r );
	}

	private static Intent parsePkgIntent( Context context, String pkg )
	{
		return context.getPackageManager().getLaunchIntentForPackage( pkg );
	}

	private static Runnable r = new Runnable()
	{
		@Override
		public void run()
		{
			Log.i( TAG, "runable to live" );
			Intent intent = parsePkgIntent( mContext, mPackageName );
			if ( intent != null )
			{
				intent.putExtra( "sys_first_start", true );
				intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				Log.i( "Launcher", "....BootStartUtil r startActivity....." );
				mContext.startActivity( intent );
				return;
			}
		}
	};
}
