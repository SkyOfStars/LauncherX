package com.dvb.thirdapi;

import java.util.List;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;

import com.dvb.common.ctrl.FCProgData;
import com.dvb.player.PlayWorker;
import com.launcher.LauApplication;
import com.launcher.Launcher;

public class DVBPlayer
{
	private static DVBPlayer mInstance;

	public synchronized static DVBPlayer getInstance()
	{
		if ( mInstance == null )
		{
			mInstance = new DVBPlayer();
		}
		return mInstance;
	}

	public void init( Context context, SurfaceView view )
	{
		// 初始化播控对象
		PlayWorker.from().init( context, view );
	}

	public void play()
	{

		// 如果是山西阳泉版本，Launcher一直播放serviceId = 301的频道
		int firstinit = Integer.parseInt( SystemProperties.get( "sys.dvb.live.firstinit", "1" ) );
		Log.i( "DVBPlayer", "firstinit:" + firstinit );
		if ( LauApplication.getClientVer().contains( "SXYQ" ) )
		{
			if ( firstinit == 1 )
			{
				// 如果是山西阳泉版本，且是开机第一次启动，则播放serviceId = 301的频道
				Log.i( "DVBPlayer",
								"---LauncherMainActivity.isFirstRun && !CommonUtil.isBootLive( context )---" );
				List< FCProgData > allDatas = PlayWorker.from().getChannelList();
				for ( FCProgData data : allDatas )
				{
					if ( data.ServiceID == 301 )// 播放阳泉新闻综合
					{
						String YQChannelId = data.getUUID();
						String YQGroupId = PlayWorker.from().getCurChannelGroupId( data );
						Log.i( "DVBPlayer", "YQChannelId:" + YQChannelId );
						Log.i( "DVBPlayer", "YQGroupId:" + YQGroupId );
						PlayWorker.from().executePlay( YQGroupId, YQChannelId, true );
						break;
					}
				}

			}
			else
			{
				Log.i( "DVBPlayer",
								"---!!!LauncherMainActivity.isFirstRun && !CommonUtil.isBootLive( context )---" );
				PlayWorker.from().callPlayByLastRecord();
			}

		}
		else
		{
			// 播放上一播放频道
			PlayWorker.from().callPlayByLastRecord();
		}
		Log.i( "DVBPlayer", "---!!!LauncherMainActivity.isFirstRun && !CommonUtil.isBootLive( context )---" );

		// if ( PlayWorker.from().isExistData() )
		// {
		// // 播放上一播放频道
		// PlayWorker.from().callPlayByLastRecord();
		// }
	}

	public void setVolMode()
	{
		if ( PlayWorker.from().isExistData() )
		{
			PlayWorker.from().setVolMode();
		}
	}

	public void stop()
	{
		PlayWorker.from().stop();
	}

	public void requestChannelData()
	{
		PlayWorker.from().requestChannelData();
	}

	public boolean isExistData()
	{
		return PlayWorker.from().isExistData();
	}

	public Boolean onKeyDownProcess( int keyCode, KeyEvent event )
	{
		switch( keyCode )
		{
		case KeyEvent.KEYCODE_VOLUME_UP:
			// 音量+
			PlayWorker.from().changeVol( true );
			return false;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			// 音量-
			PlayWorker.from().changeVol( false );
			return false;
		default:
			return false;
		}
	}

	public void onKeyUpProcess( int keyCode, KeyEvent event )
	{
		switch( keyCode )
		{
		case KeyEvent.KEYCODE_VOLUME_MUTE:
			// 静音
			PlayWorker.from().changeMuteState();
			break;
		// 画幅按键
		case 134:
			PlayWorker.from().refreshDvbWindow();
			break;
		default:
			break;
		}
	}
}
