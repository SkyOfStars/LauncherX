package com.launcher.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.launcher.R;

public class NetCheckTask
{
	private static final String TAG = NetCheckTask.class.getSimpleName();

	private static final int TIP_TIME = 5000;

	private static NetCheckTask mInstance;

	private NetCheckTask()
	{

	}

	public static NetCheckTask from()
	{
		if ( mInstance == null )
		{
			mInstance = new NetCheckTask();
		}
		return mInstance;
	}

	/**
	 * CA卡状态<br>
	 * -1: 未设置过状态<br>
	 * 0: 未插卡或无效卡<br>
	 * 1: 已插卡但未授权<br>
	 * 2: 已插卡并授权<br>
	 * 3: 网络未授权(未通过服务器鉴权)
	 */
	private static int mCaCardState = -1;

	/** 上一次CA卡状态 */
	private static int mLastCardState = -1;

	private CaStateListener mListener;

	private long check_duration = 10000;

	private Handler handler = new Handler();

	private Context mContext;

	/**
	 * 启动CA与网络状态检测
	 * 
	 * @param context
	 * @param listener
	 *            检测监听器
	 * @param duration
	 *            检测周期，单位：毫秒，大于0有效，否则默认10秒
	 */
	public void start( Context context, CaStateListener listener, long duration )
	{
		mContext = context;
		mListener = listener;
		if ( duration > 0 )
		{
			check_duration = duration;
		}
		handler.removeCallbacks( checkAction );
		handler.post( checkAction );
	}

	public void stop()
	{
		handler.removeCallbacks( checkAction );
		mCaCardState = -1;
		mLastCardState = -1;
		mListener = null;
		check_duration = 10000;
	}

	private Runnable checkAction = new Runnable()
	{
		@Override
		public void run()
		{
			Log.i( TAG, "checkAction -> running" );
			if ( !netIsConnected() )
			{
				int caCardState = getCaCardState();
				if ( mLastCardState != caCardState )
				{
					if ( caCardState == 0 )
					{
						NetToast.show( mContext, R.string.net_no_card, TIP_TIME );
					}
					else if ( caCardState == 1 )
					{
						NetToast.show( mContext, R.string.card_no_auth, TIP_TIME );
					}
					else if ( caCardState == 2 )
					{
						NetToast.show( mContext, R.string.net_no_connect, TIP_TIME );
					}
					else if ( caCardState == 3 )
					{
						NetToast.show( mContext, R.string.net_no_auth, TIP_TIME );
					}
					mLastCardState = caCardState;
				}

				if ( mListener != null )
				{
					mListener.onResult( caCardState );
				}
			}
			handler.postDelayed( checkAction, check_duration );
		}
	};

	/**
	 * 网络是否连接
	 * 
	 * @return
	 */
	private boolean netIsConnected()
	{
		ConnectivityManager cm = ( ConnectivityManager ) mContext
						.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo info = cm.getActiveNetworkInfo();
		if ( info != null && info.isAvailable() && info.isConnectedOrConnecting() )
		{
			return true;
		}
		return false;
	}

	/**
	 * 获取CA状态<br>
	 * 
	 * @return -1:未设置过状态，0:未插卡或无效卡，1:已插卡但未授权，2:已插卡并授权，3:网络未授权(未通过服务器鉴权)
	 */
	public synchronized int getCaCardState()
	{
		return mCaCardState;
	}

	/**
	 * 设置CA卡状态<br>
	 * 
	 * @param caCardState
	 *            0:未插卡或无效卡，1:已插卡但未授权，2:已插卡并授权，3:网络未授权(未通过服务器鉴权)
	 */
	public synchronized void setCaCardState( int caCardState )
	{
		NetCheckTask.mCaCardState = caCardState;
	}

	public interface CaStateListener
	{
		/**
		 * 监听结果
		 * 
		 * @param state
		 *            CA卡状态<br>
		 *            -1: 未设置过状态<br>
		 *            0: 未插卡或无效卡<br>
		 *            1: 已插卡但未授权<br>
		 *            2: 已插卡并授权<br>
		 *            3: 网络未授权(未通过服务器鉴权)
		 */
		void onResult( int state );
	}

}
