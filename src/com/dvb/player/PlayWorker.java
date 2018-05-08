package com.dvb.player;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.dvb.common.ctrl.FCDvb;
import com.dvb.common.ctrl.FCProgData;
import com.dvb.common.ctrl.PlayerControl;
import com.dvb.common.util.PublicTool;
import com.dvb.data.DataHolder;
import com.dvb.modul.Group;
import com.dvb.util.BaseUtil;
import com.dvb.util.Constant;
import com.fc.dvb.provider.DVBDBOpt;
import com.launcher.LauApplication;
import com.launcher.provider.FCProviderResolver;

public class PlayWorker
{
	private static final String TAG = "BA56PlayWorker";

	private SurfaceView mSurfaceView;

	private AudioManager audioManager;

	private WindowManager mWinMg;

	private int musicAudioMax;

	/**
	 * 系统设置中的声音设置<br>
	 * 0：全频道 1：单频道。默认为0
	 */
	private int audioSet;

	private PlayStateListener playStateListener;

	/**
	 * 播放参数对象
	 */
	private PlayBean playBean;

	private static PlayWorker playWorker;

	private Context mContext;

	private PlayerControl playControl;// = FCDvb.getInstance().getPlayerCtrl();

	private Handler handler = new Handler();

	public synchronized static PlayWorker from()
	{
		if ( playWorker == null )
		{
			playWorker = new PlayWorker();
		}
		return playWorker;
	}

	private PlayWorker()
	{

	}

	/**
	 * 切台时是否保留上个频道最后一帧
	 */
	private boolean isBlackSwitchMode = true;

	/**
	 * 初始化
	 * 
	 * @param context
	 * @param surfaceView
	 */
	public void init( Context context, SurfaceView surfaceView )
	{
		if ( mContext == null )
		{
			mContext = context.getApplicationContext();
			audioManager = ( AudioManager ) mContext.getSystemService( Context.AUDIO_SERVICE );
			mWinMg = ( WindowManager ) mContext.getSystemService( Context.WINDOW_SERVICE );
		}

		playControl = FCDvb.getInstance( mContext ).getPlayerCtrl();

		mSurfaceView = surfaceView;
		SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
		playControl.setSubDisplay( surfaceHolder );

		refreshDvbWindow();

		surfaceHolder.addCallback( smallSurfaceCallback );

		musicAudioMax = audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
		// 通过远程接口来获取智能音量状态
		// audioSet = FCSharedPres.getSysParamterAudio( mContext );
		audioSet = Integer.parseInt( FCProviderResolver.getInstance().init( mContext )
						.getConfig( "smart_sound_mode", "1" ) );
	}

	private SurfaceHolder.Callback smallSurfaceCallback = new SurfaceHolder.Callback()
	{
		@Override
		public void surfaceDestroyed( SurfaceHolder holder )
		{

		}

		@Override
		public void surfaceCreated( SurfaceHolder holder )
		{
			refreshDvbWindow();
		}

		@Override
		public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
		{
			refreshDvbWindow();
		}
	};

	/**
	 * 切换画幅时刷新DVB播放窗口
	 */
	public void refreshDvbWindow()
	{
		Rect rect = PublicTool.GetDisplayRect();
		Point outSize = new Point();
		mWinMg.getDefaultDisplay().getSize( outSize );
		int androidWidth = outSize.x;
		int androidHeight = outSize.y;

		double ratioX = ( rect.left + rect.right ) / ( double ) androidWidth;
		double ratioY = ( rect.top + rect.bottom ) / ( double ) androidHeight;

		int[] location = new int[ 2 ];
		mSurfaceView.getLocationOnScreen( location );
		if ( location[0] > 0 && location[1] > 0 )
		{
			int surfaceX = ( int ) ( location[0] * ratioX );
			int surfaceY = ( int ) ( location[1] * ratioY );
			int width = ( int ) ( mSurfaceView.getWidth() * ratioX );
			int height = ( int ) ( mSurfaceView.getHeight() * ratioY );

			playControl.setWindowPos( rect.left + surfaceX, rect.top + surfaceY, width, height );
			// playControl.setWindowPos( rect.left + surfaceX, rect.top +
			// surfaceY, width + 20, height + 10 );
		}
	}

