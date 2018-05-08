package com.hudong.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class MenuEntity implements Parcelable
{
	private String columnID;

	private String title;

	public String getColumnID()
	{
		return columnID;
	}

	public void setColumnID( String columnID )
	{
		this.columnID = columnID;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		dest.writeString( columnID );
		dest.writeString( title );
	}

	public static final Parcelable.Creator< MenuEntity > CREATOR = new Creator< MenuEntity >()
	{

		@Override
		public MenuEntity[] newArray( int size )
		{
			return new MenuEntity[ size ];
		}

		@Override
		public MenuEntity createFromParcel( Parcel source )
		{
			MenuEntity entity = new MenuEntity();
			entity.setColumnID( source.readString() );
			entity.setTitle( source.readString() );
			return entity;
		}
	};
}
