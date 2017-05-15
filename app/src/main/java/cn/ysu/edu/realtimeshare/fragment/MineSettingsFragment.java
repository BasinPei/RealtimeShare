package cn.ysu.edu.realtimeshare.fragment;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.activity.ScreenCaptureActivity;

/**
 * Created by Administrator on 2017/4/14.
 */

public class MineSettingsFragment extends Fragment {
    private View mContentView;
    private WifiP2pDevice mThisDevice = null;

    TextView tv_deviceName;

    LinearLayout screenCaptureAction;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_settings,null);
        initView();
        return mContentView;
    }

    private void initView() {
        tv_deviceName = (TextView) mContentView.findViewById(R.id.setting_device_name);
        updateThisDeviceName(mThisDevice);

        screenCaptureAction = (LinearLayout) mContentView.findViewById(R.id.setting_screen_capture);
        screenCaptureAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP)
                {
                    Toast.makeText(MineSettingsFragment.this.getActivity(),R.string.screen_capture_enable_false,Toast.LENGTH_LONG).show();
//                    Toast.makeText(MineSettingsFragment.this.getActivity(),R.string.screen_capture_tip,Toast.LENGTH_LONG).show();
                }else{
                    Intent startScreenCaptureIntent = new Intent(getActivity(),ScreenCaptureActivity.class);
                    startActivity(startScreenCaptureIntent);
                }
            }
        });
    }


    public void updateThisDeviceName(WifiP2pDevice thisDevice) {
        if(mThisDevice == null){
            mThisDevice = thisDevice;
        }

        if(tv_deviceName != null && thisDevice != null){
            tv_deviceName.setText(thisDevice.deviceName);
        }

    }
}
