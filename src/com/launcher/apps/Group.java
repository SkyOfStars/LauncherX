package com.launcher.apps;

import java.util.ArrayList;
import java.util.Map;

public final class Group
{

	public String groupName;
	public ArrayList< Map< String, Object >> groupAppInfo;

	public Group()
	{
		groupAppInfo = new ArrayList< Map< String, Object >>();
	}
}
