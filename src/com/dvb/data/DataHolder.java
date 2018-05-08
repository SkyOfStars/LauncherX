package com.dvb.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dvb.common.ctrl.FCDvb;
import com.dvb.common.ctrl.FCProgData;
import com.dvb.common.ctrl.FCProgData.PROGTYPE;
import com.dvb.modul.Group;
import com.dvb.util.Constant;
import com.fc.dvb.provider.DVBDBOpt;

import android.content.Context;
import android.hidvb.BouquetTab;
import android.hidvb.DVBEnum;
import android.hidvb.DVBEnum.CATYPE;
import android.util.Log;

public class DataHolder
{
	/**
	 * DVB分组数据是否为空
	 */
	private boolean dvbGroupIsNull = false;

	/**
	 * Map：分组与频道的关联<br>
	 * key：分组ID<br>
	 * value：分组对应的频道List
	 */
	private Map< String, List< FCProgData > > groupChannelMap;

	/**
	 * Map：分组与频道Index的关联<br>
	 * key：分组ID<br>
	 * value：Map：(key:频道唯一标识，value:频道在该分组下的Index)
	 */
	private Map< String, Map< String, Integer >> groupChannelIndexMap;

	/**
	 * 频道数据Map<br>
	 * key：频道唯一标识符<br>
	 * value：频道对象
	 */
	private Map< String, FCProgData > channelMap;

	/**
	 * 分组数据List
	 */
	private List< Group > groupList;
	
	/**
	 * 频道数据List
	 */
	private List< FCProgData > mAllChannelList;
	
	/**
	 * 分组ID与Index的关联Map<br>
	 * key：分组ID<br>
	 * value：在分组List中的Index
	 */
	private Map< String, Integer > groupIdIndexMap;

	private DVBEnum.CATYPE mCaType = CATYPE.UNKNOW;

	private Context mContext;

	private DataHolder( Context context )
	{
		mContext = context;
	}

	private static DataHolder dataHolder;

	public synchronized static DataHolder from( Context context )
	{
		if ( dataHolder == null )
		{
			dataHolder = new DataHolder( context );
		}
		return dataHolder;
	}

	public CATYPE getCAType()
	{
		if ( mCaType != CATYPE.UNKNOW )
		{
			return mCaType;
		}

		int catype = FCDvb.getInstance( mContext ).getCaOptCtrl().getCaType();
		if ( catype == -1 )
		{

		}
		else if ( catype == DVBEnum.CATYPE.CONAX.value() )
		{
			mCaType = CATYPE.CONAX;
		}
		else if ( catype == DVBEnum.CATYPE.TR.value() )
		{
			mCaType = CATYPE.TR;
		}
		else if ( catype == DVBEnum.CATYPE.TF.value() )
		{
			mCaType = CATYPE.TF;
		}
		else if ( catype == DVBEnum.CATYPE.MG.value() )
		{
			mCaType = CATYPE.MG;
		}
		else if ( catype == DVBEnum.CATYPE.SM.value() )
		{
			mCaType = CATYPE.SM;
		}
		else if ( catype == DVBEnum.CATYPE.CD.value() )
		{
			mCaType = CATYPE.CD;
		}
		else if ( catype == DVBEnum.CATYPE.IRD.value() )
		{
			mCaType = CATYPE.IRD;
		}
		else if ( catype == DVBEnum.CATYPE.SM.value() )
		{
			mCaType = CATYPE.SM;
		}
		else if ( catype == DVBEnum.CATYPE.LX.value() )
		{
			mCaType = CATYPE.LX;
		}
		else if ( catype == DVBEnum.CATYPE.DG.value() )
		{
			mCaType = CATYPE.DG;
		}
		else if ( catype == DVBEnum.CATYPE.UTI.value() )
		{
			mCaType = CATYPE.UTI;
		}
		else if ( catype == DVBEnum.CATYPE.UNKNOW.value() )
		{
			mCaType = CATYPE.UNKNOW;
		}

		return mCaType;
	}

	private Object initDataSynObj = new Object();

