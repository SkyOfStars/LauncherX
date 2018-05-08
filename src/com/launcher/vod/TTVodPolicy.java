package com.launcher.vod;

import com.fc.util.tool.cache.FCCacheConfigUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * 湖南Launcher_BA13请增加一个修改：<br/>
 * 
 * 
 * 
 * 1.
 * 在cache.properties中增加一个配置：ttvodinitstatus（天天影视出厂状态，0：出厂不启用，1：出厂启用），如果配置的是0的话
 * ，则Launcher启用下面逻辑；如果没有该配置项，或者配置的是非0，则不用走下面逻辑。该配置可以后续根据项目状态来设置；<br/>
 * 
 * 2.
 * Launcher左侧的高清影院、右侧的3个影视推荐位在点击的时候，先检查ttvodstatus的状态，如果是0，再检查launcher自己的一个配置（
 * 天天影视是否已打开启用，0未打开，1已打开）；<br/>
 * 
 * 3. 如果launcher本地的配置0，点击时弹出一个提示框：暂未提供，敬请期待；<br/>
 * 
 * 4. 此时在Launcher上组合键369（该按键写在本地代码中即可，不用从网络获取），将该是否已打开启用的状态改为1，用户再点击高清影院和图片推荐位时，
 * 就可以正常打开了；<br/>
 * 
 * 5. 恢复出厂设置，需要再次按组合键才可以使用。<br/>
 * 
 * @author Administrator
 * 
 */
public final class TTVodPolicy
{

	private static final String FILE_CONF = "vod";

	public static int getVodStatus( Context context )
	{
		return getSharedPreferences( context ).getInt( "vod_status", 0 );
	}

	public static void setVodStatus( Context context, int status )
	{
		getSharedPreferences( context ).edit().putInt( "vod_status", status ).commit();
	}

	private static SharedPreferences getSharedPreferences( Context context )
	{
		return context.getSharedPreferences( FILE_CONF, Context.MODE_PRIVATE );
	}

	public static boolean isVodEnabled( Context context )
	{
		String vodStatus = FCCacheConfigUtil.getConf( context, "ttvodinitstatus", null );
		if ( vodStatus != null && vodStatus.equals( "0" ) )
		{
			if ( getVodStatus( context ) == 0 )
			{
				Toast.makeText( context, "暂未提供，敬请期待", Toast.LENGTH_SHORT ).show();
				return false;
			}
		}

		return true;
	}

	public static void dealVodAction( Context context )
	{
		String vodStatus = FCCacheConfigUtil.getConf( context, "ttvodinitstatus", null );

		Log.e( "xxxx", "vodStatus=========" + vodStatus );

		if ( vodStatus != null && vodStatus.equals( "0" ) )
		{
			if ( getVodStatus( context ) == 0 )
			{
				Toast.makeText( context, "暂未提供，敬请期待", Toast.LENGTH_SHORT ).show();
			}
			else
			{
				startVod( context );
			}
			return;
		}

		startVod( context );
	}

	private static void startVod( Context context )
	{
		Intent vodIntent = new Intent( Intent.ACTION_VIEW );
		vodIntent.setData( Uri.parse( "tt_vod://start" ) );
		context.startActivity( vodIntent );
	}
}
