package com.launcher.util;

import java.io.File;
import java.io.FileInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CommonFileUtil
{
	private CommonFileUtil()
	{

	}

	public static Bitmap decodeImage( String filePath )
	{
		try
		{
			File file = new File( filePath );
			FileInputStream fis = null;
			if ( !file.exists() )
			{
				return null;
			}
			fis = new FileInputStream( filePath );
			Bitmap bm = BitmapFactory.decodeStream( fis );
			if ( bm != null )
			{
				return bm;
			}
			fis.close();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		return null;

	}

}
