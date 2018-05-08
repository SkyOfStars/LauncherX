package com.launcher.manager;

import com.aidl.AidlConnection;
import com.aidl.AidlConnection.AidlListenner;
import com.dvb.common.ctrl.ADControl;
import com.dvb.common.ctrl.FCDvb;

import android.content.Context;
import android.hidvb.ADPICBase;
import android.hidvb.DVBEnum;
import android.hidvb.DVBEnum.AD_Type;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class BootAdResourceLoad
{
	// private static final String TAG =
	// BootAdResourceLoad.class.getSimpleName();
	private static final String TAG = "MODE";
	private ADControl mAdCtrl;

	private BootAdResourceLoad()
	{

	}

	private static class InstanceHolder
	{
		public static BootAdResourceLoad instance = new BootAdResourceLoad();
	}

	public static BootAdResourceLoad getInstance()
	{
		return InstanceHolder.instance;
	}

	public BootAdResourceLoad loadBootAdResource( Context context, Handler handler )
	{
		Log.i( TAG, "---init----" );
		mAdCtrl = FCDvb.getInstance( context ).getADCtrl();
		getConfigParam( context, handler );
		return this;
	}

	/**
	 * 设置广告类型, 需要按照xeme.conf 中配置来设置
	 */
	private void getConfigParam( final Context context, final Handler handler )
	{
		AidlConnection.getInstance().setAidlListenner( new AidlListenner()
		{
			@Override
			public void onConnected()
			{
				String isSupportAd = AidlConnection.getInstance().getConfig( "support_ad", "0" );
				Log.i( TAG, "---isSupportAd:" + isSupportAd );
				if ( !TextUtils.isEmpty( isSupportAd ) )
				{
					if ( isSupportAd.equals( "1" ) )
					{
						mAdCtrl.setADFactory( DVBEnum.AD_Factory.FAC_MAIKE );// 司马浦广告设为迈克广告类型

						ADPICBase base = ( ADPICBase ) mAdCtrl.getADData( AD_Type.AD_LOGO, -1, -1,
										-1 );

						Log.i( TAG, "---loadBootAdResource---base:" + base );
						Message msg = Message.obtain();
						msg.obj = base;
						msg.what = 8;
						handler.sendMessage( msg );

					}
					else
					{
						handler.sendEmptyMessage( 8 );
					}

				}

			}
		} );
	}

}
