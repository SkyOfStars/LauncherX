package com.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fc.util.tool.FCActionTool;
import com.launcher.db.DBManager;
import com.launcher.db.info.MovieInfo;
import com.launcher.helper.ApkHelper;
import com.launcher.util.ApkHelper2;
import com.launcher.widget.FcTextView;
import com.launcher.widget.LayoutView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

public class Layout5 extends LayoutView
{
	private static final String TAG = "Layout5";
	private final static int AGAIN_REFRESH = 1000;

	private FcTextView mItem1, mItem2, mItem3, mItem4, mItem5, mItem6, mItem7, mItem8;

	private LinearLayout mViewGroup;

	public String currentTAG = "", currentPkg = "";

	private List< Map< String, Object >> list = new ArrayList< Map< String, Object >>();
	private int[] mItemBgs =
	{ R.drawable.m_5_1, R.drawable.m_6, R.drawable.m_3, R.drawable.m_2, R.drawable.m_1_2, R.drawable.m_1,
					R.drawable.m_10, R.drawable.m_3, R.drawable.m_5 };

	public PopupWindow popupWindow;

	public Layout5( Context context )
	{
		super( context );

		initView();
	}

	private void initView()
	{
		mItem1 = ( FcTextView ) context.findViewById( R.id.lay5_item1 );
		mItem2 = ( FcTextView ) context.findViewById( R.id.lay5_item2 );
		mItem3 = ( FcTextView ) context.findViewById( R.id.lay5_item3 );
		mItem4 = ( FcTextView ) context.findViewById( R.id.lay5_item4 );
		mItem5 = ( FcTextView ) context.findViewById( R.id.lay5_item5 );
		mItem6 = ( FcTextView ) context.findViewById( R.id.lay5_item6 );
		mItem7 = ( FcTextView ) context.findViewById( R.id.lay5_item7 );
		mItem8 = ( FcTextView ) context.findViewById( R.id.lay5_item8 );

		mItem1.setOnClickListener( clickListener );
		mItem2.setOnClickListener( clickListener );
		mItem3.setOnClickListener( clickListener );
		mItem4.setOnClickListener( clickListener );
		mItem5.setOnClickListener( clickListener );
		mItem6.setOnClickListener( clickListener );
		mItem7.setOnClickListener( clickListener );
		mItem8.setOnClickListener( clickListener );

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

		initPopupWindow();

	}

	private void initPopupWindow()
	{
		mViewGroup = ( LinearLayout ) context.findViewById( R.id.container );

		View popupWindowView = LayoutInflater.from( mContext ).inflate( R.layout.launcher_apps_dialog,
						mViewGroup, false );
		popupWindowView.findViewById( R.id.btn_replace ).setOnClickListener( popOnClickListener );
		popupWindowView.findViewById( R.id.btn_delete ).setOnClickListener( popOnClickListener );
		popupWindow = new PopupWindow( popupWindowView, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,
						true );
		popupWindow.setFocusable( true );

		popupWindow.setBackgroundDrawable( new BitmapDrawable() );// 解决响应返回键必须的语句
		popupWindowView.setOnKeyListener( new OnKeyListener()
		{
			@Override
			public boolean onKey( View v, int keyCode, KeyEvent event )
			{
				if ( keyCode == KeyEvent.KEYCODE_MENU )
				{
					FcTextView view = ( FcTextView ) v;
					Log.i( TAG, "view.getIsAppAdded() ==" + view.getIsAppAdded() );
					if ( view.getIsAppAdded() )
					{

						currentTAG = view.getTag().toString();
						currentPkg = view.getPkg();
						showPopWindow();
						return true;
					}

				}
				if ( keyCode == KeyEvent.KEYCODE_BACK )
				{
					if ( popupWindow.isShowing() )
					{
						popupWindow.dismiss();
						return true;
					}

				}
				return false;
			}
		} );
		// popupWindow.setAnimationStyle(R.style.AnimationRightFade);
		popupWindow.setOutsideTouchable( false );
	}

