package com.launcher.util;

public class CommonConstant
{
	private CommonConstant()
	{

	}

	public static final String TAG_BG = "000";
	public static final String TAG_NAV_1 = "100";
	public static final String TAG_NAV_2 = "200";
	public static final String TAG_NAV_3 = "300";
	public static final String TAG_NAV_4 = "400";
	public static final String TAG_NAV_5 = "500";

	public static final int MSG_NETWORK_CONNECTED = 1;
	public static final int MSG_DATE = 2;
	public static final int MSG_NET_FAIL = 3;
	public static final int MSG_NET_ETH = 4;
	public static final int MSG_NET_WIFI = 5;
	public static final int MSG_CA_MAIL = 6;

	public static final int MSG_REFRESH_BG = 7;
	public static final int MSG_REFRESH_SERVER_DATA = 8;
	public static final int MSG_NETWORK9 = 9;
	public static final int MSG_NETWORK10 = 10;
	public static final int MSG_NETWORK11 = 11;
	public static final int MSG_NETWORK12 = 12;

	public static boolean weatherTaskRunning = false;
	public static String cityText;

	public static String PKG_LIVE = "com.dvb.live";
	public static String PKG_SETTING = "com.fc.setting";
	public static String PKG_EMALL = "com.tt.emall";
	public static String PKG_TT_VOD = "com.gitvvideo";
	public static String PKG_SMART_CITY = "com.smartcity";
	public static String PKG_SMART_HOME = "com.smarthome";
	public static String PKG_APP_MALL = "com.fc.tvmall";
	public static String PKG_EJ_HELPER = "com.ejian";

	public static String CLASS_TT_VOD = "com.qiyi.video.ui.search.QSearchActivity";

	public static String ACTION_SETTING_MAIL = "sys.settings.mail";
	public static String ACTION_CALENDAR = "com.ejian.action.calendar";
	public static String ACTION_SETTING_NET = "sys.settings.net";
	public static String ACTION_SETTING_PROG = "sys.settings.program";
	public static String ACTION_SETTING_APHOT = "com.ejian.action.aphot";
	public static String ACTION_SETTING_MUTIL = "com.ejian.action.multiscreen";
	public static String ACTION_SETTING_UPGRADE = "sys.settings.upgrade";
	public static String ACTION_SETTING_INFO = "sys.settings.info";

	public static String ACTION_LIVE_GUIDE = "com.dvb.live.action.guide";
	public static String ACTION_LIVE_REPLAY = "com.dvb.live.action.replay";

	public static String ACTION_SMARTCITY_PARAMS_LIFE = "open_app|by_action|com.smartcity.action|{\"category\":[\"str\", \"category5\"]}";
	public static String ACTION_SMARTCITY_PARAMS_FOOD = "open_app|by_action|com.smartcity.action|{\"category\":[\"str\", \"category6\"]}";
	public static String ACTION_SMARTCITY_PARAMS_GOV = "open_app|by_action|com.smartcity.action|{\"category\":[\"str\", \"category1\"]}";
	public static String ACTION_SMARTCITY_PARAMS_HOME = "open_app|by_action|com.smartcity.action|{\"category\":[\"str\", \"category2\"]}";
	public static String ACTION_SMARTCITY_PARAMS_PLAY = "open_app|by_action|com.smartcity.action|{\"category\":[\"str\", \"category3\"]}";
	public static String ACTION_SMARTCITY_PARAMS_CONSUME = "open_app|by_action|com.smartcity.action|{\"category\":[\"str\", \"category4\"]}";

	public static String URI_APP = "apps://start";
	public static String URI_TT_VOD = "tt_vod://start";

	public static String UPDATE_DATA = "update_data";
	public static String UPDATE_DATA_KEY = "last_update";

	public static String UPDATE_AD_DATA = "update_ad_data";
	public static String UPDATE_AD_DATA_KEY = "last_ad_update";
	public static String UPDATE_AD_RESULT_KEY = "last_ad_result_update";

}
