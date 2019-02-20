package com.yanxuwen.testuibase.base;
import android.support.annotation.CallSuper;
import com.yanxuwen.testuibase.R;
import com.yanxuwen.uibase.base.BaseFragment;

/**
 * 自己定义的Base,这里简单的写个demo
 */
public class MyBaseFragment extends BaseFragment {
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
     * 设置可滑动的边缘尺寸大小，默认为半屏幕
     */
    @Override
    public int isEdgeSize() {
        return super.isEdgeSize();
    }

    /**进入动画, 或者在animator文件 重写common_open_enter */
    public int onEnterAnimator() {
        return super.onEnterAnimator();
    }

    /**退出动画，或者在animator文件 重写common_close_exit */
    public int onExitAnimator() {
        return super.onExitAnimator();
    }
    /**
     * 是否全屏，如果activity非全屏且非沉浸式(activity的isFull跟isImmersion都为false)，则无效，无法设置
     */
    @Override
    public boolean isFull() {
        return false;
    }

    /**
     * @是否显示标题
     */
    @Override
    public boolean isShowTitle() {
        return super.isShowTitle();
    }

    @Override
    public void onCreateView() {
        super.onCreateView();
        initView();
    }

    @CallSuper
    public void initView(){}
}
