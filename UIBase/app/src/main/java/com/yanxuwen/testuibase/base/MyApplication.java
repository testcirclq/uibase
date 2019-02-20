package com.yanxuwen.testuibase.base;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.yanxuwen.uibase.base.BaseApplication;

public class MyApplication  extends BaseApplication {
    public int index = 0;
    private static  MyApplication application;
    public static MyApplication getInstance() {
        if(application == null){
            application = new MyApplication();
            return application;

        }
        else return application;
    }

    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {//1
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
