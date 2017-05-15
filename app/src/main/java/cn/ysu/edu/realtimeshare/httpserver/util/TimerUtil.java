/******************************************************************
*
*	CyberUtil for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: TimerUtil.java
*
*	Revision:
*
*	01/15/03
*		- first revision.
*
******************************************************************/

package cn.ysu.edu.realtimeshare.httpserver.util;

public final class TimerUtil
{
	public final static void wait(int waitTime)
	{
		try {
			Thread.sleep(waitTime);
		}
		catch (Exception e) {}
	}

	/**
	 * @param  time
	 */
	public final static void waitRandom(int time)
	{
		int waitTime = (int)(Math.random() * time);		
		try {
			Thread.sleep(waitTime);
		}
		catch (Exception e) {}
	}
}

