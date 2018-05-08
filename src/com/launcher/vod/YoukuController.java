package com.launcher.vod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fc.lisence.LisenceInfo;
import com.fc.lisence.LisenceMgr;
import com.fc.util.http.FCHttp;
import com.fc.util.tool.FCTvDevice;
import com.launcher.util.CommonDeviceUtil;
import com.launcher.util.UriUtil;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class YoukuController implements ControllerBase
{
	public static final String TAG = "YoukuController";

	private static final String VOD_ID_MESSAGE = "10006";

	private Context context;

	private LisenceInfo info = null;

	private String server = "", version = "";

	private List< Map< String, String > > mList = new ArrayList< Map< String, String > >();

	private RecommendationCallback mCallback;

	private static class InstanceHolder
	{
		public static YoukuController instance = new YoukuController();
	}

	public static YoukuController getInstance()
	{
		return InstanceHolder.instance;
	}

	private YoukuController()
	{
		Log.i( TAG, "QiyiControler" );
	}

	@Override
	public void init( Context context )
	{
		Log.i( TAG, "init" );
		this.context = context;
		info = LisenceMgr.getLisenceData();
		server = info.getServer();
		version = info.getVersion();
	}

	@Override
	public void release()
	{

	}

	@Override
	public void setRecommendationListener( RecommendationCallback callback )
	{
		mCallback = callback;
	}

	@Override
	public void goRecommendation( int position )
	{
		Log.i( TAG, "goRecommendation position : " + position );
		if ( mList.size() >= position - 1 )
		{
			Map< String, String > map = mList.get( position );
			Log.i( TAG, "goRecommendation PROTOCOL : " + map.get( "protocol" ) );
			Log.i( TAG, "goRecommendation TITLE : " + map.get( "title" ) );
			Log.i( TAG, "goRecommendation VID : " + map.get( "vid" ) );
			Intent intent = new Intent( Intent.ACTION_VIEW );
			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			intent.putExtra( "protocol", map.get( "protocol" ) );
			intent.setData( Uri.parse( "tt_vod://detail?vid=" + map.get( "vid" ) ) );
			context.startActivity( intent );
		}
	}

	@Override
	public void connect()
	{
		Log.i( TAG, "connect" );
		new VodTask().execute( ( Void ) null );
	}

	@Override
	public void goMovie()
	{

	}

	@Override
	public void goTV()
	{

	}

	@Override
	public void goVariety()
	{

	}

	@Override
	public void goCartoon()
	{

	}

	@Override
	public void goHistory()
	{

	}

	@Override
	public void goFavorite()
	{

	}

	private class VodTask extends AsyncTask< Void, Void, String >
	{

		@Override
		protected String doInBackground( Void... params )
		{
			String url = UriUtil.createUrl("api/epg/getVod/Recommend")+"?keyid=" + VOD_ID_MESSAGE
							+ "&version=" + version+"&mac="+FCTvDevice.getEthMacAddress()+"&sn="+CommonDeviceUtil.getDecodedSN()+"&ca="+CommonDeviceUtil.getCa(  );
			
//			String url = "http://api.ottclub.com/api/epg/getVod/Recommend?keyid=" + VOD_ID_MESSAGE
//							+ "&version=" + version;
			return FCHttp.httpForGetMethod( url );
		}

		@SuppressWarnings( "unused" )
		@Override
		protected void onPostExecute( String result )
		{
			if ( result != null && !"".equals( result ) )
			{
				try
				{
					JSONObject object = new JSONObject( new String( result ) );
					if ( object == null )
					{
						return;
					}
					if ( object.length() <= 0 )
					{
						return;
					}
					if ( object.getInt( "ret" ) == 0 )
					{
						JSONArray array = object.getJSONArray( "v" );
						Log.i( TAG, "onPostExecute array : " + array );
						if ( array == null )
						{
							return;
						}

						if ( mList.size() > 0 )
						{
							mList.clear();
						}
						for ( int i = 0; i < array.length(); i++ )
						{
							JSONObject subObject = array.getJSONObject( i );
							Map< String, String > map = new HashMap< String, String >();
							map.put( "vid", subObject.getString( "vid" ) == null ? ""
											: subObject.getString( "vid" ) );
							map.put( "title", subObject.getString( "title" ) == null ? ""
											: subObject.getString( "title" ) );
							map.put( "imgurl", subObject.getString( "imgurl" ) == null ? ""
											: subObject.getString( "imgurl" ) );
							map.put( "protocol", subObject.getString( "protocol" ) == null ? ""
											: subObject.getString( "protocol" ) );
							mList.add( map );
						}

						if ( mCallback != null )
						{
							mCallback.onRecieveList( mList );
						}
						// showVod( array.toString() );

					}
				}
				catch ( JSONException e )
				{
					Log.e( TAG, e.getMessage() );
				}
			}
			super.onPostExecute( result );
		}

	}
}
