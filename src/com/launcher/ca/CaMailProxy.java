package com.launcher.ca;

import java.util.List;

import com.dvb.common.ctrl.CaOptCtrl;
import com.dvb.common.ctrl.FCDvb;
import com.dvb.common.ctrl.MessageListener;

import android.content.Context;
import android.hidvb.CaMessageInfo;
import android.hidvb.DVBEnum;
import android.util.Log;

/**
 * CaMailProxy<br>
 * 
 * 使用后须调用destroy()释放
 * 
 */
public class CaMailProxy
{
	private static final String TAG = CaMailProxy.class.getSimpleName();

	private static CaMailProxy mInstance;

	private Context mContext;
	private CaOptCtrl mCaOptCtrl;
	private IMailReceiver mailReceiver;

	private CaMailProxy( Context context )
	{
		mContext = context;
		init();
	}

	public synchronized static CaMailProxy from( Context context )
	{
		if ( mInstance == null )
		{
			mInstance = new CaMailProxy( context );
		}
		return mInstance;
	}

	public void setMailReceiver( IMailReceiver receiver )
	{
		mailReceiver = receiver;
	}

	/**
	 * 初始化
	 */
	private void init()
	{
		// 不会有阻塞，不会有卡的通讯
		mCaOptCtrl = FCDvb.getInstance( mContext ).getCaOptCtrl();
		mCaOptCtrl.setLinsten( dvbMsgListener );
	}

	/**
	 * 释放资源并重置状态
	 */
	public void destroy()
	{
		Log.i( TAG, "destroy+++++++++" );

		mCaOptCtrl.removeListen( dvbMsgListener );

		mailReceiver = null;
		mInstance = null;
		mCaOptCtrl = null;
	}

	public List< Integer > getCaMailIdList()
	{
		return mCaOptCtrl.getCaMailIdList();
	}

	private CaMessageInfo getCaMail( int id )
	{
		return mCaOptCtrl.getCaMail( id );
	}

	public int getUnreadMailCount()
	{
		List< Integer > ids = getCaMailIdList();
		if ( ids == null || ids.isEmpty() )
		{
			return 0;
		}

		int count = 0;
		for ( Integer id : ids )
		{
			CaMessageInfo info = getCaMail( id );
			if ( info != null && info.status == DVBEnum.MailStatus.UNREAD )
			{
				count++;
			}
		}
		return count;
	}

	private MessageListener dvbMsgListener = new MessageListener()
	{
		@Override
		public void notify( int what, int arg1, int arg2, Object obj )
		{
			if ( DVBEnum.MessageType.EVENT_CA_MENU.value() == what )
			{
				if ( DVBEnum.CAMenuType.CAMenuTypeMAIL.value() == arg1 )
				{
					// 邮件
					if ( mailReceiver != null )
					{
						mailReceiver.onMailReceive( arg2 );
					}
				}
			}
		}
	};

	public interface IMailReceiver
	{
		void onMailReceive( int mailId );
	}
}
