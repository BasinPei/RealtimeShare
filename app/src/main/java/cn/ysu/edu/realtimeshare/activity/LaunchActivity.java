package cn.ysu.edu.realtimeshare.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.librtsp.PreferenceInfo;
import cn.ysu.edu.realtimeshare.view.indicator.IndicatorPagerAdapter;
import cn.ysu.edu.realtimeshare.view.indicator.IndicatorTransformer;

/**
 * Created by Administrator on 2017/5/7.
 */
public class LaunchActivity extends BaseExitActivity{
    private ArrayList<View> mDisplayView = new ArrayList<>();
    private IndicatorPagerAdapter mIndicatorPagerAdapter;
    private int mDistance;

    ViewPager viewPager;
    LinearLayout indicatorContainer;
    ImageView lightDot;
    Button btnNext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
    }

    private void initView() {
        btnNext = (Button) findViewById(R.id.bt_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(LaunchActivity.this);
                pref.edit().putBoolean(PreferenceInfo.PREF_IS_FIRST_LAUNCH,false).commit();
                startActivity(new Intent(LaunchActivity.this,MainActivity.class));
            }
        });

        LayoutInflater layoutInflater = getLayoutInflater().from(this);
        View indicatorFirst = layoutInflater.inflate(R.layout.indicator_first,null);
        View indicatorSecond = layoutInflater.inflate(R.layout.indicator_second,null);
        View indicatorThird = layoutInflater.inflate(R.layout.indicator_third,null);
        mDisplayView.add(indicatorFirst);
        mDisplayView.add(indicatorSecond);
        mDisplayView.add(indicatorThird);

        viewPager = (ViewPager) findViewById(R.id.view_pager_indicator);
        mIndicatorPagerAdapter = new IndicatorPagerAdapter(mDisplayView);
        viewPager.setAdapter(mIndicatorPagerAdapter);
//        viewPager.setPageTransformer(true,new IndicatorTransformer());

        indicatorContainer = (LinearLayout) findViewById(R.id.la_indicator);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0,0,40,0);

        ImageView firstDot = new ImageView(this);
        firstDot.setImageResource(R.drawable.gray_dot);
        indicatorContainer.addView(firstDot,layoutParams);
        firstDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });

        ImageView secondDot = new ImageView(this);
        secondDot.setImageResource(R.drawable.gray_dot);
        indicatorContainer.addView(secondDot,layoutParams);
        secondDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });

        ImageView thirdDot = new ImageView(this);
        thirdDot.setImageResource(R.drawable.gray_dot);
        indicatorContainer.addView(thirdDot,layoutParams);
        thirdDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });

        lightDot = (ImageView) findViewById(R.id.la_light_dot);
        lightDot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //获得两个圆点之间的距离
                mDistance = indicatorContainer.getChildAt(1).getLeft() - indicatorContainer.getChildAt(0).getLeft();
                lightDot.getViewTreeObserver().removeGlobalOnLayoutListener(this);

            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //页面滚动时小白点移动的距离，通过设置setLayoutparams不断更新位置
                float leftMargin = mDistance * (position + positionOffset);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lightDot.getLayoutParams();
                params.leftMargin = (int) leftMargin;
                lightDot.setLayoutParams(params);
            }

            @Override
            public void onPageSelected(int position) {
                //页面直接跳转时，设置小白点的margin
                float leftMargin = mDistance * position;
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lightDot.getLayoutParams();
                params.leftMargin = (int) leftMargin;
                lightDot.setLayoutParams(params);
                if(position == 2){
                    btnNext.setVisibility(View.VISIBLE);
                }
                if(position != 2 && btnNext.getVisibility() == View.VISIBLE){
                    btnNext.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
}
