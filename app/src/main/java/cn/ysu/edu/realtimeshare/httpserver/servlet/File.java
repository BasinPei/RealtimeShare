package cn.ysu.edu.realtimeshare.httpserver.servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.file.bean.FileProperty;
import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPRequest;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPResponse;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPStatus;
import cn.ysu.edu.realtimeshare.httpserver.util.FileUtil;

/**
 * Created by KerriGan on 2016/4/24.
 */
public class File implements ServletBase{

    public static String TAG="File";


    @Override
    public void doGet(HTTPRequest httpReq, HTTPResponse res) {

        String uri=httpReq.getURI();
        String filePaths = uri.substring(5);
        int indexOf = filePaths.indexOf("&");

        if (indexOf != -1)
        {
            filePaths = filePaths.substring(0, indexOf);
        }

        if(filePaths.startsWith("/"))
            filePaths=filePaths.substring(1);

        try
        {
            int index=filePaths.indexOf("/");
            if(index>=0)
            {
                filePaths=filePaths.substring(index+1);
            }

            filePaths=filePaths.substring(0, filePaths.indexOf("."));
        }catch (IndexOutOfBoundsException e)
        {
            httpReq.returnBadRequest();
            return;
        }


        try
        {
            filePaths=getFilePathByHash(Integer.valueOf(filePaths));
        }catch (Exception e)
        {
            e.printStackTrace();
            httpReq.returnBadRequest();
            return;
        }

        try
        {
            java.io.File file = new java.io.File(filePaths);

            long contentLen = file.length();

            String contentType = FileUtil.getFileType(filePaths);

            FileInputStream contentIn = new FileInputStream(file);

            if (contentLen <= 0 || contentType.length() <= 0
                    || contentIn == null)
            {
                httpReq.returnBadRequest();
                return ;
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
            return ;
        }
        catch (IOException e)
        {
            httpReq.returnBadRequest();
            return ;
        }
        catch (Exception e)
        {
            httpReq.returnBadRequest();
            return;
        }

        return ;
    }

    @Override
    public void doPost(HTTPRequest req, HTTPResponse res) {
        doGet(req,res);
    }

    public static String getFilePathByHash(int hash)
    {
        ArrayList<FileProperty> mSharedFileList = SharedFileOperation.getSharedFileList();
        for(int i=0;i<mSharedFileList.size();i++)
        {
            FileProperty f=mSharedFileList.get(i);
            if(f.hashCode()==hash)
            {
                return f.getFilePath();
            }
        }
        return null;
    }

    public static String getSuffixByPath(String path)
    {
        return path.substring(path.lastIndexOf("."));
    }

}
