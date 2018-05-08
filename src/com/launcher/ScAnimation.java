package com.launcher;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

public class ScAnimation
{

	private ScaleAnimEffect animEffect;

	private static ScAnimation scAnimation;
	private final static String TAG = "ScAnimation";

	public enum LowAnimation
	{
		ISLOW, NOTLOW;
	}

	private Context context;

	/**
	 * 获取ImageLoader的实例。
	 * 
	 * @return ImageLoader的实例。
	 */
	public synchronized static ScAnimation getInstance( Context context )
	{
		if ( scAnimation == null )
		{
			scAnimation = new ScAnimation( context );
		}
		return scAnimation;
	}

	public ScAnimation()
	{
		this.animEffect = new ScaleAnimEffect();
	}

	public ScAnimation( Context context )
	{
		this.animEffect = new ScaleAnimEffect();
		this.context = context;
	}

	/**
	 * 获得焦点的item的动画动作
	 * 
	 * @param paramInt
	 *            获得焦点的item
	 */
	public void showOnFocusAnimation( View v )
	{
		v.bringToFront();
		String tag = ( String ) v.getTag();

		Log.i( TAG, "showOnFocusAnimation--tag==" + tag );
		if ( "401".equals( tag ) || "601".equals( tag ) )
		{
			this.animEffect.setAttributs( 1.0F, 1.05F, 1.0F, 1.05F, 0 );
		}
		else
		{
			this.animEffect.setAttributs( 1.0F, 1.10F, 1.0F, 1.10F, 0 );
		}

		Animation localAnimation = this.animEffect.createSAnimation( context );
		v.startAnimation( localAnimation );
		// show( context, v );
	}

	public void showOnFocusAnimation( RelativeLayout v1, View v )
	{
		v.bringToFront();
		// this.animEffect.setAttributs(1.0F, 1.10F, 1.0F, 1.10F, 0);
		// Animation localAnimation = this.animEffect.createSAnimation( context
		// );
		// localAnimation.setAnimationListener(new AnimationListener() {
		// @Override
		// public void onAnimationEnd(Animation animation) {
		// // TODO Auto-generated method stub
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animation animation) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onAnimationStart(Animation animation) {
		// // TODO Auto-generated method stub
		//
		// }
		// });
		// v.startAnimation(localAnimation);
		show( context, v );
	}

	/**
	 * 失去焦点的的动画动作
	 * 
	 * @param paramInt
	 *            失去焦点的item
	 */
	public void showLooseFocusAinimation( View v )
	{

		String tag = ( String ) v.getTag();
		Log.i( TAG, "showLooseFocusAinimation--tag==" + tag );
		if ( "401".equals( tag ) || "601".equals( tag ) )
		{
			this.animEffect.setAttributs( 1.05F, 1.0F, 1.05F, 1.0F, 0 );
		}
		else
		{
			this.animEffect.setAttributs( 1.10F, 1.0F, 1.10F, 1.0F, 0 );
		}
		v.startAnimation( this.animEffect.createAnimation() );
	}

	public static void show( Context context, View v )
	{
		int[] location = new int[ 2 ];
		v.getLocationInWindow( location );
		v.setScaleX( 1.1f );
		v.setScaleY( 1.1f );
	}

	public static void lostshow( Context context, View v )
	{
		int[] location = new int[ 2 ];
		v.getLocationInWindow( location );
		v.setScaleX( 1f );
		v.setScaleY( 1f );

	}

}
