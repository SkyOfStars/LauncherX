package com.launcher;

import com.dvb.thirdapi.DVBPlayer;
import com.fc.util.tool.FCActionTool;
import com.launcher.db.DBManager;
import com.launcher.db.info.MovieInfo;
import com.launcher.helper.ApkHelper;
import com.launcher.image.LoadImageTask;
import com.launcher.widget.FcTextView;
import com.launcher.widget.LayoutView;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Layout1 extends LayoutView
{
	public RelativeLayout tvLayout, lay1;
	private FcTextView mItem1, mItem2, mItem3, mItem4, mItem5, mItem6, mItem7, mItem8, mItem9, mItem10;

	/** 异常信息Panel */
	private ViewGroup errorPanel;

	/** 异常Panel：异常Text */
	private TextView errorText;

	private SurfaceView mPlayView;

	private static final String TAG = "Layout1";

	private String tvAction, tvImageUrl = "";
	private boolean isPlay = true;

	private DBManager manager;
	private final Handler handler;
	private static boolean firstreFreshTV = false;

	public Layout1( Context context, Handler handler )
	{
		super( context );
		this.handler = handler;
		initView();
	}

	private void initView()
	{
		firstreFreshTV = true;
		lay1 = ( RelativeLayout ) context.findViewById( R.id.m_layout1 );
		tvLayout = ( RelativeLayout ) context.findViewById( R.id.lay1_tv );
		mItem1 = ( FcTextView ) context.findViewById( R.id.lay1_item1 );
		mItem2 = ( FcTextView ) context.findViewById( R.id.lay1_item2 );
		mItem3 = ( FcTextView ) context.findViewById( R.id.lay1_item3 );
		mItem4 = ( FcTextView ) context.findViewById( R.id.lay1_item4 );
		mItem5 = ( FcTextView ) context.findViewById( R.id.lay1_item5 );
		mItem6 = ( FcTextView ) context.findViewById( R.id.lay1_item6 );
		mItem7 = ( FcTextView ) context.findViewById( R.id.lay1_item7 );
		mItem8 = ( FcTextView ) context.findViewById( R.id.lay1_item8 );

		tvLayout.setOnClickListener( clickListener );
		mItem1.setOnClickListener( clickListener );
		mItem2.setOnClickListener( clickListener );
		mItem3.setOnClickListener( clickListener );
		mItem4.setOnClickListener( clickListener );
		mItem5.setOnClickListener( clickListener );
		mItem6.setOnClickListener( clickListener );
		mItem7.setOnClickListener( clickListener );
		mItem8.setOnClickListener( clickListener );

		tvLayout.setOnFocusChangeListener( tvFocusListener );
		mItem1.setOnFocusChangeListener( focusListener );
		mItem2.setOnFocusChangeListener( focusListener );
		mItem3.setOnFocusChangeListener( focusListener );
		mItem4.setOnFocusChangeListener( focusListener );
		mItem5.setOnFocusChangeListener( focusListener );
		mItem6.setOnFocusChangeListener( focusListener );
		mItem7.setOnFocusChangeListener( focusListener );
		mItem8.setOnFocusChangeListener( focusListener );

		mItems = new FcTextView[ 8 ];
		mItems[0] = mItem1;
		mItems[1] = mItem2;
		mItems[2] = mItem3;
		mItems[3] = mItem4;
		mItems[4] = mItem5;
		mItems[5] = mItem6;
		mItems[6] = mItem7;
		mItems[7] = mItem8;

		errorPanel = ( ViewGroup ) context.findViewById( R.id.error_panel );
		errorText = ( TextView ) errorPanel.findViewById( R.id.error_text );

		mPlayView = ( SurfaceView ) context.findViewById( R.id.player_view );

		DVBPlayer.getInstance().init( context, mPlayView );

	}

	private Runnable showLiveR = new Runnable()
	{

		@Override
		public void run()
		{
			tvLayout.setBackgroundResource( R.drawable.m_1_1 );
			playDvbLive();
		}
	};

	private Runnable showImageR = new Runnable()
	{

		@Override
		public void run()
		{
			hideDataTip();
			hideError();
			stopDvbLive();
		}
	};

	/**
	 * 非主线程
	 */
	@Override
	public void refreshInfo( DBManager manager )
	{
		Log.i( TAG, "refreshInfo" );
		super.refreshInfo( manager );
		String tvTag = tvLayout.getTag().toString();
		this.manager = manager;
		if ( manager.isTagInside( tvTag ) )
		{
			MovieInfo info = manager.getInfo( tvTag );
			tvImageUrl = info.getImg_h();
			Log.i( TAG, "tvImageUrl ==" + tvImageUrl );
			if ( !TextUtils.isEmpty( tvImageUrl ) )
			{
				tvAction = info.getAction();
				handler.removeCallbacks( playR );
				handler.post( showImageR );
				LoadImageTask task = new LoadImageTask( context, tvLayout );
				task.execute( tvImageUrl );
			}
			// else
			// {
			// tvAction = null;
			// tvImageUrl = null;
			// handler.post( showLiveR );
			// }
		}
		else if ( firstreFreshTV )
		{
			Log.i( TAG, "refreshInfo--else" );
			tvAction = null;
			tvImageUrl = null;
			handler.post( showLiveR );
			firstreFreshTV = false;
		}

		for ( int i = 0; i < mItems.length; i++ )
		{
			String tag = mItems[i].getTag().toString();
			Log.i( TAG, "refreshInfo tag==" + tag );

			if ( manager.isTagInside( tag ) )
			{
				MovieInfo info = manager.getInfo( tag );
				if ( info == null )
				{
					continue;
				}
				if ( !TextUtils.isEmpty( info.getImg_h() ) )
				{
					initItem( mItems[i], info, "img_h" );
				}
				else if ( !TextUtils.isEmpty( info.getImg_s() ) )
				{
					initItem( mItems[i], info, "img_s" );
				}
				else if ( !TextUtils.isEmpty( info.getImg_s() ) )
				{
					initItem( mItems[i], info, "img_v" );
				}
				else if ( !TextUtils.isEmpty( info.getImg_h2() ) )
				{
					initItem( mItems[i], info, "img_h2" );
				}

				mItems[i].setPkg( info.getApp_package_name() );
				mItems[i].setAppId( info.getApp_id() );
				mItems[i].setAction( info.getAction() );
			}
		}
	}

	@Override
	public void sendMsg()
	{
		sendMsg( mItem1 );
		sendMsg( mItem2 );
		sendMsg( mItem3 );
		sendMsg( mItem4 );
		sendMsg( mItem5 );
		sendMsg( mItem6 );
		sendMsg( mItem7 );
		sendMsg( mItem8 );
		super.sendMsg();
	}

	// private Handler handler = new Handler()
	// {
	// public void handleMessage( android.os.Message msg )
	// {
	// switch( msg.what )
	// {
	// case 0:
	// Log.i( TAG, "handler 0" );
	// default:
	// break;
	// }
	//
	// };
	// };

	public void playDvbLive()
	{
		MovieInfo info = mDBManager.getInfo( tvLayout.getTag().toString() );
		Log.i( TAG, "playDvbLive()--mDBManager:" + mDBManager );
		Log.i( TAG, "playDvbLive()--info:" + info );
		Log.i( TAG, "playDvbLive()--isPlay==" + isPlay );

		if ( !isPlay )
		{
			return;
		}

		if ( info != null && !info.getImg_h().isEmpty() )
		{

			return;
		}
		Log.i( TAG, "playDvbLive()--tvImageUrl==" + tvImageUrl );
		if ( !TextUtils.isEmpty( tvImageUrl ) )
		{
			return;
		}
		
		context.registerDvbMsgListen();
		DVBPlayer.getInstance().stop();
		// 先隐藏直播相关提示
		context.findViewById( R.id.null_data_panel ).setVisibility( View.GONE );
		hideError();
		// 先隐藏surfaceview，避免黑屏
		mPlayView.setVisibility( View.INVISIBLE );
		Log.i( TAG, "playDvbLive()--playR" );
		handler.removeCallbacks( playR );
		Log.i( TAG, "playDvbLive()--playR--1" );
		handler.postDelayed( playR, 500 );
		Log.i( TAG, "playDvbLive()--playR--2" );
		context.isPlay = true;
	}

	private Runnable playR = new Runnable()
	{
		@Override
		public void run()
		{
			Log.i( TAG, "playR..." );
			boolean hasData = DVBPlayer.getInstance().isExistData();
			Log.i( TAG, "hasData:" + hasData );
			mPlayView.setVisibility( View.VISIBLE );
			if ( hasData )
			{
				Log.i( TAG, "DVBPlayer.getInstance().play()" );
				DVBPlayer.getInstance().play();
				hideError();
			}
			else
			{
				Log.i( TAG, "nullDataTip" );
				nullDataTip();
				hideError();
			}
		}
	};

	public void stopDvbLive()
	{
		Log.i( TAG, "stopDvbLive()" );
		mPlayView.setVisibility( View.INVISIBLE );
		handler.removeCallbacks( playR );
		DVBPlayer.getInstance().stop();
		context.unRegisterDvbMsgListen();
	}

	private OnClickListener clickListener = new OnClickListener()
	{
		@Override
		public void onClick( View v )
		{
			String pkg = null, appid = null, server_action = null;

			// if ( TextUtils.isEmpty( pkg ) )// 没缓存也没联网
			if ( !mDBManager.isTagInside( v.getTag().toString() ) )
			{
				Log.i( TAG, "click null" );

				// 默认跳转

				if ( v == tvLayout )
				{
					DVBPlayer.getInstance().stop();
					ApkHelper.goAppByPkg( context, "com.dvb.live" );
					( ( Launcher ) mContext ).isPlay = true;
				}
				if ( v == mItem1 )
				{
					String action = "open_app|by_action|com.dvb.live.action.guide|";
					FCActionTool.forward( mContext, action, "99900188", Launcher.APP_KEY_ID );
				}
				else if ( v == mItem2 )
				{
					String action = "open_app|by_pkg|com.tt.emall|";
					FCActionTool.forward( mContext, action, "99900188", Launcher.APP_KEY_ID );

				}
				else if ( v == mItem4 )
				{
					String action = "open_app|by_pkg_activity|com.gitvvideo|com.qiyi.video.ui.search.QSearchActivity|";
					FCActionTool.forward( mContext, action, "99900188", Launcher.APP_KEY_ID );
				}
				else if ( v == mItem7 )
				{
					// String action =
					// "open_app|by_pkg_activity|com.gitvvideo|com.qiyi.video.ui.search.QSearchActivity|";
					// FCActionTool.forward( mContext, action, "99900188",
					// Launcher.APP_KEY_ID );
					// if ( getsharedPreferences( context ) == null )
					// {
					// return;
					// }
					// Intent intent = new Intent();
					// intent.setAction( "com.hudong.action.DETAIL" );
					// intent.putExtra( "id", "100000001" );
					// context.startActivity( intent );
				}
				else if ( v == mItem8 )
				{
					// if ( getsharedPreferences( context ) == null )
					// {
					// return;
					// }
					// String action =
					// "open_app|by_pkg_activity|com.gitvvideo|com.qiyi.video.ui.search.QSearchActivity|";
					// FCActionTool.forward( mContext, action, "99900188",
					// Launcher.APP_KEY_ID );
				}
				return;
			}

			if ( v != tvLayout )
			{
				pkg = ( ( FcTextView ) v ).getPkg();
				appid = ( ( FcTextView ) v ).getAppId();
				server_action = ( ( FcTextView ) v ).getAction();
				Log.i( TAG, "click pkg : " + pkg );
			}
			else
			{
				MovieInfo info = mDBManager.getInfo( v.getTag().toString() );
				pkg = info.getApp_package_name();
				appid = info.getApp_id();
				server_action = info.getAction();
			}

			if ( !TextUtils.isEmpty( pkg ) )// 如果后台配置了包名和appid
			{
				Log.i( TAG, "click : " + pkg );
				if ( ApkHelper.isApkInstalled( context, pkg ) )// 先检查应用是否存在
				{
					Log.i( TAG, "server_action ==" + server_action );
					FCActionTool.forward( mContext, server_action, "99900188", Launcher.APP_KEY_ID );
				}
				else
				{
					if ( !TextUtils.isEmpty( appid ) )// 如果后台配置了appid
					{
						if ( !CommonUtil.isNetworkConnected( mContext ) )
						{
							Toast.makeText( mContext, "网络未连接", Toast.LENGTH_SHORT ).show();
							return;
						}
						// 未安装则跳转到下载界面
						// if ( v != tvLayout )// 直播小窗口不跳转
						// {
						Intent intent = new Intent( context, DetailsActivity.class );
						intent.putExtra( "aid", appid );
						context.startActivity( intent );
						// }
					}
					else
					// // 如果后台没有配置appid，则直接提示
					{
						Toast.makeText( mContext, "应用未安装", Toast.LENGTH_SHORT ).show();
					}
				}
			}
			else
			{
				Log.i( TAG, "click else" );
				if ( TextUtils.isEmpty( appid ) )// 如果后台没有配置包名和appid
				{
					if ( ApkHelper.isApkInstalled( context, "com.hudong.vod" ) )// 先检查应用是否存在
					{
						// if ( mRecommendIds != null )
						// {
						// String columnID = "";
						// Intent intent = new Intent();
						// if ( !manager.isTagInside( "700" ) )
						// {
						// if ( mRecommendIds.length > 1 )
						// {
						// if ( v == mItem7 )
						// {
						// columnID = mRecommendIds[7];
						// intent.setAction( "com.hudong.action.DETAIL" );
						// intent.putExtra( "id", columnID );
						// Log.i( TAG, "columnID==" + columnID );
						// context.startActivity( intent );
						// }
						// if ( v == mItem8 )
						// {
						// columnID = mRecommendIds[8];
						// intent.setAction( "com.hudong.action.DETAIL" );
						// intent.putExtra( "id", columnID );
						// Log.i( TAG, "columnID==" + columnID );
						// context.startActivity( intent );
						// }
						// }
						// }
						// }
					}
					Log.i( TAG, "no appid click else : " + server_action );
					FCActionTool.forward( mContext, server_action, "99900188", Launcher.APP_KEY_ID );
				}

			}

		}
	};

	private OnFocusChangeListener focusListener = new OnFocusChangeListener()
	{
		@Override
		public void onFocusChange( View v, boolean hasFocus )
		{
			if ( hasFocus )
			{
				itemFocusListener.onItemFocus( v, hasFocus );
				ScAnimation.getInstance( context ).showOnFocusAnimation( v );
			}
			else
			{
				ScAnimation.getInstance( context ).showLooseFocusAinimation( v );
			}
		}
	};

	private OnFocusChangeListener tvFocusListener = new OnFocusChangeListener()
	{
		@Override
		public void onFocusChange( View v, boolean hasFocus )
		{
			if ( hasFocus )
			{
				itemFocusListener.onItemFocus( v, hasFocus );
			}
		}
	};

	private CountDownTimer errorPanelTimer;

	public void showError( String errorStr, String errorCode, long duration )
	{
		Log.i( TAG, "showError--errorStr==" + errorStr + "--errorCode==" + errorCode + "--duration=="
						+ duration );
		Log.i( TAG, "showError" );
		if ( errorPanelTimer != null )
		{
			errorPanelTimer.cancel();
			errorPanelTimer = null;
		}

		if ( errorPanel.isShown() )
		{
			errorPanel.setVisibility( View.GONE );
		}
		String errorCodeText = ( TextUtils.isEmpty( errorCode ) ? "" : ( "咨询代码：" + errorCode ) );
		if ( errorCodeText != null && !errorCodeText.equals( "" ) )
		{
			errorText.setText( errorStr + "\t" + errorCodeText );
		}
		else
		{
			errorText.setText( errorStr );
		}
		Log.i( TAG, "showError--visible" );
		errorPanel.setVisibility( View.VISIBLE );

		if ( duration > 0 )
		{
			errorPanelTimer = new CountDownTimer( duration, 1000 )
			{
				@Override
				public void onTick( long millisUntilFinished )
				{

				}

				@Override
				public void onFinish()
				{
					hideError();
				}
			};
			errorPanelTimer.start();
		}
	}

	public void hideError()
	{
		Log.i( TAG, "hideError" );
		errorPanel.setVisibility( View.GONE );
	}

	public void nullDataTip()
	{
		Log.i( TAG, "nullDataTip" );
		context.findViewById( R.id.null_data_panel ).setVisibility( View.VISIBLE );
	}

	public void hideDataTip()
	{
		context.findViewById( R.id.null_data_panel ).setVisibility( View.GONE );
	}

	public void setPlay( boolean focused )
	{
		Log.i( TAG, "focused:" + focused );
		isPlay = focused;
	}

	// private static String[] mRecommendUrls;

	// private static String[] getRecommendNames( Context context )
	// {
	// if ( getsharedPreferences( context ) != null )
	// {
	// if ( mRecommendUrls == null || mRecommendUrls.length <= 1 )
	// {
	// String RecommendUrls = getsharedPreferences( context ).getString(
	// "recommendUrls",
	// "" );
	// Log.i( TAG, "RecommendUrls" + RecommendUrls );
	// mRecommendUrls = RecommendUrls.split( "\\|" );
	// }
	//
	// return mRecommendUrls;
	// }
	// return null;
	// }

	// private static String[] mRecommendIds;

	// private static String[] getRecommendIds( Context context )
	// {
	// if ( getsharedPreferences( context ) != null )
	// {
	// if ( mRecommendIds == null || mRecommendIds.length <= 1 )
	// {
	// String RecommendIds = getsharedPreferences( context )
	// .getString( "recommendIds", "" );
	// Log.i( TAG, "RecommendIds" + RecommendIds );
	// mRecommendIds = RecommendIds.split( "\\|" );
	// }
	//
	// return mRecommendIds;
	// }
	// return null;
	// }

	// private static SharedPreferences mSharedPreferences = null;

	// private static SharedPreferences getsharedPreferences( Context context )
	// {
	// if ( mSharedPreferences == null )
	// {
	// Context otherAppsContext;
	// try
	// {
	// otherAppsContext = context.createPackageContext( "com.hudong.vod",
	// Context.CONTEXT_IGNORE_SECURITY );
	// mSharedPreferences = otherAppsContext.getSharedPreferences(
	// "hudong_live",
	// Context.MODE_MULTI_PROCESS );
	// }
	// catch ( NameNotFoundException e )
	// {
	// e.printStackTrace();
	// }
	// }
	//
	// return mSharedPreferences;
	// }

	// private void setImage()
	// {
	// for ( int i = 6; i < 8; i++ )
	// {
	// if ( i == 6 )
	// {
	// MovieInfo info = new MovieInfo();
	// info.setImg_h( mRecommendUrls[7] );
	// initItem( mItems[i], info, "img_h" );
	// }
	// else
	// {
	// MovieInfo info = new MovieInfo();
	// info.setImg_h( mRecommendUrls[8] );
	// initItem( mItems[i], info, "img_h" );
	// }
	//
	// }
	// }
}
