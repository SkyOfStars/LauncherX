package com.launcher.download.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.launcher.R;
import com.launcher.image.ImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class LogoTask extends AsyncTask< String, Void, Bitmap >
{
	private ImageLoader imageLoader;
	private static final String TAG = LogoTask.class.getSimpleName();
	private String mImageUrl;
	private ImageView mImageView;
	private Context context;

	public LogoTask()
	{
	}

	public LogoTask( Context context, ImageView imageView )
	{
		this.context = context;
		this.mImageView = imageView;
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	protected Bitmap doInBackground( String... params )
	{
		mImageUrl = params[0];

		Bitmap imageBitmap = imageLoader.getBitmapFromMemoryCache( mImageUrl );
		if ( imageBitmap == null )
		{
			imageBitmap = loadImage( mImageUrl, mImageView.getTag().toString() );
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
			mImageView.setImageBitmap( bitmap );
		}
		else
		{
			mImageView.setImageResource( R.drawable.logo_default );
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
