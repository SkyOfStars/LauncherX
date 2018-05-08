package com.launcher.widget;

import java.util.Map;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.launcher.R;

public class FcTextView extends LinearLayout
{
	private ImageView imageView, iconView, shadow;
	private TextView textView;
	private AlwaysMarqueeTextView ft_video_text;
	private String pkg = "";
	private String app_id = "";
	private String title = "";
	private boolean custom = false;
	private Map< String, Object > data;
	private String action = "";
	private Boolean isVodItem = false;
	private int iamgeID;
	private Handler handler = new Handler( Looper.getMainLooper() );
	private RelativeLayout rlVod;

	private Boolean isAppAdded = false;

	public FcTextView( Context context )
	{
		this( context, null );
	}

	public FcTextView( Context context, AttributeSet attrs )
	{
		this( context, attrs, 0 );
	}

	public FcTextView( Context context, AttributeSet attrs, int defStyle )
	{
		super( context, attrs, defStyle );

		LayoutInflater inflater = ( LayoutInflater ) context
						.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		View view = inflater.inflate( R.layout.fc_text_view, this );
		imageView = ( ImageView ) view.findViewById( R.id.ft_view_image );
		iconView = ( ImageView ) view.findViewById( R.id.ft_view_icon );
		textView = ( TextView ) view.findViewById( R.id.ft_view_text );
		rlVod = ( RelativeLayout ) view.findViewById( R.id.focus_movie );
		ft_video_text = ( AlwaysMarqueeTextView ) view.findViewById( R.id.ft_video_text );
		rlVod.setVisibility( View.INVISIBLE );
		shadow = ( ImageView ) view.findViewById( R.id.shadow );
		TypedArray a = context.getTheme().obtainStyledAttributes( attrs, R.styleable.FcTextView,
						defStyle, 0 );
		int n = a.getIndexCount();
		for ( int i = 0; i < n; i++ )
		{
			int attr = a.getIndex( i );
			switch( attr )
			{
			case R.styleable.FcTextView_text:
				textView.setText( a.getString( attr ) );
				break;
			case R.styleable.FcTextView_textColor:
				textView.setTextColor( a.getColor( attr, Color.WHITE ) );
				break;
			case R.styleable.FcTextView_textSize:
				textView.setTextSize( a.getDimensionPixelSize( attr, ( int ) TypedValue
								.applyDimension( TypedValue.COMPLEX_UNIT_SP, 16, getResources()
												.getDisplayMetrics() ) ) );
				break;
			case R.styleable.FcTextView_src:
				imageView.setImageResource( a.getResourceId( attr, 0 ) );
				break;
			case R.styleable.FcTextView_icon:
				iconView.setImageResource( a.getResourceId( attr, 0 ) );
				break;
			case R.styleable.FcTextView_custom:
				setCustom( a.getBoolean( attr, false ) );
				break;
			case R.styleable.FcTextView_shadow:

				setVod( a.getBoolean( attr, false ) );
				break;
			}
		}

		// this.setOnFocusChangeListener( new OnFocusChangeListener()
		// {
		// @Override
		// public void onFocusChange( View v, boolean isFocus )
		// {
		// Log.i( "FcTextView", "onFocusChange" );
		// if ( isVodItem )
		// {
		// if ( isFocus )
		// {
		// // focusView.setVisibility( View.VISIBLE );
		// textView.setVisibility( View.VISIBLE );
		// }
		// else
		// {
		// // focusView.setVisibility( View.INVISIBLE );
		// textView.setVisibility( View.GONE );
		// }
		// }
		// }
		// } );

	}

	// public void init()
	// {
	// // this.setBackground( getResources().getDrawable(
	// // R.drawable.focus_item_selector ) );
	// }

	// public void setVodFocus()
	// {
	// rlVod.setVisibility( View.VISIBLE );
	// }

	// public void

	public void setVod( boolean vod )
	{
		Log.i( "iii", "FcTextView_shadow  " + vod );
		if ( vod )
		{
			shadow.setVisibility( View.VISIBLE );
		}
		else
		{
			shadow.setVisibility( View.GONE );
		}

	}

	public void setVodTitle( String title )
	{
		ft_video_text.setText( title );
		ft_video_text.setVisibility( View.VISIBLE );
	}

	// public void hideVodFocus()
	// {
	// rlVod.setVisibility( View.INVISIBLE );
	// }

	// public void hideVodTitle()
	// {
	// ft_video_text.setVisibility( View.INVISIBLE );
	// }

	public void setAppId( String app_id )
	{
		this.app_id = app_id;
	}

	public String getAppId()
	{
		return app_id;
	}

	public String getPkg()
	{
		return pkg;
	}

	public void setPkg( String pkg )
	{
		this.pkg = pkg;
	}

	public boolean isCustom()
	{
		return custom;
	}

	public void setCustom( boolean custom )
	{
		this.custom = custom;
	}

	public void hideIcon()
	{
		iconView.setVisibility( View.GONE );
	}

	public void hideTitle()
	{
		textView.setVisibility( View.GONE );
	}

	public void setIcon( Bitmap drawable )
	{
		iconView.setImageBitmap( drawable );
		iconView.setVisibility( View.VISIBLE );
	}

	public void setIcon( Drawable drawable, int width, int height )
	{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( width, height );
		iconView.setLayoutParams( params );
		iconView.setImageDrawable( drawable );
		iconView.setVisibility( View.VISIBLE );
	}

	public void setIcon( final int resId, final int width, final int height )
	{
		handler.post( new Runnable()
		{
			@Override
			public void run()
			{
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( width, height );
				iconView.setLayoutParams( params );
				iconView.setImageResource( resId );
				iconView.setVisibility( View.VISIBLE );
			}
		} );

	}

	public void setSrc( Bitmap bitmap )
	{
		imageView.setImageBitmap( bitmap );
	}

	public void setSrc( int resId )
	{
		imageView.setImageResource( resId );
	}

	public void setTitle( String title )
	{
		this.title = title;
		textView.setText( title );
		textView.setVisibility( View.VISIBLE );
	}

	public String getTitle()
	{
		return title;
	}

	public void setAction( String action )
	{
		this.action = action;
	}

	public Map< String, Object > getData()
	{
		return data;
	}

	public void setData( Map< String, Object > data )
	{
		this.data = data;
	}

	public String getAction()
	{
		return action;
	}

	public Boolean getIsVodItem()
	{
		return isVodItem;
	}

	public void setIsVodItem( Boolean isVodItem )
	{
		this.isVodItem = isVodItem;
		rlVod.setVisibility( isVodItem ? View.VISIBLE : View.GONE );
	}

	public int getIamgeID()
	{
		return iamgeID;
	}

	public void setIamgeID( int iamgeID )
	{
		this.iamgeID = iamgeID;
	}

	public Boolean getIsAppAdded()
	{
		return isAppAdded;
	}

	public void setIsAppAdded( Boolean isAppAdded )
	{
		this.isAppAdded = isAppAdded;
	}
}
