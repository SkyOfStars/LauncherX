package com.dvb.util;

public interface Constant
{
	/**
	 * 默认分组ID：全部
	 */
	String GROUP_ID_ALL = "-2801";

	/**
	 * 默认分组ID：音频
	 */
	String GROUP_ID_AUDIO = "-2802";

	/**
	 * 默认分组ID：收藏
	 */
	String GROUP_ID_FAV = "-2803";

	/**
	 * 申请在分组列表首位增加一个"全部分组"
	 */
	boolean APPLY_ALL_GROUP = false;

	/**
	 * PREFERENCES配置文件NAME
	 */
	String PREFERENCES_NAME = "LiveInfo";

	/**
	 * PREFERENCES_KEY：最后观看的电视分组
	 */
	String KEY_LAST_WATCH_TV_GROUP = "LastWatchTvGroup";
	

	/**
	 * PREFERENCES_KEY：最后观看的电视频道
	 */
	String KEY_LAST_WATCH_TV_CHANNEL = "LastWatchTvChannel";

	/**
	 * PREFERENCES_KEY：最后收听的音频广播
	 */
	String KEY_LAST_LISTEN_AUDIO_BROADCAST = "LastListenAudioBroadcast";

	/**
	 * PREFERENCES_KEY：全频道音量
	 */
	String KEY_ALL_CHANNEL_VOL = "AllChannelVol";

	/**
	 * PREFERENCES_KEY：全频道静音
	 */
	String KEY_ALL_CHANNEL_MUTE = "AllChannelMute";

	/**
	 * PREFERENCES_KEY：全频道声道
	 */
	String KEY_ALL_CHANNEL_TRACK = "AllChannelTrack";

	/**
	 * 分组Name：全部频道
	 */
	String GROUP_NAME_ALL = "全部频道";

	/**
	 * 分组Name：音频广播
	 */
	String GROUP_NAME_AUDIO = "音频广播";

	/**
	 * 分组Name：喜爱节目
	 */
	String GROUP_NAME_FAV = "喜爱节目";

	String PACKAGE_LIVE = "com.dvb.live";
	String ACTION_LIVE_EPG = "com.dvb.live.action.epg";
	String ACTION_LIVE_GUIDE = "com.dvb.live.action.guide";
	String ACTION_SET_APP_MANAGER = "sys.settings.app_manager";

}
