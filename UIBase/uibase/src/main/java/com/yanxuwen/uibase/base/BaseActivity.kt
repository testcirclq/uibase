package com.yanxuwen.uibase.base

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.CallSuper
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.yanxuwen.lib_common.Listener.OnFragmentResultListener
import com.yanxuwen.uibase.R
import com.yanxuwen.uibase.statusbar.StatusBar
import com.yanxuwen.uibase.swipebacklibrary.ActivityQueueManager
import com.yanxuwen.uibase.swipebacklibrary.IntentUtils
import com.yanxuwen.uibase.swipebacklibrary.SlidingLayout
import kotlinx.android.synthetic.main.activity_base.*
import java.lang.ref.WeakReference

/**
 *使用说明，1.取消onStart，   onResume，   onPause，   onStop 这4大常用生命周期，onDestroy能正常使用。
 *            改成onBaseStart,onBaseResume,onBasePause,onBaseonStop
 */
abstract class BaseActivity : AppCompatActivity(), SlidingLayout.SlidingListener, OnFragmentResultListener {
    val TAG = "base"

    private val START = 0
    private val RESUME = 1
    private val PAUSE = 2
    private val STOP = 3
    /** 是否更改状态栏的文字,目前只支持miui6以上  */
    open var isChangeText = false
    private val mHandler: Handler by lazy {
        Handler()
    }
    private val dm: DisplayMetrics by lazy {
        resources.displayMetrics
    }

    val width: Int by lazy {
        dm.widthPixels
    }

    val height: Int by lazy {
        dm.heightPixels
    }

    private val inflater: LayoutInflater by lazy {
        LayoutInflater.from(this)
    }

    val mInitOffset: Float by lazy {
        -(1.toFloat() / 3) * width
    }

    lateinit var context: BaseActivity

    lateinit var mBitmapId: String

    var mPreviewBitmap: Bitmap? = null

    var mOnFragmentResultListener: OnFragmentResultListener? = null

    var requestCode = 0
    //
    private var clickOpen = true

    /**
     * 是否需要使用默认的背景颜色。因为有些需要先透明然后在显示背景颜色，这时候就不需要背景颜色
     */
    open fun isBackground(): Boolean {
        return true
    }

    /**
     * 点击开关，用于跳转界面的时候，关闭所有的点击事件，避免多次跳转问题
     */
    open fun setClickOpen(clickOpen: Boolean) {
        this.clickOpen = clickOpen
    }

    /**
     * 设置可滑动的边缘尺寸大小，默认为半屏幕
     */
    open fun isEdgeSize(): Int {
        return (width / 2).toInt()
    }

    /**
     * 是否可滑动关闭，MainActivity,建议为false
     */
    open fun isSlideable(): Boolean {
        return true
    }

    /**
     * 是否显示标题
     */
    open fun isShowTitle(): Boolean {
        return true
    }

    /**设置布局*/
    abstract fun getLayoutResId(): Int

    /**设置标题布局*/
    abstract fun getLayoutTitleResId(): Int

    /**是否全屏，改全屏不会因此状态栏*/
    abstract fun isFull(): Boolean

    /**是否沉浸式，沉浸式目前问题是白色背景下，文字会看不到，目前仅支持miui6以上，
     * 可以使用6.0自带的设置文字颜色，但目前会有闪烁问题建议不要用。*/
    abstract fun isImmersion(): Boolean



    override fun onCreate(savedInstanceState: Bundle?) {
        if (isImmersion() || isFull()) {
            StatusBar.setTranslucentStatus(this, isChangeText)
            //6.0以上的更改状态栏文字颜色,部分手机会闪烁
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        }
            //避免在状态栏的显示状态发生变化时重新布局
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        context = this
        //添加activity堆栈
        ActivityQueueManager.getInstance().pushActivity(this)
        setLayout()

    }

