package com.yanxuwen.testuibase;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.yanxuwen.testuibase.base.MyApplication;
import com.yanxuwen.testuibase.base.MyBaseFragment;


public class TestFragment extends MyBaseFragment {

    int index = -1;

    /**
     * @return
     */
    @Override
    public int getLayoutResId() {
        return R.layout.activity_frament;
    }

    @Override
    public void initView() {
        super.initView();
        index = (MyApplication.getInstance()).index++;
        ((TextView) getRootView().findViewById(R.id.tv_fragment)).setText("TestFragment" + index);
        getRootView().findViewById(R.id.tv_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不能使用原始的跳转
                baseActivity.onStartActivity(new Intent(getActivity(),TestActivity.class));
            }
        });
        getRootView().findViewById(R.id.tv_fragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不能使用原始的跳转
                onStartFragemnt(new TestFragment());
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("xxx", "onDestroy:" + index);
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e("xxx", "onHiddenChanged :" + hidden + " : " + index);
    }

    @Override
    public void onBaseStart() {
        super.onBaseStart();
        Log.e("xxx", "onStart:"  + index);
    }

    @Override
    public void onBaseResume() {
        super.onBaseResume();
        Log.e("xxx", "onResume:"  + index);
    }
    @Override
    public void onBasePause() {
        super.onBasePause();
        Log.e("xxx", "onPause:"  + index);
    }
    @Override
    public void onBaseStop() {
        super.onBaseStop();
        Log.e("xxx", "onStop:"  + index);
    }


}