	/**
	 * 请求频道数据
	 * 
	 * @param context
	 * @return 是否有数据，true:有数据，false:无数据
	 */
	public boolean requestChannelData( Context context )
	{
		synchronized ( initDataSynObj )
		{
			DVBDBOpt dbOpt = DVBDBOpt.getInstance( context );
			// 分组数据List
			List< BouquetTab > bouquetList = dbOpt.queryAllBouquet();
			// 频道数据
			List< FCProgData > channelList = dbOpt.queryAllProgs();

			// 筛选有效频道
			List< FCProgData > validChannelList = null;
			if ( channelList != null )
			{
				Log.i( "PlayWorker", "requestChannelData channelList : " + channelList.size() );
				validChannelList = new ArrayList< FCProgData >();
				for ( FCProgData c : channelList )
				{
					if ( !c.isHided() )
					{
						validChannelList.add( c );
					}
				}
			}

			if ( validChannelList != null && !validChannelList.isEmpty() )
			{
				Log.i( "PlayWorker", "requestChannelData validChannelList : " + validChannelList.size() );
				// 设置播放所需频点List
				FCDvb.getInstance( mContext ).getPlayerCtrl().setChDotData( dbOpt.queryAllChDots() );

				initGroupData( bouquetList );

				initChannelData( context, validChannelList );

				return true;
			}
			return false;
		}
	}

	/**
	 * 初始化分组数据<br>
	 * ！重要：必须在初始化频道数据前调用
	 * 
	 * @param bouquetList
	 */
	private synchronized void initGroupData( List< BouquetTab > bouquetList )
	{
		this.groupList = new ArrayList< Group >();
		this.groupIdIndexMap = new HashMap< String, Integer >();
		this.groupChannelMap = new HashMap< String, List< FCProgData >>();

		if ( bouquetList == null || bouquetList.isEmpty() )
		{
			dvbGroupIsNull = true;
		}
		else
		{
			Log.i( "PlayWorker", "initGroupData bouquetList : " + bouquetList.size() );
			dvbGroupIsNull = false;
		}

		int groupIndex = -1;

		if ( Constant.APPLY_ALL_GROUP || dvbGroupIsNull )
		{
			// 首位"全部分组"
			Group all = new Group( Constant.GROUP_ID_ALL, Constant.GROUP_NAME_ALL, 1 );
			createSingleGroup( all, ++groupIndex );
		}

		if ( !dvbGroupIsNull )
		{
			// DVB返回的分组
			for ( BouquetTab bouquet : bouquetList )
			{
				Group group = new Group( bouquet.BouquetID + "", bouquet.BouquetName, 1 );
				createSingleGroup( group, ++groupIndex );
			}
		}

		// "音频广播"分组
		Group audio = new Group( Constant.GROUP_ID_AUDIO, Constant.GROUP_NAME_AUDIO, 1 );
		createSingleGroup( audio, ++groupIndex );

		// "收藏"分组
		Group favorite = new Group( Constant.GROUP_ID_FAV, Constant.GROUP_NAME_FAV, 1 );
		createSingleGroup( favorite, ++groupIndex );
	}
	
	
	/**
	 * 获取全部频道List
	 * 
	 * @return
	 */
	public synchronized List< FCProgData > getAllChannelList()
	{
		return mAllChannelList;
	}
	

	/**
	 * 根据频道获取组id
	 * 
	 * @param channel
	 * @return
	 */
	public synchronized String getGroupIdByChannel( FCProgData channel )
	{
		for ( String groupId : groupChannelMap.keySet() )
		{
			if ( groupChannelMap.get( groupId ).contains( channel ) )
			{
				return groupId;
			}
		}

		return null;
	}

	/**
	 * 生成单个分组的初始化状态
	 * 
	 * @param group
	 * @param groupIndex
	 */
	private void createSingleGroup( Group group, int groupIndex )
	{
		groupList.add( group );
		groupIdIndexMap.put( group.getGroupID(), groupIndex );
		groupChannelMap.put( group.getGroupID(), new ArrayList< FCProgData >() );
	}

