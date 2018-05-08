package com.launcher.apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.launcher.R;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class GroupConfigUtil
{
	private static final String GROUP = "group";
	private static final String NAME = "name";

	public static Map< String, Group > loadGroup( Context context, String xmlPath )
	{
		Map< String, Group > gMap = new HashMap< String, Group >();

		InputStream inStream = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			inStream = new FileInputStream( xmlPath );
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse( inStream );
			Element root = dom.getDocumentElement();

			NodeList items = root.getElementsByTagName( GROUP );

			for ( int i = 0; i < items.getLength(); i++ )
			{
				Element personNode = ( Element ) items.item( i );
				String name = personNode.getAttribute( NAME );

				Group group = new Group();
				group.groupName = name;
				ArrayList< Map< String, Object >> subList = new ArrayList< Map< String, Object >>();

				NodeList childsNodes = personNode.getChildNodes();
				for ( int j = 0; j < childsNodes.getLength(); j++ )
				{

					Node node = childsNodes.item( j );
					if ( node.getNodeType() == Node.ELEMENT_NODE )
					{
						Element childNode = ( Element ) node;

						Node value = childNode.getFirstChild();
						if ( null != value )
						{
							String pkgName = value.getNodeValue() == null ? " " : value
											.getNodeValue();
							subList.add( parseAppInfo( context, pkgName ) );
						}
					}
				}
				group.groupAppInfo = subList;
				gMap.put( name, group );
			}
		}
		catch ( Exception e )
		{
		}
		finally
		{
			if ( inStream != null )
			{
				try
				{
					inStream.close();
				}
				catch ( IOException e )
				{
				}
			}
		}
		return gMap;
	}

	public static void writeGroup( Map< String, Group > map, String xmlPath )
	{
		Iterator< String > iterator = map.keySet().iterator();

		// 先备份xml文件，如果写成功了，则删除备份文件
		File fromFile = null;
		File toFile = null;
		try
		{
			fromFile = new File( xmlPath );
			toFile = new File( xmlPath + ".bak" );
			if ( toFile.isFile() )
			{
				toFile.delete();
			}
			Util.copyfile( fromFile, toFile, true );
		}
		catch ( Exception ex )
		{
			ex.printStackTrace();
		}

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element rootEle = doc.createElement( "root" );
			doc.appendChild( rootEle );

			String key = null;
			while ( iterator.hasNext() )
			{
				key = iterator.next();
				Group info = map.get( key );
				Element groupEle = doc.createElement( GROUP );
				groupEle.setAttribute( NAME, info.groupName );

				if ( info.groupAppInfo != null )
				{
					for ( int i = 0; i < info.groupAppInfo.size(); i++ )
					{
						if ( info.groupAppInfo.get( i ).get( "package" ).toString() != null
										&& !"".equals( info.groupAppInfo.get( i ).get( "package" )
														.toString() ) )
						{
							Element packEle = doc.createElement( "package" );
							packEle.appendChild( doc.createTextNode( info.groupAppInfo.get( i )
											.get( "package" ).toString() ) );
							groupEle.appendChild( packEle );
						}
					}
				}
				rootEle.appendChild( groupEle );
			}
			DOMSource source = new DOMSource( doc );
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty( OutputKeys.ENCODING, "utf-8" );
			transformer.setOutputProperty( OutputKeys.INDENT, "no" );

			FileOutputStream outputStream = new FileOutputStream( xmlPath );
			PrintWriter pw = new PrintWriter( outputStream );
			StreamResult result = new StreamResult( pw );
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			transformer.setOutputProperty( OutputKeys.CDATA_SECTION_ELEMENTS, "yes" );
			transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
			transformer.transform( source, result );
			if ( outputStream != null )
			{
				outputStream.close();
			}

			if ( toFile != null && toFile.isFile() )
			{
				toFile.delete();
			}
		}
		catch ( Exception e )
		{
		}
	}

	public static Map< String, Object > parseAppInfo( Context context, String pkgName )
	{
		Map< String, Object > map = new HashMap< String, Object >();
		PackageManager pm = context.getPackageManager();
		try
		{
			ApplicationInfo info = pm.getApplicationInfo( pkgName, 0 );

			map.put( "image", info.loadIcon( pm ) );
			map.put( "name", info.loadLabel( pm ).toString() );
			map.put( "package", pkgName );
		}
		catch ( NameNotFoundException e )
		{
			map.put( "image", context.getResources().getDrawable( R.drawable.icon_add ) );
			map.put( "name", context.getResources().getString( R.string.custom ) );
			if ( pkgName == null || pkgName.equals( "" ) )
			{
				map.put( "package", " " );
			}
			else
			{
				map.put( "package", pkgName );
			}
		}
		return map;
	}
}
