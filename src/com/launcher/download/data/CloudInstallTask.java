package com.launcher.download.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

public class CloudInstallTask
{
	private static final String TAG = "CloudInstallTask";
	private static final int BUFFER_SIZE = 1024 * 64;
	private static final String DOWNLOAD_DIR = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/apps/";
	private boolean isBackgroundInstall = false;

	public static interface CallBack
	{
		void onPreInstalling( long size );

		void onProgressInstalling( int percent );

		void onPostInstalling( ApkInfo software, boolean result );

		void onInstall();
		
		void onInstallError();
	}

	private CallBack mCallback;
	private ApkInfo mSoftware;
	private Context mContext;

	public CloudInstallTask( CallBack callBack, Context context, ApkInfo software,
					boolean isBackgroundInstall )
	{
		mCallback = callBack;
		mSoftware = software;
		mContext = context;
		this.isBackgroundInstall = isBackgroundInstall;
	}

	public void doTask()
	{
		new DoTask().execute();
	}

	class DoTask extends AsyncTask< Void, Long, Boolean >
	{
		private long mTotalSize;

		public DoTask()
		{
			mTotalSize = Long.valueOf( mSoftware.mSize );
		}

		@Override
		protected void onPreExecute()
		{
			if ( mCallback != null )
			{
				mCallback.onPreInstalling( mTotalSize );
			}
		}

		@Override
		protected void onProgressUpdate( Long... values )
		{
			long v = values[0];
			int percent = v == mTotalSize ? 100 : ( int ) ( v * 100 / mTotalSize );
			if ( mCallback != null )
			{
				mCallback.onProgressInstalling( percent );
			}
		}

		@Override
		protected Boolean doInBackground( Void... arg0 )
		{
			File dir = new File( DOWNLOAD_DIR );
			if ( !dir.exists() )
			{
				dir.mkdirs();
			}
			String path = mSoftware.mDownUrl;
			File file = new File( path );
			String name = file.getName();

			String filePath = DOWNLOAD_DIR + name;
			file = new File( filePath );
			file.deleteOnExit();

			long readLen = 0;
			InputStream inputStream = null;
			HttpURLConnection http = null;
			RandomAccessFile random = null;
			try
			{
				http = httpBP( path, 0 );
				int code = http.getResponseCode();
				if ( code == 206 || code == HttpStatus.SC_OK )
				{
					Log.i( TAG, "write starting..." );
					Log.i( TAG, "ContentLength=[" + http.getContentLength() );
					inputStream = http.getInputStream();

					random = new RandomAccessFile( file, "rwd" );
					random.seek( 0 );
					byte[] buffer = new byte[ BUFFER_SIZE ];
					int count = -1;
					while ( ( count = inputStream.read( buffer ) ) != -1 )
					{
						readLen += count;
						random.write( buffer, 0, count );
						publishProgress( readLen );
					}
				}
				else
				{
					Log.e( TAG, "code=[" + code );
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
			finally
			{
				if ( http != null )
				{
					http.disconnect();
				}

				if ( random != null )
				{
					try
					{
						random.close();
					}
					catch ( IOException e )
					{
						e.printStackTrace();
					}
				}

				if ( inputStream != null )
				{
					try
					{
						inputStream.close();
					}
					catch ( IOException e )
					{
						e.printStackTrace();
					}
				}
			}

			if ( readLen == mTotalSize )
			{
				Log.i( TAG, "write ok" );
				long startTime = SystemClock.uptimeMillis();

				while ( SystemClock.uptimeMillis() - startTime < 300 )
				{
					try
					{
						Thread.sleep( 100 );
					}
					catch ( InterruptedException e )
					{
						e.printStackTrace();
					}
				}
				if ( filePath != null )
				{
					return apkInstall( filePath );
				}
			}
			else
			{
				Log.e( TAG, "write error" );
			}
			return false;
		}

		@Override
		protected void onPostExecute( Boolean result )
		{
			if ( mCallback != null )
			{
				if ( isBackgroundInstall )
				{

				}
				else
				{
					mCallback.onPostInstalling( mSoftware, result );
					Log.i( "online", "onPostExecute result : " + result );
				}
			}
		}

		private boolean apkInstall( String strApkFilePath )
		{
			if ( isBackgroundInstall )
			{
				mCallback.onInstall();
				PackageInstallObserver obs = new PackageInstallObserver();
				PackageManager packageManager = mContext.getPackageManager();
				// 获取apk的packageName
				String strPackageName = null;
				PackageInfo info = packageManager.getPackageArchiveInfo( strApkFilePath,
								PackageManager.GET_ACTIVITIES );
				if ( null != info )
				{
					ApplicationInfo appInfo = info.applicationInfo;
					strPackageName = appInfo.packageName;
				}
				else
				{
					return false;
				}
				int installFlags = 0;
				installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
				// Log.i( TAG, "要安装的包名：" + strPackageName );
				// Log.i( TAG, "apk文件路径：" + strApkFilePath );
				try
				{
					packageManager.installPackage( Uri.fromFile( new File( strApkFilePath ) ), obs,
									installFlags, strPackageName );
					synchronized ( obs )
					{
						while ( !obs.finished )
						{
							try
							{
								obs.wait();
							}
							catch ( InterruptedException e )
							{
							}
						}
						if ( obs.result == PackageManager.INSTALL_SUCCEEDED )
						{
							Log.i( TAG, "install Success" );
							return true;
						}
						else
						{
							Log.e( TAG, "install  Failure" );
							Log.e( TAG, "Test" );
							mCallback.onInstallError();
							return false;
						}
					}
				}
				catch ( Exception e )
				{
					e.printStackTrace();
					return false;
				}
				
				
			}
			else
			{
				Uri uri = Uri.fromFile( new File( strApkFilePath ) );
				Intent intent = new Intent( Intent.ACTION_VIEW );
				intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				intent.setDataAndType( uri, "application/vnd.android.package-archive" );
				mContext.startActivity( intent );
				return true;
			}
		}

		public HttpURLConnection httpBP( String url, long off )
						throws MalformedURLException, IOException
		{
			HttpURLConnection http = ( HttpURLConnection ) new URL( url ).openConnection();
			http.setConnectTimeout( 5 * 1000 );
			http.setReadTimeout( 5 * 1000 );
			http.setRequestMethod( "GET" );
			http.setRequestProperty( "Accept",
							"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*" );
			http.setRequestProperty( "Accept-Language", "zh-CN" );
			// http.setRequestProperty("Referer", url);
			http.setRequestProperty( "Charset", "UTF-8" );
			http.setRequestProperty( "Range", "bytes=" + off + "-" );// 设置获取实体数据的范围
			http.setRequestProperty( "User-Agent",
							"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)" );
			http.setRequestProperty( "Connection", "Keep-Alive" );

			return http;
		}

		class PackageInstallObserver extends IPackageInstallObserver.Stub
		{
			boolean finished;
			int result;

			public void packageInstalled( String name, int status )
			{
				synchronized ( this )
				{
					finished = true;
					result = status;
					notifyAll();
				}
			}
		}
	}
}
