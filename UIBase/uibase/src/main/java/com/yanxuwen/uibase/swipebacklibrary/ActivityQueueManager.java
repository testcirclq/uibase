package com.yanxuwen.uibase.swipebacklibrary;

import android.annotation.SuppressLint;
import android.os.Build;

import com.yanxuwen.uibase.base.BaseActivity;
import com.yanxuwen.uibase.base.BaseFragment;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by yxe on 2017/12/2.
 */
public class ActivityQueueManager {

    private static final ActivityQueueManager mInstance = new ActivityQueueManager();

    private final static String TAG = ActivityQueueManager.class.getSimpleName();
    private static LinkedList<Object> mQueue;

    private ActivityQueueManager() {
        ActivityQueueManager.mQueue = new LinkedList<Object>();
    }

    /**
     * 添加Activity
     *
     * @param activity
     * @return void
     * @throws
     */
    public void pushActivity(Object activity) {
        //添加新的Activity或者fragment的时候，则要把上一个的Activity或者fragment的预览图也就是我们向右滑动出现上一个界面的图片（mPreview）设置空
        try {
            Object lastObject = mQueue.getFirst();
            if (lastObject instanceof BaseActivity) {
                ((BaseActivity) lastObject).setPreviewBitmap(true);
            } else if (lastObject instanceof BaseFragment) {
                ((BaseFragment) lastObject).setPreviewBitmap(true);
            }

        } catch (Exception e) {
        }
        mInstance.doPushActivity(activity);
    }

    /**
     * 移除Activity
     *
     * @param activity
     * @return void
     * @throws
     */
    public void popActivity(Object activity) {
        mInstance.doPopActivity(activity);
        //移除activity或者Fragment 后，则吧上一个Activity或者Fragment设置下预览图
        try {
            Object lastObject = mQueue.getFirst();
            if (lastObject instanceof BaseActivity) {
                ((BaseActivity) lastObject).setPreviewBitmap(false);
            } else if (lastObject instanceof BaseFragment) {
                ((BaseFragment) lastObject).setPreviewBitmap(false);
            }
        } catch (Exception e) {
        }

    }

    /**
     * pop the stack top activity
     *
     * @return Activity 或者 Fragemnt
     * @throws
     */
    public Object pop() {
        if (mQueue != null && mQueue.size() > 0) {
            return mQueue.peek();
        } else {
            return null;
        }
    }


    /**
     * 获取指定的Activity
     *
     * @return Activity或者 Fragemnt
     * @throws
     */
    public Object popIndex(int postion) {
        if (mQueue != null && mQueue.size() > postion) {
            return mQueue.get(postion);
        } else {
            return null;
        }
    }

    /**
     * 关闭所有的Activity
     *
     * @return void
     * @throws
     */
    public void finishAllActivity() {
        mInstance.doFinishAll();
    }

    /**
     * 添加Activity
     */
    @SuppressLint("NewApi")
    private void doPushActivity(Object activity) {
        // 解决系统2.2版本的bug
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            mQueue.push(activity);
        } else {
            mQueue.addFirst(activity);
        }
    }

    /**
     * 移除Activity
     */
    private void doPopActivity(Object activity) {
        if (ActivityQueueManager.mQueue.contains(activity)) {
            ActivityQueueManager.mQueue.remove(activity);
        }
    }

    /**
     * 关闭所有的Activity
     */
    private void doFinishAll() {
        Iterator<Object> it = mQueue.iterator();
        while (it.hasNext()) {
            Object a = it.next();
            it.remove();
            if(a instanceof BaseActivity){
                ((BaseActivity)a).finish();
            } else if(a instanceof BaseFragment){
                ((BaseFragment)a).finish();
            }
        }
    }

    public static ActivityQueueManager getInstance() {
        return mInstance;
    }

    public LinkedList<Object> getActivityLinkQueue() {
        return mQueue;
    }

    public int getSize() {
        return mQueue.size();
    }


    /**
     * 关闭N个activities
     *
     * @param closeNumberActivities 关闭activity的个数
     */
    public void closeNumberActivities(int closeNumberActivities) {
// 关闭个数小于1的时候直接跳出
        if (closeNumberActivities <= 0) {
            return;
        }
        LinkedList<Object> mActivities = mQueue;
        if (mActivities != null && mActivities.size() <= 1) {
            return;
        }

        try {
            int countTemp = 0;
            // 倒序遍历acitivty
            for (int i = mActivities.size() - 1; i >= 0; i--) {
            // 如果当前页面为NativeAppActivity，则直接finish();
                Object object = mActivities.get(i);
                if (object != null) {
                    if(object instanceof BaseActivity){
                        ((BaseActivity)object).finish();
                    } else if(object instanceof BaseFragment){
                        ((BaseFragment)object).finish();
                    }
                    mActivities.remove(object);
                }
               // 其他情况下finish掉activity
                else {
               // 当前页面不能是最后一页
                    if (mActivities.size() > 1) {
                        if(object instanceof BaseActivity){
                            ((BaseActivity)object).finish();
                        } else if(object instanceof BaseFragment){
                            ((BaseFragment)object).finish();
                        }
                        mActivities.remove(object);
                        countTemp++;
                    } else {
                        i = -1;
                    }
                }
               // 退出循环
                if (countTemp == closeNumberActivities) {
                    i = -1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

