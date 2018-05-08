package com.launcher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AlwaysMarqueeTextView extends TextView
{

	public AlwaysMarqueeTextView( Context context, AttributeSet attrs, int defStyle )
	{
		super( context, attrs, defStyle );
	}

	public AlwaysMarqueeTextView( Context context, AttributeSet attrs )
	{
		this( context, attrs, 0 );
	}

	public AlwaysMarqueeTextView( Context context )
	{
		this( context, null );
	}

	/**
	 * 重写此方法为了解决TextView当不能获取焦点时而无法滚动
	 */
	@Override
	public boolean isFocused()
	{
		if ( isShown() )
		{
			return true;
		}

		return false;
	}

	@Override
	protected void drawableStateChanged()
	{
		super.drawableStateChanged();

	}

}
