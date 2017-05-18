package cn.ysu.edu.realtimeshare.file;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.file.bean.FileProperty;
import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;

/**
 * Created by Administrator on 2017/4/16.
 */

public class FileItemSelectAdapter extends FileItemScanAdapter implements Filterable{
    private FileFilter mFileFilter;
    private ArrayList<FileProperty> mFilterOriginList = new ArrayList<>();
    private Map<FileProperty,Boolean> mSelectedMap;

    public FileItemSelectAdapter(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        return mFilterOriginList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFilterOriginList.get(position);
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
            convertView = mLayoutInflater.inflate(R.layout.file_item_select_layout, null);
            viewHolder.fileIcon = (ImageView) convertView.findViewById(R.id.file_icon);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.file_title);
            viewHolder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
            viewHolder.isSelected = (CheckBox) convertView.findViewById(R.id.file_selected);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final FileProperty temp = mFilterOriginList.get(position);
        viewHolder.fileIcon.setBackgroundResource(temp.getIconSrcID());
        viewHolder.fileName.setText(temp.getFileName());
        if (temp.isDirectory()) {
            viewHolder.fileSize.setVisibility(View.GONE);
            viewHolder.isSelected.setVisibility(View.GONE);
        } else {
            viewHolder.fileSize.setVisibility(View.VISIBLE);
            viewHolder.isSelected.setVisibility(View.VISIBLE);
            viewHolder.fileSize.setText(temp.getFileSize());

            viewHolder.isSelected.setChecked(mSelectedMap.get(temp));
            viewHolder.isSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean lastSelected = mSelectedMap.get(temp);
                    if(!lastSelected){
                        SharedFileOperation.addSharedFile(temp);
                    }else {
                        SharedFileOperation.deleteSharedFile(temp);
                    }
                    mSelectedMap.put(temp,!lastSelected);

                }
            });
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if(null == mFileFilter){
            mFileFilter = new FileFilter();
        }
        return mFileFilter;
    }

    @Override
    public void resetData(ArrayList<FileProperty> fileInfo) {
        if(mSelectedMap == null){
            mSelectedMap = new HashMap<>(fileInfo.size());
            for(FileProperty selectedHolder:fileInfo){
                boolean isSelected = false;
                for(FileProperty judgeSelected:SharedFileOperation.getSharedFileList()){
                    if(selectedHolder.equals(judgeSelected)){
                        isSelected = true;
                        break;
                    }
                }
                mSelectedMap.put(selectedHolder,isSelected);
            }
        }
        mFileDataList.clear();
        mFileDataList.addAll(fileInfo);

        mFilterOriginList.clear();
        mFilterOriginList.addAll(mFileDataList);
        notifyDataSetChanged();
    }

    class ViewHolder {
        ImageView fileIcon;
        TextView fileName;
        TextView fileSize;
        CheckBox isSelected;
    }

    class FileFilter extends Filter{
        //该方法在子线程中执行
        //自定义过滤规则
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            ArrayList<FileProperty> fileterDate = new ArrayList<>();
            String filterString = constraint.toString().trim();

            if(TextUtils.isEmpty(filterString)){
                fileterDate.clear();
                fileterDate.addAll(mFileDataList);
            }else{
                //过滤新数据
                for(FileProperty filterTemp:mFileDataList){
                    if(filterTemp.getFileName().contains(constraint)){
                        fileterDate.add(filterTemp);
                    }
                }
            }
            results.values = fileterDate;
            results.count = fileterDate.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilterOriginList.clear();
            mFilterOriginList.addAll((ArrayList<FileProperty>) results.values);

            if(mFilterOriginList.size() > 0){
                FileItemSelectAdapter.this.resetData(mFilterOriginList);
            }else{
                FileItemSelectAdapter.this.notifyDataSetInvalidated();
            }
        }
    }

}