    private fun setLayout() {
        //如果需要添加AppBarLayout，则把内容替换成coordinatorLayout.xml
//        if (isAppBarLayout && AppBarLayoutID != 0) {
//            layoutContent.removeAllViews()
//            inflater.inflate(R.layout.coordinatorlayout, layoutContent, true)
//            contentView = (if (v != null) v.findViewById(R.id.content_view) else findViewById(R.id.content_view)) as CoordinatorLayout
//            inflater.inflate(AppBarLayoutID, contentView, true)
//        }

        //设置标题布局
        if (getLayoutTitleResId() != 0){
            try {
                layout_content.removeAllViews()
                inflater.inflate(getLayoutTitleResId(),layout_content, true)
            } catch (e: Exception) {}
        }

        //设置布局
        if (getLayoutResId() != 0) {
            if (!isBackground()) {
                layout_content.background = null
            }
            try {
                inflater.inflate(getLayoutResId(), layout_content, true)
            } catch (e: Exception) {}
        }
        //是否显示标题
        try {
            layout_content.getChildAt(0).visibility = if (isShowTitle()) View.VISIBLE else  View.GONE
        } catch (e: Exception) {}

        setBackground(R.color.background)
        //设置阴影
        layout_slide.setShadowResource(R.drawable.sliding_back_shadow)
        layout_slide.setSlidingListener(this)
        //设置边缘尺寸大小
        //slideLayout.setEdgeSize((int) (metrics.density * 20));
        layout_slide.setEdgeSize(isEdgeSize())
        layout_slide.isSlideable = isSlideable()
        //用于展示前页面的试图，顾是静态的。
        try {
            iv_preview.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        iv_preview.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    } else {
                        iv_preview.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                    setPreviewBitmap(false)
                    hidePreviewFragment()
                    setStatusFull()
                }
            })

        } catch (e: Exception) {
        }
    }

    /**
     * 设置状态栏是否全屏模式
     */
    private fun setStatusFull() {
        try {
            if (!isImmersion() or isFull()) return
            var statusBarHeight = StatusBar.getStatusHeight(context)
            var view = layout_content.getChildAt(0)
            if (view.visibility == View.GONE){
                view = layout_content.getChildAt(1)
            }
            var lp = view.layoutParams
            lp.height = view.height + statusBarHeight
            view.setPadding(view.paddingLeft,view.paddingTop + statusBarHeight,view.paddingRight,view.paddingBottom)
        } catch (e: Exception) {
        }
    }

    /**
     * 设置背景
     */
    fun setBackground(color: Int) {
        //        layoutContent.setBackgroundDrawable(new BitmapDrawable(BackgroundUtils.createBackground(this,color)));
        layout_content.setBackgroundColor(resources.getColor(color))
    }

    /**
     * 设置背景
     */
    fun setBackground(drawable: Drawable?) {
        layout_content.setBackgroundDrawable(drawable)
    }

    /**
     * 滑动监听，slideOffset=1为完全关闭，0为完全打开
     */
    override fun onPanelSlide(panel: View?, slideOffset: Float) {
        when {
            slideOffset <= 0 -> iv_preview.translationX = 0f
            slideOffset < 1 -> iv_preview.translationX = mInitOffset * (1 - slideOffset)
            else -> {
                iv_preview.translationX = 0f
                mHandler.postDelayed(object : Runnable {
                    private var mContext: WeakReference<Context>? = null
                    override fun run() {
                        onSlideFinish()
                        overridePendingTransition(R.animator.common_close_null, R.animator.common_close_null)
                    }
                }, 0)


            }
        }
    }


    /**设置上一个界面的预览图，添加新的Activity的时候，要把上一个Activity的 mPreviewBitmap置空，移除Activity的时候，要把上一个Activity的 mPreviewBitmap取出
     * 防止mPreviewBitmap内存泄露*/
    fun setPreviewBitmap(isNull: Boolean) {
        if (isNull) {
            //置空
            iv_preview.setImageBitmap(null)
            layout_slide.background = null
            if (mPreviewBitmap != null && mPreviewBitmap?.isRecycled == false) {
                mPreviewBitmap?.recycle()
                mPreviewBitmap = null
            }

        } else {
            mBitmapId = intent.extras?.getString("bitmap_id") ?: ""
            if (TextUtils.isEmpty(mBitmapId)) return
            mPreviewBitmap = IntentUtils.getInstance().getBitmap(mBitmapId)
            try {
                mPreviewBitmap = ThumbnailUtils.extractThumbnail(mPreviewBitmap, iv_preview.width, iv_preview.height)
            } catch (e: Exception) {
            }
            if (mPreviewBitmap != null) {
                iv_preview.setImageBitmap(mPreviewBitmap)
            }
            IntentUtils.getInstance().setIsDisplayed(mBitmapId, true)

        }

    }

    /**
     * 滑动关闭
     */
    private fun onSlideFinish() {
        finish()
    }

    fun onStartActivity(intent: Intent) {
        onStartActivity(intent, true)
    }

    fun onStartActivity(intent: Intent, isTransition: Boolean) {
        mHandler.postDelayed(object : Runnable {
            private var mContext: WeakReference<Context>? = null
            override fun run() {
                mContext = WeakReference(context)
                try {
                    IntentUtils.getInstance().startActivity(mContext?.get(), intent)
                } catch (e: Exception) {
                    startActivity(intent)
                }
            }
        }, 0)
    }


    fun onStartActivity(intent: Intent, options: Bundle) {
        onStartActivity(intent, options, true)
    }

    fun onStartActivity(intent: Intent, options: Bundle, isTransition: Boolean) {
        mHandler.postDelayed(object : Runnable {
            private var mContext: WeakReference<Context>? = null
            override fun run() {
                mContext = WeakReference(context)
                try {
                    IntentUtils.getInstance().startActivity(mContext!!.get(), intent, null)
                } catch (e: Exception) {
                    startActivity(intent)
                }
            }
        }, 0)
    }

    fun onStartActivityForResult(intent: Intent, code: Int) {
        onStartActivityForResult(intent, code, true)
    }

    fun onStartActivityForResult(intent: Intent, code: Int, isTransition: Boolean) {
        mHandler.postDelayed(object : Runnable {
            private var mContext: WeakReference<Context>? = null
            override fun run() {
                mContext = WeakReference(context)
                try {
                    IntentUtils.getInstance().startActivityForResult(mContext!!.get(), intent, code)
                } catch (e: Exception) {
                    startActivityForResult(intent, code)
                }
            }
        }, 0)

    }


    fun onStartActivityForResult(intent: Intent, code: Int, options: Bundle?) {
        onStartActivityForResult(intent, code, options, true)
    }

    fun onStartActivityForResult(intent: Intent, code: Int, options: Bundle?, isTransition: Boolean) {
        mHandler.postDelayed(object : Runnable {
            private var mContext: WeakReference<Context>? = null
            override fun run() {
                mContext = WeakReference(context)
                try {
                    IntentUtils.getInstance().startActivityForResult(mContext!!.get(), intent, code, options)
                } catch (e: Exception) {
                    startActivityForResult(intent, code)
                }
            }
        }, 0)
    }

    /**
     *  跳转  Fragment
     */
    fun onStartFragemnt(baseFragment: BaseFragment) {
        onStartFragemnt(baseFragment, 0)
    }

    /**
     * Fragment 跳转  Fragment
     */
    fun onStartFragemntForResult(baseFragment: BaseFragment, code: Int) {
        this.requestCode = code
        baseFragment.setOnFragmentResultListener(this)
        onStartFragemnt(baseFragment, code)
    }

    private fun onStartFragemnt(baseFragment: BaseFragment, code: Int) {
        mHandler.postDelayed(object : Runnable {
            private var mContext: WeakReference<Context>? = null
            override fun run() {
                mContext = WeakReference(context)
                try {
                    IntentUtils.getInstance().startFragmentForResult(mContext!!.get(), baseFragment, code)
                } catch (e: Exception) {
                    var args = baseFragment.arguments
                    if (args == null) {
                        args = Bundle()
                    }
                    baseFragment.arguments = args
                    baseFragment.setFramentResultCode(code)
                    fragmentManager.beginTransaction().addToBackStack(null)
                            .add(android.R.id.content, baseFragment)
                            .show(baseFragment)
                            .commitAllowingStateLoss()
                }
            }
        }, 0)
    }


    /**
     * 强制隐藏键盘
     */
    fun hideInput() {
        try {
            val v = currentFocus
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm != null && v != null && v.windowToken != null)
                imm.hideSoftInputFromWindow(v.windowToken, 0)
        } catch (e: Exception) {
            Log.e("xxx", "输入法报错" + e.message)
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //跳转过程中无法点击
        if (!clickOpen && ev.action == MotionEvent.ACTION_DOWN) {
            return true
        }
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (isShouldHideInput(v, ev)) {
                hideInput()
            }
            return super.dispatchTouchEvent(ev)
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return if (window.superDispatchTouchEvent(ev)) {
            true
        } else onTouchEvent(ev)
    }

    private fun isShouldHideInput(v: View?, event: MotionEvent): Boolean {
        if (v != null && v is EditText) {
            val leftTop = intArrayOf(0, 0)
            // 获取输入框当前的location位置
            v.getLocationInWindow(leftTop)
            val left = leftTop[0]
            val top = leftTop[1]
            val bottom = top + v.height
            val right = left + v.width
            return !(event.x > left && event.x < right
                    && event.y > top && event.y < bottom)
        }
        return false
    }

    /**
     * fragemnt的跳转
     */
    @CallSuper
    override fun onBackPressed() {
        val fragments = ActivityQueueManager.getInstance().activityLinkQueue
        //
        if (fragments.first is BaseFragment) {
            (fragments.first as BaseFragment).exit()
            showPreviewFragment(false)
        }
        super.onBackPressed()
    }

    /**onBackPressed关闭后调用接口 */
    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Intent) {}

    /**
     * 正常Activity关闭界面
     */
    @CallSuper
    override fun finish() {
        hideInput()
        showPreviewFragment(true)
        ActivityQueueManager.getInstance().popActivity(this)
        super.finish()
    }

    /**
     * 带有返回值的Activity关闭界面
     */
    @CallSuper
    fun finish(resultCode: Int, args: Bundle?) {
        val intent = Intent()
        if (args != null) {
            intent.putExtras(args)
        }
        setResult(resultCode, intent)
        finish()
    }

    override fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
        ActivityQueueManager.getInstance().popActivity(this)
        iv_preview.setImageBitmap(null)
        IntentUtils.getInstance().setIsDisplayed(mBitmapId, false)
        if (mPreviewBitmap != null && mPreviewBitmap?.isRecycled == false) {
            mPreviewBitmap?.recycle()
            mPreviewBitmap = null
        }
        super.onDestroy()

    }

    /**
     * frament跟activity结束之前，要先把上一个界面如果是Fragment,显示出来。 不建议外部调用
     * */
    private fun showPreviewFragment(isActivity: Boolean) {
        val fragments = ActivityQueueManager.getInstance().activityLinkQueue
        //取出第二个，也就是上一个界面,如果是Fragment则显示
        if (fragments != null && fragments.size >= 2) {
            if (fragments[1] is BaseFragment) {
                val fragment = fragments[1] as BaseFragment
                val transaction = fragmentManager.beginTransaction()
                transaction.show(fragment).commitAllowingStateLoss()
                fragment?.onBaseStart()
                fragment?.onBaseResume()
            } else if (fragments[1] is BaseActivity) {
                //如果关闭的是Activity，则不调用下面的。
                if (!isActivity) {
                    val activity = fragments[1] as BaseActivity
                    activity?.onBaseStart()
                    activity?.onBaseResume()
                }
            }
        }
    }

    /**
     * 跳转到新的页面后，则隐藏上一个界面的Fragment 不建议外部调用
     * */
    fun hidePreviewFragment() {
        val fragments = ActivityQueueManager.getInstance().activityLinkQueue
        //取出第二个，也就是上一个界面,如果是Fragment则显示
        if (fragments != null && fragments.size >= 2) {
            if (fragments[1] is BaseFragment) {
                val fragment = fragments[1] as BaseFragment
                val transaction = fragmentManager.beginTransaction()
                transaction.hide(fragment).commitAllowingStateLoss()
            }
        }
    }


    final override fun onStart() {
        super.onStart()
        setLifeCycle(START)

    }

    final override fun onResume() {
        super.onResume()
        setLifeCycle(RESUME)
    }

    final override fun onPause() {
        super.onPause()
        setLifeCycle(PAUSE)
    }

    final override fun onStop() {
        super.onStop()
        setLifeCycle(STOP)
    }

    @CallSuper
    open fun onBaseStart() {
    }

    @CallSuper
    open fun onBaseResume() {
        setClickOpen(true)
    }

    @CallSuper
    open fun onBasePause() {
        setClickOpen(false)
    }

    @CallSuper
    open fun onBaseStop() {
    }

    fun isTopStack(): Boolean {
        val fragments = ActivityQueueManager.getInstance().activityLinkQueue
        if (fragments != null && !fragments.isEmpty() && fragments.first == this) {
            return true
        }
        return false
    }

    private fun setLifeCycle(status: Int) {
        val fragments = ActivityQueueManager.getInstance().activityLinkQueue
        if (fragments == null || fragments.isEmpty() || fragments.first == null) {
            return
        }
        when (status) {
            START -> {
                if (fragments.first == this) {
                    (fragments.first as BaseActivity)?.onBaseStart()
                } else if (fragments.first is BaseFragment) {
                    (fragments.first as BaseFragment)?.onBaseStart()
                }
            }
            RESUME -> {
                if (fragments.first == this) {
                    (fragments.first as BaseActivity)?.onBaseResume()
                } else if (fragments.first is BaseFragment) {
                    (fragments.first as BaseFragment)?.onBaseResume()
                }
            }
            PAUSE -> {
                if (fragments.first is BaseFragment) {
                    (fragments.first as BaseFragment)?.onBasePause()
                } else {
                    onBasePause()
                }
            }
            STOP -> {
                if (fragments.first is BaseFragment) {
                    (fragments.first as BaseFragment)?.onBaseStop()
                } else {
                    onBaseStop()
                }
            }
        }
    }


    fun isAppOnForeground(): Boolean {
        val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageName = applicationContext.packageName
        val appProcesses = activityManager
                .runningAppProcesses ?: return false

        for (appProcess in appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName) && appProcess.importance === ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }

        return false
    }


}