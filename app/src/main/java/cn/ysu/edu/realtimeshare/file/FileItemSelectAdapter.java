package cn.ysu.edu.realtimeshare.file;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.file.bean.FileProperty;
import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;

/**
 * Created by Administrator on 2017/4/16.
 */

public class FileItemSelectAdapter extends FileItemScanAdapter {

    public FileItemSelectAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.file_item_select_layout, null);
            viewHolder.fileIcon = (ImageView) convertView.findViewById(R.id.file_icon);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.file_title);
            viewHolder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
            viewHolder.isSelected = (CheckBox) convertView.findViewById(R.id.file_selected);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final FileProperty temp = mFileDataList.get(position);
        viewHolder.fileIcon.setBackgroundResource(temp.getIconSrcID());
        viewHolder.fileName.setText(temp.getFileName());
        if (temp.isDirectory()) {
            viewHolder.fileSize.setVisibility(View.GONE);
            viewHolder.isSelected.setVisibility(View.GONE);
        } else {
            viewHolder.fileSize.setVisibility(View.VISIBLE);
            viewHolder.isSelected.setVisibility(View.VISIBLE);
            viewHolder.fileSize.setText(temp.getFileSize());
            viewHolder.isSelected.setChecked(temp.isSelected());
            viewHolder.isSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        SharedFileOperation.addSharedFile(temp);
                    }else {
                        SharedFileOperation.deleteSharedFile(temp);
                    }
                }
            });
        }

        return convertView;
    }

    class ViewHolder {
        ImageView fileIcon;
        TextView fileName;
        TextView fileSize;
        CheckBox isSelected;
    }

}