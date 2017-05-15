/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: HTTPServerThread.java
 *
 *	Revision;
 *
 *	10/10/03
 *		- first revision.
 *	
 ******************************************************************/

package cn.ysu.edu.realtimeshare.httpserver.http;

import android.util.Log;

import java.net.Socket;

import static cn.ysu.edu.realtimeshare.httpserver.util.EasyServer.TAG;


public class HTTPServerThread extends Thread
{
	private static final String tag = "HTTPServerThread";
	private HTTPServer httpServer;
	private Socket sock;

	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

	/**
	 *
	 * 
	 * @param httpServer
	 *
	 * @param sock
	 *
	 **/
	public HTTPServerThread(HTTPServer httpServer, Socket sock)
	{
		super("EasyServer.HTTPServerThread");
		this.httpServer = httpServer;
		this.sock = sock;
	}

	// //////////////////////////////////////////////
	// run
	// //////////////////////////////////////////////

	@Override
	public void run()
	{
		Log.d(TAG, "run: HttpServerTHread");
		HTTPSocket httpSock = new HTTPSocket(sock);
		if (httpSock.open() == false)
		{
			return;
		}
		

		HTTPRequest httpReq = new HTTPRequest();
		httpReq.setSocket(httpSock);

		while (httpReq.read() == true)
		{
			httpServer.performRequestListener(httpReq);
			if (httpReq.isKeepAlive() == false)
			{
				break;
			}
		}
		httpSock.close();
	}
}
