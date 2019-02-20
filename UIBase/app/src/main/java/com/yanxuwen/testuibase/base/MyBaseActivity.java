package com.yanxuwen.testuibase.base;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import com.yanxuwen.testuibase.R;
import com.yanxuwen.uibase.base.BaseActivity;

import org.jetbrains.annotations.Nullable;

/**
 * 自己定义的Base,这里简单的写个demo
 */
public  class MyBaseActivity extends BaseActivity {

    @CallSuper
    public void initView(){}

    /**
     * @设置布局，子类一定要重写
     */
    @Override
    public int getLayoutResId() {
        return 0;
    }

    /**
     * 设置标题布局，如果return 0，则没有布局
     */
    @Override
    public int getLayoutTitleResId() {
        return  R.layout.title_layout;
    }

    /**
     * 是否全屏，该全屏不会因此状态栏
     */
    @Override
    public boolean isFull() {
        return false;
    }
    /**是否沉浸式，沉浸式目前问题是白色背景下，文字会看不到，目前仅支持miui6以上k可以更改颜色，
     * 可以使用6.0自带的设置文字颜色，但目前会有闪烁问题建议不要用
     * 该功能isFull true 全屏的时候会失效，
     */
    @Override
    public boolean isImmersion() {
        return true;
    }

    /**
     * 设置可滑动的边缘尺寸大小，默认为半屏幕
     */
    @Override
    public int isEdgeSize() {
        return super.isEdgeSize();
    }

    /**
     * 是否显示标题
     */
    @Override
    public boolean isShowTitle() {
        return super.isShowTitle();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }
}
