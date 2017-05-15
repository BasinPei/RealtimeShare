package cn.ysu.edu.realtimeshare.view.indicator;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/7.
 */

public class IndicatorPagerAdapter extends PagerAdapter {
    private ArrayList<View> mDisplayView = new ArrayList<>();

    public IndicatorPagerAdapter(ArrayList<View> displayView){
        mDisplayView.clear();
        mDisplayView.addAll(displayView);
    }

    @Override
    public int getCount() {
        return mDisplayView.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mDisplayView.get(position));
        return mDisplayView.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mDisplayView.get(position));
    }
}
