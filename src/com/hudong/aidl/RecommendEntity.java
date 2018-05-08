package com.hudong.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class RecommendEntity implements Parcelable
{
	public String columnID;

	public String url;

	public String getColumnID()
	{
		return columnID;
	}

	public void setColumnID( String columnID )
	{
		this.columnID = columnID;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl( String url )
	{
		this.url = url;
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
		dest.writeString( url );
	}

	public static final Parcelable.Creator< RecommendEntity > CREATOR = new Creator< RecommendEntity >()
	{

		@Override
		public RecommendEntity[] newArray( int size )
		{
			return new RecommendEntity[ size ];
		}

		@Override
		public RecommendEntity createFromParcel( Parcel source )
		{
			RecommendEntity entity = new RecommendEntity();
			entity.setColumnID( source.readString() );
			entity.setUrl( source.readString() );
			return entity;
		}
	};
}
