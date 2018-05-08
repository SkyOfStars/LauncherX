package com.hudong.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 回看节目数据
 * 
 * @author blueberry
 *
 */
public class RePlayProgEntity implements Parcelable
{
	private String endtime;// 结束时间
	private String starttime;// 开始时间
	private String name;// 节目名称
	private String itemid;// 节目号
	private String date;
	private String jumpUrl_biaoqing;
	private String jumpUrl_gaoqing;
	private String jumpUrl_chaoqing;
	
	public String getJumpUrl_biaoqing()
	{
		return jumpUrl_biaoqing;
	}

	public void setJumpUrl_biaoqing( String jumpUrl_biaoqing )
	{
		this.jumpUrl_biaoqing = jumpUrl_biaoqing;
	}

	public String getJumpUrl_gaoqing()
	{
		return jumpUrl_gaoqing;
	}

	public void setJumpUrl_gaoqing( String jumpUrl_gaoqing )
	{
		this.jumpUrl_gaoqing = jumpUrl_gaoqing;
	}

	public String getJumpUrl_chaoqing()
	{
		return jumpUrl_chaoqing;
	}

	public void setJumpUrl_chaoqing( String jumpUrl_chaoqing )
	{
		this.jumpUrl_chaoqing = jumpUrl_chaoqing;
	}

	public RePlayProgEntity()
	{
		super();
	}

	public String getEndtime()
	{
		return endtime;
	}

	public void setEndtime( String endtime )
	{
		this.endtime = endtime;
	}

	public String getStarttime()
	{
		return starttime;
	}

	public void setStarttime( String starttime )
	{
		this.starttime = starttime;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getItemid()
	{
		return itemid;
	}

	public void setItemid( String itemid )
	{
		this.itemid = itemid;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate( String date )
	{
		this.date = date;
	}


	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		dest.writeString( endtime );
		dest.writeString( starttime );
		dest.writeString( name );
		dest.writeString( itemid );
		dest.writeString( date );
		dest.writeString( jumpUrl_biaoqing );
		dest.writeString( jumpUrl_gaoqing );
		dest.writeString( jumpUrl_chaoqing );
	}

	public static final Parcelable.Creator< RePlayProgEntity > CREATOR = new Creator< RePlayProgEntity >()
	{

		@Override
		public RePlayProgEntity[] newArray( int size )
		{
			return new RePlayProgEntity[ size ];
		}

		@Override
		public RePlayProgEntity createFromParcel( Parcel source )
		{
			RePlayProgEntity entity = new RePlayProgEntity();
			entity.setEndtime( source.readString() );
			entity.setStarttime( source.readString() );
			entity.setName( source.readString() );
			entity.setItemid( source.readString() );
			entity.setDate( source.readString() );
			entity.setJumpUrl_biaoqing(  source.readString() );
			entity.setJumpUrl_gaoqing(  source.readString() );
			entity.setJumpUrl_chaoqing(  source.readString() );
			return entity;
		}
	};
}
