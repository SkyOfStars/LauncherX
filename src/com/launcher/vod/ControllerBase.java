package com.launcher.vod;

import android.content.Context;

public interface ControllerBase
{
	String VID = "vid";

	String TITLE = "title";

	String IMAGE_URL = "imgurl";

	String TAG = "vod";

	String PROTOCOL = "protocol";

	void init( Context context );

	void release();

	void setRecommendationListener( RecommendationCallback callback );

	void goRecommendation( int position );

	void connect();

	void goMovie();

	void goTV();

	void goVariety();

	void goCartoon();

	void goHistory();

	void goFavorite();
}
