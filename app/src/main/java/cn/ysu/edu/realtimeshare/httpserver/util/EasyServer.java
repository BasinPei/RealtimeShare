package cn.ysu.edu.realtimeshare.httpserver.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPRequest;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPResponse;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPServerList;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPStatus;
import cn.ysu.edu.realtimeshare.httpserver.servlet.File;
import cn.ysu.edu.realtimeshare.httpserver.servlet.ServletBase;

/**
 * Created by KerriGan on 2016/4/22.
 */

//        example:
//        byte[]content=array.toString().getBytes("utf-8");
//        HTTPResponse httpRes=new HTTPResponse();
//        httpRes.setContentType("*/*");
//        httpRes.setStatusCode(HTTPStatus.OK);
//        httpRes.setContentLength(content.length);
//        httpRes.setContent(content);
//        httpReq.post(httpRes);


//        HTTPResponse httpRes = new HTTPResponse();
//        httpRes.setContentType(contentType);
//        httpRes.setStatusCode(HTTPStatus.OK);
//        httpRes.setContentLength(contentLen);
//        httpRes.setContentInputStream(contentIn);
//
//        httpReq.post(httpRes);
//
//        contentIn.close();

public class EasyServer extends Thread implements cn.ysu.edu.realtimeshare.httpserver.http.HTTPRequestListener {

    public static final String TAG = "EasyServer";

    private HTTPServerList httpServerList = new HTTPServerList();

    public int HTTPPort = SharedFileOperation.HTTP_FILE_PORT;

    public HTTPServerList getHttpServerList() {
        return httpServerList;
    }

    public int getHTTPPort() {
        return HTTPPort;
    }

    public void setHTTPPort(int hTTPPort) {
        HTTPPort = hTTPPort;
    }

    @Override
    public void run() {
        super.run();

        int retryCnt = 0;

        int bindPort = getHTTPPort();

        HTTPServerList hsl = getHttpServerList();

        while (hsl.open(bindPort) == false) {
            retryCnt++;

            if (100 < retryCnt) {
                return;
            }
            setHTTPPort(bindPort + 1);
            bindPort = getHTTPPort();
        }

        hsl.addRequestListener(EasyServer.this);

        hsl.start();

    }

    @Override
    public void httpRequestReceived(HTTPRequest httpReq) {
        Log.d(TAG, "httpRequestReceived: ---------->");

        String uri = httpReq.getURI();

        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        httpReq.setURI(uri);

        HTTPResponse res = new HTTPResponse();
        res.setContentType("*/*");
        res.setStatusCode(HTTPStatus.OK);

        ServletBase file = new File();
        file.doGet(httpReq, res);

    }

}
