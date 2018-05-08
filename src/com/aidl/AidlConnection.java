package com.aidl;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Niuniu
 * 
 *         AIDL连接管理器，在设备启动时初始化
 */
public class AidlConnection
{
	private static class InstanceHolder
	{
		public static AidlConnection instance = new AidlConnection();
	}

	public static AidlConnection getInstance()
	{
		return InstanceHolder.instance;
	}

	private AidlConnection()
	{

	}

	private static String TAG = "AidlConnection";

	private static final String ACTION_BIND_SERVICE = "com.mware.action.aidl.property";

	private IProperties mSysPropService;

	private List< AidlListenner > aidlListenners = new ArrayList< AidlListenner >();

	private boolean isServiceExist = false;

	public void init( Context context )
	{
		Intent intent = new Intent( ACTION_BIND_SERVICE );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		isServiceExist = context.bindService( intent, mConnection, Context.BIND_AUTO_CREATE );
	}

	public IProperties getProperties()
	{
		return mSysPropService;
	}

	public String getConfig( String key, String defaultValue )
	{
		Log.i( TAG, "getConfig key : " + key );
		if ( mSysPropService == null )
		{
			return defaultValue;
		}

		try
		{
			String value = mSysPropService.getConfig( key );
			Log.i( TAG, "getConfig value : " + value );
			if ( TextUtils.isEmpty( value ) )
			{
				return defaultValue;
			}
			else
			{
				return value;
			}

		}
		catch ( RemoteException e )
		{
			e.printStackTrace();
			Log.i( TAG, "getConfig error" );
			return defaultValue;
		}
	}

	public void setConfig( String key, String value )
	{
		if ( mSysPropService == null )
		{
			return;
		}

		try
		{
			mSysPropService.setConfig( key, value );
		}
		catch ( RemoteException e )
		{
			e.printStackTrace();
		}
	}

	public void setAidlListenner( AidlListenner listenner )
	{
		if ( listenner != null )
		{
			if ( !isServiceExist )
			{
				listenner.onConnected();
			}

			if ( mSysPropService != null )
			{
				listenner.onConnected();
			}
			else
			{
				aidlListenners.add( listenner );
			}

		}
	}

	public interface AidlListenner
	{
		void onConnected();
	}

	private ServiceConnection mConnection = new ServiceConnection()
	{
		@Override
		public void onServiceDisconnected( ComponentName name )
		{
			mSysPropService = null;
		}

		@Override
		public void onServiceConnected( ComponentName name, IBinder service )
		{
			mSysPropService = IProperties.Stub.asInterface( service );

			for ( AidlListenner listenner : aidlListenners )
			{
				listenner.onConnected();
			}
			aidlListenners.removeAll( aidlListenners );

		}
	};
}
