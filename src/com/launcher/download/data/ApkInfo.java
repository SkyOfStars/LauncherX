package com.launcher.download.data;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

@SuppressWarnings( "serial" )
public class ApkInfo implements Serializable
{
	public String mTitle;
	public String mType;
	public String mId;
	public String mImg;
	public String mPackage;
	public String mVersion;
	public String mNewVersion;
	public String mDeveloper;
	public String mDetails;
	public String mDownUrl;
	public int mVerCode = 0;
	public String mIcon;
	public Drawable mIconDrawable;
	public float mMark;
	public long mSize;
	public String mContorlMode;
	public String mState;
	public boolean isChecked = false;
}
