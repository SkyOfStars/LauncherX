package com.launcher;

public class LauncherMainModel
{
	private static final String TAG = "LauncherMainModel";
	private AdSwitcherTask adSwitcherTask;

	public interface ICallBackAdSwitcher
	{
		void finish();

		void finishUpDateTime( String updataTime );

		void finishSaveDate( String result );
	}

	public void getAdSwitcher( String updateTime, ICallBackAdSwitcher l )
	{
		if ( adSwitcherTask != null )
		{
			adSwitcherTask = null;
		}
		adSwitcherTask = new AdSwitcherTask( updateTime, l );
		adSwitcherTask.execute( ( Void ) null );
	}

}
