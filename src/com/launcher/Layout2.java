package com.launcher;

import java.util.List;

import com.fc.util.tool.FCActionTool;
import com.hudong.aidl.MenuEntity;
import com.hudong.aidl.ReplayConnection;
import com.launcher.TVListView.EventListener;
import com.launcher.db.DBManager;
import com.launcher.db.info.MovieInfo;
import com.launcher.helper.ApkHelper;
import com.launcher.vod.TTVodPolicy;
import com.launcher.widget.FcTextView;
import com.launcher.widget.LayoutView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Layout2 extends LayoutView
{

	private static final String TAG = "Layout2";

	private FcTextView mItem1, mItem2, mItem3, mItem4, mItem5, mItem6, mItem7, mItem8, mItem9, mItem10;

	private ScaleAnimEffect animEffect;

	private LinearLayout mLayout;

	private MenuAdapter menuAdapter;

	private DBManager manager;

	private RelativeLayout layout_SXYQ, layout_common;

	public Layout2( final Context context )
	{
		super( context );
		this.animEffect = new ScaleAnimEffect();
		initViewCOMMON();
	}

	@Override
	public void setManager( DBManager manager )
	{
		Log.i( TAG, "setManager" );
		if ( manager != null )
		{
			Log.i( TAG, "setManager manager != null  " );
			if ( manager.isTagInside( "700" ) )
			{
				Log.i( TAG, "initViewCOMMON" );
				initViewCOMMON();

			}
			else if ( manager.isTagInside( "200" ) )
			{
				Log.i( TAG, "setManager initViewSXYQ" );
				ReplayConnection.getInstance().init( context );
				initViewSXYQ();
				menuAdapter = new MenuAdapter( context, mMenulNames, mMenulIDs );
				channeLv.setAdapter( menuAdapter );
			}
			else
			{
				Log.i( TAG, "initViewCOMMON" );
				initViewCOMMON();
			}
		}
	}

	private Handler handler = new Handler()
	{
		public void handleMessage( Message msg )
		{
			switch( msg.what )
			{
			case 0:
				new Thread( new Runnable()
				{

					@Override
					public void run()
					{
						ReplayConnection.getInstance().getRecommendList();
						getRecommendIds( context );
						getRecommendUrls( context );
						getRecommendNames( context );
						if ( mRecommendUrls != null )
						{
							Log.i( TAG, "mRecommendUrls  size" + mRecommendUrls.length );
							if ( mRecommendUrls.length > 1 )
							{
								context.runOnUiThread( new Runnable()
								{
									@Override
									public void run()
									{
										setImage();
									}
								} );
							}
							else
							{
								handler.removeMessages( 0 );
								handler.sendEmptyMessageDelayed( 0, 1000 );

							}
						}
						else
						{
							handler.removeMessages( 0 );
							handler.sendEmptyMessageDelayed( 0, 1000 );
						}
					}
				} ).start();
				break;

			case 1:
				new Thread( new Runnable()
				{

					@Override
					public void run()
					{
						ReplayConnection.getInstance().getReplayList();
						getMenulIDs( context );
						getMenulNames( context );
						Log.i( TAG, "mMenulIDs  size" + mMenulIDs.length );
						if ( mMenulIDs != null )
						{
							if ( mMenulIDs.length > 1 )
							{
								context.runOnUiThread( new Runnable()
								{
									@Override
									public void run()
									{
										menuAdapter = new MenuAdapter( context, mMenulNames, mMenulIDs );
										channeLv.setAdapter( menuAdapter );
									}
								} );
							}
							else
							{
								handler.removeMessages( 1 );
								handler.sendEmptyMessageDelayed( 1, 1000 );

							}
						}
						else
						{
							handler.removeMessages( 1 );
							handler.sendEmptyMessageDelayed( 1, 1000 );
						}
					}
				} ).start();
				break;

			default:
				break;
			}

		};
	};

	private void initViewCOMMON()
	{
		layout_common = ( RelativeLayout ) context.findViewById( R.id.layout_common );
		layout_SXYQ = ( RelativeLayout ) context.findViewById( R.id.layout_SXYQ );
		mItem1 = ( FcTextView ) context.findViewById( R.id.lay7_item1 );
		mItem2 = ( FcTextView ) context.findViewById( R.id.lay7_item2 );
		mItem3 = ( FcTextView ) context.findViewById( R.id.lay7_item3 );
		mItem4 = ( FcTextView ) context.findViewById( R.id.lay7_item4 );
		mItem5 = ( FcTextView ) context.findViewById( R.id.lay7_item5 );
		mItem6 = ( FcTextView ) context.findViewById( R.id.lay7_item6 );
		mItem7 = ( FcTextView ) context.findViewById( R.id.lay7_item7 );
		mItem8 = ( FcTextView ) context.findViewById( R.id.lay7_item8 );
		mItem9 = ( FcTextView ) context.findViewById( R.id.lay7_item9 );
		mItem10 = ( FcTextView ) context.findViewById( R.id.lay7_item10 );

		mItem1.setOnClickListener( clickListener );
		mItem2.setOnClickListener( clickListener );
		mItem3.setOnClickListener( clickListener );
		mItem4.setOnClickListener( clickListener );
		mItem5.setOnClickListener( clickListener );
		mItem6.setOnClickListener( clickListener );
		mItem7.setOnClickListener( clickListener );
		mItem8.setOnClickListener( clickListener );
		mItem9.setOnClickListener( clickListener );
		mItem10.setOnClickListener( clickListener );

		mItem1.setOnFocusChangeListener( focusListener );
		mItem2.setOnFocusChangeListener( focusListener );
		mItem3.setOnFocusChangeListener( focusListener );
		mItem4.setOnFocusChangeListener( focusListener );
		mItem5.setOnFocusChangeListener( focusListener );
		mItem6.setOnFocusChangeListener( focusListener );
		mItem7.setOnFocusChangeListener( focusListener );
		mItem8.setOnFocusChangeListener( focusListener );
		mItem9.setOnFocusChangeListener( focusListener );
		mItem10.setOnFocusChangeListener( focusListener );

		mItems = new FcTextView[ 10 ];
		mItems[0] = mItem1;
		mItems[1] = mItem2;
		mItems[2] = mItem3;
		mItems[3] = mItem4;
		mItems[4] = mItem5;
		mItems[5] = mItem6;
		mItems[6] = mItem7;
		mItems[7] = mItem8;
		mItems[8] = mItem9;
		mItems[9] = mItem10;
		layout_SXYQ.setVisibility( View.GONE );
		layout_common.setVisibility( View.VISIBLE );

	}

	private void initViewSXYQ()
	{
		layout_common = ( RelativeLayout ) context.findViewById( R.id.layout_common );
		layout_SXYQ = ( RelativeLayout ) context.findViewById( R.id.layout_SXYQ );
		mLayout = ( LinearLayout ) context.findViewById( R.id.channe_ll );
		channeLv = ( TVListView ) context.findViewById( R.id.channe_lv );
		mItem2 = ( FcTextView ) context.findViewById( R.id.lay2_item1 );
		mItem3 = ( FcTextView ) context.findViewById( R.id.lay2_item2 );
		mItem4 = ( FcTextView ) context.findViewById( R.id.lay2_item3 );
		mItem5 = ( FcTextView ) context.findViewById( R.id.lay2_item4 );
		mItem6 = ( FcTextView ) context.findViewById( R.id.lay2_item5 );
		mItem7 = ( FcTextView ) context.findViewById( R.id.lay2_item6 );
		handler.removeMessages( 0 );
		handler.sendEmptyMessageDelayed( 0, 1000 );
		handler.removeMessages( 1 );
		handler.sendEmptyMessageDelayed( 1, 1000 );
		mItem2.setOnClickListener( clickListener );
		mItem3.setOnClickListener( clickListener );
		mItem4.setOnClickListener( clickListener );
		mItem5.setOnClickListener( clickListener );
		mItem6.setOnClickListener( clickListener );
		mItem7.setOnClickListener( clickListener );

		mItem2.setOnFocusChangeListener( focusListener );
		mItem3.setOnFocusChangeListener( focusListener );
		mItem4.setOnFocusChangeListener( focusListener );
		mItem5.setOnFocusChangeListener( focusListener );
		mItem6.setOnFocusChangeListener( focusListener );
		mItem7.setOnFocusChangeListener( focusListener );

		mItems = new FcTextView[ 6 ];
		mItems[0] = mItem2;
		mItems[1] = mItem3;
		mItems[2] = mItem4;
		mItems[3] = mItem5;
		mItems[4] = mItem6;
		mItems[5] = mItem7;
		layout_SXYQ.setVisibility( View.VISIBLE );
		layout_common.setVisibility( View.GONE );
		channeLv.setOnFocusChangeListener( focusListener );
		mLayout.setOnFocusChangeListener( focusListener );

		channeLv.setOnEnterEventListener( new EventListener()
		{

			@Override
			public void onItemSelected( int position, float rawY )
			{
				mBack.isHasFocus();

			}
		} );
		channeLv.setOnItemClickListener( mClickListener );
	}

	// 页面数据刷新的时候只走一次initView
	@Override
	public void refreshInfo( DBManager manager )
	{
		super.refreshInfo( manager );
		this.manager = manager;

		if ( manager.isTagInside( "200" ) )
		{
			// if ( mRecommendUrls != null )
			// {
			// if ( mRecommendUrls.length > 1 )
			// {
			// setImage();
			// }
			// }
			return;
		}

		for ( int i = 0; i < mItems.length; i++ )
		{
			String tag = mItems[i].getTag().toString();
			Log.i( "xxx", "layout 2 tag==" + tag );
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
				else if ( !TextUtils.isEmpty( info.getImg_v() ) )
				{
					initItem( mItems[i], info, "img_v" );
				}
				else if ( !TextUtils.isEmpty( info.getImg_h2() ) )
				{
					initItem( mItems[i], info, "img_h2" );
				}

				// initItem( mItems[i], info, "img_h" );
				mItems[i].setAction( info.getAction() );
				mItems[i].setPkg( info.getApp_package_name() );
				mItems[i].setAppId( info.getApp_id() );
			}
		}

	}

	private OnClickListener clickListener = new OnClickListener()
	{
		@Override
		public void onClick( View v )
		{

			String pkg = ( ( FcTextView ) v ).getPkg();
			String appid = ( ( FcTextView ) v ).getAppId();
			String action = ( ( FcTextView ) v ).getAction();
			Log.i( TAG, "pkg:" + pkg );
			if ( manager.isTagInside( "700" ) && manager.isTagInside( "200" ) && TextUtils.isEmpty( pkg ) )// 没缓存也没联网
			{
				// 默认跳转
				switch( v.getId() )
				{
				case R.id.lay7_item1:
					mContext.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "tt_vod://start" ) ) );
					break;

				case R.id.lay7_item2:

				case R.id.lay7_item3:

				case R.id.lay7_item4:

				case R.id.lay7_item5:

				case R.id.lay7_item6:

				case R.id.lay7_item7:

				case R.id.lay7_item8:

				case R.id.lay7_item9:

				case R.id.lay7_item10:
					Toast.makeText( mContext, "网络未连接", Toast.LENGTH_SHORT ).show();
					break;

				default:
					break;
				}
				return;

			}
			if ( manager.isTagInside( "700" ) )
			{
				if ( !mDBManager.isTagInside( v.getTag().toString() ) )
					if ( ( ( FcTextView ) v ).getIsVodItem() )
					{
						if ( !TTVodPolicy.isVodEnabled( mContext ) )
						{
							return;
						}
						return;
					}

				if ( !TextUtils.isEmpty( pkg ) )// 如果后台配置了包名
				{
					if ( ApkHelper.isApkInstalled( context, pkg ) )// 先检查应用是否存在
					{
						if ( action.contains( "tt_vod" ) )// 为了规避未连接爱奇艺天天影视进不去
						{
							if ( CommonUtil.isNetworkConnected( mContext ) )
							{
								FCActionTool.forward( mContext, action, "99900188", Launcher.APP_KEY_ID );
							}
							else
							{
								Toast.makeText( mContext, "网络未连接", Toast.LENGTH_SHORT ).show();
							}
						}
						else
						{
							FCActionTool.forward( mContext, action, "99900188", Launcher.APP_KEY_ID );
						}
					}
					else
					{
						if ( !TextUtils.isEmpty( appid ) )// 未安装的话如果后台配置了appid
						{
							if ( !CommonUtil.isNetworkConnected( mContext ) )
							{
								Toast.makeText( mContext, "网络未连接", Toast.LENGTH_SHORT ).show();
								return;
							}
							// 未安装则跳转到下载界面
							Intent intent = new Intent( context, DetailsActivity.class );
							intent.putExtra( "aid", appid );
							context.startActivity( intent );
						}
						else
						// 如果后台没有配置appid，则直接提示
						{
							Toast.makeText( mContext, "应用未安装", Toast.LENGTH_SHORT ).show();
						}
					}
				}
				else
				{
					if ( TextUtils.isEmpty( appid ) )// 如果后台没有配置包名和appid
					{
						FCActionTool.forward( mContext, ( ( FcTextView ) v ).getAction(), "99900188",
										Launcher.APP_KEY_ID );
					}

				}
			}
			else if ( manager.isTagInside( "200" ) )
			{
				if ( getsharedPreferences( context ) == null )
				{
					return;
				}

				if ( mRecommendIds == null || mRecommendIds.length <= 1 )
				{
					return;
				}

				String columnID = "";
				Intent intent = new Intent();
				switch( v.getId() )
				{
				case R.id.lay2_item1:
					columnID = mRecommendIds[0];
					Log.i( TAG, "columnID==" + columnID );
					intent.setAction( "com.hudong.action.DETAIL" );
					intent.putExtra( "id", columnID );
					context.startActivity( intent );
					break;
				case R.id.lay2_item2:
					columnID = mRecommendIds[1];
					intent.setAction( "com.hudong.action.DETAIL" );
					intent.putExtra( "id", columnID );
					Log.i( TAG, "columnID==" + columnID );
					context.startActivity( intent );
					break;
				case R.id.lay2_item3:
					columnID = mRecommendIds[2];
					intent.setAction( "com.hudong.action.DETAIL" );
					intent.putExtra( "id", columnID );
					Log.i( TAG, "columnID==" + columnID );
					context.startActivity( intent );
					break;
				case R.id.lay2_item4:
					columnID = mRecommendIds[3];
					intent.setAction( "com.hudong.action.DETAIL" );
					intent.putExtra( "id", columnID );
					Log.i( TAG, "columnID==" + columnID );
					context.startActivity( intent );
					break;
				case R.id.lay2_item5:
					columnID = mRecommendIds[4];
					intent.setAction( "com.hudong.action.DETAIL" );
					intent.putExtra( "id", columnID );
					Log.i( TAG, "id==" + columnID );
					context.startActivity( intent );
					break;
				case R.id.lay2_item6:
					columnID = mRecommendIds[5];
					intent.setAction( "com.hudong.action.DETAIL" );
					intent.putExtra( "id", columnID );
					Log.i( TAG, "columnID==" + columnID );
					context.startActivity( intent );
					break;
				default:
					break;
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
				Log.i( "sss", "hasFocus:" + hasFocus );
				mBack.isHasFocus();
				if ( v instanceof FcTextView )
				{
					if ( manager.isTagInside( "200" ) )
					{
						if ( getsharedPreferences( context ) != null )
						{
							if ( mRecommendNames != null )
							{
								if ( mRecommendNames.length > 1 )
								{
									( ( FcTextView ) v ).setVod( true );
									switch( v.getId() )
									{
									case R.id.lay2_item1:
										( ( FcTextView ) v ).setVodTitle( mRecommendNames[0] );
										break;
									case R.id.lay2_item2:
										( ( FcTextView ) v ).setVodTitle( mRecommendNames[1] );
										break;
									case R.id.lay2_item3:
										( ( FcTextView ) v ).setVodTitle( mRecommendNames[2] );
										break;

									case R.id.lay2_item4:
										( ( FcTextView ) v ).setVodTitle( mRecommendNames[3] );
										break;
									case R.id.lay2_item5:
										( ( FcTextView ) v ).setVodTitle( mRecommendNames[4] );
										break;
									case R.id.lay2_item6:
										( ( FcTextView ) v ).setVodTitle( mRecommendNames[5] );
										break;

									default:
										break;
									}
								}
								else
								{
									( ( FcTextView ) v ).setVod( false );
								}
							}
							else
							{
								( ( FcTextView ) v ).setVod( false );
							}

						}
						else
						{
							( ( FcTextView ) v ).setVod( false );
						}
					}
				}

				if ( v instanceof TVListView )
				{
					return;
				}

				itemFocusListener.onItemFocus( v, hasFocus );
				ScAnimation.getInstance( context ).showOnFocusAnimation( v );
			}
			else
			{
				if ( v instanceof TVListView )
				{
					return;
				}
				Log.i( "ttt", "focusListener  else" );
				ScAnimation.getInstance( context ).showLooseFocusAinimation( v );
			}
		}
	};

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage( Message msg )
		{
			super.handleMessage( msg );
			int i = msg.arg1;
			MovieInfo info = ( MovieInfo ) msg.obj;
			switch( msg.what )
			{
			case MSG_VOD_REFRESH:
				mItems[i].setIsVodItem( true );
				break;
			case MSG_COMMON_REFRESH:
				mItems[i].setIsVodItem( false );
				mItems[i].setPkg( info.getApp_package_name() );
				mItems[i].setAppId( info.getApp_id() );
				mItems[i].setAction( info.getAction() );
				mItems[i].hideTitle();
				break;
			default:
				break;
			}
		}
	};

	public boolean saveArray( List< MenuEntity > list )
	{
		SharedPreferences sp = mContext.getSharedPreferences( "ingoreList", mContext.MODE_PRIVATE );
		SharedPreferences.Editor mEdit1 = sp.edit();
		mEdit1.putInt( "Status_size", list.size() );
		for ( int i = 0; i < list.size(); i++ )
		{
			mEdit1.remove( "columnID" + i );
			mEdit1.remove( "title" + i );
			mEdit1.putString( "columnID" + i, list.get( i ).getColumnID() );
			mEdit1.putString( "title" + i, list.get( i ).getTitle() );
		}
		return mEdit1.commit();
	}

	public List< MenuEntity > loadArray( List< MenuEntity > list )
	{

		SharedPreferences mSharedPreference = mContext.getSharedPreferences( "ingoreList",
						mContext.MODE_PRIVATE );
		list.clear();
		int size = mSharedPreference.getInt( "Status_size", 0 );
		for ( int i = 0; i < size; i++ )
		{
			String columnID = mSharedPreference.getString( "columnID" + i, null );
			String title = mSharedPreference.getString( "title" + i, null );
			MenuEntity entity = new MenuEntity();
			entity.setColumnID( columnID );
			entity.setTitle( title );
			list.add( entity );
		}
		return list;
	}

	private OnItemClickListener mClickListener = new OnItemClickListener()
	{

		@Override
		public void onItemClick( AdapterView< ? > paramAdapterView, View paramView, int position,
						long paramLong )
		{

			if ( getsharedPreferences( context ) == null )
			{
				return;
			}
			if ( mMenulIDs == null || mMenulIDs.length <= 1 || mMenulNames == null || mMenulNames.length <= 1 )
			{
				return;
			}
			String columnID = mMenulIDs[position];
			Intent intent = new Intent();
			intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			Log.i( TAG, "columnID==" + columnID );
			Log.i( TAG, "title==" + mMenulNames[position] );
			intent.setAction( "com.hudong.action.LIST" );
			intent.putExtra( "title", mMenulNames[position] );
			intent.putExtra( "columnID", columnID );
			context.startActivity( intent );
		}
	};

	public void setImage()
	{
		if ( mRecommendUrls == null )
		{
			return;
		}
		if ( mRecommendUrls.length <= 1 )
		{
			return;
		}

		for ( int i = 0; i < 6; i++ )
		{
			if ( i == 0 )
			{
				Log.i( TAG, "i==0---url==" + mRecommendUrls[0] );
			}
			else
			{
				Log.i( TAG, "i==" + i + "---url==" + mRecommendUrls[i] );
			}

			switch( i )
			{
			case 0:

				MovieInfo info1 = new MovieInfo();
				info1.setImg_h( mRecommendUrls[0] );
				initItem( mItems[0], info1, "img_h" );
				Log.i( TAG, "url==" + mRecommendUrls[0] );
				break;
			case 1:
				MovieInfo info2 = new MovieInfo();
				info2.setImg_h( mRecommendUrls[1] );
				initItem( mItems[1], info2, "img_h" );
				Log.i( TAG, "url==" + mRecommendUrls[1] );
				break;
			case 2:
				MovieInfo info3 = new MovieInfo();
				info3.setImg_h( mRecommendUrls[2] );
				initItem( mItems[2], info3, "img_h" );
				Log.i( TAG, "url==" + mRecommendUrls[2] );
				break;
			case 3:
				MovieInfo info4 = new MovieInfo();
				info4.setImg_h( mRecommendUrls[3] );
				initItem( mItems[3], info4, "img_h" );
				Log.i( TAG, "url==" + mRecommendUrls[3] );
				break;
			case 4:
				MovieInfo info5 = new MovieInfo();
				info5.setImg_h( mRecommendUrls[4] );
				initItem( mItems[4], info5, "img_h" );
				Log.i( TAG, "url==" + mRecommendUrls[4] );
				break;
			case 5:
				MovieInfo info6 = new MovieInfo();
				info6.setImg_h( mRecommendUrls[5] );
				initItem( mItems[5], info6, "img_h" );
				Log.i( TAG, "url==" + mRecommendUrls[5] );
				break;
			default:
				break;
			}

			// if ( i == 0 )
			// {
			// MovieInfo info = new MovieInfo();
			// info.setImg_h( mRecommendUrls[0] );
			// initItem( mItems[i], info, "img_h" );
			// }
			// else
			// {
			// MovieInfo info = new MovieInfo();
			// info.setImg_h( mRecommendUrls[i] );
			// initItem( mItems[i], info, "img_h" );
			// }

		}
	}

	private static SharedPreferences mSharedPreferences = null;

	private static SharedPreferences getsharedPreferences( Context context )
	{
		if ( mSharedPreferences == null )
		{
			Context otherAppsContext;
			try
			{
				otherAppsContext = context.createPackageContext( "com.hudong.vod",
								Context.CONTEXT_IGNORE_SECURITY );
				mSharedPreferences = otherAppsContext.getSharedPreferences( "hudong_live",
								Context.MODE_MULTI_PROCESS );
			}
			catch ( NameNotFoundException e )
			{
				e.printStackTrace();
			}
		}

		return mSharedPreferences;
	}

	private static String[] mRecommendUrls;

	private static String[] getRecommendUrls( Context context )
	{
		if ( getsharedPreferences( context ) != null )
		{
			if ( mRecommendUrls == null || mRecommendUrls.length <= 1 )
			{
				String RecommendUrls = getsharedPreferences( context ).getString( "recommendUrls", "" );
				Log.i( TAG, "RecommendUrls" + RecommendUrls );
				mRecommendUrls = RecommendUrls.split( "\\|" );
			}

			return mRecommendUrls;
		}
		return null;
	}

	private static String[] mRecommendIds;

	private static String[] getRecommendIds( Context context )
	{
		if ( getsharedPreferences( context ) != null )
		{
			if ( mRecommendIds == null || mRecommendIds.length <= 1 )
			{
				String RecommendIds = getsharedPreferences( context ).getString( "recommendIds", "" );
				Log.i( TAG, "RecommendIds" + RecommendIds );
				mRecommendIds = RecommendIds.split( "\\|" );
			}

			return mRecommendIds;
		}
		return null;
	}

	private static String[] mRecommendNames;

	private static String[] getRecommendNames( Context context )
	{
		if ( getsharedPreferences( context ) != null )
		{
			if ( mRecommendNames == null || mRecommendIds.length <= 1 )
			{
				String RecommendNames = getsharedPreferences( context ).getString( "recommendNames", "" );
				mRecommendNames = RecommendNames.split( "\\|" );
			}

			return mRecommendNames;
		}
		return null;
	}

	private static String[] mMenulIDs;

	private static String[] getMenulIDs( Context context )
	{
		Log.i( TAG, "getMenulIDs" );
		Context otherAppsContext;
		SharedPreferences sharedPreferences = null;
		try
		{
			if ( !isAvilible( context, "com.hudong.vod" ) )
			{
				return null;
			}
			otherAppsContext = context.createPackageContext( "com.hudong.vod",
							Context.CONTEXT_IGNORE_SECURITY );

			sharedPreferences = otherAppsContext.getSharedPreferences( "hudong_live",
							Context.MODE_MULTI_PROCESS );
		}
		catch ( NameNotFoundException e )
		{
			e.printStackTrace();
		}

		if ( sharedPreferences != null )
		{
			if ( mMenulIDs == null || mMenulIDs.length <= 1 )
			{
				String MenulIDs = sharedPreferences.getString( "menulIDs", "" );
				Log.i( TAG, "MenulIDs" + MenulIDs );
				mMenulIDs = MenulIDs.split( "\\|" );
			}
			return mMenulIDs;
		}
		return null;

	}

	private static String[] mMenulNames;

	private static String[] getMenulNames( Context context )
	{
		Log.i( TAG, "getMenulNames" );
		Context otherAppsContext;
		SharedPreferences sharedPreferences = null;
		try
		{
			if ( !isAvilible( context, "com.hudong.vod" ) )
			{
				return null;
			}
			otherAppsContext = context.createPackageContext( "com.hudong.vod",
							Context.CONTEXT_IGNORE_SECURITY );
			sharedPreferences = otherAppsContext.getSharedPreferences( "hudong_live",
							Context.MODE_MULTI_PROCESS );
		}
		catch ( NameNotFoundException e )
		{
			e.printStackTrace();
		}

		if ( sharedPreferences != null )
		{
			if ( mMenulNames == null || mMenulNames.length <= 1 )
			{
				String MenulNames = sharedPreferences.getString( "menulNames", "" );
				Log.i( TAG, "MenulNames" + MenulNames );
				mMenulNames = MenulNames.split( "\\|" );
			}

			return mMenulNames;
		}
		return null;
	}

	/**
	 * 
	 * @param context
	 *            上下文对象
	 * @return
	 */
	public static boolean isAvilible( Context context, String packageName )
	{
		PackageManager packageManager = context.getPackageManager();

		// 获取手机系统的所有APP包名，然后进行一一比较
		List< PackageInfo > pinfo = packageManager.getInstalledPackages( 0 );
		for ( int i = 0; i < pinfo.size(); i++ )
		{
			if ( ( ( PackageInfo ) pinfo.get( i ) ).packageName.equalsIgnoreCase( packageName ) )
				return true;
		}
		return false;
	}

}