	private boolean isExistData = false;

	public void requestChannelData()
	{
		isExistData = DataHolder.from( mContext ).requestChannelData( mContext );
	}

	public boolean isExistData()
	{
		return isExistData;
	}

	/**
	 * 获取频道列表数据
	 * 
	 * @return
	 */
	public List< FCProgData > getChannelList()
	{
		return DataHolder.from( mContext ).getAllChannelList();
	}

	public String getCurChannelGroupId( FCProgData channel )
	{
		return DataHolder.from( mContext ).getGroupIdByChannel( channel );
	}

	/**
	 * 播放最后观看的频道，没有则播放第一个分组中的第一个频道
	 */
	public void callPlayByLastRecord()
	{
		// 湖南移动版本默认播第一个频道
		if ( LauApplication.isHnMobileVersion() )
		{
			forwardDefaultPlay();
			return;
		}

		String curGroupID = BaseUtil.getLastWatchTvGroupID( mContext );
		boolean groupIsValid = false;
		if ( !TextUtils.isEmpty( curGroupID ) )
		{
			if ( DataHolder.from( mContext ).getGroupByID( curGroupID ) != null )
			{
				groupIsValid = true;
			}
		}
		if ( !groupIsValid )
		{
			curGroupID = DataHolder.from( mContext ).getAllGroupList().get( 0 ).getGroupID();
		}

		String curChannelID = BaseUtil.getLastWatchTvChannelID( mContext );
		boolean channelIsValid = false;
		if ( !TextUtils.isEmpty( curChannelID ) )
		{
			DataHolder dh = DataHolder.from( mContext );
			if ( dh.getChannelByUniqueID( curChannelID ) != null
							&& dh.getChannelIndexByGroupAndChannel( curGroupID, curChannelID ) >= 0 )
			{
				channelIsValid = true;
			}
		}

		if ( channelIsValid )
		{
			executePlay( curGroupID, curChannelID, true );
		}
		else
		{
			forwardDefaultPlay();
		}
	}

	/**
	 * 跳转默认播放
	 */
	private void forwardDefaultPlay()
	{
		String playGroupID = null;
		String playChannelID = null;

		List< Group > groupList = DataHolder.from( mContext ).getAllGroupList();
		for ( Group group : groupList )
		{
			List< FCProgData > channelList = DataHolder.from( mContext ).getChannelListByGroupID(
							group.getGroupID() );
			if ( channelList != null && !channelList.isEmpty() )
			{
				playGroupID = group.getGroupID();
				playChannelID = DataHolder.from( mContext ).createChannelUniqueID( channelList.get( 0 ) );
				break;
			}
		}

		if ( playGroupID != null && playChannelID != null )
		{
			executePlay( playGroupID, playChannelID, true );
		}
	}

	public void executePlay( String groupID, String channelUniqueID, boolean nowPlay )
	{
		Log.i( TAG, "---executePlay---" );
		clearPlayTask();
		DataHolder.from( mContext ).setCurChannelUniqueID( channelUniqueID );
		PlayBean playBean = new PlayWorker.PlayBean( groupID, channelUniqueID, DVBPlayStateListener );
		PlayWorker.from().addPlayTask( playBean );
	}

	/**
	 * 播放状态监听器
	 */
	private PlayStateListener DVBPlayStateListener = new PlayStateListener()
	{
		private String token = "[playStateListener]:";

		@Override
		public void onPreparePlay( PlayBean playBean )
		{
		}

		@Override
		public void onPlaySucess( PlayBean playBean )
		{
		}

		@Override
		public void onPlayError( PlayBean playBean )
		{
			Log.e( TAG, token + " onPlayError->" );
		}

		@Override
		public void onPlayCompletion( PlayBean playBean )
		{
			Log.e( TAG, token + " onPlayCompletion->" );
		}
	};

