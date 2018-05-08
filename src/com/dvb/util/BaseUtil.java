package com.dvb.util;

import android.content.Context;
import android.content.SharedPreferences;

public class BaseUtil
{
	private static final String TAG = BaseUtil.class.getSimpleName();

	/**
	 * 获取最后观看的电视分组ID
	 * 
	 * @param context
	 * @return
	 */
	public static String getLastWatchTvGroupID( Context context )
	{
		return getLivePreferences( context, Constant.KEY_LAST_WATCH_TV_GROUP );
	}

	/**
	 * 获取最后观看的电视频道ID
	 * 
	 * @param context
	 * @return
	 */
	public static String getLastWatchTvChannelID( Context context )
	{
		return getLivePreferences( context, Constant.KEY_LAST_WATCH_TV_CHANNEL );
	}

	/**
	 * 获取最后收听的音频广播
	 * 
	 * @param context
	 * @return
	 */
	public static String getLastListenAudioChannelID( Context context )
	{
		return getLivePreferences( context, Constant.KEY_LAST_LISTEN_AUDIO_BROADCAST );
	}

	public static String getLivePreferences( Context context, String key )
	{
		try
		{
			Context liveContext = context.createPackageContext( Constant.PACKAGE_LIVE,
							Context.CONTEXT_IGNORE_SECURITY );
			SharedPreferences shareData = liveContext.getSharedPreferences(
							Constant.PREFERENCES_NAME, Context.MODE_WORLD_READABLE
											| Context.MODE_MULTI_PROCESS );
			if ( shareData != null && !shareData.getAll().isEmpty() )
			{
				return shareData.getString( key, null );
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		return null;
	}
}
