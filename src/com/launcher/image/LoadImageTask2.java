package com.launcher.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

public class LoadImageTask2 extends AsyncTask< String, Void, Bitmap >
{
	private static String TAG = "LoadImageTask";
	private String mImageUrl;
	private ImageLoader imageLoader;
	private Context context;
	private String view_tag;
	private ICallBack l;

	public interface ICallBack
	{
		void finish( Bitmap bitmap );
	}

	public LoadImageTask2( Context context, String view_tag, ICallBack l )
	{
		this.context = context;
		this.view_tag = view_tag;
		this.l = l;
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	protected Bitmap doInBackground( String... params )
	{
		mImageUrl = params[0];
		Bitmap imageBitmap = imageLoader.getBitmapFromMemoryCache( mImageUrl );
		if ( imageBitmap == null )
		{
			imageBitmap = loadImage( mImageUrl, view_tag );
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
			if ( l != null )
				l.finish( bitmap );
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
			bitmap = ( bitmap == null ) ? null : bitmap;
			if ( bitmap != null )
			{
				imageLoader.addBitmapToMemoryCache( imageUrl, bitmap );
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
		String imagePath = imageDir + tag + "_" + imageName;
		return imagePath;
	}

}
