package com.launcher;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fc.util.tool.FCActionTool;
import com.launcher.db.DBManager;
import com.launcher.db.info.MovieInfo;
import com.launcher.helper.ApkHelper;
import com.launcher.widget.FcTextView;
import com.launcher.widget.LayoutView;

public class Layout6 extends LayoutView
{
	private FcTextView mItem1, mItem2, mItem3, mItem4, mItem5;

	public Layout6( Context context )
	{
		super( context );
		initView();
	}

	private void initView()
	{
		mItem1 = ( FcTextView ) context.findViewById( R.id.lay6_item1 );
		mItem2 = ( FcTextView ) context.findViewById( R.id.lay6_item2 );
		mItem3 = ( FcTextView ) context.findViewById( R.id.lay6_item3 );
		mItem4 = ( FcTextView ) context.findViewById( R.id.lay6_item4 );
		mItem5 = ( FcTextView ) context.findViewById( R.id.lay6_item5 );

		mItem1.setOnClickListener( clickListener );
		mItem2.setOnClickListener( clickListener );
		mItem3.setOnClickListener( clickListener );
		mItem4.setOnClickListener( clickListener );
		mItem5.setOnClickListener( clickListener );

		mItem1.setOnFocusChangeListener( focusListener );
		mItem2.setOnFocusChangeListener( focusListener );
		mItem3.setOnFocusChangeListener( focusListener );
		mItem4.setOnFocusChangeListener( focusListener );
		mItem5.setOnFocusChangeListener( focusListener );

		mItems = new FcTextView[ 5 ];
		mItems[0] = mItem1;
		mItems[1] = mItem2;
		mItems[2] = mItem3;
		mItems[3] = mItem4;
		mItems[4] = mItem5;
	}

	@Override
	public void refreshInfo( DBManager manager )
	{
		super.refreshInfo( manager );

		// for ( int i = 0; i < mItems.length; i++ )
		// {
		// if ( manager.isTagInside( mItems[i].getTag().toString() ) )
		// {
		// if ( mItems[i] == mItem1 || mItems[i] == mItem2 || mItems[i] ==
		// mItem3 )
		// {
		// initItem( mItems[i], manager.getInfo( mItems[i].getTag().toString()
		// ), "img_v" );
		// }
		// else
		// {
		// initItem( mItems[i], manager.getInfo( mItems[i].getTag().toString()
		// ), "img_h" );
		// }
		// mItems[i].setIcon( 0, 0, 0 );
		//
		// mItems[i].setPkg( manager.getInfo( mItems[i].getTag().toString() )
		// .getApp_package_name() );
		// mItems[i].setAppId( manager.getInfo( mItems[i].getTag().toString()
		// ).getApp_id() );
		// mItems[i].setAction( info.getAction() );
		//
		// }
		// }
		for ( int i = 0; i < mItems.length; i++ )
		{
			String tag = mItems[i].getTag().toString();
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

				// initItem( mItems[i], info, "img_h" );
				mItems[i].setAction( info.getAction() );
				mItems[i].setPkg( info.getApp_package_name() );
				mItems[i].setAppId( info.getApp_id() );
			}
		}
	}

	@Override
	public void refreshBtnApp( List< Map< String, Object >> list )
	{
		super.refreshBtnApp( list );
	}

	private OnClickListener clickListener = new OnClickListener()
	{
		@Override
		public void onClick( View v )
		{

			String pkg = ( ( FcTextView ) v ).getPkg();
			String appid = ( ( FcTextView ) v ).getAppId();
			String action = ( ( FcTextView ) v ).getAction();

			if ( TextUtils.isEmpty( pkg ) )// 没缓存也没联网
			{
				// 默认跳转
				if ( v == mItem1 )
				{
					ApkHelper.goAppByPkg( context, "com.fc.tvmall" );//
					// 应用市场
				}
				// if ( v == mItem2 )
				// {
				// ApkHelper.goAppByAction( context, "sys.settings.program"
				// );// 节目管理
				// }
				// if ( v == mItem3 )
				// {
				// ApkHelper.goAppByAction( context,
				// "com.ejian.action.aphot" );
				// // 无线热点
				// }
				if ( v == mItem4 )
				{
					ApkHelper.goAppByAction( context, "com.ejian.action.weather" );
				}
				if ( v == mItem5 )
				{
					ApkHelper.goAppByPkg( context, "com.fc.setting" );
				}
				return;
			}
			if ( !TextUtils.isEmpty( pkg ) )// 如果后台配置了包名
			{
				if ( ApkHelper.isApkInstalled( context, pkg ) )// 先检查应用是否存在
				{
					FCActionTool.forward( mContext, action, "99900188", Launcher.APP_KEY_ID );
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
	};

	@Override
	public void sendMsg()
	{
		sendMsg( mItem2 );
		sendMsg( mItem1 );
		sendMsg( mItem3 );
		sendMsg( mItem4 );
		sendMsg( mItem5 );
		super.sendMsg();
	}

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

}
