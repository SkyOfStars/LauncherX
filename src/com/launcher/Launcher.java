package com.launcher;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dvb.common.ctrl.FCDvb;
import com.dvb.common.ctrl.FCProgData;
import com.dvb.common.ctrl.MessageListener;
import com.dvb.data.DataHolder;
import com.dvb.player.PlayWorker;
import com.dvb.thirdapi.DVBPlayer;
import com.fc.lisence.LisenceInfo;
import com.fc.lisence.LisenceMgr;
import com.fc.util.http.FCHttp;
import com.fc.util.tool.FCActionTool;
import com.fc.util.tool.FCCityUtil;
import com.fc.util.tool.FCNetwork;
import com.fc.util.tool.FCTvDevice;
import com.fc.util.tool.confparams.FCConfigParamsUtil;
import com.hudong.aidl.ReplayConnection;
import com.launcher.LauncherMainModel.ICallBackAdSwitcher;
import com.launcher.ScrollTextView.ScrollCallback;
import com.launcher.ca.CaMailProxy;
import com.launcher.ca.CaMailProxy.IMailReceiver;
import com.launcher.db.DBManager;
import com.launcher.db.info.MovieInfo;
import com.launcher.helper.ApkHelper;
import com.launcher.image.AsyncImageLoader;
import com.launcher.image.ImageLoader;
import com.launcher.manager.BootAdResourceLoad;
import com.launcher.manager.BootToLiveManager;
import com.launcher.provider.FCProviderResolver;
import com.launcher.receiver.NetStateReceiver;
import com.launcher.util.BitMapManager;
import com.launcher.util.CommonConstant;
import com.launcher.util.CommonDeviceUtil;
import com.launcher.util.CommonFileUtil;
import com.launcher.util.CommonImageUtil;
import com.launcher.util.CommonUtil;
import com.launcher.util.DisplayIntentHelper;
import com.launcher.util.UriUtil;
import com.launcher.widget.FcScrollView;
import com.launcher.widget.FcTextView;
import com.launcher.widget.LayoutView;
import com.launcher.widget.LayoutView.Layout2CallBack;
import com.launcher.widget.LayoutView.OnItemFocusListener;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hidvb.ADPICBase;
import android.hidvb.CAMessage;
import android.hidvb.DVBEnum;
import android.hidvb.DVBEnum.CATYPE;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.VPDJni;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class Launcher extends Activity implements OnItemFocusListener
{
	private static String TAG = "Launcher";
	public static final String APP_KEY_ID = "10160";
	private static final String UPDATE_DATA = "update_data";
	private static final int MSG_DATA_TIMING = 10;

	private boolean isFirstInit = true;

	private TextView mWeather, mDate, mTime, mCity, mWeek, mTemp;
	private ImageView btnMsg, btnCalendar, btnCard;
	private TextView mailCountTxt;

	private FcScrollView scrollView;
	private View view1, view2, view3, view4, view5, view6;

	private TextView mMenu1, mMenu2, mMenu3, mMenu4, mMenu5, mMenu6;
	private Button focus;
	private View focusView;
	private int focusId = 0;

	private Map< String, Object > keyMap = new HashMap< String, Object >();

	private LayoutView layout1, layout2, layout3, layout4, layout5, layout6;
	private Boolean isFirstFocus = true;
	private int page = 0;
	private int oldPage = 0;

	private int oldX = 0;
	private int oldY = 0;
	private int oldW = 0;
	private int oldH = 0;

	private WeatherTask weatherTast;
	private static String cityText;
	private static boolean weatherTaskRunning = false;
	private AsyncImageLoader imageLoader = new AsyncImageLoader();

	private LisenceInfo info = null;
	private String server = "", version = "", deviceSN = "", deviceID = "";

	private SharedPreferences updatePre = null;
	private DBManager manager;

	private NetStateReceiver netReceiver;
	private static final int MAIL_MSG = 1919;
	public boolean isPlay = false;
	private boolean isResult = false;

	private ImageView iv_logo;// 开机显示全屏的logo
	private RelativeLayout ll_main;

	private RelativeLayout rl_root;
	private ImageView ivLaba, ivLabaLine;
	private TextSwitcher tvNotice;
	private ScrollTextView autoScrollTextView;

	private List< String > strings;
	public static boolean isFirstRun = false;// Laucnher是否第一次启动

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		Resources res = getResources();
		Configuration config = new Configuration();
		ReplayConnection.getInstance().init( this );
		config.setToDefaults();
		res.updateConfiguration( config, res.getDisplayMetrics() );
		getWindow().setFormat( PixelFormat.RGBA_8888 );
		setContentView( R.layout.activity_launcher );
		info = LisenceMgr.getLisenceData();
		server = info.getServer();
		version = info.getVersion();
		deviceSN = getDecodedSN();
		deviceID = FCTvDevice.getDeviceId();
		manager = new DBManager( Launcher.this );
		MovieInfo backgroudInfo = manager.getInfo( "000" );
		isFirstRun = true;
		if ( backgroudInfo != null )
		{
			CommonImageUtil.saveLauncherBgImageToFile( backgroudInfo.getImg_full(), handler,
							false );
		}

		updatePre = getSharedPreferences( UPDATE_DATA, Context.MODE_PRIVATE );
		ReplayConnection.getInstance().init( this );

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					while ( !FCNetwork.isNetworkActives( Launcher.this ) )
					{
						Thread.sleep( 500 );
					}
					handler.sendEmptyMessage( 2 );
				}
				catch ( Exception ex )
				{
					Log.e( TAG, "onCreate check net,", ex );
				}
			}
		} ).start();

		if ( iv_logo == null )
		{
			initView();
			initCallBalk();
		}
		handler.post( dateRun );

		netReceiver = new NetStateReceiver( this, handler );
		IntentFilter filter = new IntentFilter();
		filter.addAction( WifiManager.NETWORK_STATE_CHANGED_ACTION );
		filter.addAction( WifiManager.WIFI_STATE_CHANGED_ACTION );
		filter.addAction( ConnectivityManager.CONNECTIVITY_ACTION );
		this.registerReceiver( netReceiver, filter );

		CaMailProxy.from( this ).setMailReceiver( mailReceiver );

		( ( Layout1 ) layout1 ).tvLayout.requestFocus();
		( ( Layout1 ) layout1 ).tvLayout.post( new Runnable()
		{
			@Override
			public void run()
			{
				showFocus( ( ( Layout1 ) layout1 ).tvLayout, 100 );
			}
		} );
		refreshAddAppUninstalled();
		Intent intent = getIntent();
		refreshAddItem( intent );
	}

	private void initCallBalk()
	{
		autoScrollTextView.setScrollCallback( new ScrollCallback()
		{
			@Override
			public void onScrollStart()
			{

			}

			@Override
			public void onScrollError()
			{

			}

			@Override
			public void onScrollEnd()
			{
				adIndex++;
				if ( adIndex >= strings.size() )
				{
					adIndex = 0;
				}

				runOnUiThread( new Runnable()
				{

					@Override
					public void run()
					{
						autoScrollTextView.setVisibility( View.GONE );
						autoScrollTextView.stopScroll();
						autoScrollTextView.setContent( strings.get( adIndex ) );
						autoScrollTextView.startScroll();
						autoScrollTextView.setVisibility( View.VISIBLE );
					}
				} );
			}
		} );
		layout2.setCallback( new Layout2CallBack()
		{
			@Override
			public void isHasFocus()
			{
				Log.i( TAG, "layout2 setCallback" );
				if ( isPlay )
				{
					layout1.stopDvbLive();
				}
				isPlay = false;
				if ( page != 1 )
				{
					focus.setVisibility( View.GONE );
					page = 1;
					scrollView.gotoPage( page );
				}
			}
		} );
	}

	@Override
	protected void onRestoreInstanceState( Bundle bundle )
	{
		// TODO Auto-generated method stub
		Log.i( TAG, "onRestoreInstanceState" );
		page = bundle.getInt( "page", 0 );

		showMenuSelected();
		if ( page != 0 )
		{
			layout1.stopDvbLive();
			( ( Layout1 ) layout1 ).setPlay( false );
			isPlay = false;
		}

		super.onRestoreInstanceState( bundle );
	}

	@Override
	protected void onSaveInstanceState( Bundle outState )
	{
		Log.i( TAG, "onSaveInstanceState" );
		outState.putInt( "page", page );
		super.onSaveInstanceState( outState );
	}

	private void refreshAddAppUninstalled()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction( Intent.ACTION_PACKAGE_REMOVED );
		filter.addAction( Intent.ACTION_PACKAGE_ADDED );
		filter.addDataScheme( "package" );
		filter.setPriority( 1000 );
		registerReceiver( new BroadcastReceiver()
		{
			@Override
			public void onReceive( Context context, Intent intent )
			{
				// 接收卸载广播
				if ( intent.getAction().equals( Intent.ACTION_PACKAGE_REMOVED ) )
				{

					// String packageName = intent.getDataString();
					String[] ss = intent.getDataString().split( ":" );
					String packageName = ss[1];
					Log.i( TAG, "refreshAddAppUninstalled:" + packageName );
					if ( !packageName.isEmpty() )
					{
						if ( layout5 != null )
						{
							layout5.refreshInfo( manager );
						}
					}

				}
			}
		}, filter );
	}

	private void toLive( int durion )
	{
		VPDJni VPD = new VPDJni();
		if ( VPD != null )
		{
			// 已经激活
			if ( VPD.isActivated() == 0 )
			{
				Log.i( TAG, "已经激活" );
				BootToLiveManager.delayStartPkg( Launcher.this, "com.dvb.live", durion );
			}
		}
	}

	private Runnable toLiveR = new Runnable()
	{
		@Override
		public void run()
		{
			Log.i( TAG, "toLiveR" );
			if ( LauApplication.getClientVer().contains( "SXYQ" ) )
			{

			}
			else
			{
				iv_logo.setVisibility( View.GONE );
				ll_main.setVisibility( View.VISIBLE );
				layout1.playDvbLive();
				isPlay = true;
				( ( Layout1 ) layout1 ).setPlay( true );
				toLive( 13 * 1000 );// 无操作10s进入直播
			}

			SystemProperties.set( "sys.fc.system.init", "1" );
		}
	};

	private void showMenuSelected()
	{
		Log.i( TAG, "showMenuSelected()" );
		switch( page )
		{
		case 0:
			mMenu1.setSelected( true );
			mMenu2.setSelected( false );
			mMenu3.setSelected( false );
			mMenu5.setSelected( false );
			mMenu4.setSelected( false );
			mMenu6.setSelected( false );
			break;
		case 1:

			mMenu1.setSelected( false );
			mMenu2.setSelected( true );
			mMenu3.setSelected( false );
			mMenu5.setSelected( false );
			mMenu4.setSelected( false );
			mMenu6.setSelected( false );
			break;
		case 2:

			mMenu1.setSelected( false );
			mMenu2.setSelected( false );
			mMenu3.setSelected( true );
			mMenu5.setSelected( false );
			mMenu4.setSelected( false );
			mMenu6.setSelected( false );
			break;
		case 3:
			mMenu1.setSelected( false );
			mMenu2.setSelected( false );
			mMenu3.setSelected( false );
			mMenu5.setSelected( false );
			mMenu4.setSelected( true );
			mMenu6.setSelected( false );
			break;
		case 4:

			mMenu1.setSelected( false );
			mMenu2.setSelected( false );
			mMenu3.setSelected( false );
			mMenu5.setSelected( true );
			mMenu4.setSelected( false );
			mMenu6.setSelected( false );
			break;

		case 5:

			mMenu1.setSelected( false );
			mMenu2.setSelected( false );
			mMenu3.setSelected( false );
			mMenu5.setSelected( false );
			mMenu4.setSelected( false );
			mMenu6.setSelected( true );
			break;

		}
	}

	private void initView()
	{
		rl_root = ( RelativeLayout ) findViewById( R.id.rl_root );
		refreshBackGround();
		// setBackground();
		iv_logo = ( ImageView ) findViewById( R.id.iv_logo );
		ll_main = ( RelativeLayout ) findViewById( R.id.ll_main );

		tvNotice = ( TextSwitcher ) findViewById( R.id.ts_ad );
		ivLaba = ( ImageView ) findViewById( R.id.iv_laba );
		ivLabaLine = ( ImageView ) findViewById( R.id.iv_laba_line );
		autoScrollTextView = ( ScrollTextView ) findViewById( R.id.autoscroll );

		mDate = ( TextView ) findViewById( R.id.lau_date );
		mTime = ( TextView ) findViewById( R.id.lau_time );
		mWeek = ( TextView ) findViewById( R.id.lau_week );
		mCity = ( TextView ) findViewById( R.id.lau_city );
		mTemp = ( TextView ) findViewById( R.id.lau_temp );
		mWeather = ( TextView ) findViewById( R.id.lau_weather );
		btnMsg = ( ImageView ) findViewById( R.id.btn_msg );
		btnCalendar = ( ImageView ) findViewById( R.id.btn_calendar );
		btnCard = ( ImageView ) findViewById( R.id.btn_card );
		mailCountTxt = ( TextView ) findViewById( R.id.lau_mail_count );

		btnMsg.setOnClickListener( clickListener );
		btnCalendar.setOnClickListener( clickListener );
		btnCard.setOnClickListener( clickListener );
		btnMsg.setOnFocusChangeListener( focusListener );
		btnCalendar.setOnFocusChangeListener( focusListener );
		btnCard.setOnFocusChangeListener( focusListener );

		mMenu1 = ( TextView ) findViewById( R.id.lau_menu1 );
		mMenu2 = ( TextView ) findViewById( R.id.lau_menu2 );
		mMenu3 = ( TextView ) findViewById( R.id.lau_menu3 );
		mMenu4 = ( TextView ) findViewById( R.id.lau_menu4 );
		mMenu5 = ( TextView ) findViewById( R.id.lau_menu5 );
		mMenu6 = ( TextView ) findViewById( R.id.lau_menu6 );

		mMenu1.setOnClickListener( clickListener );
		mMenu2.setOnClickListener( clickListener );
		mMenu3.setOnClickListener( clickListener );
		mMenu4.setOnClickListener( clickListener );
		mMenu5.setOnClickListener( clickListener );
		mMenu6.setOnClickListener( clickListener );

		mMenu1.setOnFocusChangeListener( focusListener );
		mMenu2.setOnFocusChangeListener( focusListener );
		mMenu3.setOnFocusChangeListener( focusListener );
		mMenu4.setOnFocusChangeListener( focusListener );
		mMenu5.setOnFocusChangeListener( focusListener );
		mMenu6.setOnFocusChangeListener( focusListener );
		focus = ( Button ) findViewById( R.id.lau_focus );
		scrollView = ( FcScrollView ) findViewById( R.id.lau_scroll_view );

		LayoutParams params2 = new LayoutParams( CommonUtil.getWinWidth( this ), 550 );

		view1 = findViewById( R.id.m_layout1 );
		view2 = findViewById( R.id.m_layout2 );
		view3 = findViewById( R.id.m_layout3 );
		view4 = findViewById( R.id.m_layout4 );
		view5 = findViewById( R.id.m_layout5 );
		view6 = findViewById( R.id.m_layout6 );

		view1.setLayoutParams( params2 );
		view2.setLayoutParams( params2 );
		view3.setLayoutParams( params2 );
		view4.setLayoutParams( params2 );
		view5.setLayoutParams( params2 );
		view6.setLayoutParams( params2 );

		layout1 = new Layout1( this, handler );
		layout2 = new Layout2( this );
		layout3 = new Layout3( this );
		layout4 = new Layout4( this );
		layout5 = new Layout5( this );
		layout6 = new Layout6( this );
		layout2.setManager( manager );
		if ( isFirstInit )
		{
			// 公告控件初始化
			tvNotice.setFactory( new ViewSwitcher.ViewFactory()
			{
				public View makeView()
				{
					TextView tv = new TextView( Launcher.this );
					// 设置文字大小
					tv.setTextSize( 22 );
					// 设置文字 颜色
					tv.setTextColor( getResources().getColor( android.R.color.white ) );
					tv.setSingleLine();
					tv.setFocusable( false );
					tv.setFocusable( false );
					tv.setClickable( false );
					tv.setGravity( Gravity.CENTER | Gravity.LEFT );
					tv.setEllipsize( TruncateAt.END );
					isFirstInit = false;
					return tv;
				}
			} );
		}

		refreshImageData();
		refreshMenuData();
	}

	public void getAdSwticherCacheData( Context context )
	{
		SharedPreferences updatePre = context.getSharedPreferences( CommonConstant.UPDATE_AD_DATA,
						Context.MODE_PRIVATE );
		String result = updatePre.getString( CommonConstant.UPDATE_AD_RESULT_KEY, "" );
		JSONObject object = null;
		List< String > strsList = new ArrayList< String >();
		List< String > textList = new ArrayList< String >();
		Log.d( TAG, "========" + result );

		try
		{
			object = new JSONObject( result );
			if ( object.getString( "ret" ).equals( "0" ) )
			{
				int textShowType = 1;
				if ( !object.isNull( "config" ) )
				{
					JSONObject configObj = ( JSONObject ) object.get( "config" );
					if ( !configObj.isNull( "roll_direction" ) )
					{
						textShowType = configObj.getInt( "roll_direction" );
					}
				}
				/**
				 * 文字列表
				 */
				if ( !object.isNull( "text" ) )
				{
					Object commonObj = object.opt( "text" );
					if ( commonObj instanceof JSONArray )
					{
						JSONArray textArray = ( JSONArray ) commonObj;
						for ( int i = 0; i < textArray.length(); i++ )
						{
							textList.add( textArray.getString( i ) );
						}
					}
					else if ( commonObj instanceof String )
					{
						textList.add( commonObj.toString() );
					}

				}

				Log.d( TAG, textList.size() + "=====" );
				if ( textList.size() > 0 && textShowType == 1 )// 左右文字
				{
					strsList.clear();
					for ( int i = 0; i < textList.size(); i++ )
					{
						strsList = CommonUtil.toMultiLine( strsList, textList.get( i ), 75 );
						strsList.add( "" );
					}

				}
				if ( textShowType == 2 )
				{
					strsList = textList;
				}
				showAdSwitcher( strsList, textShowType );
				return;
			}
			else
			{
				if ( object.getInt( "ret" ) < 0 )
				{
					hideAdSwitcher();
				}
				else
				{
					showAdSwitcher( strsList, -1 );
				}
			}
		}

		catch ( JSONException e )
		{
		}
		Log.e( TAG, "hideAdSwitcher()" );
		hideAdSwitcher();
	}

	public void showAdSwitcher( List< String > strsList, int textShowType )
	{
		this.strings = strsList;
		if ( textShowType == 1 )
		{
			adIndex = 0;
			ivLaba.setVisibility( View.VISIBLE );
			ivLabaLine.setVisibility( View.VISIBLE );
			tvNotice.setVisibility( View.GONE );
			autoScrollTextView.stopScroll();
			autoScrollTextView.setVisibility( View.GONE );
			tvNotice.setTag( strings );
			tvNotice.removeCallbacks( tvNoticeAction );
			tvNotice.postDelayed( tvNoticeAction, 0 * 1000 );
		}
		else if ( textShowType == 2 )
		{
			adIndex = 0;
			ivLaba.setVisibility( View.VISIBLE );
			ivLabaLine.setVisibility( View.VISIBLE );
			tvNotice.setVisibility( View.GONE );
			tvNotice.removeCallbacks( tvNoticeAction );
			autoScrollTextView.stopScroll();
			autoScrollTextView.setContent( strings.get( adIndex ) );
			autoScrollTextView.startScroll();
			autoScrollTextView.setVisibility( View.VISIBLE );
			// 有问题
			// mTextHandler.sendEmptyMessage( 0 );

			bringToFront( autoScrollTextView );
		}
	}

	/**
	 * 使view显示在最上层
	 * 
	 * @param view
	 */
	private void bringToFront( View view )
	{
		view.getParent().requestLayout();
		view.bringToFront();
	}

	public void hideAdSwitcher()
	{
		Log.i( TAG, "hideAdSwitcher" );
		adIndex = 0;
		tvNotice.removeCallbacks( tvNoticeAction );
		autoScrollTextView.stopScroll();
		ivLaba.setVisibility( View.INVISIBLE );
		ivLabaLine.setVisibility( View.INVISIBLE );
		tvNotice.setVisibility( View.GONE );
		autoScrollTextView.setVisibility( View.GONE );
	}

	public void getAdSwticher( final Context context )
	{
		Log.i( TAG, "getAdSwticher" );
		final SharedPreferences updatePre = context.getSharedPreferences(
						CommonConstant.UPDATE_AD_DATA, Context.MODE_PRIVATE );
		String m_last_update = updatePre.getString( CommonConstant.UPDATE_AD_DATA_KEY,
						"1900-01-01 00:00:00" );
		String result = updatePre.getString( CommonConstant.UPDATE_AD_RESULT_KEY, "" );
		if ( "".equals( result ) )
		{
			m_last_update = "";
			Log.d( TAG, ".equals( result )" );
		}
		else
		{
			if ( autoScrollTextView.getVisibility() != View.VISIBLE
							&& tvNotice.getVisibility() != View.VISIBLE )
			{
				getAdSwticherCacheData( context );
			}
		}
		LauncherMainModel model = new LauncherMainModel();
		model.getAdSwitcher( m_last_update, new ICallBackAdSwitcher()
		{
			@Override
			public void finish()
			{
				Log.i( TAG, "ICallBackAdSwitcher finish" );
				getAdSwticherCacheData( context );
			}

			@Override
			public void finishUpDateTime( String updataTime )
			{
				Editor editor = updatePre.edit();
				editor.putString( CommonConstant.UPDATE_AD_DATA_KEY, updataTime );
				editor.apply();
				editor.commit();
			}

			@Override
			public void finishSaveDate( String result )
			{
				Editor editor = updatePre.edit();
				editor.putString( CommonConstant.UPDATE_AD_RESULT_KEY, result );
				editor.apply();
				editor.commit();
			}
		} );
	}

	private int adIndex = 0;
	/**
	 * 公告的滚动
	 */
	private Runnable tvNoticeAction = new Runnable()
	{

		@Override
		public void run()
		{
			@SuppressWarnings( "unchecked" )
			List< String > strsList = ( List< String > ) tvNotice.getTag();
			ivLaba.setVisibility( View.VISIBLE );
			ivLabaLine.setVisibility( View.VISIBLE );
			tvNotice.setVisibility( View.VISIBLE );
			tvNotice.setAnimationCacheEnabled( false );
			tvNotice.clearAnimation();
			tvNotice.clearAccessibilityFocus();
			tvNotice.clearFocus();

			if ( strsList.isEmpty() )
			{
				return;
			}
			if ( strsList.size() <= adIndex )
			{
				return;
			}
			String text = strsList.get( adIndex );
			Log.i( TAG, "text:" + text );
			adIndex++;
			if ( adIndex > strsList.size() - 1 )
			{
				adIndex = 0;
			}
			if ( text.equals( "" ) )
			{
				tvNotice.postDelayed( tvNoticeAction, 1 * 1000 );
				return;
			}
			if ( strsList.size() > 1 )
			{
				// 设置切入动画

				tvNotice.setInAnimation( AnimationUtils.loadAnimation( Launcher.this,
								R.anim.slide_in_bottom ) );
				// 设置切出动画
				tvNotice.setOutAnimation( AnimationUtils.loadAnimation( Launcher.this,
								R.anim.slide_out_up ) );
			}
			// items是一个字符串列表，index就是动态的要显示的items中的索引
			tvNotice.setText( text );
			tvNotice.postDelayed( tvNoticeAction, 2 * 1000 );
		}
	};

	@SuppressWarnings( "deprecation" )
	private void refreshBackGround()
	{
		String imageLauncherBgPath = SystemProperties.get( "fc.config.path", "/fc/config/" )
						+ "launcher_bg.png";

		Log.i( TAG, "imageLauncherBgPath==" + imageLauncherBgPath );
		Bitmap bm = CommonFileUtil.decodeImage( imageLauncherBgPath );
		if ( bm != null )
		{
			rl_root.setBackgroundDrawable( new BitmapDrawable( bm ) );
		}
		else
		{
			rl_root.setBackgroundResource( R.drawable.bg );
		}
	}

	private void refreshMenuData()
	{
		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				if ( manager.isTagInside( "100" ) )
				{
					final MovieInfo info = manager.getInfo( "100" );
					if ( info != null )
					{
						handler.post( new Runnable()
						{
							@Override
							public void run()
							{
								mMenu1.setText( info.getTitle() );
								mMenu1.setTag( info.getAction() );
							}
						} );

					}

				}

				if ( manager.isTagInside( "200" ) )
				{
					final MovieInfo info = manager.getInfo( "200" );
					if ( info != null )
					{
						handler.post( new Runnable()
						{
							@Override
							public void run()
							{
								mMenu2.setText( info.getTitle() );
								mMenu2.setTag( info.getAction() );
							}
						} );

					}

				}
				else if ( manager.isTagInside( "700" ) )
				{
					final MovieInfo info = manager.getInfo( "700" );
					if ( info != null )
					{
						handler.post( new Runnable()
						{
							@Override
							public void run()
							{
								mMenu2.setText( info.getTitle() );
								mMenu2.setTag( info.getAction() );
							}
						} );

					}
				}

				if ( manager.isTagInside( "300" ) )
				{

					final MovieInfo info = manager.getInfo( "300" );
					if ( info != null )
					{
						handler.post( new Runnable()
						{
							@Override
							public void run()
							{
								mMenu3.setText( info.getTitle() );
								mMenu3.setTag( info.getAction() );
							}
						} );

					}

				}

				if ( manager.isTagInside( "400" ) )
				{

					final MovieInfo info = manager.getInfo( "400" );
					if ( info != null )
					{
						handler.post( new Runnable()
						{
							@Override
							public void run()
							{
								mMenu4.setText( info.getTitle() );
								mMenu4.setTag( info.getAction() );
							}
						} );

					}

				}

				if ( manager.isTagInside( "500" ) )
				{
					final MovieInfo info = manager.getInfo( "500" );
					if ( info != null )
					{
						handler.post( new Runnable()
						{
							@Override
							public void run()
							{
								mMenu5.setText( info.getTitle() );
								mMenu5.setTag( info.getAction() );
							}
						} );

					}

				}

				if ( manager.isTagInside( "600" ) )
				{
					final MovieInfo info = manager.getInfo( "600" );
					if ( info != null )
					{
						handler.post( new Runnable()
						{
							@Override
							public void run()
							{
								mMenu6.setText( info.getTitle() );
								mMenu6.setTag( info.getAction() );
							}
						} );

					}

				}
			}
		} ).start();

	}

	/**
	 * 刷新加载坑位数据，放在子线程
	 */
	private void refreshImageData()
	{
		Log.i( TAG, "refreshImageData()" );
		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				if ( layout1 != null )
				{
					layout1.refreshInfo( manager );
				}
				if ( layout2 != null )
				{
					layout2.refreshInfo( manager );
				}
				if ( layout3 != null )
				{
					layout3.refreshInfo( manager );
				}
				if ( layout4 != null )
				{
					layout4.refreshInfo( manager );
				}
				if ( layout5 != null )
				{
					layout5.refreshInfo( manager );
				}
				if ( layout6 != null )
				{
					layout6.refreshInfo( manager );
				}
			}
		} ).start();
	}

	protected void onStart()
	{
		Log.i( TAG, "onStart" );
		getAdSwticherCacheData( this );
		DVBPlayer.getInstance().requestChannelData();
		DVBPlayer.getInstance().setVolMode();
		notifyUpdateWeather( this );
		handler.removeMessages( MAIL_MSG );
		handler.sendEmptyMessage( MAIL_MSG );
		if ( isResult )
		{
			isResult = false;
		}
		else
		{
			handler.removeMessages( 7 );
			Message msg = handler.obtainMessage( 7, 0, 0, page );
			handler.sendMessageDelayed( msg, 1000 );
		}
		super.onStart();
	}

	@Override
	protected void onNewIntent( Intent intent )
	{
		Log.i( TAG, "onNewIntent" );
		refreshAddItem( intent );
		Log.i( TAG, "onNewIntent isplay:" + isPlay );
		super.onNewIntent( intent );
	}

	private void refreshAddItem( Intent intent )
	{
		Layout5 lay = ( Layout5 ) layout5;
		String tag = intent.getStringExtra( "tag" );
		String title = intent.getStringExtra( "title" );
		String packagename = intent.getStringExtra( "package" );
		Bitmap bitmap = BitMapManager.getMapManager().getBitMap();
		if ( tag != null && title != null && packagename != null && bitmap != null )
		{
			lay.refreshAddedItem( manager, tag, title, packagename, bitmap );// 刷新Add添加APK
			BitMapManager.getMapManager().setBitMap( null );
		}
		if ( page == 0 )
		{
			Log.i( TAG, "refreshAddItem onNewIntent" );
			layout1.playDvbLive();
			isPlay = true;
			( ( Layout1 ) layout1 ).setPlay( true );
		}
	}

	@Override
	protected void onResume()
	{
		Log.i( TAG, "onResume" );
		handler.removeMessages( MSG_DATA_TIMING );
		handler.sendEmptyMessage( MSG_DATA_TIMING );
		if ( page != 0 )
		{

			handler.postDelayed( new Runnable()
			{
				@Override
				public void run()
				{
					layout1.stopDvbLive();
					( ( Layout1 ) layout1 ).setPlay( false );
				}
			}, 500 );

		}
		String systemInitState = SystemProperties.get( "sys.fc.system.init", "1" );
		Log.i( TAG, "systemInitState:" + systemInitState );

		if ( "0".equalsIgnoreCase( systemInitState ) )
		{
			layout1.stopDvbLive();

			String deviceVer = FCTvDevice.getDeviceVersion();
			Log.i( TAG, "---deviceVer:" + deviceVer );
			if ( deviceVer != null && deviceVer.contains( "SM" ) )
			{
				BootAdResourceLoad.getInstance().loadBootAdResource( Launcher.this, handler );
			}
			else
			{
				handler.postDelayed( toLiveR, 0 * 1000 );
			}
			SystemProperties.set( "sys.fc.system.init", "1" );
			Log.e( TAG, "onStart -> init=" + systemInitState + " set after" );

		}
		else
		{
			iv_logo.setVisibility( View.GONE );
			ll_main.setVisibility( View.VISIBLE );
			if ( page == 0 )
			{
				( ( Layout1 ) layout1 ).setPlay( true );
				layout1.playDvbLive();
				isPlay = true;
			}
		}

		Log.i( TAG, "onResume isPlay:" + isPlay );

		if ( Integer.parseInt( FCProviderResolver.getInstance().init( this )
						.getConfig( "smart_sound_mode", "1" ) ) == 1 )
		{
			SystemProperties.set( "sys.smart.vol", "true" );
			Log.i( TAG, "smart volume : true" );
		}
		else
		{
			SystemProperties.set( "sys.smart.vol", "false" );
			Log.i( TAG, "smart volume : false" );
		}
		DisplayIntentHelper.sendDisplayBroadcast( this, "time" );
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		handler.removeMessages( MSG_DATA_TIMING );
		BootToLiveManager.breakStartLive();
		SystemProperties.set( "sys.smart.vol", "false" );
		Log.i( TAG, "smart volume : false" );
		Log.i( TAG, "onPause isPlay:" + isPlay );
		if ( isPlay )
		{
			layout1.stopDvbLive();
			isPlay = false;
		}
		isFirstRun = false;
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		hideAdSwitcher();
		unRegisterDvbMsgListen();
		isFirstRun = false;
		handler.removeMessages( MAIL_MSG );
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		isFirstRun = false;
		unregisterReceiver( netReceiver );
	}

	@Override
	public boolean onKeyUp( int keyCode, KeyEvent event )
	{
		DVBPlayer.getInstance().onKeyUpProcess( keyCode, event );
		// 开机启动10S钟无操作则进入直播
		handler.removeCallbacks( toLiveR );
		BootToLiveManager.breakStartLive();
		return super.onKeyUp( keyCode, event );
	}

	@Override
	public boolean onKeyDown( int keyCode, KeyEvent event )
	{
		DVBPlayer.getInstance().onKeyDownProcess( keyCode, event );

		Log.i( TAG, "onKeyDown:v:" + getCurrentFocus() );
		if ( keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME )
		{
			Layout5 lay = ( Layout5 ) layout5;
			if ( lay.popupWindow.isShowing() )
			{
				lay.popupWindow.dismiss();
				return true;
			}
			return true;
		}
		if ( keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1
						|| keyCode == KeyEvent.KEYCODE_2 || keyCode == KeyEvent.KEYCODE_3
						|| keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5
						|| keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_7
						|| keyCode == KeyEvent.KEYCODE_8 || keyCode == KeyEvent.KEYCODE_9 )
		{
			KeyOperation.doAction( Launcher.this, keyCode, keyMap );
		}
		if ( keyCode == KeyEvent.KEYCODE_DPAD_DOWN )
		{
			if ( btnCalendar.hasFocus() || btnCard.hasFocus() || btnMsg.hasFocus() )
			{
				switch( page )
				{
				case 0:
					mMenu1.requestFocus();
					break;
				case 1:
					mMenu2.requestFocus();
					break;
				case 2:
					mMenu3.requestFocus();
					break;
				case 3:
					mMenu4.requestFocus();
					break;
				case 4:
					mMenu5.requestFocus();
					break;
				case 5:
					mMenu6.requestFocus();
					break;

				}
				return true;
			}

			if ( mMenu3.hasFocus() )
			{
				layout3.mItems[0].requestFocus();
				return true;
			}

			if ( page == 0 )
			{
				if ( btnCard.isFocused() )
				{
					( ( Layout1 ) layout1 ).tvLayout.requestFocus();
					showFocus( ( ( Layout1 ) layout1 ).tvLayout, 100 );
					return true;
				}
				if ( keyCode == KeyEvent.KEYCODE_HOME )
				{
					return true;
				}
			}
			if ( page == 2 )
			{
				if ( btnCard.isFocused() )
				{
					layout3.mItems[0].requestFocus();
					return true;
				}

			}
			else if ( page == 4 )
			{
				if ( btnCard.isFocused() )
				{
					layout6.mItems[0].requestFocus();
					return true;
				}

			}
		}

		if ( keyCode == KeyEvent.KEYCODE_DPAD_UP )
		{
		}

		if ( keyCode == KeyEvent.KEYCODE_MENU )
		{
			if ( page == 4 )
			{

				Layout5 lay = ( Layout5 ) layout5;
				View view = this.getCurrentFocus();

				if ( view instanceof FcTextView )
				{
					FcTextView v = ( FcTextView ) view;
					if ( v.getIsAppAdded() )
					{
						lay.showPopWindow();
						lay.currentPkg = v.getPkg();
						lay.currentTAG = v.getTag().toString();
					}
				}

			}

		}

		return super.onKeyDown( keyCode, event );
	}

	private void getKeyValue()
	{
		if ( keyMap != null && !keyMap.isEmpty() )
		{
			Log.i( TAG, "keyMap already ok" );
			return;
		}

		new Thread()
		{
			public void run()
			{
				if ( keyMap != null && !keyMap.isEmpty() )
				{
					Log.i( TAG, "keyMap already ok" );
					return;
				}
				Log.i( TAG, "keyMap is going" );

				Log.i( TAG, "request keycode action" );
				// String url = server + "/api/ad/getAdInfo?adid=18&version=" +
				// version;
				String url = UriUtil.createUrl( "api/ad/getAdInfo" ) + "?adid=18&version=" + version
								+ "&mac=" + FCTvDevice.getEthMacAddress() + "&sn="
								+ CommonDeviceUtil.getDecodedSN() + "&ca="
								+ CommonDeviceUtil.getCa();
				try
				{
					String jsonResult = FCHttp.httpForGetMethod( url );
					if ( jsonResult != null && !"".equals( jsonResult ) )
					{
						JSONObject jsonObject = new JSONObject( jsonResult );
						if ( jsonObject != null )
						{
							if ( jsonObject.getString( "ret" ).equals( "0" ) )
							{
								keyMap.put( "key", jsonObject.getJSONArray( "text" ) );
								keyMap.put( "action", jsonObject.getJSONArray( "action" ) );
							}
						}
					}
				}
				catch ( Exception e )
				{
					Log.e( TAG, e.getMessage() );
				}
			}
		}.start();
	}

	@Override
	public void onItemFocus( View v, boolean bool )
	{
		Log.i( TAG, "onItemFocus v : " + v.getTag() );
		focusView = v;
		focusId = v.getId();
		Log.i( TAG, "onItemFocus page ==: " + page );
		if ( page != 0 )
		{
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
		}

		if ( page == 0 )
		{
			if ( !isPlay )
			{
				{
					layout1.playDvbLive();
				}
				isPlay = true;
			}
		}

		if ( page != 1 )
		{
			if ( layout2.channeLv != null )
			{
				layout2.channeLv.setSelection( 0 );
			}
		}
		if ( v.getTag() == "101" )
		{
			if ( focus != null )
			{
				handler.postDelayed( new Runnable()
				{

					@Override
					public void run()
					{
						focus.setVisibility( View.VISIBLE );

					}
				}, 500 );

			}
		}
		else
		{
			if ( focus != null )
			{
				focus.setVisibility( View.GONE );
			}

		}
		switch( v.getId() )
		{
		case R.id.lay1_tv:

			if ( isFirstFocus )
			{
				handler.postDelayed( new Runnable()
				{
					@Override
					public void run()
					{
						focus.setVisibility( View.VISIBLE );
						isFirstFocus = false;

					}
				}, 800 );
			}
			else
			{
				focus.setVisibility( View.VISIBLE );
			}

			Log.i( "ttt", "lay1_tv onItemFocus" );
			if ( bool )
			{
				if ( isPlay )
				{
					showFocus( v, 100 );
				}
			}
			else
			{
				focus.setVisibility( View.GONE );
			}

			break;
		case R.id.lay1_item1:
			( ( Layout1 ) layout1 ).setPlay( true );
			if ( !isPlay )
			{
				layout1.playDvbLive();
			}
			isPlay = true;
			break;
		case R.id.lay1_item2:
			if ( page != 0 )
			{

				focus.setVisibility( View.GONE );
				page = 0;
				scrollView.gotoPage( page );
			}

			( ( Layout1 ) layout1 ).setPlay( true );
			if ( !isPlay )
			{
				layout1.playDvbLive();
			}
			isPlay = true;

			break;
		// case R.id.lay1_item3:
		case R.id.lay1_item4:

		case R.id.lay1_item5:
			focus.setVisibility( View.GONE );
			if ( !isPlay )
			{
				layout1.playDvbLive();
			}
			isPlay = true;
			if ( page != 0 )
			{
				if ( page == 1 )
				{
					page = 0;
					scrollView.gotoRight( page, view1.getWidth() - CommonUtil.getWinWidth( this ) );
				}
				else
				{
					page = 0;
					scrollView.gotoPage( page );
				}

			}
			if ( focusId == R.id.lay1_item1 || focusId == R.id.lay1_item4 )
			{
				scrollView.gotoLeft( page );
				if ( !isPlay )
				{
					layout1.playDvbLive();
				}
				isPlay = true;
			}
			if ( focusId == R.id.lay1_item2 || focusId == R.id.lay1_item5 )
			{
				scrollView.gotoRight( page, view1.getWidth() - CommonUtil.getWinWidth( this ) );
				if ( !isPlay )
				{
					layout1.playDvbLive();
				}
				isPlay = true;
			}

			break;
		case R.id.lay1_item8:
			focus.setVisibility( View.GONE );
			if ( !isPlay )
			{
				layout1.playDvbLive();
			}
			isPlay = true;
			if ( page != 0 )
			{
				page = 0;
				scrollView.gotoRight( page, view1.getWidth() - CommonUtil.getWinWidth( this ) );
			}

			break;
		case R.id.lay2_item1:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 1 )
			{
				focus.setVisibility( View.GONE );
				page = 1;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay2_item2:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 1 )
			{
				focus.setVisibility( View.GONE );
				page = 1;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay2_item3:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 1 )
			{
				focus.setVisibility( View.GONE );
				page = 1;
				scrollView.gotoPage( page );
			}
			break;

		case R.id.lay2_item4:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 1 )
			{
				focus.setVisibility( View.GONE );
				page = 1;
				scrollView.gotoPage( page );
			}

			break;

		case R.id.lay2_item6:
			Log.i( "ttt", "lay2_item4lay2_item4" );

			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 1 )
			{
				focus.setVisibility( View.GONE );
				page = 1;
				scrollView.gotoPage( page );
			}

			break;
		case R.id.lay3_item1:
		case R.id.lay3_item2:
		case R.id.lay3_item3:
		case R.id.lay3_item4:
		case R.id.lay3_item5:
		case R.id.lay3_item6:
		case R.id.lay3_item7:
		case R.id.lay3_item8:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 2 )
			{
				focus.setVisibility( View.GONE );

				page = 2;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay4_item1:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 3 )
			{
				focus.setVisibility( View.GONE );
				page = 3;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay4_item2:

			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 3 )
			{
				focus.setVisibility( View.GONE );
				page = 3;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay5_item1:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 4 )
			{
				focus.setVisibility( View.GONE );
				page = 4;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay5_item2:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 4 )
			{
				focus.setVisibility( View.GONE );
				page = 4;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay5_item3:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 4 )
			{
				focus.setVisibility( View.GONE );
				page = 4;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay5_item4:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 4 )
			{
				focus.setVisibility( View.GONE );
				page = 4;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay5_item5:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 4 )
			{
				focus.setVisibility( View.GONE );
				page = 4;
				scrollView.gotoPage( page );
			}
		case R.id.lay5_item6:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 4 )
			{
				focus.setVisibility( View.GONE );
				page = 4;
				scrollView.gotoPage( page );
			}
			break;

		case R.id.lay5_item8:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 4 )
			{
				focus.setVisibility( View.GONE );
				page = 4;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay6_item1:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 5 )
			{
				focus.setVisibility( View.GONE );
				page = 5;
				scrollView.gotoPage( page );
			}
			break;

		case R.id.lay6_item2:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 5 )
			{
				focus.setVisibility( View.GONE );
				page = 5;
				scrollView.gotoPage( page );
			}
			break;

		case R.id.lay6_item3:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 5 )
			{
				focus.setVisibility( View.GONE );
				page = 5;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay6_item4:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 5 )
			{
				focus.setVisibility( View.GONE );
				page = 5;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay6_item5:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 5 )
			{
				focus.setVisibility( View.GONE );
				page = 5;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay7_item1:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 1 )
			{
				focus.setVisibility( View.GONE );
				page = 1;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay7_item6:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 1 )
			{
				focus.setVisibility( View.GONE );
				page = 1;
				scrollView.gotoPage( page );
			}
			break;

		case R.id.lay7_item3:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 1 )
			{
				focus.setVisibility( View.GONE );
				page = 1;
				scrollView.gotoPage( page );
			}
			break;
		case R.id.lay7_item10:
			if ( isPlay )
			{
				layout1.stopDvbLive();
			}
			isPlay = false;
			if ( page != 1 )
			{
				focus.setVisibility( View.GONE );
				page = 1;
				scrollView.gotoPage( page );
			}
			break;
		}
		( ( Layout1 ) layout1 ).setPlay( isPlay );
		showMenuSelected();
	}

	private void showFocus( View v, int duration )
	{
		Log.i( TAG, "showFocus" );
		if ( v != null )
		{
			int[] location = new int[ 2 ];
			v.getLocationInWindow( location );

			int x = location[0];
			int y = location[1];
			int width = 0;
			int height = 0;

			if ( x == 0 || y == 0 )
			{
				return;
			}

			if ( v == mMenu1 || v == mMenu2 || v == mMenu3 || v == mMenu5 || v == mMenu6
							|| v == mMenu4 )
			{
				x = location[0] - CommonUtil.dip2px( this, 20 );
				y = location[1] - CommonUtil.dip2px( this, 20 );
				width = v.getMeasuredWidth() + CommonUtil.dip2px( this, 40 );
				height = v.getMeasuredHeight() + CommonUtil.dip2px( this, 40 );
			}
			else
			{
				x = location[0] - CommonUtil.dip2px( this, 12 );
				y = location[1] - CommonUtil.dip2px( this, 15 );
				width = v.getMeasuredWidth() + CommonUtil.dip2px( this, 27 );
				height = v.getMeasuredHeight() + CommonUtil.dip2px( this, 31 );
			}

			if ( focus.getVisibility() == View.GONE )
			{
				oldX = x;
				oldY = y;
				oldW = width;
				oldH = height;
			}
			focus.setVisibility( View.VISIBLE );
			AnimatorSet bouncer = new AnimatorSet();
			ObjectAnimator animator1 = ObjectAnimator.ofFloat( focus, View.TRANSLATION_X, oldX, x );
			ObjectAnimator animator2 = ObjectAnimator.ofFloat( focus, View.TRANSLATION_Y, oldY, y );

			ObjectAnimator animator3 = ObjectAnimator.ofInt( focus, "width", oldW, width );
			ObjectAnimator animator4 = ObjectAnimator.ofInt( focus, "height", oldH, height );

			bouncer.setDuration( duration );

			bouncer.play( animator1 ).with( animator2 ).with( animator3 ).with( animator4 );
			bouncer.start();

			oldX = x;
			oldY = y;
			oldW = width;
			oldH = height;
		}
	}

	private void notifyUpdateWeather( Context context )
	{
		String curCity = FCCityUtil.getCity( context );

		if ( !TextUtils.isEmpty( curCity ) && !curCity.equals( cityText ) && weatherTast != null )
		{
			new Thread( new Runnable()
			{
				@SuppressWarnings( "static-access" )
				@Override
				public void run()
				{
					int count = 0;
					while ( weatherTaskRunning && count < 20 )
					{
						try
						{
							count++;
							Thread.currentThread().sleep( 500 );
						}
						catch ( InterruptedException e )
						{
							Log.e( TAG, "notifyUpdateWeather", e );
						}
					}
					weatherTast = new WeatherTask();
					weatherTast.execute( ( Void ) null );
				}
			} ).start();
		}
	}

	private OnClickListener clickListener = new OnClickListener()
	{
		@Override
		public void onClick( View v )
		{
			if ( v == btnMsg )
			{
				ApkHelper.goAppByAction( Launcher.this, "sys.settings.mail" );
			}
			if ( v == btnCalendar )
			{
				ApkHelper.goAppByAction( Launcher.this, "com.ejian.action.calendar" );
			}
			if ( v == btnCard )
			{
				ApkHelper.goAppByAction( Launcher.this, "sys.settings.net" );
			}
			if ( v == mMenu1 )
			{
				DVBPlayer.getInstance().stop();

				if ( manager.isTagInside( "100" ) )
				{

					FCActionTool.forward( Launcher.this, v.getTag().toString(), "99900188",
									Launcher.APP_KEY_ID );
					return;
				}

				ApkHelper.goAppByPkg( Launcher.this, "com.dvb.live" );
			}
			if ( v == mMenu2 )
			{
				if ( manager.isTagInside( "200" ) )
				{

					FCActionTool.forward( Launcher.this, v.getTag().toString(), "99900188",
									Launcher.APP_KEY_ID );
					return;
				}
				else if ( manager.isTagInside( "700" ) )
				{

					FCActionTool.forward( Launcher.this, v.getTag().toString(), "99900188",
									Launcher.APP_KEY_ID );
					return;
				}

				startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "tt_vod://start" ) ) );
			}
			if ( v == mMenu3 )
			{
				if ( manager.isTagInside( "300" ) )
				{

					FCActionTool.forward( Launcher.this, v.getTag().toString(), "99900188",
									Launcher.APP_KEY_ID );
					return;
				}

				String action = "open_app|by_uri|android.intent.action.VIEW|fc://com.smartcity/[category?cid=0&cname=&priority_mode=&tpl_type=&page=&news_list=&news_view=&icon=]";
				FCActionTool.forward( Launcher.this, action, "99900188", Launcher.APP_KEY_ID );

			}
			if ( v == mMenu4 )
			{

				if ( manager.isTagInside( "400" ) )
				{

					FCActionTool.forward( Launcher.this, v.getTag().toString(), "99900188",
									Launcher.APP_KEY_ID );
					return;
				}

				FCActionTool.forward( Launcher.this, "open_app|by_pkg|com.tt.emall|", "99900188",
								Launcher.APP_KEY_ID );
			}
			if ( v == mMenu5 )
			{
				if ( manager.isTagInside( "500" ) )
				{
					FCActionTool.forward( Launcher.this, v.getTag().toString(), "99900188",
									Launcher.APP_KEY_ID );
					return;
				}
				startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "apps://start" ) ) );
			}
			if ( v == mMenu6 )
			{
				if ( manager.isTagInside( "600" ) )
				{
					int s = FCActionTool.forward( Launcher.this, v.getTag().toString(), "99900188",
									Launcher.APP_KEY_ID );
					return;
				}
				// ApkHelper.goAppByPkg( Launcher.this, "com.fc.setting" );
			}
		}
	};

	private OnFocusChangeListener focusListener = new OnFocusChangeListener()
	{
		@Override
		public void onFocusChange( View v, boolean hasFocus )
		{
			Log.i( TAG, "focusListener--onFocusChange" );
			if ( hasFocus )
			{
				focusView = null;
				focusId = 0;
				Log.i( TAG, "tag ===" + v.getTag() );
				if ( v == btnMsg || v == btnCalendar || v == btnCard )
				{
					focus.setVisibility( View.GONE );
				}
				else
				{
					focus.setVisibility( View.GONE );
					if ( v == mMenu1 )
					{
						Log.i( TAG, "focusListener--onFocusChange--mMenu1" );
						page = 0;
						if ( !isPlay )
						{
							( ( Layout1 ) layout1 ).setPlay( true );
							layout1.playDvbLive();

						}
						isPlay = true;
					}
					else
					{
						( ( Layout1 ) layout1 ).setPlay( false );
					}
					if ( v == mMenu2 )
					{
						Log.i( TAG, "focusListener--onFocusChange--mMenu2" );
						page = 1;
						if ( isPlay )
						{
							// 延迟1000毫秒为了解决快速切换导致直播停止不掉
							handler.postDelayed( new Runnable()
							{
								@Override
								public void run()
								{
									layout1.stopDvbLive();
									( ( Layout1 ) layout1 ).setPlay( false );
								}
							}, 200 );// 500

						}

						isPlay = false;

					}
					if ( v == mMenu3 )
					{
						page = 2;
						if ( isPlay )
						{
							layout1.stopDvbLive();
							( ( Layout1 ) layout1 ).setPlay( false );
						}
						isPlay = false;
					}
					if ( v == mMenu4 )
					{
						page = 3;
						if ( isPlay )
						{
							layout1.stopDvbLive();
							( ( Layout1 ) layout1 ).setPlay( false );
						}
						isPlay = false;
					}
					if ( v == mMenu5 )
					{
						page = 4;
						if ( isPlay )
						{
							layout1.stopDvbLive();
							( ( Layout1 ) layout1 ).setPlay( false );
						}
						isPlay = false;
					}
					if ( v == mMenu6 )
					{
						page = 5;
						if ( isPlay )
						{
							layout1.stopDvbLive();
							( ( Layout1 ) layout1 ).setPlay( false );
						}
						isPlay = false;
					}
					if ( oldPage != page )
					{
						handler.removeMessages( 7 );
						Message msg = handler.obtainMessage( 7, 0, 0, page );
						handler.sendMessageDelayed( msg, 1000 );
					}
					// aaa
					showMenuSelected();
					Log.i( "ttt", "focu this" );
					scrollView.gotoPage( page );
					oldPage = page;
					Log.i( TAG, "focusListener--Last" );
				}
			}
		}
	};

	private class WeatherTask extends AsyncTask< Void, Void, String >
	{
		@SuppressWarnings( "deprecation" )
		@Override
		protected String doInBackground( Void... params )
		{
			String weatherurl = UriUtil.createUrl( "api/helper/getCalendar" ) + "?skin=white";
			Log.i( TAG, "weatherurl==" + weatherurl );
			String tmp_city = FCCityUtil.getCity( Launcher.this );
			if ( !TextUtils.isEmpty( tmp_city ) )
			{
				weatherurl += ( "&city=" + URLEncoder.encode( tmp_city ) );
			}
			String result = FCHttp.httpForGetMethod( weatherurl );
			return result;
		}

		@Override
		protected void onPostExecute( String result )
		{

			Log.i( TAG, "result ==" + result );
			if ( result != null && !"".equals( result ) )
			{
				try
				{
					JSONObject object = new JSONObject( result );

					if ( object.length() <= 0 )
					{
						return;
					}

					if ( object.getInt( "ret" ) == 0 )
					{
						JSONObject wObject = object.getJSONObject( "weather" )
										.getJSONObject( "current" );
						if ( wObject != null )
						{
							cityText = object.getString( "city" ) == null ? ""
											: object.getString( "city" );
							mCity.setText( cityText );
							String temperature = wObject.getString( "temperature" ) == null ? ""
											: wObject.getString( "temperature" );
							mTemp.setText( temperature );
							mWeather.setVisibility( View.VISIBLE );
							String weather = wObject.getString( "condition" ) == null ? ""
											: wObject.getString( "condition" );
							Log.i( TAG, "weather==" + weather );
							mWeather.setText( weather );
						}
					}
				}
				catch ( Exception e )
				{
					Log.e( TAG, "WeatherTast.onPostExecute", e );
				}
			}
			super.onPostExecute( result );
			weatherTaskRunning = false;
		}

		@Override
		protected void onPreExecute()
		{
			weatherTaskRunning = true;
			super.onPreExecute();
		}
	}

	private void loadImage( final String url, final ImageView image )
	{
		Drawable cacheImage = imageLoader.loadDrawable( url, new AsyncImageLoader.ImageCallback()
		{
			public void imageLoaded( Drawable imageDrawable )
			{
				image.setImageDrawable( imageDrawable );
			}
		} );
		if ( cacheImage != null )
		{
			image.setImageDrawable( cacheImage );
		}
	}

	public static String getDecodedSN()
	{
		try
		{
			VPDJni jni = ( VPDJni ) Class.forName( "android.os.VPDJni" ).newInstance();
			byte[] b = jni.readDecodedSN();
			if ( null == b )
			{
				return "";
			}
			return new String( b ).trim();
		}
		catch ( Exception e )
		{
			Log.e( TAG, "getDecodedSN exception", e );
			return "";
		}
	}

	private MovieInfo Json2Info( JSONObject object )
	{
		MovieInfo info = null;
		try
		{
			if ( object != null && object.length() > 0 )
			{
				info = new MovieInfo();
				info.setTag( object.getString( "tag" ) == null ? "" : object.getString( "tag" ) );
				info.setTitle( object.getString( "title" ) == null ? ""
								: object.getString( "title" ) );
				info.setAction( object.getString( "action" ) == null ? ""
								: object.getString( "action" ) );

				info.setApp_package_name( object.getString( "app_package_name" ) == null ? ""
								: object.getString( "app_package_name" ) );
				info.setApp_id( object.getString( "app_id" ) == null ? ""
								: object.getString( "app_id" ) );

				if ( object.has( "img_v" ) )
				{
					info.setImg_v( object.getString( "img_v" ) == null ? ""
									: object.getString( "img_v" ) );
				}
				else
				{
					info.setImg_v( "" );
				}
				if ( object.has( "img_h" ) )
				{
					info.setImg_h( object.getString( "img_h" ) == null ? ""
									: object.getString( "img_h" ) );
				}
				else
				{
					info.setImg_h( "" );
				}

				if ( object.has( "img_s" ) )
				{
					info.setImg_s( object.getString( "img_s" ) == null ? ""
									: object.getString( "img_s" ) );
				}
				else
				{
					info.setImg_s( "" );
				}
				if ( object.has( "img_h2" ) )
				{
					info.setImg_h2( object.getString( "img_h2" ) == null ? ""
									: object.getString( "img_h2" ) );
				}
				else
				{
					info.setImg_h2( "" );
				}
				if ( object.has( "img_h3" ) )
				{
					info.setImg_h3( object.getString( "img_h3" ) == null ? ""
									: object.getString( "img_h3" ) );
				}
				else
				{
					info.setImg_h3( "" );
				}
				if ( object.has( "img_full" ) )
				{
					info.setImg_full( object.getString( "img_full" ) == null ? ""
									: object.getString( "img_full" ) );
				}
				else
				{
					info.setImg_full( "" );
				}
			}
		}
		catch ( JSONException e )
		{
			Log.e( TAG, "Json2Info", e );
		}
		return info;
	}

	DataTask dataTask;

	private class DataTask extends AsyncTask< Void, Void, String >
	{
		@Override
		protected String doInBackground( Void... params )
		{
			String url = UriUtil.createUrl( "api/system/getLauncherTag" ) + "?version=" + version
							+ "&sn=" + deviceSN + "&deviceid=" + deviceID + "&appkeyid="
							+ APP_KEY_ID + "&api_data_ver="
							+ updatePre.getString( "api_data_ver", "" ) + "&mac="
							+ FCTvDevice.getEthMacAddress() + "&ca=" + CommonDeviceUtil.getCa();
			Log.i( TAG, "DataTask--doInBackground--url==" + url );
			return FCHttp.httpForGetMethod( url );
		}

		@Override
		protected void onPostExecute( String result )
		{
			Log.i( TAG, "DataTask--onPostExecute--result==" + result );

			if ( result != null && !"".equals( result ) )
			{
				try
				{
					JSONObject object = new JSONObject( new String( result ) );
					if ( object != null && object.length() > 0 && object.getInt( "ret" ) == 0 )
					{
						VPDJni VPD = new VPDJni();
						// 已经激活
						if ( !( VPD.isActivated() == 0 ) )
						{
							return;
						}
						Log.i( TAG, "DataTask update" );
						Editor editor = updatePre.edit();
						editor.putString( "api_data_ver", object.getString( "api_data_ver" ) );
						editor.commit();

						JSONArray array = object.getJSONArray( "data" );
						if ( array != null )
						{
							manager.clearData();
							for ( int i = 0; i < array.length(); i++ )
							{
								JSONObject o = array.getJSONObject( i );
								MovieInfo info = Json2Info( o );
								if ( info != null )
								{
									if ( manager.isTagInside( info.getTag() ) )
									{
										manager.updateInfo( info );
									}
									else
									{
										manager.insertInfo( info );
									}
								}
							}
						}
						handler.sendEmptyMessage( 3 );
						layout2.setManager( manager );
						// }
					}
				}
				catch ( JSONException e )
				{
					Log.e( TAG, "DataTask", e );
				}
			}

			super.onPostExecute( result );
		}
	}

	Runnable dateRun = new Runnable()
	{
		@Override
		public void run()
		{
			Log.i( "dateRun", "dateRun" );
			Calendar c = Calendar.getInstance();
			int year = c.get( Calendar.YEAR );
			int month = c.get( Calendar.MONTH );
			int day = c.get( Calendar.DAY_OF_MONTH );
			int week = c.get( Calendar.DAY_OF_WEEK );
			int hour = c.get( Calendar.HOUR_OF_DAY );
			int minute = c.get( Calendar.MINUTE );
			if ( year >= 2016 )
			{
				String sTime = "";
				if ( minute < 10 )
				{
					sTime = hour + ":0" + minute;
				}
				else
				{
					sTime = hour + ":" + minute;
				}
				String sMonth = "";
				if ( month + 1 < 10 )
				{
					sMonth = "0" + ( month + 1 );
				}
				else
				{
					sMonth = "" + ( month + 1 );
				}
				String sDay = "";
				if ( day < 10 )
				{
					sDay = "0" + day;
				}
				else
				{
					sDay = "" + day;
				}

				String sWeek = "";
				switch( week )
				{
				case 1:
					sWeek = getString( R.string.sunday );
					break;
				case 2:
					sWeek = getString( R.string.monday );
					break;
				case 3:
					sWeek = getString( R.string.tuesday );
					break;
				case 4:
					sWeek = getString( R.string.wednesday );
					break;
				case 5:
					sWeek = getString( R.string.thursday );
					break;
				case 6:
					sWeek = getString( R.string.friday );
					break;
				case 7:
					sWeek = getString( R.string.saturday );
					break;
				}

				mDate.setText( sMonth + "/" + sDay );
				mTime.setText( sTime );
				mWeek.setText( sWeek );
				Log.i( "dateRun", "mDate:" + ( sMonth + "/" + sDay ) );
				Log.i( "dateRun", "mTime:" + sTime );
				Log.i( "dateRun", "mWeek:" + sWeek );
			}
			handler.postDelayed( dateRun, 10000 );
		}
	};

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage( Message msg )
		{
			Log.i( TAG, "handleMessage" );
			switch( msg.what )
			{
			case 1:
				switch( focusId )
				{
				case R.id.lay1_item1:
				case R.id.lay1_item4:
					Log.i( "ttt", "lay1_item4" );
					scrollView.gotoPage( page );
					break;
				}
				break;
			case 2:
				weatherTast = new WeatherTask();
				weatherTast.execute( ( Void ) null );

				dataTask = new DataTask();
				dataTask.execute( ( Void ) null );

				// 保证有网的时候进行图片刷新
				layout2.setManager( manager );
				refreshImageData();
				refreshMenuData();
				handler.removeCallbacks( dateRun );
				handler.post( dateRun );
				getKeyValue();
				Log.d( TAG, "handler (2)" );
				handler.removeMessages( MSG_DATA_TIMING );
				handler.sendEmptyMessageDelayed( MSG_DATA_TIMING, 10 * 1000 );
				break;
			case 3:
				refreshImageData();
				refreshMenuData();
				MovieInfo backgroudInfo = manager.getInfo( "000" );
				if ( backgroudInfo != null )
				{
					CommonImageUtil.saveLauncherBgImageToFile( backgroudInfo.getImg_full(), handler,
									true );
				}
				break;
			case 4:
				btnCard.setImageResource( R.drawable.icon_net );
				break;
			case 5:
				int strength = Integer.parseInt( msg.obj.toString() );
				btnCard.setImageResource( R.drawable.wifi_state );
				btnCard.setImageLevel( strength );
				break;
			case 6:
				btnCard.setImageResource( R.drawable.icon_net_off );
				break;
			case 7:
				int mPage = Integer.parseInt( msg.obj.toString() );
				switch( mPage )
				{
				case 0:
					layout1.sendMsg();
					break;
				case 1:
					layout2.sendMsg();
					break;
				case 2:
					layout3.sendMsg();
					break;
				case 3:
					layout4.sendMsg();
					break;
				case 4:
					layout5.sendMsg();
					break;
				case 5:
					layout6.sendMsg();
					break;
				}
				break;
			case 8:

				ADPICBase base = ( ADPICBase ) msg.obj;
				Log.i( TAG, "---base:" + base );
				if ( base != null )
				{
					Log.i( TAG, "---base.picPath:" + base.picPath );
					if ( base.picFormat.equals( ADPICBase.PICFORMAT.GIF ) )
					{
						Log.i( TAG, "base.picFormat.equals( ADPICBase.PICFORMAT.GIF )" );

					}
					Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource( base.picPath );
					iv_logo.setVisibility( View.VISIBLE );
					ll_main.setVisibility( View.GONE );
					iv_logo.setImageBitmap( bitmap );
					handler.postDelayed( toLiveR, 0 * 1000 );

				}
				else
				{
					handler.postDelayed( toLiveR, 0 * 1000 );
				}
				break;
			case MAIL_MSG:
			{
				setMailThing();
				handler.removeMessages( MAIL_MSG );
				handler.sendEmptyMessageDelayed( MAIL_MSG, 60 * 1000 );
				break;
			}
			case MSG_DATA_TIMING:

				Log.i( TAG, "handler MSG_DATA_TIMING" );
				if ( dataTask != null )
				{
					dataTask = null;
					dataTask = new DataTask();
					dataTask.execute( ( Void ) null );
				}
				getAdSwticher( Launcher.this );
				int time = 600;
				try
				{
					String refreshTime = FCConfigParamsUtil.getPlatformOnlineConfParams(
									Launcher.this, "get_refresh_time", "600" );
					time = Integer.parseInt( refreshTime );
				}
				catch ( Exception e )
				{

				}
				Log.i( TAG, "handler--time==" + time );
				if ( time != 0 )
				{
					handler.removeMessages( MSG_DATA_TIMING );
					handler.sendEmptyMessageDelayed( MSG_DATA_TIMING, time * 1000 );
				}
				break;
			case 11:
				refreshBackGround();
				break;
			case NOTIFY_MSG:
				Log.i( TAG, "NOTIFY_MSG" );
				Bundle bundle = msg.getData();
				int msgID = bundle.getInt( "msgID" );
				int arg1 = bundle.getInt( "arg1" );
				int arg2 = bundle.getInt( "arg2" );
				Object arg3 = msg.obj;

				if ( null != curChannel && !curChannel.isLocked()
								&& DVBEnum.MessageType.EVENT_HW_TUNER_LOCK.value() == msgID )
				{
					// 免费节目
					Log.i( TAG, "NOTIFY_MSG--layout1.hideError()" );
					layout1.hideError();
				}
				else
				{
					CATYPE caType = DataHolder.from( Launcher.this ).getCAType();
					if ( caType == CATYPE.CONAX )
					{
						procConaxMsg( msgID, arg1, arg2, arg3 );
					}
					else if ( caType == CATYPE.TR )
					{
						procTRMsg( msgID, arg1, arg2, arg3 );
					}
					else if ( caType == CATYPE.CD )
					{
						procCDMsg( msgID, arg1, arg2, arg3 );
					}
					else if ( caType == CATYPE.IRD )
					{
						procIRDMsg( msgID, arg1, arg2, arg3 );
					}
					else if ( caType == CATYPE.BY )
					{
						procBYMsg( msgID, arg1, arg2, arg3 );
					}
					else if ( caType == CATYPE.MG )
					{
						procMGMsg( msgID, arg1, arg2, arg3 );
					}
					else if ( caType == CATYPE.SM )
					{
						procSMMsg( msgID, arg1, arg2, arg3 );
					}
					else if ( caType == CATYPE.LX )
					{
						procLXMsg( msgID, arg1, arg2, arg3 );
					}
					else if ( caType == CATYPE.DG )
					{
						procDGMsg( msgID, arg1, arg2, arg3 );
					}
					else if ( caType == CATYPE.UTI )
					{
						procUTIMsg( msgID, arg1, arg2, arg3 );
					}
					else
					{
						procNoCAMsg( msgID, arg1, arg2, arg3 );
					}

				}
				break;
			}
		}

	};

	private void setMailThing()
	{
		int count = CaMailProxy.from( this ).getUnreadMailCount();
		if ( count == 0 )
		{
			mailCountTxt.setVisibility( View.INVISIBLE );
		}
		else
		{
			mailCountTxt.setVisibility( View.VISIBLE );
			mailCountTxt.setText( "" + count );
		}
	}

	private static int count = 0, preMsgID;

	/**
	 * 注册DVB消息监听
	 */
	public void registerDvbMsgListen()
	{
		Log.i( TAG, "registerDvbMsgListen" );

		FCDvb.getInstance( getApplicationContext() ).getCaOptCtrl().setLinsten( dvbMsgListener );
	}

	/**
	 * 解注册DVB消息监听
	 */
	public void unRegisterDvbMsgListen()
	{
		Log.i( TAG, "unRegisterDvbMsgListen" );
		FCDvb.getInstance( getApplicationContext() ).getCaOptCtrl().removeListen( dvbMsgListener );
	}

	private void procNoCAMsg( int msgID, int arg1, int arg2, Object arg3 )
	{
		if ( DVBEnum.MessageType.EVENT_HW_TUNER_UNLOCK.value() == msgID )
		{
			count++;

			if ( count == 2 )
			{
				layout1.showError( "信号异常，请检查信号", null, 0 );
				count = 0;
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_LOCK.value() == msgID )
		{
			layout1.hideError();
		}
	}

	private void procMGMsg( int msgID, int arg1, int arg2, Object arg3 )
	{
		String tmp1 = String.format( "msgId: 0x%x  arg1: %d  arg2: %d ", msgID, arg1, arg2 );
		Log.d( TAG, tmp1 );

		if ( DVBEnum.MessageType.EVENT_HW_TUNER_UNLOCK.value() == msgID )
		{
			count++;

			if ( count == 2 )
			{
				layout1.showError( "信号异常，请检查信号", null, 0 );
				count = 0;
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_LOCK.value() == msgID )
		{
			layout1.hideError();
		}

		if ( DVBEnum.MessageType.EVENT_CA_SCEVENT.value() == msgID )
		{
			Log.e( TAG, "MessageType.EVENT_CA_SCEVENT arg1:" + arg1 );
			if ( arg1 == DVBEnum.MGMSGCODE.FC_ERR_CARD_NOTFOUND.value() )
			{
				layout1.showError( "请插入智能卡", null, 0 );
			}
			else
			{
				String tmp = String.format( "msgId: 0x%x  arg1: %d  arg2: %d ", msgID, arg1, arg2 );
				Log.d( TAG, tmp );
			}

			preMsgID = arg1;
		}
	}

	private void procBYMsg( int msgID, int arg1, int arg2, Object arg3 )
	{
		String tmp1 = String.format( "msgId: 0x%x  arg1: %d  arg2: %d ", msgID, arg1, arg2 );
		Log.d( TAG, tmp1 );

		if ( DVBEnum.MessageType.EVENT_SC_SCEVENT.value() == msgID )
		{
			FCProgData curChannel = DataHolder.from( Launcher.this ).getCurChannel();
			// 加密频道才显示插卡信息
			if ( curChannel != null && curChannel.isLocked() )
			{
				if ( DVBEnum.SCEvent.EN_CA_CARD_INSERT.value() == arg1 )
				{
					layout1.hideError();
				}
				else if ( DVBEnum.SCEvent.EN_CA_NO_CARD_INSERT.value() == arg1 )
				{
					layout1.showError( "请插入智能卡", null, 0 );
				}
			}
			else
			{
				layout1.hideError();
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_UNLOCK.value() == msgID )
		{
			count++;

			if ( count == 2 )
			{
				layout1.showError( "信号异常，请检查信号", null, 0 );
				count = 0;
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_LOCK.value() == msgID )
		{
			layout1.hideError();
		}

		if ( DVBEnum.MessageType.EVENT_CA_SCEVENT.value() == msgID )
		{
			Log.e( TAG, "MessageType.EVENT_CA_SCEVENT arg1:" + arg1 );

			if ( arg1 == DVBEnum.BYMSGCODE.BYCA_ERROR_NO_CARD.value() )
			{
				FCProgData curChannel = DataHolder.from( Launcher.this ).getCurChannel();
				// 加密频道才显示插卡信息
				if ( curChannel != null && curChannel.isLocked() )
				{
					layout1.showError( "请插入智能卡", null, 0 );
				}
			}
			else if ( arg1 == DVBEnum.BYMSGCODE.BYCA_NO_ERROR.value() )
			{
				// hideError();
			}

			preMsgID = arg1;
		}
	}

	private void procTRMsg( int msgID, int arg1, int arg2, Object arg3 )
	{
		if ( DVBEnum.MessageType.EVENT_SC_SCEVENT.value() == msgID )
		{
			FCProgData curChannel = DataHolder.from( Launcher.this ).getCurChannel();
			// 加密频道才显示插卡信息
			if ( curChannel != null && curChannel.isLocked() )
			{
				if ( DVBEnum.SCEvent.EN_CA_CARD_INSERT.value() == arg1 )
				{
					layout1.hideError();
				}
				else if ( DVBEnum.SCEvent.EN_CA_NO_CARD_INSERT.value() == arg1 )
				{
					layout1.showError( "E04  请插入智能卡", null, 0 );
				}
			}
			else
			{
				layout1.hideError();
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_UNLOCK.value() == msgID )
		{
			count++;

			if ( count == 2 )
			{
				layout1.showError( "E35  信号异常，请检查信号", null, 0 );
				count = 0;
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_LOCK.value() == msgID )
		{
			layout1.hideError();
		}

		// HN版本CA提示
		if ( DVBEnum.MessageType.EVENT_CA_SCEVENT.value() == msgID )
		{
			Log.e( TAG, "MessageType.EVENT_CA_SCEVENT arg1:" + arg1 );

			// begin - add by apple
			String str = String.format( "%s%02d ", "E", arg1 );
			if ( arg1 == DVBEnum.CAMsgCode.CAS_STATE_E00.value() )
			{
				layout1.hideError();
			}
			else if ( arg1 == DVBEnum.CAMsgCode.CAS_STATE_E01.value() )
			{
				layout1.showError( str + "请插入CA模块", null, 0 );
			}
			else if ( arg1 == DVBEnum.CAMsgCode.CAS_STATE_E04.value() )
			{
				layout1.showError( str + "请插入智能卡", null, 0 );
			}
			else if ( arg1 == DVBEnum.CAMsgCode.CAS_STATE_E23.value() )
			{
				layout1.hideError();
			}
			// end - add by apple
		}
	}

	private void procConaxMsg( int msgID, int arg1, int arg2, Object arg3 )
	{
		// conax
		if ( DVBEnum.MessageType.EVENT_SC_SCEVENT.value() == msgID )
		{
			FCProgData curChannel = DataHolder.from( Launcher.this ).getCurChannel();
			// 加密频道才显示插卡信息
			if ( curChannel != null && curChannel.isLocked() )
			{
				if ( DVBEnum.SCEvent.EN_CA_CARD_INSERT.value() == arg1 )
				{
					layout1.hideError();
				}
				else if ( DVBEnum.SCEvent.EN_CA_NO_CARD_INSERT.value() == arg1 )
				{
					layout1.showError( "请插入智能卡", "104", 0 );
				}
			}
			else
			{
				layout1.hideError();
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_UNLOCK.value() == msgID )
		{
			count++;

			if ( count == 2 )
			{
				layout1.showError( "信号中断，请检查室内线路", "101", 0 );
				count = 0;
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_LOCK.value() == msgID )
		{
			layout1.hideError();
		}
		else if ( DVBEnum.MessageType.EVENT_CA_PLAYEVENT.value() == msgID )
		{
			if ( DVBEnum.CAPLAYEvent.ACCESS_STATUS_NOT_FOR_NOACCESS.value() == arg1 )
			{
				layout1.hideError();
			}
			else if ( DVBEnum.CAPLAYEvent.GEOGRAPHICAL_BLACKOUT.value() == arg1 )
			{
				FCProgData curChannel = DataHolder.from( Launcher.this ).getCurChannel();
				// 非加密频道显示无信号
				long snr = FCDvb.getInstance( this ).getPlayerCtrl().getSignalSNR();
				Log.i( TAG, "++++  snr : " + snr + "  ++++" );
				if ( snr <= 28 || ( curChannel != null && !curChannel.isLocked() ) )
				{
					// 无信号导致的
					layout1.showError( "信号中断，请检查线路", "001", 0 );
				}
			}
		}
	}

	/**
	 * 永新视博CA
	 * 
	 * @param msgID
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	private void procCDMsg( int msgID, int arg1, int arg2, Object arg3 )
	{
		Log.d( TAG, "procCDMsg" );
		if ( DVBEnum.MessageType.EVENT_SC_SCEVENT.value() == msgID )
		{
			FCProgData curChannel = DataHolder.from( Launcher.this ).getCurChannel();
			// 加密频道才显示插卡信息
			if ( curChannel != null && curChannel.isLocked() )
			{
				if ( DVBEnum.SCEvent.EN_CA_CARD_INSERT.value() == arg1 )
				{
					layout1.hideError();
				}
				else if ( DVBEnum.SCEvent.EN_CA_NO_CARD_INSERT.value() == arg1 )
				{
					layout1.showError( "请插入智能卡", null, 0 );
				}
			}
			else
			{
				layout1.hideError();
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_UNLOCK.value() == msgID )
		{
			count++;
			if ( count == 2 )
			{
				layout1.showError( "信号异常，请检查信号", null, 0 );
				count = 0;
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_LOCK.value() == msgID )
		{
			layout1.hideError();
		}
		else if ( DVBEnum.MessageType.EVENT_CA_SCEVENT.value() == msgID )
		{
			Log.i( TAG, "procCDMsg msgID : " + msgID );
			Log.i( TAG, "procCDMsg arg1 : " + arg1 );
			Log.i( TAG, "procCDMsg arg2 : " + arg2 );

			if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_CANCEL_TYPE.value() )
			{
				layout1.hideError();
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_BADCARD_TYPE.value() )
			{
				layout1.showError( "无法识别卡", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_EXPICARD_TYPE.value() )
			{
				layout1.showError( "智能卡过期，请更换新卡", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_INSERTCARD_TYPE.value() )
			{
				layout1.showError( "加扰节目，请插入智能卡", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_NOOPER_TYPE.value() )
			{
				layout1.showError( "不支持节目运营商", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_BLACKOUT_TYPE.value() )
			{
				layout1.showError( "条件禁播", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_OUTWORKTIME_TYPE.value() )
			{
				layout1.showError( "当前时段被设定为不能观看", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_WATCHLEVEL_TYPE.value() )
			{
				layout1.showError( "节目级别高于设定的观看级别", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_PAIRING_TYPE.value() )
			{
				layout1.showError( "智能卡与本机顶盒不对应", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_NOENTITLE_TYPE.value() )
			{
				layout1.showError( "没有授权", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_DECRYPTFAIL_TYPE.value() )
			{
				layout1.showError( "节目解密失败", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_NOMONEY_TYPE.value() )
			{
				layout1.showError( "卡内金额不足", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_ERRREGION_TYPE.value() )
			{
				layout1.showError( "区域不正确", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_NEEDFEED_TYPE.value() )
			{
				layout1.showError( "子卡需要和母卡对应，请插入母卡", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_ERRCARD_TYPE.value() )
			{
				layout1.showError( "智能卡校验失败，请联系运营商", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_UPDATE_TYPE.value() )
			{
				layout1.showError( "智能卡升级中，请不要拔卡或者关机", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_LOWCARDVER_TYPE.value() )
			{
				layout1.showError( "请升级智能卡", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_VIEWLOCK_TYPE.value() )
			{
				layout1.showError( "请勿频繁切换频道", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_MAXRESTART_TYPE.value() )
			{
				layout1.showError( "智能卡暂时休眠，请5分钟后重新插卡", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_FREEZE_TYPE.value() )
			{
				layout1.showError( "账户被锁定，请联系运营商", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_CALLBACK_TYPE.value() )
			{
				layout1.showError( "智能卡已暂停，请回传收视记录给运营商", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_CURTAIN_TYPE.value() )
			{
				layout1.showError( "高级预览节目，该阶段不能免费观看", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_CARDTESTSTART_TYPE.value() )
			{
				layout1.showError( "升级测试卡测试中...", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_CARDTESTFAILD_TYPE.value() )
			{
				layout1.showError( "升级测试卡测试失败，请检查机卡通讯模块", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_CARDTESTSUCC_TYPE.value() )
			{
				layout1.showError( "升级测试卡测试成功", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_NOCALIBOPER_TYPE.value() )
			{
				layout1.showError( "卡中不存在节目运营商", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_STBLOCKED_TYPE.value() )
			{
				layout1.showError( "请重启机顶盒", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_STBFREEZE_TYPE.value() )
			{
				layout1.showError( "机顶盒被冻结", null, 0 );
			}
			else if ( arg2 == DVBEnum.CDMSGCODE.CDCA_MESSAGE_UNSUPPORTDEVICE_TYPE.value() )
			{
				layout1.showError( "不支持的终端类型", null, 0 );
			}
		}
	}

	/**
	 * 艾迪德 CA
	 * 
	 * @param msgID
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	private void procIRDMsg( int msgID, int arg1, int arg2, Object arg3 )
	{
		Log.i( TAG, "procIRDMsg" );
		if ( DVBEnum.MessageType.EVENT_SC_SCEVENT.value() == msgID )
		{
			FCProgData curChannel = DataHolder.from( Launcher.this ).getCurChannel();
			// 加密频道才显示插卡信息
			if ( curChannel != null && curChannel.isLocked() )
			{
				if ( DVBEnum.SCEvent.EN_CA_CARD_INSERT.value() == arg1 )
				{
					layout1.hideError();
				}
				else if ( DVBEnum.SCEvent.EN_CA_NO_CARD_INSERT.value() == arg1 )
				{
					layout1.showError( "请插入智能卡", "E04-4", 0 );
				}
			}
			else
			{
				layout1.hideError();
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_UNLOCK.value() == msgID )
		{
			count++;

			if ( count == 2 )
			{
				layout1.showError( "信号异常，请检查信号", null, 0 );
				count = 0;
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_LOCK.value() == msgID )
		{
			layout1.hideError();
		}
		else if ( DVBEnum.MessageType.EVENT_CA_SCEVENT.value() == msgID )
		{
			if ( arg3 == null || !( arg3 instanceof CAMessage ) )
			{
				Log.i( TAG, "arg3 error" );
				return;
			}

			CAMessage msg = ( CAMessage ) ( arg3 );
			String errorLevel = "";
			switch( arg2 )
			{
			case 1:
				errorLevel = "F";
				break;
			case 2:
				errorLevel = "E";
				break;
			case 3:
				errorLevel = "W";
				break;
			case 4:
				errorLevel = "I";
				break;
			case 5:
				errorLevel = "D";
				break;
			default:
				break;
			}

			String errorCode = String.format( "%02d", msg.param1 );

			StringBuilder errorCodeBuilder = new StringBuilder();
			errorCodeBuilder.append( errorLevel );
			errorCodeBuilder.append( errorCode );
			errorCodeBuilder.append( "-" );
			errorCodeBuilder.append( msg.param2 );
			String errorCodeString = errorCodeBuilder.toString();

			Log.i( TAG, "errorCodeBuilder : " + errorCodeBuilder );

			if ( errorCodeString.contains( "D100" ) )
			{
				layout1.hideError();
			}
			else if ( errorCodeString.contains( "D00-4" ) )
			{
				layout1.hideError();
			}
			else if ( errorCodeString.contains( "D29-4" ) )
			{
				layout1.hideError();
			}
			else if ( "E100-2".equals( errorCodeString ) )
			{
				layout1.showError( "错误的操作码或序列", errorCodeString, 0 );
			}
			else if ( "F101-2".equals( errorCodeString ) || "E101-3".equals( errorCodeString )
							|| "E101-5".equals( errorCodeString )
							|| "E101-8".equals( errorCodeString )
							|| "E101-27".equals( errorCodeString )
							|| "E101-28".equals( errorCodeString )
							|| "E101-36".equals( errorCodeString ) )
			{
				layout1.showError( "未完成定义", errorCodeString, 0 );
			}
			else if ( "F102-2".equals( errorCodeString ) )
			{
				layout1.showError( "格式错误", errorCodeString, 0 );
			}
			else if ( "E04-4".equals( errorCodeString ) )
			{
				layout1.showError( "请插入智能卡", errorCodeString, 0 );
			}
			else if ( "E05-4".equals( errorCodeString ) )
			{
				layout1.showError( "无法识别智能卡", errorCodeString, 0 );
			}
			else if ( "E06-4".equals( errorCodeString ) )
			{
				layout1.showError( "卡通信异常", errorCodeString, 0 );
			}
			else if ( "E16-4".equals( errorCodeString ) )
			{
				layout1.showError( "禁止播放", errorCodeString, 0 );
			}
			else if ( "E18-4".equals( errorCodeString ) )
			{
				layout1.showError( "节目无效", errorCodeString, 0 );
			}
			else if ( "E19-4".equals( errorCodeString ) )
			{
				layout1.showError( "节目授权已过期", errorCodeString, 0 );
			}
			else if ( "E20-4".equals( errorCodeString ) )
			{
				layout1.showError( "节目无效", errorCodeString, 0 );
			}
			else if ( "E21-4".equals( errorCodeString ) )
			{
				layout1.showError( "目前无法查看该通道", errorCodeString, 0 );
			}
			else if ( "E30-4".equals( errorCodeString ) )
			{
				layout1.showError( "智能卡身份验证失败", errorCodeString, 0 );
			}
			else if ( "E32-4".equals( errorCodeString ) )
			{
				layout1.showError( "查看暂时受阻，敬请关注", errorCodeString, 0 );
			}
			else if ( "E33-4".equals( errorCodeString ) )
			{
				layout1.showError( "检查是否正确插入智能卡", errorCodeString, 0 );
			}
			else if ( "E100-4".equals( errorCodeString ) )
			{
				layout1.showError( "目前无法查看该通道", errorCodeString, 0 );
			}
			else if ( "E101-4".equals( errorCodeString ) )
			{
				layout1.showError( "目前无法查看该通道", errorCodeString, 0 );
			}
			else if ( "E107-4".equals( errorCodeString ) )
			{
				layout1.showError( "智能卡未完全授权", errorCodeString, 0 );
			}
			else if ( "E108-4".equals( errorCodeString ) )
			{
				layout1.showError( "目前无法查看该通道", errorCodeString, 0 );
			}
			else if ( "E109-4".equals( errorCodeString ) )
			{
				layout1.showError( "目前无法查看该通道", errorCodeString, 0 );
			}
			else if ( "E116-4".equals( errorCodeString ) )
			{
				layout1.showError( "目前无法查看该通道", errorCodeString, 0 );
			}
			else if ( "E117-4".equals( errorCodeString ) )
			{
				layout1.showError( "智能卡未完全授权", errorCodeString, 0 );
			}
			else if ( "E118-4".equals( errorCodeString ) )
			{
				layout1.showError( "目前无法查看该通道", errorCodeString, 0 );
			}
			else if ( "E120-4".equals( errorCodeString ) )
			{
				layout1.showError( "卡正在同步，请稍后", errorCodeString, 0 );
			}
			else if ( "E124-4".equals( errorCodeString ) )
			{
				layout1.showError( "目前无法查看该通道", errorCodeString, 0 );
			}
			else if ( "E127-4".equals( errorCodeString ) )
			{
				layout1.showError( "禁止播放", errorCodeString, 0 );
			}
			else if ( "E128-4".equals( errorCodeString ) )
			{
				layout1.showError( "无PVR订阅", errorCodeString, 0 );
			}
			else if ( "E130-4".equals( errorCodeString ) )
			{
				layout1.showError( "不支持PVR", errorCodeString, 0 );
			}
			else if ( "E131-4".equals( errorCodeString ) )
			{
				layout1.showError( "无PVR MSK", errorCodeString, 0 );
			}
			else if ( "E106-9".equals( errorCodeString ) )
			{
				layout1.showError( "请插入正确的智能卡", errorCodeString, 0 );
			}
			else if ( "E101-11".equals( errorCodeString ) )
			{
				layout1.showError( "不支持此服务", errorCodeString, 0 );
			}
			else if ( "E24-12".equals( errorCodeString ) )
			{
				layout1.showError( "已过预览期", errorCodeString, 0 );
			}
			else if ( "E93-48".equals( errorCodeString ) )
			{
				layout1.showError( "错误的PPV模式", errorCodeString, 0 );
			}
			else if ( "E129-65".equals( errorCodeString ) )
			{
				layout1.showError( "内容已过期", errorCodeString, 0 );
			}
			else if ( "E129-65".equals( errorCodeString ) )
			{
				layout1.showError( "内容已过期", errorCodeString, 0 );
			}
			else if ( "E37-32".equals( errorCodeString ) )
			{
				layout1.showError( "未知服务", errorCodeString, 0 );
			}
			else if ( "E38-32".equals( errorCodeString ) )
			{
				layout1.showError( "服务未运行", errorCodeString, 0 );
			}
			else if ( "E39-32".equals( errorCodeString ) )
			{
				layout1.showError( "正查找服务", errorCodeString, 0 );
			}
			else if ( "E40-32".equals( errorCodeString ) )
			{
				layout1.showError( "解扰内存已满", errorCodeString, 0 );
			}
			else if ( "E41-32".equals( errorCodeString ) )
			{
				layout1.showError( "当前服务无效", errorCodeString, 0 );
			}
			else if ( "E43-32".equals( errorCodeString ) )
			{
				layout1.showError( "该地域已禁止", errorCodeString, 0 );
			}
			else if ( "E45-32".equals( errorCodeString ) )
			{
				layout1.showError( "该服务已禁用", errorCodeString, 0 );
			}
			else if ( "E46-32".equals( errorCodeString ) )
			{
				layout1.showError( "正读取卫星信息", errorCodeString, 0 );
			}
			else if ( "E47-32".equals( errorCodeString ) )
			{
				layout1.showError( "信号堵塞", errorCodeString, 0 );
			}
			else if ( "E48-32".equals( errorCodeString ) )
			{
				layout1.showError( "无信号", errorCodeString, 0 );
			}
			else if ( "E49-32".equals( errorCodeString ) )
			{
				layout1.showError( "LNB 过载", errorCodeString, 0 );
			}
			else if ( "E50-32".equals( errorCodeString ) )
			{
				layout1.showError( "无有效服务", errorCodeString, 0 );
			}
			else if ( "E52-32".equals( errorCodeString ) )
			{
				layout1.showError( "正查找信号", errorCodeString, 0 );
			}
			else if ( "E64-32".equals( errorCodeString ) )
			{
				layout1.showError( "无效TUNER参数", errorCodeString, 0 );
			}
			else if ( "E00-32".equals( errorCodeString ) )
			{
				layout1.showError( "服务未加扰", errorCodeString, 0 );
			}
			else if ( "E53-32".equals( errorCodeString ) )
			{
				layout1.showError( "Pin码错误", errorCodeString, 0 );
			}
			else if ( "E54-32".equals( errorCodeString ) )
			{
				layout1.showError( "IPPV 正确", errorCodeString, 0 );
			}
			else if ( "E56-32".equals( errorCodeString ) )
			{
				layout1.showError( "CA模块不兼容", errorCodeString, 0 );
			}
			else if ( "E57-32".equals( errorCodeString ) )
			{
				layout1.showError( "未知通道", errorCodeString, 0 );
			}
			else if ( "E58-32".equals( errorCodeString ) )
			{
				layout1.showError( "无有用通道", errorCodeString, 0 );
			}
			else if ( "E66-32".equals( errorCodeString ) )
			{
				layout1.showError( "当前服务禁用", errorCodeString, 0 );
			}
			else if ( "E67-32".equals( errorCodeString ) )
			{
				layout1.showError( "请插入nagravision智能卡", errorCodeString, 0 );
			}
			else if ( "E68-32".equals( errorCodeString ) )
			{
				layout1.showError( "nagravision智能卡未正确插入", errorCodeString, 0 );
			}
			else if ( "E69-32".equals( errorCodeString ) )
			{
				layout1.showError( "免费无线服务已禁用", errorCodeString, 0 );
			}
			else if ( "E01-32".equals( errorCodeString ) )
			{
				layout1.showError( "请插入CA模块", errorCodeString, 0 );
			}
			else if ( "E02-32".equals( errorCodeString ) )
			{
				layout1.showError( "CA模块存储失败", errorCodeString, 0 );
			}
			else if ( "E03-32".equals( errorCodeString ) )
			{
				layout1.showError( "CA模块失败", errorCodeString, 0 );
			}
			else if ( "E14-32".equals( errorCodeString ) )
			{
				layout1.showError( "服务已加扰", errorCodeString, 0 );
			}
			else if ( "E35-32".equals( errorCodeString ) )
			{
				layout1.showError( "服务已加扰", errorCodeString, 0 );
			}
			else if ( "E36-32".equals( errorCodeString ) )
			{
				layout1.showError( "智能卡不兼容", errorCodeString, 0 );
			}
			else if ( "E70-32".equals( errorCodeString ) )
			{
				layout1.showError( "此服务在您的第二台电视上无效", errorCodeString, 0 );
			}
			else if ( "E71-32".equals( errorCodeString ) )
			{
				layout1.showError( "歌曲标题和艺术家的信息不能被查阅", errorCodeString, 0 );
			}
			else if ( "E72-32".equals( errorCodeString ) )
			{
				layout1.showError( "正扫描，请等待", errorCodeString, 0 );
			}
			else if ( "E73-32".equals( errorCodeString ) )
			{
				layout1.showError( "TV连接过载", errorCodeString, 0 );
			}
			else if ( "E74-32".equals( errorCodeString ) )
			{
				layout1.showError( "LNB1超载", errorCodeString, 0 );
			}
			else if ( "E75-32".equals( errorCodeString ) )
			{
				layout1.showError( "LNB2超载", errorCodeString, 0 );
			}
			else if ( "E76-32".equals( errorCodeString ) )
			{
				layout1.showError( "电视2被禁用，请联系客户服务", errorCodeString, 0 );
			}
			else if ( "E210-32".equals( errorCodeString ) )
			{
				layout1.showError( "感应检测会话无效", errorCodeString, 0 );
			}
			else if ( "E211-32".equals( errorCodeString ) )
			{
				layout1.showError( "感应检测通信失败", errorCodeString, 0 );
			}
			else if ( "E212-32".equals( errorCodeString ) )
			{
				layout1.showError( "感应检测消息超时", errorCodeString, 0 );
			}
			else if ( "E213-32".equals( errorCodeString ) )
			{
				layout1.showError( "感应检测环路超时", errorCodeString, 0 );
			}
			else if ( "E214-32".equals( errorCodeString ) )
			{
				layout1.showError( "感应检测会话过期", errorCodeString, 0 );
			}
			else
			{
				layout1.showError( "", errorCodeString, 0 );
			}
		}
	}

	private void procSMMsg( int msgID, int arg1, int arg2, Object arg3 )
	{
		String tmp1 = String.format( "msgId: 0x%x  arg1: %d  arg2: %d ", msgID, arg1, arg2 );
		Log.d( TAG, "procSMMsg--tmp1==" + tmp1 + "--msgID==" + msgID + "--"
						+ DVBEnum.MessageType.EVENT_SC_SCEVENT.value() );

		if ( DVBEnum.MessageType.EVENT_HW_TUNER_UNLOCK.value() == msgID )
		{
			layout1.showError( "信号中断", null, 0 );
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_LOCK.value() == msgID )
		{
			layout1.hideError();
		}

		if ( DVBEnum.MessageType.EVENT_SC_SCEVENT.value() == msgID )
		{
			FCProgData curChannel = DataHolder.from( Launcher.this ).getCurChannel();
			// 加密频道才显示插卡信息
			if ( curChannel != null && curChannel.isLocked() )
			{
				if ( DVBEnum.SCEvent.EN_CA_CARD_INSERT.value() == arg1 )
				{
					layout1.hideError();
				}
				else if ( DVBEnum.SCEvent.EN_CA_NO_CARD_INSERT.value() == arg1 )
				{
					layout1.showError( "请插卡", null, 0 );
				}
			}
			else
			{
				layout1.hideError();
			}
		}

		if ( DVBEnum.MessageType.EVENT_CA_SCEVENT.value() == msgID )
		{
			Log.e( TAG, "MessageType.EVENT_CA_SCEVENT arg1:" + arg1 );

			if ( arg1 == 100 )
			{
				// hide
				layout1.hideError();
			}
			else if ( arg1 == 0 )
			{
				layout1.showError( "收看级别不够", null, 0 );
			}
			else if ( arg1 == 1 )
			{
				layout1.showError( "不在收看时间段内", null, 0 );
			}
			else if ( arg1 == 2 )
			{
				layout1.showError( "没有机卡对应", null, 0 );
			}
			else if ( arg1 == 4 )
			{
				layout1.showError( "请插卡", null, 0 );
			}
			else if ( arg1 == 5 )
			{
				layout1.showError( "没有购买此节目", null, 0 );
			}
			else if ( arg1 == 6 )
			{
				layout1.showError( "运营商限制观看此节目", null, 0 );
			}
			else if ( arg1 == 7 )
			{
				layout1.showError( "运营商限制区域观看", null, 0 );
			}
			else if ( arg1 == 8 )
			{
				layout1.showError( "此卡为子卡，已经被限制收看，请与母卡配对", null, 0 );
			}
			else if ( arg1 == 9 )
			{
				layout1.showError( "余额不足，不能观看此节目，请及时充值", null, 0 );
			}
			else if ( arg1 == 10 )
			{
				layout1.showError( "此节目为IPPV节目，请到IPPV节目确认/取消购买菜单下确认购买此节目", null, 0 );
			}
			else if ( arg1 == 11 )
			{
				layout1.showError( "此节目为IPPV节目，您没有预订和确认购买，不能观看此节目", null, 0 );
			}
			else if ( arg1 == 12 )
			{
				layout1.showError( "此节目为IPPT节目，请到IPPT节目确认/取消购买菜单下确认购买此节目", null, 0 );
			}
			else if ( arg1 == 13 )
			{
				layout1.showError( "此节目为IPPT节目，您没有预订和确认购买，不能观看此节目", null, 0 );
			}
			else if ( arg1 == 16 )
			{
				layout1.showError( "数据无效", null, 0 );
			}
			else if ( arg1 == 18 )
			{
				layout1.showError( "IC卡被禁止服务", null, 0 );
			}
			else if ( arg1 == 20 )
			{
				layout1.showError( "此卡未被激活，请联系运营商", null, 0 );
			}
			else if ( arg1 == 21 )
			{
				layout1.showError( "请联系运营商回传IPP节目信息", null, 0 );
			}
			else if ( arg1 == 22 )
			{
				layout1.showError( "用户您好，此节目您尚未购买，正在免费预览中", null, 0 );
			}
		}
	}

	/**
	 * 同方凌汛CA
	 * 
	 * @param msgID
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	private void procLXMsg( int msgID, int arg1, int arg2, Object arg3 )
	{
		Log.i( "CALXEvent", "procLXMsg msgID : " + msgID );
		if ( DVBEnum.MessageType.EVENT_SC_SCEVENT.value() == msgID )
		{
			FCProgData curChannel = DataHolder.from( this ).getCurChannel();
			// 加密频道才显示插卡信息
			if ( curChannel != null && curChannel.isLocked() )
			{
				if ( DVBEnum.SCEvent.EN_CA_CARD_INSERT.value() == arg1 )
				{
					layout1.hideError();
				}
				else if ( DVBEnum.SCEvent.EN_CA_NO_CARD_INSERT.value() == arg1 )
				{
					layout1.showError( "请插入智能卡", null, 0 );
				}
			}
			else
			{
				layout1.hideError();
			}
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_UNLOCK.value() == msgID )
		{

			layout1.showError( "信号异常，请检查信号", null, 0 );
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_LOCK.value() == msgID )
		{
			layout1.hideError();
		}
		else if ( DVBEnum.MessageType.EVENT_CA_SCEVENT.value() == msgID )
		{
			Log.i( "CALXEvent", "procLXMsg msgID : " + msgID );
			Log.i( "CALXEvent", "procLXMsg arg1 : " + arg1 );
			Log.i( "CALXEvent", "procLXMsg arg2 : " + arg2 );

			if ( arg1 == DVBEnum.CALXEvent.ACT_INACTIVE.value() )
			{
				layout1.showError( "机顶盒尚未激活", null, 0 );
			}
			else if ( arg1 == DVBEnum.CALXEvent.ACTIVE_ERROR.value() )
			{
				layout1.showError( "激活错误", null, 0 );
			}
			else if ( arg1 == DVBEnum.CALXEvent.CLEAR_SCREEN.value() )
			{
				layout1.hideError();
			}
			else if ( arg1 == DVBEnum.CALXEvent.UNSUPPORTED_FRONTEND.value() )
			{
				layout1.showError( "非本系统加密，无法收看", null, 0 );
			}
			else if ( arg1 == DVBEnum.CALXEvent.GCA_BLOCKED.value() )
			{
				layout1.showError( "区域阻塞，无法收看此节目", null, 0 );
			}
			else if ( arg1 == DVBEnum.CALXEvent.BLACKLISTED.value() )
			{
				layout1.showError( "机顶盒被列入黑名单，无法收看此节目", null, 0 );
			}
			else if ( arg1 == DVBEnum.CALXEvent.USER_FROZEN.value() )
			{
				layout1.showError( "机顶盒已被禁用", null, 0 );
			}
			else if ( arg1 == DVBEnum.CALXEvent.USER_UNFROZEN.value() )
			{
				layout1.showError( "机顶盒已解冻", null, 0 );
			}
			else if ( arg1 == DVBEnum.CALXEvent.NOT_IN_WORKING_PERIOD.value() )
			{
				layout1.showError( "机顶盒不在工作时段内", null, 0 );
			}
			else if ( arg1 == DVBEnum.CALXEvent.NO_SUCH_ENTITLEMENT.value() )
			{
				layout1.showError( "没有授权，无法收看此节目", null, 0 );
			}
			else if ( arg1 == DVBEnum.CALXEvent.ENTITLEMENT_OVERDUE.value() )
			{
				layout1.showError( "用户授权已过期", null, 0 );
			}
		}
	}

	// 数视通
	private void procDGMsg( int msgID, int arg1, int arg2, Object arg3 )
	{
		String tmp1 = String.format( "msgId: 0x%x  arg1: %d  arg2: %d ", msgID, arg1, arg2 );
		Log.d( TAG, tmp1 );

		if ( DVBEnum.MessageType.EVENT_HW_TUNER_UNLOCK.value() == msgID )
		{
			// layout1.showError( " 没有信号(请检查电视线路)", null, 0 );
			layout1.showError( "没有信号(请检查电视线路)", null, 0 );
		}
		else if ( DVBEnum.MessageType.EVENT_HW_TUNER_LOCK.value() == msgID )
		{
			layout1.hideError();
		}

		if ( DVBEnum.MessageType.EVENT_SC_SCEVENT.value() == msgID )
		{
			FCProgData curChannel = DataHolder.from( this ).getCurChannel();
			// 加密频道才显示插卡信息
			if ( curChannel != null && curChannel.isLocked() )
			{
				if ( DVBEnum.SCEvent.EN_CA_CARD_INSERT.value() == arg1 )
				{
					layout1.hideError();
				}
				else if ( DVBEnum.SCEvent.EN_CA_NO_CARD_INSERT.value() == arg1 )
				{
					layout1.showError( "请插入用户卡", null, 0 );
				}
			}
			else
			{
				layout1.hideError();
			}
		}

		if ( DVBEnum.MessageType.EVENT_CA_SCEVENT.value() == msgID )
		{
			Log.e( TAG, "MessageType.EVENT_CA_SCEVENT arg1:" + arg1 );

			int event = arg1;
			int param1 = arg2;
			int param2 = 0, param3 = 0;
			String data = "";

			if ( arg3 != null )
			{
				if ( arg3 instanceof CAMessage )
				{
					CAMessage msg = ( ( CAMessage ) arg3 );
					data = msg.data;
					param2 = msg.param1;
					param3 = msg.param2;
				}
			}
			if ( DVBEnum.CADGEvent.CAS_AUTH_EVENT.value() == event )
			{
				// layout1.showError( "无有效授权(请确认是否缴费)", null, 0 );
				if ( LauApplication.getClientVer().contains( "HH" ) )
				{
					layout1.showError( "节目未开通，请联系88857115", null, 0 );
				}
				else
				{
					layout1.showError( "无有效授权(请确认是否缴费)", null, 0 );
				}

			}
			else if ( DVBEnum.CADGEvent.CAS_CARDSTATUS_EVENT.value() == event )
			{
				if ( param1 == 0x00 )
				{
					layout1.showError( "初始化安装，未不可使用", null, 0 );
				}
				else if ( param1 == 0x01 )
				{
					layout1.showError( "正在安装，卡等待激活", null, 0 );
				}
				else if ( param1 == 0x02 )
				{
					layout1.hideError();
				}
				else if ( param1 == 0x03 )
				{
					layout1.showError( "欠费停用", null, 0 );
				}
				else if ( param1 == 0x04 )
				{
					layout1.showError( "维修停用", null, 0 );
				}
				else if ( param1 == 0x05 )
				{
					layout1.showError( "申请报停停用", null, 0 );
				}
				else if ( param1 == 0x09 )
				{
					layout1.showError( "注销状态", null, 0 );
				}
			}
			else if ( DVBEnum.CADGEvent.CAS_EMAIL_EVENT.value() == event )
			{

			}
			else if ( DVBEnum.CADGEvent.CAS_HIDE_EVENT.value() == event )
			{
				layout1.hideError();
			}
			else if ( DVBEnum.CADGEvent.CAS_INVALIDCARD_EVENT.value() == event )
			{
				layout1.showError( "无效卡", null, 0 );
			}
			else if ( DVBEnum.CADGEvent.CAS_LIMITPLAY_EVENT.value() == event )
			{
				if ( param1 == 0x01 )
				{
					layout1.showError( "区域验证错误", null, 0 );
				}
				else if ( param1 == 0x00 )
				{
					layout1.hideError();
				}
			}
			else if ( DVBEnum.CADGEvent.CAS_NOINSERTCARD_EVENT.value() == event )
			{
				layout1.showError( "未插卡", null, 0 );
			}
			else if ( DVBEnum.CADGEvent.CAS_PARENTCTRL_EVENT.value() == event )
			{
				if ( param1 == 0x01 )
				{
					layout1.showError( "当前节目级别低于家长控制级别", null, 0 );
				}
				else if ( param1 == 0x00 )
				{
					layout1.hideError();
				}
			}
			else if ( DVBEnum.CADGEvent.CAS_PARTNER_EVENT.value() == event )
			{
				layout1.showError( "机卡配对错误", null, 0 );
			}
			else if ( DVBEnum.CADGEvent.CAS_SONCARD_EVENT.value() == event )
			{
				if ( param1 == 0x00 )
				{
					layout1.hideError();
				}
				else if ( param1 == 0x01 )
				{
					layout1.showError( "子卡可临时激活", null, 0 );
				}
			}
			else if ( DVBEnum.CADGEvent.CAS_SONCARDACTIVITE_EVENT.value() == event )
			{
				if ( param1 == 0x00 )
				{
					layout1.hideError();
				}
				else if ( param1 == 0x01 )
				{
					layout1.showError( "请插母卡", null, 0 );
				}
				else if ( param1 == 0x02 )
				{
					layout1.showError( "无效母卡", null, 0 );
				}
				else if ( param1 == 0x03 )
				{
					layout1.showError( "请插子卡", null, 0 );
				}
				else if ( param1 == 0x04 )
				{
					layout1.showError( "无效子卡", null, 0 );
				}
				else if ( param1 == 0x05 )
				{
					layout1.showError( "子母卡不匹配", null, 0 );
				}
				else if ( param1 == 0x06 )
				{
					layout1.showError( "母卡通信失败，请检查母卡", null, 0 );
				}
				else if ( param1 == 0x07 )
				{
					layout1.showError( "子卡通信失败，请检查子卡", null, 0 );
				}
				else if ( param1 == 0x08 )
				{
					layout1.showError( "不在激活时间段内", null, 0 );
				}
				else if ( param1 == 0x09 )
				{
					layout1.showError( "激活成功，请插入下一张卡", null, 0 );
				}
				else if ( param1 == 0x10 )
				{
					layout1.showError( "终止了激活子卡操作", null, 0 );
				}
			}
		}
	}

	private String[] caErrorStrings;

	private void procUTIMsg( int msgID, int arg1, int arg2, Object arg3 )
	{
		Log.i( TAG, "procUTIMsg" );
		Log.i( TAG, "procUTIMsg msgID : " + msgID );
		Log.i( TAG, "procUTIMsg arg1 : " + arg1 );
		Log.i( TAG, "procUTIMsg arg2 : " + arg2 );

		if ( caErrorStrings == null )
		{
			caErrorStrings = getResources().getStringArray( R.array.ca_error );
		}

		if ( DVBEnum.MessageType.EVENT_CA_SCEVENT.value() == msgID )
		{
			if ( caErrorStrings == null || caErrorStrings.length == 0 )
			{
				return;
			}

			if ( arg2 == 8 )
			{
				layout1.hideError();
			}
			else
			{
				layout1.showError( caErrorStrings[arg2], null, 5000 );
			}
		}
	}

	private CaMailProxy.IMailReceiver mailReceiver = new IMailReceiver()
	{
		@Override
		public void onMailReceive( int mailId )
		{
			handler.removeMessages( MAIL_MSG );
			handler.sendEmptyMessage( MAIL_MSG );
		}
	};

	private static final int NOTIFY_MSG = 1000;
	private FCProgData curChannel;

	private MessageListener dvbMsgListener = new MessageListener()
	{
		@Override
		public void notify( int msgID, int arg1, int arg2, Object arg3 )
		{
			Log.i( TAG, "dvbMsgListener--notify" );
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putInt( "msgID", msgID );
			bundle.putInt( "arg1", arg1 );
			bundle.putInt( "arg2", arg2 );
			msg.setData( bundle );
			msg.obj = arg3;
			msg.what = NOTIFY_MSG;

			curChannel = DataHolder.from( Launcher.this ).getCurChannel();

			handler.sendMessage( msg );
		}
	};

	public void play()
	{
		if ( PlayWorker.from().isExistData() )
		{
			String deviceVer = FCTvDevice.getDeviceVersion();
			if ( deviceVer != null && deviceVer.contains( "SXYQ" ) )
			{
				if ( 1 == Integer
								.parseInt( SystemProperties.get( "sys.dvb.live.firstinit", "1" ) ) )
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
		}
	}
}
