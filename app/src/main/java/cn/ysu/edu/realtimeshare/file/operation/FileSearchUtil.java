package cn.ysu.edu.realtimeshare.file.operation;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by BasinPei on 2017/4/15.
 */

public class FileSearchUtil {
    public static final String TAG = "FileSearchUtil";

    public static ArrayList<File> getAllMediaFile(String filterRootPath,Context context, ISearch search)
    {

        String linkType=" like '%.mp4'"+" or _data like '%.wmv'";
        linkType+=" or _data like '%.avi' or _data like '%.rmvb'"+" or _data like '%.mkv' or _data like '%.3gp'";
        return searchFilesByType(filterRootPath,context,linkType,search);
    }

    public static ArrayList<File> getAllMusicFile(String filterRootPath,Context context,ISearch search)
    {
        String linkType=" like '%.mp3' or _data like '%.wav'or _data like '%.m4a' or _data like '%.mid' or _data like '%.xmf' or _data like '%.ogg'";
        return searchFilesByType(filterRootPath,context,linkType,search);
    }

    public static ArrayList<File> getAllApkFile(String filterRootPath,Context context,ISearch search)
    {
        String linkType=" like '%.apk'";
        return searchFilesByType(filterRootPath,context,linkType,search);
    }


    public static ArrayList<File> getAllImageFile(String filterRootPath,Context context,ISearch search)
    {
        String linkType=" like '%.jpg' or _data like '%.png' or _data like '%.gif' or _data like '%.jpeg' or _data like '%.bmp'";
        return searchFilesByType(filterRootPath,context,linkType,search);
    }

    public static ArrayList<File> getAllDocFile(String filterRootPath,Context context,ISearch search)
    {
        String linkType=" like '%.txt' or _data like '%.doc' or _data like '%.ppt' or _data like '%.html' or _data like '%.xls' or _data like '%.pdf'";
        return searchFilesByType(filterRootPath,context,linkType,search);
    }

    public static ArrayList<File> getAllRarFile(String filterRootPath,Context context,ISearch search)
    {
        String linkType=" like '%.rar' or _data like '%.zip' or _data like '%.gzip' or _data like '%.bz2'" +
                " or _data like '%7-zip'";
        return searchFilesByType(filterRootPath,context,linkType,search);
    }

    public static ArrayList<File> searchFilesByType(String filterRootPath,Context context,String linkType,ISearch search)
    {

        ArrayList<File> fileList=new ArrayList<>();
        ContentResolver cr=context.getContentResolver();
        if(cr!=null)
        {
            Uri uri= android.provider.MediaStore.Files.getContentUri("external");
            Cursor c=cr.query(uri,new String[]{
                            android.provider.MediaStore.Files.FileColumns.DATA,
                            android.provider.MediaStore.Files.FileColumns.SIZE},
                    android.provider.MediaStore.Files.FileColumns.DATA+linkType,null,null);

            if(c!=null)
            {
                c.moveToFirst();
                while (!c.isAfterLast())
                {
                    String filePath=c.getString(
                            c.getColumnIndex(android.provider.MediaStore
                                    .Files.FileColumns.DATA));
                    if(filePath==null)
                        continue;
                    if(filePath.startsWith(filterRootPath)){
                        File f=new File(filePath);
                        fileList.add(f);

                        if(search!=null)
                            search.search(f);
                    }
                    c.moveToNext();
                }
                c.close();
            }
        }
        return fileList;
    }

    public interface ISearch
    {
        void search(File file);
    }

    public static String convertFileSize(float fileSize) {
        float size = fileSize * 1.0f;
        String res;
        float num;
        if (size >= 1024 && size <= 1024 * 1024) {
            num = size / 1024;
            res = String.valueOf(num);
            res = res.substring(0, res.indexOf('.') + 2);
            res += " KB";
        } else if (size >= 1024 * 1024 && size <= 1024 * 1024 * 1024) {
            num = size / 1024 / 1024;
            res = String.valueOf(num);
            res = res.substring(0, res.indexOf('.') + 2);
            res += " M";
        } else if (size >= 1024 * 1024 * 1024) {
            num = size / 1024 / 1024 / 1024;
            res = String.valueOf(num);
            res = res.substring(0, res.indexOf('.') + 2);
            res += " G";
        } else {
            res = String.valueOf(size);
            res += " B";
        }
        return res;
    }
}
