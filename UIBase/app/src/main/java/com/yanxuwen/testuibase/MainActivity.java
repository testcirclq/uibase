package com.yanxuwen.testuibase;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.yanxuwen.testuibase.base.MyBaseActivity;

public class MainActivity extends MyBaseActivity {

    /**
     * @设置不可滑动
     */
    @Override
    public boolean isSlideable() {
        return false;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        super.initView();
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
        Log.e("xxx", "onStart:" );
    }

    @Override
    public void onBaseResume() {
        super.onBaseResume();
        Log.e("xxx", "onResume:" );
    }
    @Override
    public void onBasePause() {
        super.onBasePause();
        Log.e("xxx", "onPause:" );
    }
    @Override
    public void onBaseStop() {
        super.onBaseStop();
        Log.e("xxx", "onStop:" );
    }


}
