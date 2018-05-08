package com.dvb.modul;

public class Group
{
	private static final String TAG = Group.class.getSimpleName();

	/**
	 * 分组ID
	 */
	private String groupID;

	/**
	 * 分组名称
	 */
	private String groupName;

	/**
	 * 本组频道开始编号
	 */
	private int startNum;

	public Group( String groupID, String groupName, int startNum )
	{
		this.groupID = groupID;
		this.groupName = groupName;
		this.startNum = startNum;
	}

	public String getGroupID()
	{
		return groupID;
	}

	public void setGroupID( String groupID )
	{
		this.groupID = groupID;
	}

	public String getGroupName()
	{
		return groupName;
	}

	public void setGroupName( String groupName )
	{
		this.groupName = groupName;
	}

	public int getStartNum()
	{
		return startNum;
	}

	public void setStartNum( int startNum )
	{
		this.startNum = startNum;
	}

	@Override
	public String toString()
	{
		return TAG + "{groupID=" + groupID + ", groupName=" + groupName + ", startNum=" + startNum
						+ "}";
	}

}
