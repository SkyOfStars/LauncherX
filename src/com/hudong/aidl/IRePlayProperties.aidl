package com.hudong.aidl;

import com.hudong.aidl.RePlayProgEntity;
import com.hudong.aidl.MenuEntity;
import com.hudong.aidl.RecommendEntity;

interface IRePlayProperties
{
	List< RePlayProgEntity > getReplayPrograms(String startTime,String endTime, String curChanId);
	
	boolean getTimeShiftList( boolean isFromOutside );
	
	boolean getReplayList( boolean isFromOutside );	
	
	boolean doReplay( String name, String id );
	
	boolean doTimeShift( String name, String id );
	
	List< MenuEntity > getMenuList();
	
	List< RecommendEntity > getRecommendList();
}