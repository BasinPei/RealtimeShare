package cn.ysu.edu.realtimeshare.view.tab.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

/**
 * Created by chenpengfei on 2017/3/21.
 */
public class TabViewPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragmentArray;
    private FragmentManager mFragmentManager;

    public TabViewPagerAdapter(FragmentManager mFragmentManager, Fragment[] fragmentArray) {
        super(mFragmentManager);
        this.mFragmentManager = mFragmentManager;
        this.fragmentArray = fragmentArray;
    }


    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        fragment = fragmentArray[position];
        Bundle bundle = new Bundle();
        bundle.putString("id",String.valueOf(position));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return fragmentArray.length;
    }

    @Override
    public Fragment instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container,
                position);
        mFragmentManager.beginTransaction().show(fragment).commit();
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // super.destroyItem(container, position, object);
        Fragment fragment = fragmentArray[position];
        mFragmentManager.beginTransaction().hide(fragment).commit();
    }
}