	/**
	 * 添加播放任务
	 * 
	 * @param playBean
	 *            播放参数对象
	 * @param listener
	 *            播放监听器
	 */
	private void addPlayTask( PlayBean playBean )
	{
		// playControl.setSwitchProgMode( isBlackSwitchMode );

		handler.removeCallbacks( playRunnable );

		this.playBean = playBean;
		this.playStateListener = playBean.playListener;

		handler.postDelayed( playRunnable, 100 );
	}

	/**
	 * 清除播放任务
	 */
	private void clearPlayTask()
	{
		handler.removeCallbacks( playRunnable );
	}

	private Runnable playRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			playStateListener.onPreparePlay( PlayWorker.this.playBean );

			playControl.setPlayList( ( ArrayList< FCProgData > ) DataHolder.from( mContext )
							.getChannelListByGroupID( playBean.groupID ) );
			playControl.StartPlay( DataHolder.from( mContext ).getChannelIndexByGroupAndChannel(
							playBean.groupID, playBean.channelID ) );

			// start CurChannel epg data
			FCProgData channel = DataHolder.from( mContext ).getCurChannel();
			FCDvb.getInstance( mContext ).getEPGCtrl().StopEGP();
			FCDvb.getInstance( mContext ).getEPGCtrl().StartCurEPG( channel.getUUID() );

			// 声道 - begin
			int track = getAudioTrack();
			if ( track > -1 )
			{
				playControl.setAudioTrack( track );
			}
			// 声道 - end

			if ( LauApplication.needControlDvbVol() )
			{
				// 静音 - begin
				// 3719版本不存在开机动画影响开机播放，如果以后版本有影响开机播放的情况，需参考DvbLive的逻辑来处理静音
				// boolean muteConfig = isMute();
				// audioManager.setStreamMute( AudioManager.STREAM_MUSIC,
				// muteConfig );
				// playControl.setAudioMute( muteConfig );
				// 静音 - end

				// 音量 - begin
				setVolMode();
				// 音量 - end
			}

