package com.yanxuwen.testuibase;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.yanxuwen.testuibase.base.MyBaseActivity;

public class TestActivity extends MyBaseActivity {

    @Override
    public int getLayoutResId() {
        return R.layout.activity_test;
    }

    @Override
    public void initView() {
        super.initView();
        ((TextView)findViewById(R.id.tv_activity)).setText("TestActivity");
        findViewById(R.id.tv_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不能使用原始的跳转
                onStartActivity(new Intent(context,TestActivity.class));
            }
        });
        findViewById(R.id.tv_fragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不能使用原始的跳转
                onStartFragemnt(new TestFragment());
            }
        });
    }

    @Override
    public void onBaseStart() {
        super.onBaseStart();
        Log.e("xxx", "onStart1:" );
    }

    @Override
    public void onBaseResume() {
        super.onBaseResume();
        Log.e("xxx", "onResume1:" );
    }
    @Override
    public void onBasePause() {
        super.onBasePause();
        Log.e("xxx", "onPause1:" );
    }
    @Override
    public void onBaseStop() {
        super.onBaseStop();
        Log.e("xxx", "onStop1:" );
    }
}
