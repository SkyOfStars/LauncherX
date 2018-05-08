package com.launcher.vod;

//import com.dvb.provider.FCProviderResolver;

public class ControllerFactory
{
	public static ControllerBase getInstance()
	{

		//if ( "CIBN".endsWith( FCProviderResolver.getInstance().getConfig( "vod_license", "GITV" ) ) )
		// {
		// return YoukuController.getInstance();
		// }
		// else
		{
			return QiyiController.getInstance();
		}
	}
}
