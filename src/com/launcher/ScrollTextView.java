package com.launcher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 *
 */
public class ScrollTextView extends SurfaceView implements SurfaceHolder.Callback
{
	private static final String TAG = "ScrollTextView";

	private Context context;

	private SurfaceHolder surfaceHolder;

	private Paint paint;

	private int viewWidth = 0;

	private int viewHeight = 0;

	private float density = 1;

	private float textWidth = 0f;

	private float scrollWidth = 0f;

	private float textX = 0f;

	private float textY = 0f;

	private float speed = 1f; // scroll-speed

	private float textSize = 24f; // text size

	private String text = "";

	private ScrollCallback callback;

	private boolean isEnd = false;

	private Thread thread;

	private boolean isSurfaceCreated = false;

	private boolean isMeasured = false;

	/**
	 *
	 */
	public ScrollTextView( Context context )
	{
		super( context );
		this.context = context;
		init();
	}

	/**
	 *
	 */
	public ScrollTextView( Context context, AttributeSet attrs )
	{
		super( context, attrs );
		this.context = context;
		init();
	}

	private void init()
	{
		Log.i( TAG, "init" );
		surfaceHolder = this.getHolder(); // get The surface holder
		surfaceHolder.addCallback( this );
		paint = new Paint();
		paint.setColor( Color.WHITE );
		paint.setTextSize( textSize );
		setZOrderOnTop( true );// 使surfaceview放到最顶层
		surfaceHolder.setFormat( PixelFormat.TRANSLUCENT );
		DisplayMetrics metric = new DisplayMetrics();
		( ( Activity ) context ).getWindowManager().getDefaultDisplay().getMetrics( metric );
		density = metric.density;
	}

	public ScrollTextView setContenList( List< String > strings )
	{
		return this;
	}

	public ScrollTextView setContent( String string )
	{
		text = string;
		return this;
	}

	public void startScroll( long duration )
	{
		startScroll();
	}

	public void startScroll( int times )
	{
		startScroll();
	}

	public void stopScroll()
	{
		isEnd = true;
	}

	public void setScrollCallback( ScrollCallback callback )
	{
		this.callback = callback;
	}

	public void startScroll()
	{
		Log.i( TAG, "startScroll textY : " + textY );
		thread = new ScrollThread();
		thread.start();
	}

	private synchronized void draw( float X, float Y )
	{
		Canvas canvas = surfaceHolder.lockCanvas();
		if ( canvas != null )
		{
			canvas.setDrawFilter( new PaintFlagsDrawFilter( 0,
							Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG ) );
			canvas.drawColor( Color.TRANSPARENT, PorterDuff.Mode.CLEAR );
			canvas.drawText( text, X, Y, paint );
			surfaceHolder.unlockCanvasAndPost( canvas );
		}
	}

	private class ScrollThread extends Thread
	{
		@Override
		public void run()
		{
			super.run();
			Log.i( TAG, "ScrollThread run" );
			while ( !isSurfaceCreated || !isMeasured )
			{
				SystemClock.sleep( 100 );
			}

			textWidth = paint.measureText( text );
			while ( textWidth == 0 )
			{
				Log.i( TAG, "while textWidth : " + textWidth );
				textWidth = paint.measureText( text );
				SystemClock.sleep( 100 );
			}

			scrollWidth = viewWidth + textWidth;
			textY = ( viewHeight - textSize ) / 2 + textSize + getPaddingTop() - getPaddingBottom()
							- 2;
			isEnd = false;
			textX = 0;
			if ( callback != null )
			{
				callback.onScrollStart();
			}

			while ( !isEnd )
			{
				draw( viewWidth - textX, textY );
				textX += speed;
				// Log.i( TAG, "ScrollTextThread textX : " + textX );
				if ( textX > scrollWidth )
				{
					textX = 0;
					if ( callback != null )
					{
						callback.onScrollEnd();
					}
				}

				SystemClock.sleep( 5 );
			}
		}
	}

	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
	{
		super.onMeasure( widthMeasureSpec, heightMeasureSpec );
		viewWidth = MeasureSpec.getSize( widthMeasureSpec );
		viewHeight = MeasureSpec.getSize( heightMeasureSpec );
		isMeasured = true;
	}

	@Override
	public void surfaceCreated( SurfaceHolder holder )
	{
		setZOrderOnTop( true );// 使surfaceview放到最顶层
		holder.setFormat( PixelFormat.TRANSLUCENT );
		isSurfaceCreated = true;
	}

	@Override
	public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
	{

	}

	@Override
	public void surfaceDestroyed( SurfaceHolder holder )
	{

		isSurfaceCreated = false;
	}

	public interface ScrollCallback
	{
		void onScrollStart();

		void onScrollEnd();

		void onScrollError();
	}
}
