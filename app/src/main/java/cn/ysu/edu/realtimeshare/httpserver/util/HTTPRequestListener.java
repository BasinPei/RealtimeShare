package cn.ysu.edu.realtimeshare.httpserver.util;


import cn.ysu.edu.realtimeshare.httpserver.http.HTTPRequest;

public interface HTTPRequestListener
{
	public void httpRequestReceived(HTTPRequest httpReq);

}
