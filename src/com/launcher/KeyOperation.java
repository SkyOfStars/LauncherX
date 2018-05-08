package com.launcher;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import com.fc.util.tool.FCActionTool;
import com.launcher.helper.ApkHelper;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

public class KeyOperation
{
	private static String TAG = "KeyOperation";
	private static long mLastDmgClickTime = 0;
	private static StringBuffer mDmgBuilder = new StringBuffer();

	private KeyOperation()
	{
	}

	public static void doAction( Context context, int keyCode, Map< String, Object > map )
	{

		Log.e( TAG, "" + map );

		if ( ( SystemClock.uptimeMillis() - mLastDmgClickTime ) > 1000 )
		{
			mDmgBuilder.delete( 0, mDmgBuilder.length() );
			mDmgBuilder.append( getKeyValue( keyCode ) );
			mLastDmgClickTime = SystemClock.uptimeMillis();
		}
		else
		{
			mLastDmgClickTime = SystemClock.uptimeMillis();
			mDmgBuilder.append( getKeyValue( keyCode ) );
		}

		JSONArray keySet = ( JSONArray ) map.get( "key" );
		JSONArray keyAction = ( JSONArray ) map.get( "action" );
		if ( keySet == null || keyAction == null )
		{
			// 网络数据为空的情况下，只支持333调起产品检测工具
			if ( "333".equals( mDmgBuilder.toString() ) )
			{
				if ( ApkHelper.isApkInstalled( context, "com.fc.dmg" ) )
				{
					Intent intent = new Intent( "com.dmg" );
					intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
					context.startActivity( intent );
				}
			}
		}
		else
		{
			for ( int i = 0; i < keySet.length(); i++ )
			{
				try
				{
					String key = keySet.getString( i );
					String action = keyAction.getString( i );

					if ( key != null && !key.equals( "" ) || action != null && !action.equals( "" ) )
					{
						if ( mDmgBuilder.toString().equals( key ) )
						{
							FCActionTool.forward( context, action, "99900188", "17" );
						}
					}
				}
				catch ( JSONException e )
				{
					Log.e( TAG, e.getMessage() );
				}
			}
		}
	}

	private static String getKeyValue( int keyCode )
	{
		String keyValue = "";
		switch( keyCode )
		{
			case KeyEvent.KEYCODE_0:
				keyValue = "0";
				break;
			case KeyEvent.KEYCODE_1:
				keyValue = "1";
				break;
			case KeyEvent.KEYCODE_2:
				keyValue = "2";
				break;
			case KeyEvent.KEYCODE_3:
				keyValue = "3";
				break;
			case KeyEvent.KEYCODE_4:
				keyValue = "4";
				break;
			case KeyEvent.KEYCODE_5:
				keyValue = "5";
				break;
			case KeyEvent.KEYCODE_6:
				keyValue = "6";
				break;
			case KeyEvent.KEYCODE_7:
				keyValue = "7";
				break;
			case KeyEvent.KEYCODE_8:
				keyValue = "8";
				break;
			case KeyEvent.KEYCODE_9:
				keyValue = "9";
				break;
		}
		return keyValue;
	}
}
