package com.launcher.vod;

import java.util.List;
import java.util.Map;

public interface RecommendationCallback
{
	void onRecieveList( List< Map< String, String >> list );
}
