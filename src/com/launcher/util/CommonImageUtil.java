package com.launcher.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;

public class CommonImageUtil
{
	private CommonImageUtil()
	{

	}

	public static Bitmap decodeSampledBitmapFromResource( String pathName )
	{
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;
		options.inSampleSize = 1;
		options.inPurgeable = true;

		BitmapFactory.decodeFile( pathName, options );

		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;

		Bitmap bitmap = BitmapFactory.decodeFile( pathName, options );
		return bitmap;
	}

	public static Bitmap toRoundCorner( Bitmap bitmap, int pixels )
	{
		if ( bitmap == null )
		{
			return null;
		}

		Bitmap output = Bitmap.createBitmap( bitmap.getWidth(), bitmap.getHeight(),
						android.graphics.Bitmap.Config.ARGB_8888 );
		Canvas canvas = new Canvas( output );
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect( 0, 0, bitmap.getWidth(), bitmap.getHeight() );
		final RectF rectF = new RectF( rect );
		final float roundPx = pixels;
		paint.setAntiAlias( true );
		canvas.drawARGB( 0, 0, 0, 0 );
		paint.setColor( color );
		canvas.drawRoundRect( rectF, roundPx, roundPx, paint );
		paint.setXfermode( new PorterDuffXfermode( Mode.SRC_IN ) );
		canvas.drawBitmap( bitmap, rect, rect, paint );
		return output;
	}

	/**
	 * 按照高度缩放，比控件多出的宽度按照两边裁剪
	 * 
	 * @param bitmap
	 * @param w
	 * @param h
	 * @return
	 */
	public static Bitmap zoomBitmap( Bitmap bitmap, int w, int h )
	{
		Log.i( "zoomBitmap", "w:" + w );
		Log.i( "zoomBitmap", "h:" + h );

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Log.i( "zoomBitmap", "width1:" + width );
		Log.i( "zoomBitmap", "height1:" + height );

		Matrix matrix = new Matrix();

		// float scaleWidht = ( ( float ) w / width );

		float scaleHeight = ( ( float ) h / height );

		matrix.postScale( scaleHeight, scaleHeight );

		Bitmap newbmp = Bitmap.createBitmap( bitmap, 0, 0, width, height, matrix, true );

		int newWidth = newbmp.getWidth();

		Log.i( "zoomBitmap", "width2:" + newbmp.getWidth() );
		Log.i( "zoomBitmap", "height2:" + newbmp.getHeight() );

		int x = newWidth > w ? ( newWidth - w ) / 2 : 0;// 居中裁剪两边宽度
		Bitmap finalBitmap = Bitmap.createBitmap( newbmp, x, 0, w, h, null, false );

		return toRoundCorner( finalBitmap, 15 );

	}

	public static void saveLauncherBgImageToFile( final String imageUrl, final Handler handler,
					boolean isRefresh )
	{
		final String imageLauncherBgPath = SystemProperties.get( "fc.config.path", "/fc/config/" )
						+ "launcher_bg.png";
		File file = new File( imageLauncherBgPath );
		if ( file.exists() && file.isFile() && file.length() > 1024 && !isRefresh )
		{
			return;
		}

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				saveImageToFile( imageUrl, imageLauncherBgPath );
				handler.sendEmptyMessage( 11 );
			}
		} ).start();
	}

	public static void saveImageToFile( String imageUrl, String imagePath )
	{
		URLConnection con = null;
		try
		{
			URL url = new URL( imageUrl );
			con = url.openConnection();
			InputStream is = con.getInputStream();// 注：放在主线程会报异常存储数据失败
			byte[] bs = new byte[ 512 ];
			int len;
			OutputStream os = new FileOutputStream( imagePath );
			while ( ( len = is.read( bs ) ) != -1 )
			{
				os.write( bs, 0, len );
			}
			os.flush();
			os.close();
			is.close();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

}
