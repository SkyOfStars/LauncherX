package com.launcher;

import org.json.JSONException;
import org.json.JSONObject;

import com.fc.lisence.LisenceInfo;
import com.fc.util.http.FCHttp;
import com.fc.util.tool.FCTvDevice;
import com.launcher.download.data.ApkInfo;
import com.launcher.download.data.CloudInstallTask;
import com.launcher.download.data.LogoTask;
import com.launcher.helper.ApkHelper;
import com.launcher.util.CommonDeviceUtil;
import com.launcher.util.UriUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class DetailsActivity extends Activity
{
	private final static String TAG = DetailsActivity.class.getSimpleName();

	private ImageView mIcon, mCtrMode1, mCtrMode2, mCtrMode3;
	private TextView mLabel, mVersion, mSize, mDeveloper, mAppInfo, mButton;
	private RatingBar mRatingBar;
    
	private LinearLayout ll;

	private ProgressBar mProgressBar;

	private CloudInstallTask installTask;

	private ApkInfo info = new ApkInfo();

	private LisenceInfo lInfo = null;
	private String server = "";
	private String version = "";

	private MReceiver mReceiver;
	private boolean isDownload = false;

	private class MReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive( Context context, Intent intent )
		{
			Log.i( "online", "MReceiver" );
			String action = intent.getAction();

			if ( Intent.ACTION_PACKAGE_REMOVED.equals( action )
							|| Intent.ACTION_PACKAGE_ADDED.equals( action ) )
			{
				// mProgressBar.setProgress( 0 );
				Log.i( "online", "MReceiver add" );
				if ( ApkHelper.isApkInstalled( DetailsActivity.this, info.mPackage ) )
				{
					Log.i( "online", "MReceiver isApkInstalled" );
					try
					{
						int versionCode = DetailsActivity.this.getPackageManager().getPackageInfo(
										info.mPackage, 0 ).versionCode;
						if ( versionCode < info.mVerCode )
						{
							Log.i( "online", "MReceiver isApkInstalled update" );
							mButton.setText( getString( R.string.update ) );
						}
						else
						{
							Log.i( "online", "MReceiver isApkInstalled open" );
							mButton.setText( getString( R.string.open ) );
							isDownload = false;
						}
					}
					catch ( Exception e )
					{
						Log.e( TAG, e.getMessage() );
						mButton.setText( getString( R.string.download ) );
					}
				}
				else
				{
					mButton.setText( getString( R.string.download ) );
				}
			}
		}
	}

	@Override
	protected void onCreate( Bundle arg0 )
	{
		super.onCreate( arg0 );
		setFinishOnTouchOutside( false );
		setContentView( R.layout.activity_details );

		server = CommonUtil.getServer();
		version = CommonUtil.getVersion();

		initView();

		mReceiver = new MReceiver();
		IntentFilter msgFilter = new IntentFilter();
		msgFilter.addAction( Intent.ACTION_PACKAGE_REMOVED );
		msgFilter.addAction( Intent.ACTION_PACKAGE_ADDED );
		msgFilter.addDataScheme( "package" );
		this.registerReceiver( mReceiver, msgFilter );

	}

	private void initView()
	{
		mIcon = ( ImageView ) findViewById( R.id.details_icon );
		mLabel = ( TextView ) findViewById( R.id.details_label );
		mRatingBar = ( RatingBar ) findViewById( R.id.details_rating_bar );
		mVersion = ( TextView ) findViewById( R.id.details_version );
		mSize = ( TextView ) findViewById( R.id.details_size );
		mDeveloper = ( TextView ) findViewById( R.id.details_developer );
		mAppInfo = ( TextView ) findViewById( R.id.details_appinfo );

		mCtrMode1 = ( ImageView ) findViewById( R.id.details_contorl_mode1 );
		mCtrMode2 = ( ImageView ) findViewById( R.id.details_contorl_mode2 );
		mCtrMode3 = ( ImageView ) findViewById( R.id.details_contorl_mode3 );

		mProgressBar = ( ProgressBar ) findViewById( R.id.details_progress );
		mButton = ( TextView ) findViewById( R.id.details_button );
	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	private DetailsTask mDetailsTask;

	@Override
	protected void onResume()
	{
		super.onResume();
		Intent intent = getIntent();
		String aId = intent.getStringExtra( "aid" ) == null ? "0" : intent.getStringExtra( "aid" );

		mDetailsTask = ( DetailsTask ) new DetailsTask().execute( aId );
		super.onResume();
	}

	@Override
	protected void onStop()
	{
		// unregisterReceiver( mReceiver );
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		if ( mDetailsTask != null )
		{
			if ( !mDetailsTask.isCancelled() )
			{
				mDetailsTask.cancel( true );
			}
		}
		unregisterReceiver( mReceiver );
		super.onDestroy();
	}

	private class DetailsTask extends AsyncTask< String, Void, String >
	{
		@Override
		protected String doInBackground( String... params )
		{
//			String url = server + "/api/apps/GetDetail?aid=" + params[0] + "&version=" + version;
			String url = UriUtil.createUrl( "api/apps/GetDetail" ) + "?aid=" + params[0] + "&version=" + version+"&mac="+FCTvDevice.getEthMacAddress()+"&sn="+CommonDeviceUtil.getDecodedSN()+"&ca="+CommonDeviceUtil.getCa(  );
			return FCHttp.httpForGetMethod( url );
		}

		@Override
		protected void onPostExecute( String result )
		{
			if ( result != null && !"".equals( result ) )
			{
				try
				{
					JSONObject object = new JSONObject( new String( result ) );
					if ( object.length() <= 0 )
					{
						return;
					}

					info.mId = object.getString( "id" ) == null ? "0" : object.getString( "id" );
					info.mIcon = object.getString( "thumb" ) == null ? "0" : object.getString( "thumb" );
					info.mTitle = object.getString( "title" ) == null ? "" : object.getString( "title" );
					String size = object.getString( "size" ) == null ? "1000000" : object.getString( "size" );
					info.mSize = Long.parseLong( size );
					info.mDeveloper = object.getString( "developer" ) == null ? "" : object
									.getString( "developer" );
					String verCode = object.getString( "version_code" ) == null ? "0" : object
									.getString( "version_code" );
					info.mVerCode = Integer.parseInt( verCode );
					info.mVersion = object.getString( "version" ) == null ? "" : object.getString( "version" );
					info.mPackage = object.getString( "package" ) == null ? "" : object.getString( "package" );
					info.mDetails = object.getString( "info" ) == null ? "" : object.getString( "info" );
					String mark = object.getString( "mark" ) == null ? "0" : object.getString( "mark" );
					info.mMark = Float.parseFloat( mark );
					info.mDownUrl = object.getString( "path" ) == null ? "" : object.getString( "path" );
					info.mContorlMode = object.getString( "contorlmode" ) == null ? "" : object
									.getString( "contorlmode" );
					initData();
				}
				catch ( JSONException e )
				{
					Log.e( TAG, e.getMessage() );
				}
			}
			super.onPostExecute( result );
		}
	}

	private void initData()
	{
		new LogoTask( this, mIcon ).execute( info.mIcon );
		mLabel.setText( info.mTitle );

		mRatingBar.setRating( info.mMark );
		if ( info.mVersion.indexOf( "v" ) >= 0 || info.mVersion.indexOf( "V" ) >= 0 )
		{
			mVersion.setText( getString( R.string.app_version ) + info.mVersion );
		}
		else
		{
			mVersion.setText( getString( R.string.app_version ) + "V" + info.mVersion );
		}
		mSize.setText( getString( R.string.app_size ) + Formatter.formatFileSize( this, info.mSize ) );
		mDeveloper.setText( getString( R.string.app_developer ) + info.mDeveloper );
		mAppInfo.setText( info.mDetails );

		String[] ctrModes = info.mContorlMode.split( "," );
		for ( String ctrMode : ctrModes )
		{
			if ( ctrMode.equals( "1" ) )
			{
				mCtrMode1.setVisibility( View.VISIBLE );
			}
			else if ( ctrMode.equals( "2" ) )
			{
				mCtrMode2.setVisibility( View.VISIBLE );
			}
			else if ( ctrMode.equals( "3" ) )
			{
				mCtrMode3.setVisibility( View.VISIBLE );
			}
			else
			{
				mCtrMode1.setVisibility( View.VISIBLE );
			}
		}

		if ( ApkHelper.isApkInstalled( this, info.mPackage ) )
		{
			try
			{
				int versionCode = this.getPackageManager().getPackageInfo( info.mPackage, 0 ).versionCode;
				if ( versionCode < info.mVerCode )
				{
					mButton.setText( getString( R.string.update ) );
				}
				else
				{
					mButton.setText( getString( R.string.open ) );
				}
			}
			catch ( Exception e )
			{
				Log.e( TAG, e.getMessage() );
				mButton.setText( getString( R.string.download ) );
			}
		}
		else
		{
			mButton.setText( getString( R.string.download ) );
		}
	}

	public void btnClick( View v )
	{
		if ( mButton.getText() != null )
		{
			if ( mButton.getText().toString().equals( getString( R.string.open ) ) )
			{
				if ( ApkHelper.isApkInstalled( this, info.mPackage ) )
				{
					ApkHelper.goAppByPkg( this, info.mPackage );
					finish();
				}
			}
			else
			{
				if ( !isDownload && info.mDownUrl != null && !info.mDownUrl.equals( "" ) )
				{
					isDownload = true;
					installTask = new CloudInstallTask( mCloudCallBack, this, info, true );
					installTask.doTask();
					mButton.setBackgroundResource( 0 );
				}
			}
		}
	}

	private CloudInstallTask.CallBack mCloudCallBack = new CloudInstallTask.CallBack()
	{

		@Override
		public void onPreInstalling( long size )
		{

		}

		@Override
		public void onProgressInstalling( int percent )
		{
			if ( percent <= 100 )
			{
				mButton.setText( getString( R.string.download ) + percent + "%" );
				mProgressBar.setProgress( percent );
			}
		}

		@Override
		public void onPostInstalling( ApkInfo software, boolean result )
		{
			isDownload = false;
			mButton.setText( getString( R.string.download_finish ) );
			mButton.setBackgroundResource( R.drawable.progress_seek );
		}

		@Override
		public void onInstall()
		{
			runOnUiThread( new Runnable()
			{
				@Override
				public void run()
				{
					mButton.setText( getString( R.string.install ) );
				}
			} );
		}

		@Override
		public void onInstallError()
		{
			Log.d( TAG, "Test" );

			runOnUiThread( new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText( DetailsActivity.this, getString( R.string.install_error ),
									Toast.LENGTH_SHORT ).show();
					finish();
				}
			} );
		}
	};

	@Override
	public boolean onKeyDown( int keyCode, KeyEvent event )
	{
		if ( keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME )
		{
			if ( isDownload )
			{
				Toast.makeText( DetailsActivity.this, getString( R.string.download_tip ), Toast.LENGTH_SHORT )
								.show();
				return true;
			}
		}
		return super.onKeyDown( keyCode, event );
	}

}
