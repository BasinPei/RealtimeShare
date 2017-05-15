package cn.ysu.edu.realtimeshare.file.operation;

import android.content.Intent;
import android.net.Uri;

import java.io.File;

import cn.ysu.edu.realtimeshare.R;

/**
 * Created by Administrator on 2017/4/17.
 */
public class FileOperationOfType {
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_VIDEO = 2;

    public static final int TYPE_IMAGE = 3;
    public static final int TYPE_DOC = 4;
    public static final int TYPE_APK = 5;
    public static final int TYPE_HTML = 6;
    public static final int TYPE_PDF = 7;
    public static final int TYPE_PPT = 8;
    public static final int TYPE_TEXT = 9;
    public static final int TYPE_XLS = 10;
    public static final int TYPE_COMPRESSED = 11;
    public static final int TYPE_OTHERS = 12;

    public static int getFileType(String fileName) {
        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") ||
                end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            return TYPE_AUDIO;
        } else if (end.equals("3gp") || end.equals("mp4") || end.equals("wmv") ||
                end.equals("avi") || end.equals("rmvb") || end.equals("mkv")) {
            return TYPE_VIDEO;
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") ||
                end.equals("jpeg") || end.equals("bmp")) {
            return TYPE_IMAGE;
        } else if (end.equals("rar") || end.equals("zip") || end.equals("gzip") ||
                end.equals("bz2") || end.equals("7-zip")) {
            return TYPE_COMPRESSED;
        } else if (end.equals("apk")) {
            return TYPE_APK;
        } else if (end.equals("ppt")) {
            return TYPE_PPT;
        } else if (end.equals("html")) {
            return TYPE_HTML;
        } else if (end.equals("xls")) {
            return TYPE_XLS;
        } else if (end.equals("doc")) {
            return TYPE_DOC;
        } else if (end.equals("pdf")) {
            return TYPE_PDF;
        } else if (end.equals("txt")) {
            return TYPE_TEXT;
        } else {
            return TYPE_OTHERS;
        }
    }

    public static int getFileTypeIcon(File file){
        int fileType = getFileType(file.getName());
        int iconId = -1;

        switch (fileType) {
            case TYPE_AUDIO:
                iconId = R.mipmap.icon_music;
                break;
            case TYPE_VIDEO:
                iconId = R.mipmap.icon_video;
                break;
            case TYPE_IMAGE:
                iconId = R.mipmap.category_icon_image;
                break;
            case TYPE_DOC:
                iconId = R.mipmap.icon_doc;
                break;
            case TYPE_APK:
                iconId = R.mipmap.icon_exe;
                break;
            case TYPE_HTML:
                iconId = R.mipmap.icon_html;
                break;
            case TYPE_PDF:
                iconId = R.mipmap.icon_pdf;
                break;
            case TYPE_PPT:
                iconId = R.mipmap.icon_ppt;
                break;
            case TYPE_TEXT:
                iconId = R.mipmap.icon_text;
                break;
            case TYPE_XLS:
                iconId = R.mipmap.icon_xls;
                break;
            case TYPE_COMPRESSED:
                iconId = R.mipmap.icon_compressed_files;
                break;
            case TYPE_OTHERS:
                iconId = R.mipmap.icon_unknown;
                break;
            default:
                iconId = R.mipmap.icon_unknown;
                break;
        }
        return iconId;

    }

    public static Intent getOpenFileIntent(String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            return null;
        int fileType = getFileType(file.getName());
        switch (fileType) {
            case TYPE_AUDIO:
                return getAudioFileIntent(filePath);
            case TYPE_VIDEO:
                return getVideoFileIntent(filePath);
            case TYPE_IMAGE:
                return getImageFileIntent(filePath);
            case TYPE_DOC:
                return getWordFileIntent(filePath);
            case TYPE_APK:
                return getApkFileIntent(filePath);
            case TYPE_HTML:
                return getHtmlFileIntent(filePath);
            case TYPE_PDF:
                return getPdfFileIntent(filePath);
            case TYPE_PPT:
                return getPptFileIntent(filePath);
            case TYPE_TEXT:
                return getTextFileIntent(filePath);
            case TYPE_XLS:
                return getExcelFileIntent(filePath);
            /*case TYPE_COMPRESSED:
                return getAllIntent(filePath);
            case TYPE_OTHERS:
                return getAllIntent(filePath);*/
            default:
                return getAllIntent(filePath);
        }
    }

    //Android获取一个文件不能直接打开的intent
    public static Intent getAllIntent(String param) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "*/*");
        return intent;
    }

    //Android获取一个用于打开APK文件的intent
    public static Intent getApkFileIntent(String param) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        return intent;
    }

    //Android获取一个用于打开VIDEO文件的intent
    public static Intent getVideoFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "video/*");
        return intent;
    }

    //Android获取一个用于打开AUDIO文件的intent
    public static Intent getAudioFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "audio/*");
        return intent;
    }

    //Android获取一个用于打开Html文件的intent
    public static Intent getHtmlFileIntent(String param) {

        Uri uri = Uri.parse(param).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param).build();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    //Android获取一个用于打开图片文件的intent
    public static Intent getImageFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    //Android获取一个用于打开PPT文件的intent
    public static Intent getPptFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    //Android获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent(String param) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    //Android获取一个用于打开Word文件的intent
    public static Intent getWordFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }


    //Android获取一个用于打开文本文件的intent
    public static Intent getTextFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri2 = Uri.fromFile(new File(param));
        intent.setDataAndType(uri2, "text/plain");
        return intent;
    }

    //Android获取一个用于打开PDF文件的intent
    public static Intent getPdfFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/pdf");
        return intent;
    }
}
