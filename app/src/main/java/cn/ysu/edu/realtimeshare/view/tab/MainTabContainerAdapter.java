package cn.ysu.edu.realtimeshare.view.tab;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.media.MediaBrowserCompat;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.view.tab.adapter.BaseAdapter;


/**
 * Created by chenpengfei on 2017/3/21.
 */
public class MainTabContainerAdapter extends BaseAdapter {

    private Fragment[] fragmentArray;
    private FragmentManager fragmentManager;
    private Context mContext;

    public MainTabContainerAdapter(Context context,FragmentManager fragmentManager, Fragment[] fragmentArray) {
        this.mContext = context;
        this.fragmentManager = fragmentManager;
        this.fragmentArray = fragmentArray;
    }

    @Override
    public int getCount() {
        return fragmentArray.length;
    }


    @Override
    public String[] getTextArray() {
        Resources resources = mContext.getResources();
        String tab_local = resources.getString(R.string.tab_local_device);
        String tab_nearby = resources.getString(R.string.tab_nearby_device);
        String tab_mine = resources.getString(R.string.tab_mine);
        return new String[] {tab_local,tab_nearby,tab_mine};
    }

    @Override
    public Fragment[] getFragmentArray() {
        return fragmentArray;
    }

    @Override
    public int[] getIconImageArray() {
        return new int[] {R.mipmap.ic_tab_local, R.mipmap.ic_tab_nearby, R.mipmap.ic_tab_me};
    }

    @Override
    public int[] getSelectedIconImageArray() {
        return new int[] {R.mipmap.ic_tab_local_focused, R.mipmap.ic_tab_nearby_focused, R.mipmap.ic_tab_me_focused};
    }

    @Override
    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }
}
