package com.launcher.receiver;

import com.launcher.util.NetCheckTask;
import com.launcher.util.NetToast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetCheckReceiver extends BroadcastReceiver
{
	private static final String ACTION_OTTSWITCH = "android.intent.action.OTTSWITCH";

	private static final String ACTION_AUTHEXPIRED = "android.intent.action.AUTHEXPIRED";

	@Override
	public void onReceive( Context context, Intent intent )
	{
		String action = intent.getAction();
		// Log.i( TAG, "action=" + action );
		if ( ACTION_OTTSWITCH.equals( action ) )
		{
			int reason = intent.getIntExtra( "reason", -1 );
			if ( reason == 0 )
			{
				NetCheckTask.from().setCaCardState( 0 );
			}
			else if ( reason == 1 )
			{
				NetCheckTask.from().setCaCardState( 1 );
			}
			else if ( reason == 2 )
			{
				NetCheckTask.from().setCaCardState( 2 );
			}
			else if ( reason == 3 )
			{
				NetCheckTask.from().setCaCardState( 3 );
			}
		}
		else if ( ACTION_AUTHEXPIRED.equals( action ) )
		{
			int days = intent.getIntExtra( "remain_days", 0 );
			NetToast.show( context, String.format( "智能卡还剩%s天到期，请及时续费", days ), 5000 );
		}
	}
}
