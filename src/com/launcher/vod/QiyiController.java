package com.launcher.vod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fc.util.tool.FCTvDevice;
import com.qiyi.tv.client.ConnectionListener;
import com.qiyi.tv.client.ErrorCode;
import com.qiyi.tv.client.QiyiClient;
import com.qiyi.tv.client.Result;
import com.qiyi.tv.client.data.Channel;
import com.qiyi.tv.client.data.Media;
import com.qiyi.tv.client.feature.common.PageType;
import com.qiyi.tv.client.feature.common.RecommendationType;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 对接爱奇艺api
 * 
 * @author Niuniu
 * 
 */
public class QiyiController implements ControllerBase
{
	public static final int WHAT_CHANNEL_LIST = 10000;

	public static final int WHAT_RECOMMENDATION = 10001;

	public static final int WHAT_RECONNECT = 10002;

	public static final int RETRY_DELAY = 60 * 1000;

	private static final String TAG = QiyiController.class.getSimpleName();

	private static final String ZH1088_Hi3719 = "glbhxuiax08pbnpzm9pohtul0mygxrqkom033vrmfl75vrrv";

	private static final String DC5800_Hi3716 = "5yt9h0fmdn6rzdyz&g#08klwvk&0erqkom0vv3byil75vrrv";

	private QiyiClient mQiyiClient = null;

	private RecommendationCallback mCallback;

	private List< Channel > mChannelList;

	private Context context;

	private List< Media > mediaList = new ArrayList< Media >();

	private List< Map< String, String > > mList = new ArrayList< Map< String, String > >();

	private static class InstanceHolder
	{
		public static QiyiController instance = new QiyiController();
	}

	public static QiyiController getInstance()
	{
		return InstanceHolder.instance;
	}

	private QiyiController()
	{
		Log.i( TAG, "QiyiControler" );
	}

	public void init( Context context )
	{
		Log.i( TAG, "init" );
		this.context = context;
	}

	public void connect()
	{
		if ( context == null )
		{
			return;
		}

		Log.i( TAG, "connect" );
		mQiyiClient = QiyiClient.instance();

		// HBV 3719,HDV 3716MV400
		String deviceVer = FCTvDevice.getDeviceVersion();
		if ( deviceVer.contains( "HBV" ) )
		{
			mQiyiClient.initialize( context, ZH1088_Hi3719 );
		}
		else if ( deviceVer.contains( "HDV" ) )
		{
			mQiyiClient.initialize( context, DC5800_Hi3716 );
		}
		else
		{
			mQiyiClient.initialize( context, ZH1088_Hi3719 );
		}

		mQiyiClient.setListener( mConnectionListener );
		mQiyiClient.connect();
	}

	public void release()
	{
		if ( mQiyiClient != null )
		{
			mQiyiClient.disconnect();
			mQiyiClient.release();
		}
	}

	/**
	 * 获取当前鉴权状态，鉴权成功才能正确与服务器交互
	 * 
	 * @return true 鉴权成功
	 */
	public boolean isAuthSuccess()
	{
		if ( mQiyiClient == null )
		{
			Log.i( TAG, "isAuthSuccess : null" );
			return false;
		}

		Log.i( TAG, "isAuthSuccess : " + mQiyiClient.isAuthSuccess() );
		return mQiyiClient.isAuthSuccess();
	}

	public boolean isConnected()
	{
		return mQiyiClient.isConnected();
	}

	@Override
	public void setRecommendationListener( RecommendationCallback callback )
	{
		mCallback = callback;
	}

	@Override
	public void goTV()
	{
		goChannelById( Channel.ID_EPISODE );
	}

	@Override
	public void goMovie()
	{
		goChannelById( Channel.ID_FILM );
	}

	@Override
	public void goVariety()
	{
		goChannelById( Channel.ID_VARIETY );
	}

	@Override
	public void goCartoon()
	{
		goChannelById( Channel.ID_CARTOON );
	}

	@Override
	public void goHistory()
	{
		goPageByType( PageType.PAGE_HISTORY );
	}

	@Override
	public void goFavorite()
	{
		goPageByType( PageType.PAGE_FAVORITE );
	}

	@Override
	public void goRecommendation( int position )
	{
		if ( !isAuthSuccess() || mediaList.size() - 1 < position )
		{
			return;
		}

		Log.i( TAG, "media : " + mediaList.get( position ) );
		mQiyiClient.openMedia( mediaList.get( position ) );
	}