			playStateListener.onPlaySucess( PlayWorker.this.playBean );
		}
	};

	public void setVolMode()
	{
		if ( playBean == null )
		{
			return;
		}

		int vol = getAudioVol();
		if ( vol > -1 )
		{
			// playControl.setAudioVol( vol );
			// updateAndroidVol( vol );

			int sdkVol = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
			Log.i( TAG, "sdkVol : " + sdkVol );
			// playControl.setAudioVol( vol );
			if ( audioSet == 1 )
			{
				updateAndroidVol( vol );
			}

		}
	}

	/**
	 * 获取音量配置项的值
	 * 
	 */
	private int getAudioVol()
	{
		int vol = -1;
		if ( audioSet == 0 || playBean == null )
		{
			String allChannelVol = BaseUtil.getLivePreferences( mContext, Constant.KEY_ALL_CHANNEL_VOL );
			if ( !TextUtils.isEmpty( allChannelVol ) )
			{
				vol = Integer.parseInt( allChannelVol );
			}
			// 如果没有全频道设置则使用当前频道的设置
			else
			{
				vol = DataHolder.from( mContext ).getChannelByUniqueID( playBean.channelID ).AudioVol;
			}
		}
		else if ( audioSet == 1 )
		{
			Log.i( TAG, "getAudioVol audioSet == 1 channel : "
							+ DataHolder.from( mContext ).getChannelByUniqueID( playBean.channelID ) );
			Log.i( TAG, "getAudioVol audioSet == 1 playBean.channelID " + playBean.channelID );
			FCProgData fcProgData = DataHolder.from( mContext ).getChannelByUniqueID( playBean.channelID );
			if ( fcProgData != null )
			{
				vol = DataHolder.from( mContext ).getChannelByUniqueID( playBean.channelID ).AudioVol;
			}

		}

		return vol;
	}

	/**
	 * 更新音量配置项，并设置Player的音量
	 * 
	 * @param vol
	 * @return 设置是否成功
	 */
	private boolean setAudioVol( int vol )
	{
		boolean result = false;

		if ( LauApplication.needControlDvbVol() )
		{
			if ( audioSet == 0 || playBean == null )
			{
				result = true;
			}
			else if ( audioSet == 1 )
			{
				FCProgData channel = DataHolder.from( mContext ).getChannelByUniqueID( playBean.channelID );
				channel.AudioVol = vol;
				if ( DVBDBOpt.getInstance( mContext ).updateProgData( channel ) )
				{
					result = true;
				}
			}

			if ( result )
			{
				// if ( isMute() )
				// {
				// setMute( false );
				// }

				// playControl.setAudioVol( vol );
			}
		}

		return result;
	}

	/**
	 * 音量加减
	 * 
	 * @param isUp
	 *            true:加 ； false:减
	 * @return 调整后的音量
	 */
	public int changeVol( boolean isUp )
	{
		Log.i( TAG, "changeVol" );
		if ( LauApplication.needControlDvbVol() )
		{
			// audioManager.setStreamMute( AudioManager.STREAM_MUSIC, false );
			// playControl.setAudioMute( false );

			int sdkVol = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );

			int targetVol = 0;

			if ( isUp )
			{
				targetVol = sdkVol + 1;
			}
			else
			{
				targetVol = sdkVol - 1;
			}

			if ( targetVol < 0 )
			{
				targetVol = 0;
			}
			else if ( targetVol > musicAudioMax )
			{
				targetVol = musicAudioMax;
			}

			return setAudioVol( targetVol ) ? targetVol : sdkVol;
		}
		else
		{
			return audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
		}
	}

	/**
	 * 获取静音配置项的状态
	 * 
	 * @return
	 */
	private boolean isMute()
	{
		boolean isMute = false;

		// // if ( audioSet == 0 )
		// // {
		// String allChannelMute = BaseUtil.getLivePreferences( mContext,
		// Constant.KEY_ALL_CHANNEL_MUTE );
		// if ( !TextUtils.isEmpty( allChannelMute ) )
		// {
		// Log.i( TAG, "isMute allChannelMute" );
		// isMute = Boolean.parseBoolean( allChannelMute );
		// }
		// // 如果没有全频道设置则使用当前频道的设置
		// else
		// {
		// isMute = DataHolder.from( mContext ).getChannelByUniqueID(
		// playBean.channelID ).bIsMute;
		// }
		// // }
		// // else if ( audioSet == 1 )
		// // {
		// // isMute = DataHolder.from( mContext ).getChannelByUniqueID(
		// // playBean.channelID ).bIsMute;
		// // }
		isMute = audioManager.isStreamMute( AudioManager.STREAM_MUSIC );

		Log.i( TAG, "isMute : " + isMute );
		return isMute;
	}

	public void changeMuteState()
	{
		if ( LauApplication.needControlDvbVol() )
		{
			setMute( !isMute() );
		}
	}

	/**
	 * 更新静音配置项，并设置Player的静音状态
	 * 
	 * @param isMute
	 */
	private void setMute( boolean isMute )
	{
		// if ( LauApplication.needControlDvbVol() )
		// {
		// boolean result = false;
		//
		// // if ( audioSet == 0 )
		// // {
		// result = true;
		// // }
		// // else if ( audioSet == 1 )
		// // {
		// // FCProgData channel = DataHolder.from( mContext
		// // ).getChannelByUniqueID(
		// // playBean.channelID );
		// // channel.bIsMute = isMute;
		// // if ( DVBDBOpt.getInstance( mContext ).updateProgData( channel ) )
		// // {
		// // result = true;
		// // }
		// // }
		//
		// if ( result )
		// {
		// playControl.setAudioMute( isMute );
		// }
		// }
	}

	/**
	 * 更新android音量
	 * 
	 * @param vol
	 */
	private void updateAndroidVol( int vol )
	{
		if ( vol > musicAudioMax )
		{
			vol = musicAudioMax;
		}

		Log.i( TAG, "updateAndroidVol vol : " + vol );
		audioManager.setStreamVolume( AudioManager.STREAM_MUSIC, vol,
						AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE );
	}

	/**
	 * 获取当前声道
	 * 
	 * @return
	 */
	private int getAudioTrack()
	{
		int track = -1;

		if ( playBean == null )
		{
			return track;
		}

		if ( audioSet == 0 )
		{
			String allChannelTrack = BaseUtil.getLivePreferences( mContext, Constant.KEY_ALL_CHANNEL_TRACK );
			if ( !TextUtils.isEmpty( allChannelTrack ) )
			{
				track = Integer.parseInt( allChannelTrack );
			}
			// 如果没有全频道设置则使用当前频道的设置
			else
			{
				track = DataHolder.from( mContext ).getChannelByUniqueID( playBean.channelID ).audioTrack;
			}
		}
		else if ( audioSet == 1 )
		{
			track = DataHolder.from( mContext ).getChannelByUniqueID( playBean.channelID ).audioTrack;
		}

		return track;
	}

	/**
	 * 改变声道(小窗口lau暂不提供)<br>
	 * 
	 * @return -1:改变声道失败 ； 否则返回在{@link Constant#AUDIO_TRACK}中的Index
	 */
	private int changeAudioTrack()
	{
		int targetTrack = -1;

		int track = getAudioTrack();

		if ( track == 0 )
		{
			targetTrack = 5;
		}
		else if ( track == 5 )
		{
			targetTrack = 6;
		}
		else if ( track == 6 )
		{
			targetTrack = 0;
		}

		if ( targetTrack > -1 )
		{
			if ( setAudioTrack( targetTrack ) )
			{
				return targetTrack;
			}
		}

		return -1;
	}

	/**
	 * 设置声道
	 * 
	 * @return 设置是否成功
	 */
	private boolean setAudioTrack( int track )
	{
		boolean result = false;

		if ( playBean == null )
		{
			return false;
		}

		if ( audioSet == 0 )
		{
			result = true;
		}
		else if ( audioSet == 1 )
		{
			FCProgData channel = DataHolder.from( mContext ).getChannelByUniqueID( playBean.channelID );
			channel.audioTrack = track;
			if ( DVBDBOpt.getInstance( mContext ).updateProgData( channel ) )
			{
				result = true;
			}
		}

		if ( result )
		{
			playControl.setAudioTrack( track );
		}

		return result;
	}

	public void stop()
	{
		if ( LauApplication.needControlDvbVol() )
		{
			// begin - 避免退出DVB直播后影响其他程序的声音
			FCDvb.getInstance( mContext ).getPlayerCtrl().pause();
			// FCDvb.getInstance( mContext ).getPlayerCtrl().setAudioVol( 100 );
			// FCDvb.getInstance( mContext ).getPlayerCtrl().setAudioMute( false
			// );
			// end - 避免退出DVB直播后影响其他程序的声音
		}
		clearPlayTask();
		FCDvb.getInstance( mContext ).getEPGCtrl().StopEGP();
		PlayWorker.from().release( 1 );
	}

	public void freeRes()
	{
		// 释放DVB相关资源
		FCDvb.freeInstance();
	}

	/**
	 * 释放资源
	 * 
	 * @param mode
	 *            （此参数已失效）<br>
	 *            1:不保留最后一帧。 0:保留最后一帧
	 */
	private void release( int mode )
	{
		Log.i( TAG, "release:" + mode );
		FCDvb.getInstance( mContext ).getPlayerCtrl().stop( mode );
	}

	/**
	 * 播放状态监听器
	 */
	public interface PlayStateListener
	{
		/**
		 * 开始准备播放
		 * 
		 * @param playBean
		 */
		void onPreparePlay( PlayBean playBean );

		/**
		 * 播放成功
		 * 
		 * @param playBean
		 *            播放参数对象
		 */
		void onPlaySucess( PlayBean playBean );

		/**
		 * 播放完成
		 * 
		 * @param playBean
		 */
		void onPlayCompletion( PlayBean playBean );

		/**
		 * 播放Error
		 * 
		 * @param playBean
		 *            播放参数对象
		 */
		void onPlayError( PlayBean playBean );
	}

	public static class PlayBean
	{
		public String groupID;
		public String channelID;
		public PlayStateListener playListener;

		public PlayBean( String groupID, String channelID, PlayStateListener playListener )
		{
			this.groupID = groupID;
			this.channelID = channelID;
			this.playListener = playListener;
		}
	}
}
