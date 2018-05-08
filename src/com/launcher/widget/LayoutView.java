package com.launcher.widget;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.launcher.Launcher;
import com.launcher.TVListView;
import com.launcher.db.DBManager;
import com.launcher.db.info.MovieInfo;
import com.launcher.image.LoadImageTask;

public class LayoutView extends View
{
	public Launcher context;
	public OnItemFocusListener itemFocusListener;
	public FcTextView[] mItems;
	private Handler handler = new Handler( Looper.getMainLooper() );
	public DBManager mDBManager;
	protected static final int MSG_VOD_REFRESH = 0;
	protected static final int MSG_COMMON_REFRESH = 1;
	public TVListView channeLv;

	public LayoutView( Context context )
	{
		super( context );
		this.context = ( Launcher ) context;
		itemFocusListener = ( OnItemFocusListener ) context;
	}

	public LayoutView( Context context, AttributeSet attrs )
	{
		super( context );
		this.context = ( Launcher ) context;
		itemFocusListener = ( OnItemFocusListener ) context;
	}

	public interface OnItemFocusListener
	{
		public void onItemFocus( View v, boolean bool );
	}

	public void refreshBtnApp( List< Map< String, Object > > list )
	{
	}

	public void refreshInfo( DBManager manager )
	{
		mDBManager = manager;

	}

	public void initItem( final FcTextView v, final MovieInfo info, String img_size )
	{
		if ( info == null )
		{
			return;
		}
		// handler.post( new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// v.setTitle( info.getTitle() );
		// }
		// } );

		LoadImageTask task = new LoadImageTask( context, v );
		if ( img_size.equals( "img_v" ) )
		{
			task.execute( info.getImg_v() );
		}
		if ( img_size.equals( "img_h" ) )
		{
			task.execute( info.getImg_h() );
		}
		if ( img_size.equals( "img_s" ) )
		{
			task.execute( info.getImg_s() );
		}
		if ( img_size.equals( "img_h2" ) )
		{
			task.execute( info.getImg_h2() );
		}
		if ( img_size.equals( "img_full" ) )
		{
			task.execute( info.getImg_full() );
		}
	}

	public void playDvbLive()
	{

	}

	public void stopDvbLive()
	{

	}

	public void showError( String errorStr, String errorCode, long duration )
	{

	}

	public void hideError()
	{

	}

	public void nullDataTip()
	{

	}

	public void sendMsg()
	{

	}

	public void setManager( DBManager manager )
	{

	}

	public void sendMsg( FcTextView v )
	{
		// if ( v.isCustom() )
		// {
		// if ( ApkHelper.isApkInstalled( context, v.getPkg() ) )
		// {
		// ApkHelper.showLogo( context, v.getTag().toString(), 0, v.getPkg() );
		// }
		// }
		// else
		// {
		// ApkHelper.showLogo( context, v.getTag().toString(), 0, v.getTitle()
		// );
		// }
	}

	public Layout2CallBack mBack;

	public interface Layout2CallBack
	{
		public void isHasFocus();
	}

	public void setCallback( Layout2CallBack back )
	{
		mBack = back;
	}

}