	private ConnectionListener mConnectionListener = new ConnectionListener()
	{
		@Override
		public void onAuthSuccess()
		{
			Log.d( TAG, "onAuthSuccess()" );

			new ChannelTask().execute( ( Void ) null );
			new RecommendationTask().execute( ( Void ) null );
		}

		@Override
		public void onError( int code )
		{
			mHandler.sendEmptyMessageDelayed( WHAT_RECONNECT, RETRY_DELAY );
			Log.e( TAG, "onError()" + ", code = " + code );
		}

		@Override
		public void onDisconnected()
		{
			Log.d( TAG, "onDisconnected()" );
		}

		@Override
		public void onConnected()
		{
			Log.d( TAG, "onConnected()" );
			if ( mList.size() <= 0 )
			{
				new RecommendationTask().execute( ( Void ) null );
			}
			else
			{
				mCallback.onRecieveList( mList );
			}
		}
	};

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage( Message msg )
		{
			super.handleMessage( msg );

			switch( msg.what )
			{
			case WHAT_CHANNEL_LIST:
				new ChannelTask().execute( ( Void ) null );
				break;

			case WHAT_RECOMMENDATION:
				Log.i( TAG, "WHAT_RECOMMENDATION" );
				new RecommendationTask().execute( ( Void ) null );
				break;

			case WHAT_RECONNECT:
				mQiyiClient.disconnect();
				mQiyiClient.connect();
				break;

			default:
				break;
			}
		}
	};

	private void goChannelById( int channelId )
	{
		if ( mChannelList == null || mChannelList.size() < 1 )
		{
			return;
		}

		Channel reChannel = null;
		for ( Channel channel : mChannelList )
		{
			if ( channelId == channel.getId() )
			{
				reChannel = channel;
				break;
			}
		}

		if ( reChannel == null )
		{
			return;
		}

		int code = ErrorCode.ERROR_UNKNOWN;
		code = mQiyiClient.openChannel( reChannel, reChannel.getName() );
		Log.i( TAG, "goChannelById id : " + channelId + " code : " + code );
	}

	private void goPageByType( int pageType )
	{
		if ( !isAuthSuccess() )
		{
			return;
		}

		int code = ErrorCode.ERROR_UNKNOWN;
		mQiyiClient.open( pageType );
		Log.i( TAG, "goPageByType pageType : " + pageType + " code : " + code );
	}

	private class ChannelTask extends AsyncTask< Void, Void, Result< List< Channel > > >
	{
		@Override
		protected Result< List< Channel > > doInBackground( Void... params )
		{
			Log.d( TAG, "ChannelTask begin" );
			return mQiyiClient.getChannelList();
		}

		@Override
		protected void onPostExecute( Result< List< Channel > > result )
		{
			super.onPostExecute( result );

			if ( result.code == ErrorCode.SUCCESS )
			{
				mChannelList = result.data;
				Log.d( TAG, "ChannelTask onPostExecute : " + mChannelList.size() );
			}
			else
			{
				Log.d( TAG, "ChannelTask error" );
				mHandler.sendEmptyMessageDelayed( WHAT_CHANNEL_LIST, RETRY_DELAY );
			}

			Log.d( TAG, "ChannelTask end" );
		}
	}

	private class RecommendationTask extends AsyncTask< Void, Void, Result< List< Media > > >
	{
		@Override
		protected Result< List< Media > > doInBackground( Void... params )
		{
			Log.d( TAG, "RecommendationTask begin" );
			int position = RecommendationType.EXTRUDE;
			return mQiyiClient.getRecommendation( position );
		}

		@Override
		protected void onPostExecute( Result< List< Media > > result )
		{
			super.onPostExecute( result );
			// Log.d( TAG, "RecommendationTask mList : " + result.data.size() );
			Log.d( TAG, "RecommendationTask code : " + result.code );
			if ( result.code == ErrorCode.SUCCESS )
			{
				mediaList = result.data;

				int i = 0;
				for ( Media media : result.data )
				{
					if ( i > 8 )
					{
						break;
					}
					Map< String, String > map = new HashMap< String, String >();
					map.put( ControllerBase.VID, "" );
					map.put( ControllerBase.TITLE, media.getName() );
					map.put( ControllerBase.IMAGE_URL, media.getPicUrl() );
					map.put( ControllerBase.PROTOCOL, "" );
					Log.d( TAG, "RecommendationTask mList TITLE: " + media.getName() );
					Log.d( TAG, "RecommendationTask mList IMAGE_URL: " + media.getPicUrl() );
					mList.add( map );
				}

				Log.d( TAG, "RecommendationTask mList : " + mList.size() );
				if ( mCallback != null )
				{
					mCallback.onRecieveList( mList );
				}
			}
			else
			{
				Log.d( TAG, "RecommendationTask error" );
				mHandler.sendEmptyMessageDelayed( WHAT_RECOMMENDATION, RETRY_DELAY );
			}

			Log.d( TAG, "RecommendationTask end" );
		}
	}
}
