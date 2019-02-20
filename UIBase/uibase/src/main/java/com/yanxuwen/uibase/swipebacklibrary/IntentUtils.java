package com.yanxuwen.uibase.swipebacklibrary;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.yanxuwen.uibase.base.BaseFragment;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by yanxuwen on 15/10/15.
 */
public class IntentUtils {

    private static final IntentUtils INSTANCE = new IntentUtils();

    private LinkedHashMap<String, BitmapItem> mCachedBitmaps;

    private IntentUtils() {
        mCachedBitmaps = new LinkedHashMap<String, BitmapItem>(0, 0.75f, true);
    }

    public void clear() {
        for (Map.Entry<String, BitmapItem> entry : mCachedBitmaps.entrySet()) {
            entry.getValue().clear();
        }
        mCachedBitmaps.clear();
    }

    public void setIsDisplayed(String id, boolean isDisplayed) {
        BitmapItem item = mCachedBitmaps.get(id);
        if (null != item) {
            item.setIsDisplayed(isDisplayed);
        }
    }

    private BitmapItem getBitmapItem(int width, int height) {
        int size = mCachedBitmaps.size();

        if (size > 0) {
            BitmapItem reuseItem = null;
            for (Map.Entry<String, BitmapItem> entry : mCachedBitmaps.entrySet()) {
                BitmapItem item = entry.getValue();
                if (item.getReferenceCount() <= 0) {
                    reuseItem = item;
                }
            }

            if (null != reuseItem) {
                return reuseItem;
            } else {
                return crateItem(width, height);
            }
        } else {
            return crateItem(width, height);
        }
    }

    private BitmapItem crateItem(int width, int height) {
        BitmapItem item = BitmapItem.create(width, height);
        String id = "id_" + System.currentTimeMillis();
        item.setId(id);
        mCachedBitmaps.put(id, item);
        return item;
    }

    public static IntentUtils getInstance() {
        return INSTANCE;
    }

    public Bitmap getBitmap(String id) {
        return mCachedBitmaps.get(id).getBitmap();
    }

    public void startActivity(final Context context, final Intent intent) {
        final View v = ((Activity) context).findViewById(android.R.id.content);
        BitmapItem item = getBitmapItem(v.getWidth(), v.getHeight());
        final Bitmap bitmap = item.getBitmap();
        intent.putExtra("bitmap_id", item.getId());
        v.draw(new Canvas(bitmap));
        context.startActivity(intent);
    }

    public void startActivity(final Context context, final Intent intent, @Nullable final Bundle options) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final View v = ((Activity) context).findViewById(android.R.id.content);
            BitmapItem item = getBitmapItem(v.getWidth(), v.getHeight());
            final Bitmap bitmap = item.getBitmap();
            intent.putExtra("bitmap_id", item.getId());
            v.draw(new Canvas(bitmap));
            context.startActivity(intent, options);
        } else {
            startActivity(context, intent);
        }
    }

    public void startActivityForResult(final Context context, final Intent intent, final int code) {
        final View v = ((Activity) context).findViewById(android.R.id.content);
        BitmapItem item = getBitmapItem(v.getWidth(), v.getHeight());
        final Bitmap bitmap = item.getBitmap();
        intent.putExtra("bitmap_id", item.getId());
        v.draw(new Canvas(bitmap));
        ((Activity) context).startActivityForResult(intent, code);
    }

    public void startFragmentForResult(final Context context, final BaseFragment baseFragment, final int code) {
        final View v = ((Activity) context).findViewById(android.R.id.content);
        BitmapItem item = getBitmapItem(v.getWidth(), v.getHeight());
        final Bitmap bitmap = item.getBitmap();
        Bundle bundle = new Bundle();
        bundle.putString("bitmap_id", item.getId());
        baseFragment.setArguments(bundle);
        v.draw(new Canvas(bitmap));
        startFrament(context, baseFragment, code);

    }

    public void startActivityForResult(final Context context, final Intent intent, final int code, @Nullable final Bundle options) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final View v = ((Activity) context).findViewById(android.R.id.content);
            BitmapItem item = getBitmapItem(v.getWidth(), v.getHeight());
            final Bitmap bitmap = item.getBitmap();
            intent.putExtra("bitmap_id", item.getId());
            v.draw(new Canvas(bitmap));
            ((Activity) context).startActivityForResult(intent, code, options);
        } else {
            startActivityForResult(context, intent, code);
        }

    }

    private void startFrament(final Context context, final BaseFragment baseFragment, int code) {
        Bundle args = baseFragment.getArguments();
        if (args == null) {
            args = new Bundle();
        }
        baseFragment.setArguments(args);
        baseFragment.setFramentResultCode(code);
        final FragmentTransaction transaction = ((Activity) context).getFragmentManager().beginTransaction();
        transaction.addToBackStack(null)
                .add(android.R.id.content, baseFragment)
                .commitAllowingStateLoss();

    }
}
