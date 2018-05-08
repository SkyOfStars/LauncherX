package com.launcher.util;

import android.content.Context;
import android.content.Intent;

public class DisplayIntentHelper
{
	public static void sendDisplayBroadcast( Context context, String content )
	{
		Intent intent = new Intent( "com.fc.display_tube" );
		intent.putExtra( "display_content", content );
		context.sendBroadcast( intent );
	}
}
