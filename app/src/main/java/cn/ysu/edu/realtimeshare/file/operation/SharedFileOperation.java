package cn.ysu.edu.realtimeshare.file.operation;

import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.file.bean.FileProperty;

/**
 * Created by BasinPei on 2017/4/18.
 */

public class SharedFileOperation {
    public static final int HTTP_FILE_PORT = 8008;
    //是否分享屏幕
    private static boolean IS_SHARE_SCREEN = false;

    public static  void setIsShareScreen(boolean isShareScreen){
        IS_SHARE_SCREEN = isShareScreen;
    }

    public static boolean getIsShareScreen(){
        return IS_SHARE_SCREEN;
    }

    private static ArrayList<FileProperty> mSharedFileListData = new ArrayList<>();
    public static boolean checkFileSelected(FileProperty file)
    {
        for(FileProperty fileProperty:mSharedFileListData){
            if(file.equals(fileProperty)){
                return true;
            }
        }
        return false;
    }

    public static void addSharedFile(FileProperty fileProperty){
        mSharedFileListData.add(fileProperty);
    }

    public static void deleteSharedFile(FileProperty fileProperty){
        mSharedFileListData.remove(fileProperty);
    }

    public static ArrayList<FileProperty> getSharedFileList(){
        return mSharedFileListData;
    }

    public static void setSharedFileList(ArrayList<FileProperty> sharedFileList){
        mSharedFileListData.clear();
        mSharedFileListData.addAll(sharedFileList);
    }

}
