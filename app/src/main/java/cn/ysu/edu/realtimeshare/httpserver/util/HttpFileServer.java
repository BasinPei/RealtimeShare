package cn.ysu.edu.realtimeshare.httpserver.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Vector;

import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPRequest;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPResponse;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPServer;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPServerList;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPStatus;
import cn.ysu.edu.realtimeshare.service.InitService;


/**
 * Created by KerriGan on 2016/4/10.
 */
public class HttpFileServer extends Thread implements cn.ysu.edu.realtimeshare.httpserver.http.HTTPRequestListener{
    public static final String CONTENT_EXPORT_URI = "/http";

    public static final String tag = "mobilephone.sharefile.server";

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

        int retryCnt = 0;

        int bindPort = getHTTPPort();

        HTTPServerList hsl = getHttpServerList();

        while (hsl.open(bindPort)==false)
        {
            retryCnt++;

            if (100 < retryCnt)
            {
                if(_listener!=null)
                    _listener.ready("",0);
                return;
            }
            setHTTPPort(bindPort + 1);
            bindPort = getHTTPPort();
        }

        hsl.addRequestListener(this);

        hsl.start();

        FileUtil.ip = hsl.getHTTPServer(0).getBindAddress();
        FileUtil.port = hsl.getHTTPServer(0).getBindPort();
        if(_listener!=null)
        {
            ServerList=hsl;

            if(!hsl.getHTTPServer(0).isOpened() || hsl.getHTTPServer(0).getServerSock().isClosed()
                    || FileUtil.port!=InitService.GROUP_OWNER_PORT)
                _listener.ready("",0);
            else
                _listener.ready(FileUtil.ip,FileUtil.port);


            _listener=null;
        }
    }

    @Override
    public void httpRequestReceived(HTTPRequest httpReq)
    {

        String uri = httpReq.getURI();

        uri=URLDecoder.decode(uri);

        if (uri.startsWith(CONTENT_EXPORT_URI) == false)
        {
            httpReq.returnBadRequest();
            return;
        }

        if(httpReq.getHeaderValue("param").equals("getHttpFiles"))
        {

            try {
                if(_shareFileList!=null)
                {
                    JSONArray array=new JSONArray();

                    for(int i=0;i<_shareFileList.size();i++)
                    {
                        File file=_shareFileList.get(i);
                        JSONObject obj=new JSONObject();
                        obj.put("name",file.getName());
                        obj.put("path",file.getPath());
                        array.put(obj);
                    }

                    byte[] content=array.toString().getBytes("utf-8");
                    HTTPResponse httpRes=new HTTPResponse();
                    httpRes.setContentType("*/*");
                    httpRes.setStatusCode(HTTPStatus.OK);
                    httpRes.setContentLength(content.length);
                    httpRes.setContent(content);
                    httpReq.post(httpRes);

                }
                else
                {
                   httpReq.returnOK();
                }


            } catch (IOException e) {
                httpReq.returnBadRequest();
                e.printStackTrace();
            } catch (JSONException e) {
                httpReq.returnBadRequest();
                e.printStackTrace();
            }

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

        String filePaths = uri.substring(5);


        int indexOf = filePaths.indexOf("&");

        if (indexOf != -1)
        {
            filePaths = filePaths.substring(0, indexOf);
        }


        try
        {
            File file = new File(filePaths);

            long contentLen = file.length();

            String contentType = FileUtil.getFileType(filePaths);

            FileInputStream contentIn = new FileInputStream(file);

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
        catch (IOException e)
        {
            httpReq.returnBadRequest();
            return;
        }

    }

    /**
     *  return the actual http request path
     */
    public static String getRequestPathByLocalPath(String path)
    {

        String newPath = null;
        String ipVal = FileUtil.ip;
        int portVal = FileUtil.port;
        String httpReq = "http://" + ipVal + ":" + portVal + CONTENT_EXPORT_URI;

        try {
            path = URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        newPath = httpReq + path;

        return newPath;
    }

    public static IReady _listener=null;

    /**
     *  only if listener invoked , the listener will be erased.
     */
    public static void setServerListener(IReady listener)
    {
        _listener=listener;
    }


    public interface IReady
    {
        void ready(String hostIP, int port);
    }

    private static ArrayList<File> _shareFileList=null;

    public static void setShareFileList(ArrayList<File> list)
    {
        _shareFileList=list;
    }

    public static Vector<HTTPServer> ServerList=null;
}
