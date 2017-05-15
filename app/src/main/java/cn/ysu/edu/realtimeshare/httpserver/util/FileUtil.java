package cn.ysu.edu.realtimeshare.httpserver.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by KerriGan in 2016.4.25
 */
public class FileUtil
{
	private static String type = "*/*";
	public static String ip = "127.0.0.1";
	public static int port = 0;
	
	
	public static String getFileType(String uri)
	{
		if (uri == null)
		{
			return type; 
		}
		
		if (uri.endsWith(".mp3"))
		{
			return "audio/mpeg"; 
		}
		
		if (uri.endsWith(".mp4"))
		{
			return "video/mp4"; 
		} 
		
		return type; 
	}
}
