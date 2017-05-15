package cn.ysu.edu.realtimeshare.file;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.file.bean.FileProperty;

/**
 * Created by Administrator on 2017/4/17.
 */

public class FileItemScanAdapter extends BaseAdapter {
    protected LayoutInflater mLayoutInflater;
    protected ArrayList<FileProperty> mFileDataList = new ArrayList<>();
    protected Context mContext;

    public FileItemScanAdapter(Context context) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mFileDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.file_item_scan_layout, null);
            viewHolder.fileIcon = (ImageView) convertView.findViewById(R.id.file_icon);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.file_title);
            viewHolder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final FileProperty temp = mFileDataList.get(position);
        viewHolder.fileIcon.setBackgroundResource(temp.getIconSrcID());
        viewHolder.fileName.setText(temp.getFileName());
        viewHolder.fileSize.setText(temp.getFileSize());
        return convertView;
    }

    private class ViewHolder {
        ImageView fileIcon;
        TextView fileName;
        TextView fileSize;
    }

    public void resetData(ArrayList<FileProperty> fileInfo) {
        mFileDataList.clear();
        mFileDataList.addAll(fileInfo);
        notifyDataSetChanged();
    }
}
