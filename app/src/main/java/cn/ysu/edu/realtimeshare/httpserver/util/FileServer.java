package cn.ysu.edu.realtimeshare.httpserver.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;

import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPRequest;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPResponse;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPServerList;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPStatus;
import cn.ysu.edu.realtimeshare.service.InitService;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;


public class FileServer extends Thread implements cn.ysu.edu.realtimeshare.httpserver.http.HTTPRequestListener
{
	
	public static final String CONTENT_EXPORT_URI = "/smb";
	public static final String tag = "dlna.certus.iptv.sharefile.server.ShareFileManager";
	private HTTPServerList httpServerList = new HTTPServerList();

	private int HTTPPort = SharedFileOperation.HTTP_FILE_PORT;

	private String bindIP = null;

	
	public String getBindIP()
	{
		return bindIP;
	}

	public void setBindIP(String bindIP)
	{
		this.bindIP = bindIP;
	}

	public HTTPServerList getHttpServerList()
	{
		return httpServerList;
	}

	public void setHttpServerList(HTTPServerList httpServerList)
	{
		this.httpServerList = httpServerList;
	}

	public int getHTTPPort()
	{
		return HTTPPort;
	}

	public void setHTTPPort(int hTTPPort)
	{
		HTTPPort = hTTPPort;
	}

	@Override
	public void run()
	{
		super.run();

		/**************************************************
		 * 
		 * ����CyberGarage.http�����������չ�������
		 * 
		 *************************************************/
		// ���Դ���
		int retryCnt = 0;
		int bindPort = getHTTPPort();

		HTTPServerList hsl = getHttpServerList();
		while (hsl.open(bindPort) == false)
		{
			retryCnt++;
			// ���Դ������ڷ��������Դ���ʱ����
			if (100 < retryCnt)
			{
				return;
			}
			setHTTPPort(bindPort + 1);
			bindPort = getHTTPPort();
		}
		// �������е�ÿ��HTTPServer�������HTTPRequestListener����
		hsl.addRequestListener(this);
		// ���ü���������HTTPServer��start����
		hsl.start(); 
		
		FileUtil.ip = hsl.getHTTPServer(0).getBindAddress();
		FileUtil.port = hsl.getHTTPServer(0).getBindPort();
		 
	}

	@Override
	public void httpRequestReceived(HTTPRequest httpReq)
	{

		String uri = httpReq.getURI();

		if (uri.startsWith(CONTENT_EXPORT_URI) == false)
		{
			httpReq.returnBadRequest();
			return;
		}

		try
		{
			uri = URLDecoder.decode(uri, "UTF-8");
		}
		catch (UnsupportedEncodingException e1)
		{ 
			e1.printStackTrace();
		}
		//��ȡ�ļ�����Ϣ
		String filePaths = "smb://" + uri.substring(5);
		
		//�ж�uri���Ƿ��������
		int indexOf = filePaths.indexOf("&");
		
        if (indexOf != -1)
        {
        	filePaths = filePaths.substring(0, indexOf);
        }
		

		try
		{
			SmbFile file = new SmbFile(filePaths);
			
			// ��ȡ�ļ��Ĵ�С
			long contentLen = file.length();
			// ��ȡ�ļ�����
			String contentType = FileUtil.getFileType(filePaths);
			// ��ȡ���ļ���
			InputStream contentIn = file.getInputStream();

			if (contentLen <= 0 || contentType.length() <= 0
					|| contentIn == null)
			{
				httpReq.returnBadRequest();
				return;
			} 
		 
			
			HTTPResponse httpRes = new HTTPResponse();
			httpRes.setContentType(contentType);
			httpRes.setStatusCode(HTTPStatus.OK);
			httpRes.setContentLength(contentLen);
			httpRes.setContentInputStream(contentIn);

			httpReq.post(httpRes);

		    contentIn.close(); 
		}
		catch (MalformedURLException e)
		{
			httpReq.returnBadRequest();
			return;
		}
		catch (SmbException e)
		{
			httpReq.returnBadRequest();
			return;
		}
		catch (IOException e)
		{
			httpReq.returnBadRequest();
			return;
		}

	}

}
