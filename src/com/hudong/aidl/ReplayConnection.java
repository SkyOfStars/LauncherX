package com.hudong.aidl;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ReplayConnection
{
	private static final String TAG = "ReplayConnection";

	private static final String ACTION_BIND_SERVICE = "com.hudong.action.aidl.REPLAY";

	private static ReplayConnection instance;

	private IRePlayProperties mRePlayProperties;

	private boolean isServiceExist = false;

	private ReplayConnection()
	{

	}

	public static ReplayConnection getInstance()
	{
		if ( instance == null )
		{
			instance = new ReplayConnection();
		}

		return instance;
	}

	public void init( Context context )
	{
		if ( isServiceExist )
		{
			return;
		}

		Intent intent = new Intent( ACTION_BIND_SERVICE );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		isServiceExist = context.bindService( intent, mConnection, Context.BIND_AUTO_CREATE );
		Log.i( TAG, "init isServiceExist : " + isServiceExist );
	}

	public List< RePlayProgEntity > getConfig( String startTime,String endTime, String curChanId )
	{
		Log.i( "test", "mRePlayProperties ==" +mRePlayProperties );
		if ( mRePlayProperties == null )
		{
			return null;
		}

		try
		{
			List< RePlayProgEntity > entitys = mRePlayProperties.getReplayPrograms( startTime, endTime,curChanId );
			if ( entitys == null )
			{
				return null;
			}
			else
			{
				Log.i( TAG, "getConfig entitys : " + entitys.size() );
				return entitys;
			}

		}
		catch ( RemoteException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public boolean getTimeShiftList( boolean isFromOutside )
	{
		Log.i( TAG, "getTimeShiftList" );

		if ( mRePlayProperties == null )
		{
			return false;
		}

		try
		{
			return mRePlayProperties.getTimeShiftList( isFromOutside );

		}
		catch ( RemoteException e )
		{
			e.printStackTrace();
			return false;
		}
	}

	public boolean getReplayList( boolean isFromOutside )
	{
		Log.i( TAG, "getReplayList" );

		if ( mRePlayProperties == null )
		{
			Log.i( TAG, "getReplayList mRePlayProperties == null" );
			return false;
		}

		try
		{
			Log.i( TAG, "getReplayList mRePlayProperties != null" );
			return mRePlayProperties.getReplayList( isFromOutside );
		}
		catch ( RemoteException e )
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public List<MenuEntity> getReplayList()
	{
		Log.i( TAG, "getReplayList" );

		if ( mRePlayProperties == null )
		{
			Log.i( TAG, "getReplayList mRePlayProperties == null" );
			return null;
		}
		try
		{
			Log.i( TAG, "getReplayList mRePlayProperties != null" );
			return mRePlayProperties.getMenuList();
		}
		catch ( RemoteException e )
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public List<RecommendEntity> getRecommendList()
	{
		Log.i( TAG, "getReplayList" );

		if ( mRePlayProperties == null )
		{
			Log.i( TAG, "getReplayList mRePlayProperties == null" );
			return null;
		}
		try
		{
			Log.i( TAG, "getReplayList mRePlayProperties != null" );
			return mRePlayProperties.getRecommendList();
		}
		catch ( RemoteException e )
		{
			e.printStackTrace();
			return null;
		}
	}
	

	private ServiceConnection mConnection = new ServiceConnection()
	{
		@Override
		public void onServiceDisconnected( ComponentName name )
		{
			Log.i( TAG, "onServiceDisconnected" );
			mRePlayProperties = null;
		}

		@Override
		public void onServiceConnected( ComponentName name, IBinder service )
		{
			mRePlayProperties = IRePlayProperties.Stub.asInterface( service );
			Log.i( TAG, "onServiceConnected mRePlayProperties : " + mRePlayProperties );
		}
	};
}
