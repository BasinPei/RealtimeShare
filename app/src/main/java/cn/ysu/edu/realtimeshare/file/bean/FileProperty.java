package cn.ysu.edu.realtimeshare.file.bean;

import java.io.Serializable;

/**
 * Created by BasinPei on 2017/4/15.
 */

public class FileProperty implements Serializable{
    public static final String NAME_KEY = "name_key";
    public static final String PATH_KEY = "path_key";
    public static final String SIZE_KEY = "size_key";
    public static final String ICON_KEY = "icon_key";

    private boolean isDirectory;
    private String fileName;
    private String filePath;
    private String fileSize;
    private int iconSrcID;
    private boolean isSelected;

    public FileProperty() {
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getIconSrcID() {
        return iconSrcID;
    }

    public void setIconSrcID(int iconSrcID) {
        this.iconSrcID = iconSrcID;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileProperty that = (FileProperty) o;

        if (isDirectory != that.isDirectory) return false;
        if (iconSrcID != that.iconSrcID) return false;
        if(!fileSize.equals(that.fileSize)) return false;
        if(!filePath.equals(that.filePath)) return false;
        if(!fileName.equals(that.fileName)) return false;
        return true;

    }

    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 31 * result + filePath.hashCode();
        result = 31 * result + fileSize.hashCode();
        result = 31 * result + iconSrcID;
        return result;
    }

    @Override
    public String toString() {
        return "FileProperty{" +
                "isDirectory=" + isDirectory +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", iconSrcID=" + iconSrcID +
                ", isSelected=" + isSelected +
                '}';
    }
}
