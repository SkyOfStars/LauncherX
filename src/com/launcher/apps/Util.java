package com.launcher.apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.util.Log;

public class Util
{
	private static String TAG = "Launcher Util";

	public static void copyfile( File fromFile, File toFile, Boolean rewrite )
	{
		if ( !fromFile.exists() )
		{
			Log.e( TAG, "fromFile is not exists: " + fromFile );
			return;
		}
		if ( !fromFile.isFile() )
		{
			Log.e( TAG, "fromFile is not file: " + fromFile );
			return;
		}
		if ( !fromFile.canRead() )
		{
			Log.e( TAG, "fromFile cannot read: " + fromFile );
			return;
		}
		if ( !toFile.getParentFile().exists() )
		{
			Log.e( TAG, "toFile dir is not exist: " + toFile.getParentFile() );
			toFile.getParentFile().mkdirs();
		}
		if ( toFile.exists() && rewrite )
		{
			toFile.delete();
		}
		// 当文件不存时，canWrite一直返回的都是false
		// if (!toFile.canWrite()) {
		// MessageDialog.openError(new Shell(),"错误信息","不能够写将要复制的目标文件" +
		// toFile.getPath());
		// Toast.makeText(this,"不能够写将要复制的目标文件", Toast.LENGTH_SHORT);
		// return ;
		// }
		try
		{
			FileInputStream fosfrom = new FileInputStream( fromFile );
			FileOutputStream fosto = new FileOutputStream( toFile );
			byte bt[] = new byte[ 1024 ];
			int c;
			while ( ( c = fosfrom.read( bt ) ) > 0 )
			{
				fosto.write( bt, 0, c ); // 将内容写到新文件当中
			}
			toFile.setReadable( true, false );
			toFile.setWritable( true, false );
			fosfrom.close();
			fosto.close();
		}
		catch ( Exception ex )
		{
			Log.e( "readfile", ex.getMessage() );
		}
	}
}
