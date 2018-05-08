package com.launcher;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import com.fc.util.http.FCHttp;
import com.fc.util.tool.FCTvDevice;
import com.launcher.LauncherMainModel.ICallBackAdSwitcher;
import com.launcher.util.CommonDeviceUtil;
import com.launcher.util.UriUtil;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

public class AdSwitcherTask extends AsyncTask< Void, Void, String >
{
	private static final String TAG = "AdSwitcherTask";
	// private List< String > strsList = new ArrayList< String >();
	private ICallBackAdSwitcher l;
	private String updateTime;
	private String last_update;

	public final String URL = "http://api.ottclub.com/api";

//	public String GET_AD = URL
//					+ "/ad/getAdInfo?adid=%s&picsize=%s&version=%s&timestamp=%s&nocache=%s";

	public String GET_AD = UriUtil.createUrl( "api/ad/getAdInfo" )
					+ "?adid=%s&picsize=%s&version=%s&timestamp=%s"+"&mac="+FCTvDevice.getEthMacAddress()+"&sn="+CommonDeviceUtil.getDecodedSN()+"&ca="+CommonDeviceUtil.getCa(  );

	public AdSwitcherTask( String updateTime, ICallBackAdSwitcher l )
	{
		this.l = l;
		this.updateTime = updateTime;
	}

	@Override
	protected String doInBackground( Void... arg0 )
	{
		String url = "";

		try
		{
			url = String.format( GET_AD, "109", "1280x720", CommonDeviceUtil.VERSION,
							URLEncoder.encode( updateTime, "UTF-8" ));
			Log.d( TAG, url );
		}
		catch ( UnsupportedEncodingException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i( TAG, "url:" + url );
		return FCHttp.httpForGetMethod( url );
	}

	@Override
	protected void onPostExecute( String result )
	{
		Log.i( TAG, "result:" + result );

		JSONObject object = null;
		try
		{
			object = new JSONObject( result );
			Log.e( TAG, "ret :" + object.getInt( "ret" ) );
			if ( object.getInt( "ret" ) < 0 )
			{
				if ( TextUtils.isEmpty( object.getString( "timestamp" ) ) )
				{
					// 网络报错时，jar包会回调-1，且没有时间
				}
				else
				{
					l.finishSaveDate( result );
					String last_update=object.getString( "timestamp" ) == null ? "1900-01-01 00:00:00" : object
									.getString( "timestamp" );
					l.finishUpDateTime( last_update );
				}

				l.finish();
				return;
			}
			else if ( object.getInt( "ret" ) > 0 )
			{
				return;
			}

			last_update = object.getString( "timestamp" ) == null ? "1900-01-01 00:00:00" : object
							.getString( "timestamp" );
			Log.i( TAG, "last_update : " + last_update );
			Log.i( TAG, "updateTime : " + updateTime );
			if ( last_update.equals( updateTime ) )// 如果不更新则不刷新
			{
				return;
			}

			l.finishSaveDate( result );
			l.finishUpDateTime( last_update );
			l.finish();
		}

		catch ( JSONException e )
		{
			e.printStackTrace();
		}

		super.onPostExecute( result );

	}
}
