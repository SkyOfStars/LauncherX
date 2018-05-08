package com.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class TVListView extends ListView
{
	private EventListener mEventListener;

	/**
	 * 当前选中的item
	 */
	private int mSelectPosition = -1;
	/**
	 * 失去焦点时选中的position
	 */
	private int mLastSelectPosition = -1;

	public TVListView( Context context )
	{
		this( context, null );
	}

	public TVListView( Context context, AttributeSet attrs )
	{
		this( context, attrs, 0 );
	}

	public TVListView( Context context, AttributeSet attrs, int defStyleAttr )
	{
		super( context, attrs, defStyleAttr );
		EventItemSelectedListener mEventItemSelectedListener = new EventItemSelectedListener();
		this.setOnItemSelectedListener( mEventItemSelectedListener );
	}

	@Override
	public boolean dispatchKeyEvent( KeyEvent event )
	{
		return super.dispatchKeyEvent( event );
	}

	@Override
	protected void onFocusChanged( boolean gainFocus, int direction, Rect previouslyFocusedRect )
	{
		super.onFocusChanged( gainFocus, direction, previouslyFocusedRect );
		if ( mEventListener == null )
		{
			return;
		}
		if ( gainFocus )
		{// TODO 为了解决selected事件首次进入即position==0时不执行的问题
			if ( getSelectedItemPosition() == 0 || getSelectedItemPosition() == getCount() - 1 )
			{
				// item == 0时，即列表第一条itemView获取焦点时
				if ( mLastSelectPosition == -1 || mLastSelectPosition == 0 )
				{
					Log.d( "zzg", "mLastSelectPosition:" + mLastSelectPosition );
					mSelectPosition = getSelectedItemPosition();
					setSelect();
				}
				// item == size-1,即列表最后一条itemView获取焦点时
				if ( getSelectedItemPosition() == getCount() - 1 && mLastSelectPosition == getCount() - 1 )
				{
					Log.d( "zzg", "mLastSelectPosition:" + mLastSelectPosition );
					mSelectPosition = getSelectedItemPosition();
					setSelect();
				}
			}
		}
	}

	/**
	 * 回调select监听
	 */
	private void setSelect()
	{
		final int[] location = new int[ 2 ];
		if ( getSelectedView() != null )
		{
			getSelectedView().getLocationOnScreen( location );
		}
		
		mEventListener.onItemSelected( getSelectedItemPosition(), location[1] );
	}

	public class EventItemSelectedListener implements OnItemSelectedListener
	{

		@Override
		public void onItemSelected( AdapterView< ? > parent, View view, final int position, long id )
		{
			mSelectPosition = position;
			if ( mEventListener != null && isFocused() )
			{
				setSelect();
			}
		}

		@Override
		public void onNothingSelected( AdapterView< ? > parent )
		{
			mSelectPosition = -1;
		}

	}

	public void setOnEnterEventListener( EventListener eventListener )
	{
		this.mEventListener = eventListener;

	}

	public EventListener getOnEnterEventListener()
	{
		return this.mEventListener;
	}

	public interface EventListener
	{
		/**
		 * item selected监听
		 * 
		 * @param position
		 *            selected item position
		 * @param rawY
		 *            当前选中ItemView 在屏幕中的y轴坐标
		 */
		void onItemSelected( int position, float rawY );
	}
}