package com.launcher;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppInfoAdapter extends BaseAdapter
{

	private List< AppInfo > mlistAppInfo = new ArrayList< AppInfo >();
	private LayoutInflater infater;

	public AppInfoAdapter( Context context, List< AppInfo > apps )
	{
		infater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		mlistAppInfo = apps;
	}

	@Override
	public int getCount()
	{
		return mlistAppInfo.size();
	}

	@Override
	public Object getItem( int position )
	{
		return mlistAppInfo.get( position );
	}

	@Override
	public long getItemId( int position )
	{
		return position;
	}

	@Override
	public View getView( final int position, View convertview, ViewGroup viewGroup )
	{
		View view = null;
		ViewHolder holder = null;
		if ( convertview == null || convertview.getTag() == null )
		{
			view = infater.inflate( R.layout.launcher_apps_item, viewGroup, false );
			holder = new ViewHolder( view );
			view.setTag( holder );
		}
		else
		{
			view = convertview;
			holder = ( ViewHolder ) convertview.getTag();
		}
		AppInfo appInfo = ( AppInfo ) getItem( position );
		holder.appIcon.setImageDrawable( appInfo.getAppIcon() );
		holder.tvAppLabel.setText( appInfo.getAppLabel() );
		return view;
	}

	class ViewHolder
	{
		ImageView appIcon;
		TextView tvAppLabel;
		TextView tvPkgName;

		public ViewHolder( View view )
		{
			this.appIcon = ( ImageView ) view.findViewById( R.id.item_appicon );
			this.tvAppLabel = ( TextView ) view.findViewById( R.id.item_appLabel );
		}
	}
}
