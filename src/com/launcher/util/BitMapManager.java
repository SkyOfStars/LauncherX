package com.launcher.util;
/** 
* @author  Cyy
* Creat on：2017年12月4日 上午11:30:53 
* @version V1.0  
*/

import android.graphics.Bitmap;

public class BitMapManager
{
	private BitMapManager()
	{
	}

	private Bitmap bitmap;
	
	private static BitMapManager instance;
	
	public static BitMapManager getMapManager()
	{
		if ( instance == null )
		{
			instance = new BitMapManager();
		}
		return instance;
	}

	public void setBitMap( Bitmap bitmap )
	{
		this.bitmap = bitmap;
	}

	public Bitmap getBitMap()
	{
		return bitmap;
	}

}
