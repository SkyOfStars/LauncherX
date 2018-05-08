package com.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.launcher.util.ApkHelper2;
import com.launcher.util.BitMapManager;
import com.launcher.util.CommonUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class AppsActivity extends Activity
{
	private GridView gridView;
	private AppInfoAdapter adapter;
	private List< AppInfo > mlistAppInfo = new ArrayList< AppInfo >();
	private String tag = "";
	private final static String REPLACEMENT_PKG_PATH = SystemProperties.get( "fc.config.path", "/fc/config/" )
					+ "apps_conf.properties";
	private Properties properties;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.launcher_apps_layout );
		tag = getIntent().getStringExtra( "tag" );
		properties = CommonUtil.getFileContent( REPLACEMENT_PKG_PATH );

		filterAppInfo();// ��ȡӦ���б�

		if ( mlistAppInfo.size() <= 0 )
		{
			findViewById( R.id.none_tip ).setVisibility( View.VISIBLE );
		}
		else
		{
			gridView = ( GridView ) findViewById( R.id.apps_grid_view );
			adapter = new AppInfoAdapter( AppsActivity.this, mlistAppInfo );
			gridView.setAdapter( adapter );
			gridView.setOnItemClickListener( itemListener );
		}

		IntentFilter filter2 = new IntentFilter();
		filter2.addAction( Intent.ACTION_PACKAGE_REMOVED );
		filter2.addAction( Intent.ACTION_PACKAGE_ADDED );
		filter2.addDataScheme( "package" );
		filter2.setPriority( 1000 );
		registerReceiver( mBroadcastReceiver, filter2 );
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive( Context context, Intent intent )
		{
			String action = intent.getAction();
			if ( ( Intent.ACTION_PACKAGE_REMOVED.equals( action ) || Intent.ACTION_PACKAGE_ADDED
							.equals( action ) ) && adapter != null && mlistAppInfo != null )
			{
				filterAppInfo();
				adapter.notifyDataSetChanged();

			}
		}
	};

	@Override
	protected void onDestroy()
	{
		unregisterReceiver( mBroadcastReceiver );
		super.onDestroy();
	}

	private void filterAppInfo()
	{
		if ( mlistAppInfo.size() > 0 )
		{
			mlistAppInfo.clear();
		}
		PackageManager pm = getPackageManager();
		List< ResolveInfo > resolveInfos = ApkHelper2.queryAllResolveInfo( this );
		for ( ResolveInfo reInfo : resolveInfos )
		{
			String pkgName = reInfo.activityInfo.packageName;

			// PackageInfo mPackageInfo =
			// this.getPackageManager().getPackageInfo( pkgName, 0 );
			// if ( ( mPackageInfo.applicationInfo.flags &
			// ApplicationInfo.FLAG_SYSTEM ) <= 0 )// �ж��Ƿ�ϵͳӦ��
			// {
			// AppInfo appInfo = parseAppInfo( reInfo, pm );
			// mlistAppInfo.add( appInfo );
			// }
			if ( properties != null )// �ж�ϵͳ�����ļ��Ƿ��������Ҫ���ص�Ӧ��
			{
				if ( !properties.containsValue( pkgName ) )
				{
					AppInfo appInfo = parseAppInfo( reInfo, pm );
					mlistAppInfo.add( appInfo );
				}
			}

		}
	}

	private AppInfo parseAppInfo( ResolveInfo reInfo, PackageManager pm )
	{
		String activityName = reInfo.activityInfo.name;
		String pkgName = reInfo.activityInfo.packageName;
		String appLabel = ( String ) reInfo.loadLabel( pm );
		Drawable icon = reInfo.loadIcon( pm );

		Intent launchIntent = new Intent();
		launchIntent.setComponent( new ComponentName( pkgName, activityName ) );
		launchIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

		AppInfo appInfo = new AppInfo();
		appInfo.setAppLabel( appLabel );
		appInfo.setPkgName( pkgName );
		appInfo.setAppIcon( icon );
		appInfo.setIntent( launchIntent );

		return appInfo;
	}

	private OnItemClickListener itemListener = new OnItemClickListener()
	{
		@Override
		public void onItemClick( AdapterView< ? > parent, View v, int pos, long id )
		{
			AppInfo appInfo = mlistAppInfo.get( pos );
			Intent intent = new Intent( AppsActivity.this, Launcher.class );
			intent.putExtra( "tag", tag );
			intent.putExtra( "title", appInfo.getAppLabel() );
			intent.putExtra( "package", appInfo.getPkgName() );
			// intent.putExtra( "icon", ( ( BitmapDrawable )
			// appInfo.getAppIcon() ).getBitmap() );
			BitMapManager.getMapManager().setBitMap( ( ( BitmapDrawable ) appInfo.getAppIcon() ).getBitmap() );
			startActivity( intent );
			overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );// ����û�ã�
			finish();

		}
	};

}
