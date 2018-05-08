package com.launcher.receiver;

import java.lang.reflect.Method;

import com.fc.util.tool.FCTvDevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.pppoe.PppoeManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NetStateReceiver extends BroadcastReceiver
{
	Context context;
	Handler handler;
	private ConnectivityManager cm;

	public NetStateReceiver( Context context, Handler handler )
	{
		this.context = context;
		this.handler = handler;
		cm = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
		getStrength( context );
	}

	@Override
	public void onReceive( Context context, Intent intent )
	{
		String action = intent.getAction();

		int tmp = -1;
		NetworkInfo wifiInfo = cm.getNetworkInfo( ConnectivityManager.TYPE_WIFI );
		if ( wifiInfo != null && wifiInfo.isConnected() )
		{
			tmp = 0;
		}

		NetworkInfo ethInfo = cm.getNetworkInfo( ConnectivityManager.TYPE_ETHERNET );
		if ( ethInfo != null && ethInfo.isConnected() )
		{
			tmp = 1;
		}
		//512
		if ( android.os.Build.VERSION.RELEASE.startsWith( "4.4" )
						&& FCTvDevice.getDeviceVersion().contains( "HBV" ) )
		{
			PppoeManager pppoeManager = ( PppoeManager ) context
							.getSystemService( Context.PPPOE_SERVICE );
			boolean r = false;
			try
			{
				Method method = NetStateReceiver.getMethodByName( "android.net.pppoe.PppoeManager",
								"getPppoeConnectStatus" );
				r = ( Boolean ) method.invoke( pppoeManager );
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
			if ( r )
			{
				tmp = 1;
			}

		}

		if ( action.equals( ConnectivityManager.CONNECTIVITY_ACTION ) )
		{
			NetworkInfo info = ( NetworkInfo ) intent.getParcelableExtra( "networkInfo" );
			State state = info.getState();
			if ( info.getType() == ConnectivityManager.TYPE_PPPOE )
			{
				if ( state == State.CONNECTED )
				{
					tmp = 1;
				}
			}
		}

		switch( tmp )
		{
			case 0:
				int strength = getStrength( context );// 当前网络信号强度
				Log.i( "WifiStateReceiver", "strength=" + strength );
				Message msg = handler.obtainMessage( 5, 0, 0, strength );
				handler.sendMessage( msg );
				break;
			case 1:
				handler.sendEmptyMessage( 4 );
				break;
			case -1:
				handler.sendEmptyMessage( 6 );
				break;
		}

	}
	
	public static Method getMethodByName( String className, String methodName )
	{
		try
		{
			Method[] allMethod = Class.forName( className ).getMethods();
			for ( Method m : allMethod )
			{
				if ( m.getName().equals( methodName ) )
				{
					return m;
				}
			}
			return null;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		return null;
	}
	

	private int getStrength( Context context )
	{
		WifiManager wifiManager = ( WifiManager ) context.getSystemService( Context.WIFI_SERVICE );
		WifiInfo info = wifiManager.getConnectionInfo();
		if ( info.getBSSID() != null )
		{
			int strength = WifiManager.calculateSignalLevel( info.getRssi(), 2 ) + 1;
			return strength;
		}
		return 0;
	}

}
