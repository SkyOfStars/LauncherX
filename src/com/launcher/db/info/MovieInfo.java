package com.launcher.db.info;

import java.io.Serializable;

public class MovieInfo implements Serializable
{
	public String tag;
	public String title;
	public String action;
	public String img_v;
	public String img_h;
	public String img_s;
	public String img_h2;
	public String img_h3;
	public String img_full;

	public String app_package_name;
	public String app_id;

	public String getApp_package_name()
	{
		return app_package_name;
	}

	public void setApp_package_name( String app_package_name )
	{
		this.app_package_name = app_package_name;
	}

	public String getApp_id()
	{
		return app_id;
	}

	public void setApp_id( String app_id )
	{
		this.app_id = app_id;
	}

	public String getTag()
	{
		return tag;
	}

	public void setTag( String tag )
	{
		this.tag = tag;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction( String action )
	{
		this.action = action;
	}

	public String getImg_v()
	{
		return img_v;
	}

	public void setImg_v( String img_v )
	{
		this.img_v = img_v;
	}

	public String getImg_h()
	{
		return img_h;
	}

	public void setImg_h( String img_h )
	{
		this.img_h = img_h;
	}

	public String getImg_s()
	{
		return img_s;
	}

	public void setImg_s( String img_s )
	{
		this.img_s = img_s;
	}

	public String getImg_h2()
	{
		return img_h2;
	}

	public String getImg_h3()
	{
		return img_h3;
	}

	public void setImg_h3( String img_h3 )
	{
		this.img_h3 = img_h3;
	}

	public void setImg_h2( String img_h2 )
	{
		this.img_h2 = img_h2;
	}

	public String getImg_full()
	{
		return img_full;
	}

	public void setImg_full( String img_full )
	{
		this.img_full = img_full;
	}

}
