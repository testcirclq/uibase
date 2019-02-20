package com.yanxuwen.uibase.statusbar;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBar {
    public static void setTranslucentStatus(Activity activity,boolean change)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            Window win = activity.getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |= bits;
            win.setAttributes(winParams);
        }
        //5.0,荣耀7i会有状态栏隐藏不了，但是酷狗音乐也会，应该属于手机问题。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			 NAVIGATION为虚拟键。打开布局就会在虚拟键下面
            Window window =activity. getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//			 |WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//					 | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
//			 window.setNavigationBarColor(Color.TRANSPARENT);
        }
        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0);//状态栏无背景
        if(change){
            setStatusBarTextColor(activity, 1);
        }
    }
    public static void setStatusBarTextColor(Activity context,int type){
        if (!isMiUIV6()){
            return;
        }
        Window window = context.getWindow();
        Class clazz = window.getClass();
        try {
            int tranceFlag = 0;
            int darkModeFlag = 0;
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_TRANSPARENT");
            tranceFlag = field.getInt(layoutParams);
            field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (type == 0){
                extraFlagField.invoke(window, tranceFlag, tranceFlag);//只需要状态栏透明
            }else if(type == 1){
                extraFlagField.invoke(window, tranceFlag | darkModeFlag, tranceFlag | darkModeFlag);//状态栏透明且黑色字体
            }else {
                extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
            }
        }catch (Exception e){

        }
    }
    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static boolean isMiUIV6() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            String name = prop.getProperty(KEY_MIUI_VERSION_NAME, "");
            if ("V6".equals(name)||"V7".equals(name)){
                return  true;
            }else {
                return false;
            }
//	            return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
//	                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
//	                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } catch (final IOException e) {
            return false;
        }
    }
}
