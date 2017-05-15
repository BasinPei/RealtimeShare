package cn.ysu.edu.realtimeshare.httpserver.servlet;


import cn.ysu.edu.realtimeshare.httpserver.http.HTTPRequest;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPResponse;

/**
 * Created by KerriGan on 2016/4/22.
 */
public interface ServletBase {

    void doGet(HTTPRequest httpReq, HTTPResponse httpRes);

    void doPost(HTTPRequest httpReq, HTTPResponse httpRes);

}
