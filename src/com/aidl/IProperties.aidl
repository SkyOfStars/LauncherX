package com.aidl;

interface IProperties
{
	String getProperty( String key );
	void setProperty( String key, String value );
	String getConfig( String key );
	void setConfig( String key, String value );
}