	public void showPopWindow()
	{
		popupWindow.showAtLocation( mViewGroup, Gravity.RIGHT, 0, 0 );
	}

	private Handler mHandler = new Handler()
	{
		public void handleMessage( android.os.Message msg )
		{
			switch( msg.what )
			{
			case AGAIN_REFRESH:
				MovieInfo info = ( MovieInfo ) msg.obj;
				int m = msg.arg1;
				Log.i( TAG,
								"ApkHelper.isApkInstalled:"
												+ ApkHelper.isApkInstalled( context,
																info.getApp_package_name() ) );
				if ( ApkHelper.isApkInstalled( context, info.getApp_package_name() ) )// 先检查应用是否存在
				{

					Bitmap bitmap = ApkHelper2.getAppIcon( mContext, info.getApp_package_name() );
					if ( bitmap != null )
					{
						Bitmap bmBG = BitmapFactory.decodeResource( mContext.getResources(), mItemBgs[m] );// 本地添加应用就用默认背景
						Layout5.this.setImageLocalData( true, mItems[m], info.getTitle(),
										info.getApp_package_name(), bitmap, bmBG );
					}

				}

				break;

			default:
				break;
			}
		};

	};
	private OnClickListener popOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick( View v )
		{
			int id = v.getId();
			switch( id )
			{
			case R.id.btn_replace:
				Intent intent2 = new Intent( mContext, AppsActivity.class );
				intent2.putExtra( "tag", currentTAG );
				intent2.putExtra( "package", currentPkg );
				mContext.startActivity( intent2 );
				if ( popupWindow.isShowing() )
				{
					popupWindow.dismiss();
				}
				break;
			case R.id.btn_delete:
				for ( int i = 0; i < mItems.length; i++ )
				{
					if ( mItems[i].getTag().toString().equals( currentTAG ) )
					{
						Bitmap bmBG = BitmapFactory.decodeResource( mContext.getResources(), mItemBgs[i] );
						Bitmap bmIcon = BitmapFactory.decodeResource( mContext.getResources(),
										R.drawable.icon_add );
						setImageLocalData( false, mItems[i], "", "", bmIcon, bmBG );
						mDBManager.clearAddedData( mItems[i].getTag().toString() );
						if ( popupWindow.isShowing() )
						{
							popupWindow.dismiss();
						}
					}
				}
				break;

			default:
				break;
			}
		}
	};

	@Override
	public void refreshBtnApp( List< Map< String, Object >> list )
	{
		this.list = list;
		super.refreshBtnApp( list );
	}

	@Override
	public void refreshInfo( final DBManager manager )
	{
		super.refreshInfo( manager );

		for ( int i = 0; i < mItems.length; i++ )
		{
			final String tag = mItems[i].getTag().toString();
			final int m = i;
			if ( manager.isTagInside( tag ) )
			{
				new Handler( Looper.getMainLooper() ).post( new Runnable()
				{
					@Override
					public void run()
					{
						MovieInfo info = manager.getInfo( tag );
						if ( info != null )
						{
							// if ( mItems[m] == mItem1 )
							// {
							// initItem( mItems[m], info, "img_v" );
							// }
							// else
							// {
							if ( !TextUtils.isEmpty( info.getImg_h() ) )
							{
								initItem( mItems[m], info, "img_h" );
							}
							else if ( !TextUtils.isEmpty( info.getImg_s() ) )
							{
								initItem( mItems[m], info, "img_s" );
							}
							else if ( !TextUtils.isEmpty( info.getImg_s() ) )
							{
								initItem( mItems[m], info, "img_v" );
							}
							else if ( !TextUtils.isEmpty( info.getImg_h2() ) )
							{
								initItem( mItems[m], info, "img_h2" );
							}

						}
						mItems[m].setAction( info.getAction() );
						mItems[m].setPkg( info.getApp_package_name() );
						mItems[m].setAppId( info.getApp_id() );
						mItems[m].setIsAppAdded( false );
						mItems[m].setCustom( true );// 后台运营
						mItems[m].hideIcon();
						mItems[m].hideTitle();
					}
					// }
				} );
				continue;

			}
			else if ( manager.isAddTagInside( tag ) )
			{
				MovieInfo info = manager.getAddedInfo( tag );

				if ( info != null )
				{
					if ( !ApkHelper.isApkInstalled( context, info.getApp_package_name() ) )// 先检查应用是否存在
					{
						{
							Log.i( TAG, "mItems[m] != mItem1 " );
							Bitmap bmBG = BitmapFactory.decodeResource( mContext.getResources(), mItemBgs[i] );
							Bitmap bmIcon = BitmapFactory.decodeResource( mContext.getResources(),
											R.drawable.icon_add );
							this.setImageLocalData( false, mItems[i], "", "", bmIcon, bmBG );
						}
						Message msg = Message.obtain();
						msg.what = AGAIN_REFRESH;
						msg.arg1 = m;
						msg.obj = info;
						mHandler.sendMessageDelayed( msg, 2000 );
						continue;
					}

					Bitmap bitmap = ApkHelper2.getAppIcon( mContext, info.getApp_package_name() );
					if ( bitmap != null )
					{
						Log.i( TAG, "bitmap != null" );
						Bitmap bmBG = BitmapFactory.decodeResource( mContext.getResources(), mItemBgs[i] );// 本地添加应用就用默认背景
						this.setImageLocalData( true, mItems[i], info.getTitle(), info.getApp_package_name(),
										bitmap, bmBG );

						continue;
					}
				}

			}
			else
			{
				{

					Log.i( TAG, "else mItems[m] != mItem1 " );
					Bitmap bmBG = BitmapFactory.decodeResource( mContext.getResources(), mItemBgs[i] );
					if ( i == 0 || i == 4 )
					{
						continue;
					}
					Bitmap bmIcon = BitmapFactory.decodeResource( mContext.getResources(),
									R.drawable.icon_add );
					this.setImageLocalData( false, mItems[i], "", "", bmIcon, bmBG );
				}
			}
		}

	}

	/**
	 * 开机时加载已经添加的APK
	 * 
	 * @param view
	 * @param title
	 * @param packagename
	 * @param bitmap
	 */
	public void setImageLocalData( final Boolean isAdded, final FcTextView view, final String title,
					final String packagename, final Bitmap iconBitmap, final Bitmap bitmap )
	{
		new Handler( Looper.getMainLooper() ).post( new Runnable()
		{
			@Override
			public void run()
			{
				view.setSrc( bitmap );
				view.setIsAppAdded( isAdded );
				view.setIcon( iconBitmap );
				view.setPkg( packagename );
				view.setTitle( title );
				view.setCustom( false );// 本地添加的为非后台运营
			}
		} );
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
		// sendMsg( mItem9 );
		super.sendMsg();
	}

	private OnClickListener clickListener = new OnClickListener()
	{
		@Override
		public void onClick( View v )
		{
			FcTextView view = ( FcTextView ) v;
			String pkg = ( ( FcTextView ) v ).getPkg();
			String appid = ( ( FcTextView ) v ).getAppId();
			String action = ( ( FcTextView ) v ).getAction();

			Log.i( TAG, "tag:" + view.getTag().toString() );
			Log.i( TAG, "pkg:" + pkg );
			Log.i( TAG, "appid:" + appid );
			Log.i( TAG, "action:" + action );
			Log.i( TAG, "getIsAppAdded:" + view.getIsAppAdded() );

			if ( !mDBManager.isTagInside( v.getTag().toString() ) )
				if ( TextUtils.isEmpty( pkg ) )// 没缓存也没联网
				{
					Log.i( TAG, "onClick1~~~~~~~~~~~~~~" );
					// 应用市场
					if ( v == mItem1 )
					{
						Log.i( TAG, " v == mItem1 ~~~~~~~~~~~" );
						String action1 = "open_app|by_pkg|com.fc.tvmall|";
						FCActionTool.forward( mContext, action1, "99900188", Launcher.APP_KEY_ID );
						return;

					}
					// 我的应用
					else if ( v == mItem5 )
					{
						Log.i( TAG, " v == mItem5 ~~~~~~~~~~~" );
						String action2 = "open_app|by_uri|android.intent.action.VIEW|apps://start||";
						FCActionTool.forward( mContext, action2, "99900188", Launcher.APP_KEY_ID );
						return;

					}

					if ( !view.getIsAppAdded() )
					{
						Log.i( TAG, "is Added...TAG:" + view.getTag() );
						Intent intent = new Intent( mContext, AppsActivity.class );
						intent.putExtra( "tag", v.getTag().toString() );
						intent.putExtra( "package", pkg );
						mContext.startActivity( intent );
					}
					else
					{

						ApkHelper.goAppByPkg( mContext, pkg );
					}

					return;

				}
			if ( !TextUtils.isEmpty( pkg ) )// 如果后台配置了包名
			{
				Log.i( TAG, "onClick2" );

				if ( ApkHelper.isApkInstalled( context, pkg ) )// 先检查应用是否存在
				{
					Log.i( TAG, "onClick3" );

					if ( view.getIsAppAdded() )// 如果是手动添加的本地应用根据包名跳转
					{
						Log.i( TAG, "onClick4" );

						ApkHelper.goAppByPkg( mContext, pkg );
					}
					else
					{
						Log.i( TAG, "onClick5" );

						FCActionTool.forward( mContext, action, "99900188", Launcher.APP_KEY_ID );
					}
				}

				else
				{
					Log.i( TAG, "onClick6" );

					if ( !TextUtils.isEmpty( appid ) )// 未安装的话如果后台配置了appid
					{
						Log.i( TAG, "onClick7" );

						if ( !CommonUtil.isNetworkConnected( mContext ) )
						{
							Log.i( TAG, "onClick8" );

							Toast.makeText( mContext, "网络未连接", Toast.LENGTH_SHORT ).show();
							return;
						}
						Log.i( TAG, "onClick9" );

						// 未安装则跳转到下载界面
						Intent intent = new Intent( context, DetailsActivity.class );
						intent.putExtra( "aid", appid );
						context.startActivity( intent );
					}
					else
					// 如果后台没有配置appid，则直接提示
					{
						if ( v == mItem1 )
						{
							String action1 = "open_app| by_pkg| com.fc.tvmall|";
							FCActionTool.forward( mContext, action1, "99900188", Launcher.APP_KEY_ID );
							return;

						}
						// 我的应用
						else if ( v == mItem5 )
						{
							String action2 = "open_app| by_uri| android.intent.action.VIEW| apps://start| |";
							FCActionTool.forward( mContext, action2, "99900188", Launcher.APP_KEY_ID );
							return;

						}
						// Log.i( TAG, "onClick10" );
						//
						// Toast.makeText( mContext, "应用未安装", Toast.LENGTH_SHORT
						// ).show();
					}
				}
			}
			else
			{
				Log.i( TAG, "onClick11" );

				if ( TextUtils.isEmpty( appid ) )// 如果后台没有配置包名和appid
				{
					Log.i( TAG, "onClick12" );

					FCActionTool.forward( mContext, ( ( FcTextView ) v ).getAction(), "99900188",
									Launcher.APP_KEY_ID );
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

	public void refreshAddedUninstall( DBManager manager, final String pkg )
	{

	}

	public void refreshAddedItem( DBManager manager, final String tag, final String title,
					final String packagename, final Bitmap bitmap )
	{

		for ( int i = 0; i < mItems.length; i++ )
		{
			if ( mItems[i].getTag().toString().equals( tag ) )
			{
				mItems[i].requestFocus();
				currentTAG = tag;
				mItems[i].setIsAppAdded( true );
				mItems[i].setIcon( bitmap );
				mItems[i].setPkg( packagename );
				mItems[i].setTitle( title );

				if ( manager.isAddTagInside( tag ) )
				{
					Log.i( "bbbb", "packagename1:" + packagename );
					manager.updateInfo( tag, title, packagename );
				}
				else
				{
					Log.i( "bbbb", "packagename2:" + packagename );
					manager.insertAddInfo( tag, title, packagename );
				}

			}
		}

	}
}
