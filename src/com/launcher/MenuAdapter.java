package com.launcher;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MenuAdapter extends BaseAdapter
{
	private static final String TAG = "MenuAdapter";

	private Context mContext;

	private LayoutInflater mInflater;

	private String[] mDatas = { "频道", "电影", "电视剧", "综艺", "音乐", "动漫", "教育" };
	private String[] mMenulIDs;

	public MenuAdapter( Context context, String[] mMenulNames, String[] mMenulIDs )
	{
		Log.i( TAG, "ChannelAdapter" );
		mInflater = LayoutInflater.from( context );
		mContext = context;
		if ( mMenulNames != null )
		{
			if ( mMenulNames.length > 1 )
			{
				mDatas = null;
				mDatas = mMenulNames;
			}
		}

		this.mMenulIDs = mMenulIDs;
	}

	@Override
	public int getCount()
	{
		return mDatas == null ? 0 : mDatas.length;
	}

	@Override
	public long getItemId( int arg0 )
	{
		return arg0;
	}

	@Override
	public View getView( int arg0, View convertView, ViewGroup arg2 )
	{
		final ItemHolder holder;

		if ( convertView == null )
		{
			holder = new ItemHolder();
			convertView = mInflater.inflate( R.layout.channel_item, null );
			holder.channelNumber = ( TextView ) convertView.findViewById( R.id.channel_item_name );
			convertView.setTag( holder );
		}
		else
		{
			holder = ( ItemHolder ) convertView.getTag();
		}

		holder.channelNumber.setText( mDatas[arg0] );

		return convertView;
	}

	private class ItemHolder
	{
		TextView channelNumber;
	}

	@Override
	public Object getItem( int arg0 )
	{
		return mDatas[arg0];
	}

}
