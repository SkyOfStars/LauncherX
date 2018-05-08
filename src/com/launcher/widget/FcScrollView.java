package com.launcher.widget;

import java.util.ArrayList;

import com.launcher.AutoScrollTextView.SavedState;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.BaseSavedState;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;

public class FcScrollView extends HorizontalScrollView
{
	private int subChildCount = 0;
	private ViewGroup firstChild = null;
	private ArrayList< Integer > leftList = new ArrayList< Integer >();

	private ScrollListener scrollListener;

	private int currentPage;

	public FcScrollView( Context context, AttributeSet attrs, int defStyle )
	{
		super( context, attrs, defStyle );
		init();
	}

	public FcScrollView( Context context, AttributeSet attrs )
	{
		super( context, attrs );
		init();
	}

	public FcScrollView( Context context )
	{
		super( context );
		init();
	}

	private void init()
	{
		setHorizontalScrollBarEnabled( false );
	}

	public static class SavedState extends BaseSavedState
	{
		public int page = 0;

		SavedState( Parcelable superState )
		{
			super( superState );
		}

		@Override
		public void writeToParcel( Parcel out, int flags )
		{
			super.writeToParcel( out, flags );
			if ( out == null )
			{
				return;
			}
			out.writeInt( page );
		}

		public static final Parcelable.Creator< SavedState > CREATOR = new Parcelable.Creator< SavedState >()
		{

			public SavedState[] newArray( int size )
			{
				return new SavedState[ size ];
			}

			@Override
			public SavedState createFromParcel( Parcel in )
			{
				return new SavedState( in );
			}
		};

		private SavedState( Parcel in )
		{
			super( in );
			page = in.readInt();
		}
	}

	@Override
	public Parcelable onSaveInstanceState()
	{
		this.getViewTreeObserver().removeGlobalOnLayoutListener( onGlobalLayoutListener );
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState( superState );

		ss.page = currentPage;
		return ss;
	}

	@Override
	public void onRestoreInstanceState( Parcelable state )
	{
		if ( !( state instanceof SavedState ) )
		{
			super.onRestoreInstanceState( state );
			return;
		}
		SavedState ss = ( SavedState ) state;
		super.onRestoreInstanceState( ss.getSuperState() );

		currentPage = ss.page;
		Log.i( "test1", "ss.page:" + ss.page );
		this.getViewTreeObserver().addOnGlobalLayoutListener( onGlobalLayoutListener );

	}

	private OnGlobalLayoutListener onGlobalLayoutListener = new OnGlobalLayoutListener()
	{

		@Override
		public void onGlobalLayout()
		{
			receiveChildInfo();
			gotoPage( currentPage );
		}
	};

	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
	{
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
		receiveChildInfo();
	}

	public void receiveChildInfo()
	{
		firstChild = ( ViewGroup ) getChildAt( 0 );
		if ( firstChild != null )
		{
			subChildCount = firstChild.getChildCount();
			for ( int i = 0; i < subChildCount; i++ )
			{
				if ( ( ( View ) firstChild.getChildAt( i ) ).getWidth() > 0 )
				{
					leftList.add( ( ( View ) firstChild.getChildAt( i ) ).getLeft() );
				}
			}
		}

	}

	public void gotoPage( int page )
	{
		Log.i( "xxx", "gotoPage  page==" +page +"subChildCount=="+subChildCount);
		currentPage = page;
		if ( leftList.size() > 0 )
		{
			if ( page >= 0 && page < subChildCount )
			{
				smoothScrollTo( leftList.get( page ), 0 );
			}
		}
	}

	public void gotoLeft( int page )
	{
		if ( leftList.size() > 0 )
		{
			if ( page >= 0 && page < subChildCount )
			{
				smoothScrollTo( leftList.get( page ), 0 );
			}
		}
	}

	public void gotoRight( int page, int width )
	{
		if ( leftList.size() > 0 )
		{
			if ( page >= 0 && page < subChildCount )
			{
				smoothScrollTo( leftList.get( page ) + width, 0 );
			}
		}
	}

	@Override
	protected void onScrollChanged( int l, int t, int oldl, int oldt )
	{
		super.onScrollChanged( l, t, oldl, oldt );
		if ( scrollListener != null )
		{
			scrollListener.onScrollChanged();
		}
	}

	public interface ScrollListener
	{
		void onScrollChanged();
	}

	public void setScrollListener( ScrollListener scrollListener )
	{
		this.scrollListener = scrollListener;
	}

}