	/**
	 * 初始化频道数据<br>
	 * ！重要：必须在初始化分组数据后且初始化节目数据前调用
	 * 
	 * @param context
	 * @param progList
	 */
	private synchronized void initChannelData( Context context, List< FCProgData > progList )
	{
		if ( groupChannelMap != null && !groupChannelMap.isEmpty() )
		{
			groupChannelIndexMap = new HashMap< String, Map< String, Integer > >();
			channelMap = new HashMap< String, FCProgData >();
			
			mAllChannelList = progList;

			// begin - 关联分组，填充channelMap
			for ( FCProgData prog : progList )
			{
				String channelUniqueID = createChannelUniqueID( prog );
				channelMap.put( channelUniqueID, prog );

				// 关联应用生成的"全部分组"
				if ( ( Constant.APPLY_ALL_GROUP || dvbGroupIsNull )
								&& prog.getProgType() != PROGTYPE.E_BORADCAST )
				{
					connectGroupAndChannel( Constant.GROUP_ID_ALL, prog );
				}

				// 关联"DVB返回的分组"
				List< Long > bouquetIdList = prog.bouquetList;
				if ( bouquetIdList != null )
				{
					for ( Long bouquetId : bouquetIdList )
					{
						connectGroupAndChannel( bouquetId + "", prog );
					}
				}

				// 关联"音频广播"分组
				if ( prog.getProgType() == PROGTYPE.E_BORADCAST )
				{
					connectGroupAndChannel( Constant.GROUP_ID_AUDIO, prog );
				}

				// 关联"收藏"分组
				if ( prog.isLiked() )
				{
					connectGroupAndChannel( Constant.GROUP_ID_FAV, prog );
				}
			}
			// end - 关联分组，填充channelMap
		}
	}

	/**
	 * 关联分组与频道
	 * 
	 * @param groupID
	 * @param channel
	 */
	private void connectGroupAndChannel( String groupID, FCProgData channel )
	{
		List< FCProgData > list = groupChannelMap.get( groupID );
		// 如果list不为空，则是initGroupData()中初始化过的分组，则将频道添加至list中
		if ( list != null )
		{
			list.add( channel );

			Map< String, Integer > channelIndexMap = groupChannelIndexMap.get( groupID );
			if ( channelIndexMap == null )
			{
				channelIndexMap = new HashMap< String, Integer >();
				groupChannelIndexMap.put( groupID, channelIndexMap );
			}
			channelIndexMap.put( createChannelUniqueID( channel ), channelIndexMap.size() );
		}
	}

	/**
	 * 生成频道对象的唯一标识
	 * 
	 * @param prog
	 * @return
	 */
	public String createChannelUniqueID( FCProgData prog )
	{
		return prog != null ? prog.getUUID() : null;
	}

//	public String createChannelUniqueID( String networkID, String tsID, String serviceID )
//	{
//		return networkID + "|" + tsID + "|" + serviceID;
//	}

	/**
	 * 获取全部分组List
	 * 
	 * @return
	 */
	public synchronized List< Group > getAllGroupList()
	{
		return this.groupList;
	}

	/**
	 * 根据分组ID获取分组
	 * 
	 * @param groupID
	 * @return
	 */
	public synchronized Group getGroupByID( String groupID )
	{
		if ( groupIdIndexMap != null )
		{
			Integer index = groupIdIndexMap.get( groupID );
			if ( index != null )
			{
				return groupList.get( index );
			}
		}
		return null;
	}

	/**
	 * 根据频道唯一标识符获取频道
	 * 
	 * @param channelUniqueID
	 * @return
	 */
	public synchronized FCProgData getChannelByUniqueID( String channelUniqueID )
	{
		return channelMap.get( channelUniqueID );
	}

	/**
	 * 根据分组ID、频道UniqueID获取频道在分组中的Index<br>
	 * 不存在关联的Index则返回-1
	 * 
	 * @param groupID
	 * @param channelUniqueID
	 * @return
	 */
	public synchronized int getChannelIndexByGroupAndChannel( String groupID, String channelUniqueID )
	{
		Map< String, Integer > channelIndexMap = groupChannelIndexMap.get( groupID );
		if ( channelIndexMap != null )
		{
			Integer index = channelIndexMap.get( channelUniqueID );
			if ( index != null )
			{
				return index;
			}
		}
		return -1;
	}

	/**
	 * 根据分组ID获取分组下的频道List
	 * 
	 * @return
	 */
	public synchronized List< FCProgData > getChannelListByGroupID( String groupID )
	{
		return groupChannelMap.get( groupID );
	}

	/**
	 * 当前使用的频道
	 */
	private String curChannelUniqueID;

	/**
	 * 获取当前使用的频道对象
	 * 
	 * @return
	 */
	public synchronized FCProgData getCurChannel()
	{
		return channelMap.get( curChannelUniqueID );
	}

	/**
	 * 设置当前使用的频道
	 * 
	 * @param channelUniqueID
	 */
	public synchronized void setCurChannelUniqueID( String channelUniqueID )
	{
		curChannelUniqueID = channelUniqueID;
	}
}
