package com.launcher.util;

import com.launcher.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class NetToast
{
	private static View mToastView;

	private static TextView mTextView;

	private static WindowManager mWM;

	private static WindowManager.LayoutParams mParams;

	private static Handler handler = new Handler()
	{

	};

	public static void show( Context context, CharSequence text, long showTime )
	{
		handler.removeCallbacks( hideAction );
		if ( mWM == null )
		{
			mWM = ( WindowManager ) context.getSystemService( Context.WINDOW_SERVICE );
		}
		if ( mToastView == null )
		{
			mToastView = LayoutInflater.from( context ).inflate( R.layout.net_toast, null );
			mTextView = ( TextView ) mToastView.findViewById( R.id.toast_text );
		}

		if ( mToastView.isShown() )
		{
			mWM.removeViewImmediate( mToastView );
		}

		mTextView.setText( text );
		mWM.addView( mToastView, getCustomLayoutParams() );

		handler.postDelayed( hideAction, showTime );
	}

	public static void show( Context context, int resId, long showTime )
					throws Resources.NotFoundException
	{
		show( context, context.getText( resId ), showTime );
	}

	private static Runnable hideAction = new Runnable()
	{
		@Override
		public void run()
		{
			if ( mToastView != null && mToastView.isShown() )
			{
				mWM.removeViewImmediate( mToastView );
			}
		}
	};

	private static WindowManager.LayoutParams getCustomLayoutParams()
	{
		if ( mParams == null )
		{
			mParams = new WindowManager.LayoutParams();
			mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
			mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
			mParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
			// mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
			// | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
			// | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
			mParams.format = PixelFormat.TRANSLUCENT;
			mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
			mParams.gravity = Gravity.CENTER;
		}
		return mParams;
	}
}
