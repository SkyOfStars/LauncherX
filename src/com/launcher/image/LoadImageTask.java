package com.launcher.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.launcher.widget.FcTextView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

public class LoadImageTask extends AsyncTask< String, Void, Bitmap >
{
	private ImageLoader imageLoader;
	private static String TAG = "LoadImageTask";
	private String mImageUrl;
	private FcTextView mImageView;
	private View view;
	private Context context;

	public LoadImageTask()
	{
	}

	public LoadImageTask( Context context, View imageView )
	{
		this.context = context;
		if ( imageView instanceof FcTextView )
		{
			this.mImageView = ( FcTextView ) imageView;
		}
		else
		{
			this.view = imageView;
		}
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	protected Bitmap doInBackground( String... params )
	{
		mImageUrl = params[0];
		Bitmap imageBitmap = imageLoader.getBitmapFromMemoryCache( mImageUrl );
		if ( imageBitmap == null )
		{
			if ( mImageView instanceof FcTextView )
			{
				imageBitmap = loadImage( mImageUrl, mImageView.getTag().toString() );
			}
			else
			{
				imageBitmap = loadImage( mImageUrl, view.getTag().toString() );
			}

		}

		// 缓存的是进行圆角处理后的bitmap，所以直接返回
		return imageBitmap;
		// return toRoundCorner( imageBitmap, 10 );
	}

	@Override
	protected void onPostExecute( Bitmap bitmap )
	{
		if ( bitmap != null )
		{
			if ( mImageView != null )
			{
				mImageView.setSrc( bitmap );
			}
			else if ( view != null )
			{
				view.setBackground( new BitmapDrawable( context.getResources(), bitmap ) );
			}
		}

	}

	private Bitmap loadImage( String imageUrl, String tag )
	{
		String name = getImagePath( imageUrl, tag );
		String img_name[] = name.split( "_" );
		File imageFile = new File( name );

		if ( !imageFile.exists() )
		{
			String imagePath = "data/data/" + context.getPackageName() + "/image/";
			File f = new File( imagePath );
			File[] files = f.listFiles();
			for ( File file : files )
			{
				String imageName = file.getName();
				String tag_name[] = imageName.split( "_" );
				int lastSlashIndex = tag_name[0].lastIndexOf( "/" );
				String imgTag = tag_name[0].substring( lastSlashIndex + 1 );

				if ( img_name.length > 1 && tag_name.length > 1 )
				{
					if ( imgTag.equals( tag ) && !tag_name[1].equals( img_name[1] ) )
					{
						file.delete();
						break;
					}
				}
			}
			downloadImage( imageUrl, tag );
		}

		// 首次进行图片下载的情况，下载完成后，已经缓存起来了，直接返回
		Bitmap imageBitmap = imageLoader.getBitmapFromMemoryCache( imageUrl );
		if ( imageBitmap != null )
		{
			return imageBitmap;
		}

		if ( imageUrl != null )
		{
			imageFile = new File( name );
			if ( imageFile.exists() )
			{
				Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource( imageFile.getPath() );
				// bitmap = ( bitmap == null ) ? null : toRoundCorner( bitmap,
				// 12 );
				bitmap = ( bitmap == null ) ? null : bitmap;
				if ( bitmap != null )
				{
					imageLoader.addBitmapToMemoryCache( imageUrl, bitmap );

					return bitmap;
				}
				else
				{
					downloadImage( imageUrl, tag );
				}
			}
		}
		return null;
	}

	private void downloadImage( String imageUrl, String tag )
	{
		URLConnection con = null;
		File imageFile = null;
		String imagePath = getImagePath( imageUrl, tag );
		try
		{
			URL url = new URL( imageUrl );
			con = url.openConnection();
			InputStream is = con.getInputStream();
			byte[] bs = new byte[ 1024 ];
			int len;
			OutputStream os = new FileOutputStream( imagePath );
			while ( ( len = is.read( bs ) ) != -1 )
			{
				os.write( bs, 0, len );
			}
			os.flush();
			os.close();
			is.close();
			imageFile = new File( imagePath );
		}
		catch ( Exception ex )
		{
			Log.e( TAG, "downloadImage error=" + ex.getMessage() );
		}

		if ( imageFile != null )
		{
			Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource( imageFile.getPath() );
			// bitmap = ( bitmap == null ) ? null : toRoundCorner( bitmap, 12 );
			bitmap = ( bitmap == null ) ? null : bitmap;
			if ( bitmap != null )
			{
				imageLoader.addBitmapToMemoryCache( imageUrl, bitmap );

				// if ( bitmap != null && !bitmap.isRecycled() )
				// {
				// bitmap.recycle();
				// bitmap = null;
				// }
			}

		}
	}

	private String getImagePath( String imageUrl, String tag )
	{
		int lastSlashIndex = imageUrl.lastIndexOf( "/" );
		String imageName = imageUrl.substring( lastSlashIndex + 1 );
		String imageDir = "data/data/" + context.getPackageName() + "/image/";
		File file = new File( imageDir );
		if ( !file.exists() )
		{
			file.mkdirs();
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append( imageDir );
		buffer.append( tag );
		buffer.append( "_" );
		buffer.append( StringMD5( imageUrl ) );
		buffer.append( "_" );
		buffer.append( imageName );
		String imagePath = buffer.toString();
		return imagePath;
	}

	// private Bitmap toRoundCorner1( Bitmap bitmap, int pixels )
	// {
	// if ( bitmap == null )
	// {
	// return null;
	// }
	//
	// Bitmap output = Bitmap.createBitmap( bitmap.getWidth(),
	// bitmap.getHeight(),
	// android.graphics.Bitmap.Config.ARGB_4444 );
	// Canvas canvas = new Canvas( output );
	// final int color = 0xff424242;
	// final Paint paint = new Paint();
	// final Rect rect = new Rect( 0, 0, bitmap.getWidth(), bitmap.getHeight()
	// );
	// final RectF rectF = new RectF( rect );
	// final float roundPx = pixels;
	// paint.setAntiAlias( true );
	// canvas.drawARGB( 0, 0, 0, 0 );
	// paint.setColor( color );
	// canvas.drawRoundRect( rectF, roundPx, roundPx, paint );
	// paint.setXfermode( new PorterDuffXfermode( Mode.SRC_IN ) );
	// canvas.drawBitmap( bitmap, rect, rect, paint );
	// if ( !bitmap.isRecycled() )
	// {
	// bitmap.recycle();
	// bitmap = null;
	// }
	// // bitmap.recycle();
	// return output;
	// }

	public String StringMD5( String string )
	{
		if ( TextUtils.isEmpty( string ) )
		{
			return "";
		}
		MessageDigest md5 = null;

		try
		{
			md5 = MessageDigest.getInstance( "MD5" );
			byte[] bytes = md5.digest( string.getBytes() );
			String result = "";
			for ( byte b : bytes )
			{
				String temp = Integer.toHexString( b & 0xff );
				if ( temp.length() == 1 )
				{
					temp = "0" + temp;
				}
				result += temp;
			}
			return result;
		}
		catch ( NoSuchAlgorithmException e )
		{
			e.printStackTrace();
		}
		return "";
	}
}
