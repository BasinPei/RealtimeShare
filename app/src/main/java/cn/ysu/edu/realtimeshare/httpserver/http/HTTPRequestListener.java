/******************************************************************
*
*	CyberHTTP for Java
*
*	Copyright (C) Satoshi Konno 2002
*
*	File: HTTPRequestListener.java
*
*	Revision;
*
*	12/13/02
*		- first revision.
*	
******************************************************************/

package cn.ysu.edu.realtimeshare.httpserver.http;

public interface HTTPRequestListener
{
	void httpRequestReceived(HTTPRequest httpReq);
}
