package com.yanxuwen.uibase.base

import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.Fragment
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.support.annotation.CallSuper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.yanxuwen.lib_common.Listener.OnFragmentResultListener
import com.yanxuwen.uibase.R
import com.yanxuwen.uibase.statusbar.StatusBar
import com.yanxuwen.uibase.swipebacklibrary.ActivityQueueManager
import com.yanxuwen.uibase.swipebacklibrary.IntentUtils
import com.yanxuwen.uibase.swipebacklibrary.SlidingLayout
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_base.view.*


/**
 *使用说明，1.取消onStart，   onResume，   onPause，   onStop 这4大常用生命周期，onDestroy能正常使用。
 *            改成onBaseStart,onBaseResume,onBasePause,onBaseonStop
 */
abstract class BaseFragment : Fragment(), OnFragmentResultListener, SlidingLayout.SlidingListener {
    //进入动画
    var enterAnimation = true
    //退出动画
    var exitAnimation = false


    lateinit var baseActivity: BaseActivity

    var mOnFragmentResultListener: OnFragmentResultListener? = null

    /**请求的code */
    var requestCode: Int = 0

    var mPreviewBitmap: Bitmap? = null

    lateinit var mBitmapId: String

    lateinit var rootView: View

    /**是否是滑动关闭的*/
    var isoSlideFinish = false


    /**
     * 是否需要使用默认的背景颜色。因为有些需要先透明然后在显示背景颜色，这时候就不需要背景颜色
     */
    open fun isBackground(): Boolean {
        return true
    }

    /**
     * 设置可滑动的边缘尺寸大小，默认为半屏幕
     */
    open fun isEdgeSize(): Int {
        return (baseActivity?.width / 2).toInt()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //添加activity堆栈
        ActivityQueueManager.getInstance().pushActivity(this)
    }

    /**设置布局*/
    abstract fun getLayoutResId(): Int

    /**设置标题布局*/
    abstract fun getLayoutTitleResId(): Int

    /**是否全屏，如果activity非全屏且非沉浸式，则无效，无法设置*/
    abstract fun isFull(): Boolean

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity is BaseActivity) {
            baseActivity = activity as BaseActivity
        }
        rootView = inflater.inflate(R.layout.activity_base, container, false)

        //设置标题布局
        if (getLayoutTitleResId() != 0){
            try {
                rootView.layout_content.removeAllViews()
                inflater.inflate(getLayoutTitleResId(),rootView.layout_content, true)
            } catch (e: Exception) {}
        }

        //设置布局
        if (getLayoutResId() != 0) {
            try {
                inflater.inflate(getLayoutResId(), rootView.layout_content, true)
            } catch (e: Exception) {}
        }
        if (!isBackground()) {
            setBackground(null)
        }
        try {
            rootView.layout_content.getChildAt(0).visibility = if (isShowTitle()) View.VISIBLE else  View.GONE
        } catch (e: Exception) {
        }

        //设置阴影
        rootView.layout_slide.setShadowResource(R.drawable.sliding_back_shadow)
        rootView.layout_slide.setSlidingListener(this)
        //设置边缘尺寸大小
        rootView.layout_slide.setEdgeSize(isEdgeSize())
        rootView.layout_slide.isSlideable = isSlideable()
        //用于展示前页面的试图，顾是静态的。
        try {
            rootView.iv_preview.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        rootView.iv_preview.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    } else {
                        rootView.iv_preview.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                    setPreviewBitmap(false)
                    setStatusFull()
                }
            })

        } catch (e: Exception) {
        }
        rootView.isFocusable = true;
        rootView.isFocusableInTouchMode = true
        rootView.requestFocus()
        onCreateView()
        return rootView
    }

    open fun onCreateView(){}

    /**
     * 设置状态栏是否全屏模式
     */
    private fun setStatusFull() {
        try {
            //如果baseActivity非全屏且非沉浸式，则直接return，因为无论怎么设置都无用
            if (isFull() or (!baseActivity.isImmersion() && !baseActivity.isFull())) return
            var statusBarHeight = StatusBar.getStatusHeight(activity)
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
        rootView.layout_content.setBackgroundColor(resources.getColor(color))
    }

    /**
     * 设置背景
     */
    fun setBackground(drawable: Drawable?) {
        rootView.layout_content.setBackgroundDrawable(drawable)
    }

    fun setFramentResultCode(code: Int) {
        this.requestCode = code
    }

    fun setOnFragmentResultListener(l: OnFragmentResultListener) {
        mOnFragmentResultListener = l
    }


    /**设置进出动画, */
    final override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator {
        var animator: Animator
        if (enter) {
            if (enterAnimation) {
                var previousActivity = ActivityQueueManager.getInstance().popIndex(1)

                //进入动画只能第一次有效,动画完成后，需要把上个界面的Frament 隐藏起来
                enterAnimation = false
                animator = AnimatorInflater.loadAnimator(activity, onEnterAnimator())
                animator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) = Unit
                    override fun onAnimationCancel(animation: Animator?) = Unit
                    override fun onAnimationStart(animation: Animator?) {
                        //跳转到Fragment,activity的onBasePause和onBaseStop不会被调用，所以要手动调用
                        if (previousActivity != null && previousActivity is BaseActivity) {
                            previousActivity.onBasePause()
                        } else if (previousActivity != null && previousActivity is BaseFragment) {
                            previousActivity.onBasePause()
                        }
                    }
                    override fun onAnimationEnd(animation: Animator?) {
                        baseActivity.hidePreviewFragment()
                        //由于第一次创建并不会调用，所以手动调用一次
                        onHiddenChanged(false)
                        onBaseStart()
                        onBaseResume()
                        //如果是activity跳转到Fragment,activity的onBasePause和onBaseStop会被拦截所以这边要手动调用
                        if (previousActivity != null && previousActivity is BaseActivity) {
                            previousActivity.onBaseStop()
                        } else if (previousActivity != null && previousActivity is BaseFragment) {
                            previousActivity.onBaseStop()
                        }

                    }
                })
            } else {
                animator = AnimatorInflater.loadAnimator(activity, R.animator.common_close_fragment_null)
            }
        } else {
            animator = AnimatorInflater.loadAnimator(activity, if (!isoSlideFinish && exitAnimation) onExitAnimator() else R.animator.common_close_fragment_null)
            exitAnimation = false
        }
        return animator
    }

    /**进入动画,  注意该动画无法更改上一个界面的动画*/
    open fun onEnterAnimator(): Int {
        return R.animator.common_open_enter
    }

    /**退出动画， 注意该动画无法更改上一个界面的动画*/
    open fun onExitAnimator(): Int {
        return R.animator.common_close_exit
    }


    /**
     * 正常跳转
     */
    fun onStartFragemnt(baseFragment: BaseFragment) {
        baseActivity?.onStartFragemnt(baseFragment)
    }

    /**
     * 能够有返回值的跳转，，类似Activity,前提那个界面返回要调用onBackPressed(int resultCode,Bundle args)
     */
    fun onStartFragemntForResult(baseFragment: BaseFragment, code: Int) {
        this.requestCode = code
        baseFragment.setOnFragmentResultListener(this)
        baseActivity?.onStartFragemntForResult(baseFragment, code)
    }

    /**onBackPressed关闭后调用接口 */
    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Intent) {}

    /**
     * 正常Fragment关闭界面
     */
    fun finish() {
        baseActivity?.hideInput()
        baseActivity?.onBackPressed()
    }

    /**
     * 带返回值得关闭
     */
    fun finish(resultCode: Int, args: Bundle) {
        val intent = Intent()
        intent.putExtras(args)
        if (mOnFragmentResultListener != null) {
            if (mOnFragmentResultListener != null) mOnFragmentResultListener?.onFragmentResult(requestCode, resultCode, intent)
        }
        baseActivity?.onBackPressed()
    }

    override fun onDestroy() {
        baseActivity?.setClickOpen(true)
        ActivityQueueManager.getInstance().popActivity(this)
        rootView.iv_preview.setImageBitmap(null)
        IntentUtils.getInstance().setIsDisplayed(mBitmapId, false)
        if (mPreviewBitmap != null && mPreviewBitmap?.isRecycled == false) {
            mPreviewBitmap?.recycle()
            mPreviewBitmap = null
        }
        super.onDestroy()
    }

    /**设置上一个界面的预览图，添加新的Activity的时候，要把上一个Activity的 mPreviewBitmap置空，移除Activity的时候，要把上一个Activity的 mPreviewBitmap取出
     * 防止mPreviewBitmap内存泄露*/
    fun setPreviewBitmap(isNull: Boolean) {
        if (isNull) {
            //置空
            rootView.iv_preview.setImageBitmap(null)
            if (mPreviewBitmap != null && mPreviewBitmap?.isRecycled == false) {
                mPreviewBitmap?.recycle()
                mPreviewBitmap = null
            }

        } else {
            mBitmapId = arguments?.getString("bitmap_id") ?: ""
            IntentUtils.getInstance().setIsDisplayed(mBitmapId, true)
            if (TextUtils.isEmpty(mBitmapId)) return
            mPreviewBitmap = IntentUtils.getInstance().getBitmap(mBitmapId)
            try {
                mPreviewBitmap = ThumbnailUtils.extractThumbnail(mPreviewBitmap, iv_preview.width, iv_preview.height)
            } catch (e: Exception) {
            }
            if (mPreviewBitmap != null) {
                iv_preview.setImageBitmap(mPreviewBitmap)
            }

        }
    }

    /**
     * 滑动监听，slideOffset=1为完全关闭，0为完全打开
     */
    override fun onPanelSlide(panel: View?, slideOffset: Float) {
        when {
            slideOffset <= 0 -> rootView.iv_preview.translationX = 0f
            slideOffset < 1 -> rootView.iv_preview.translationX = baseActivity.mInitOffset * (1 - slideOffset)
            else -> {
                rootView.iv_preview.translationX = 0f
                onSlideFinish()
            }
        }
    }

    fun exit() {
        exitAnimation = true
    }

    /**
     * 滑动关闭
     */
    private fun onSlideFinish() {
        isoSlideFinish = true
        finish()
    }


    /**取消重写, 由于一个Activity对应多个Fragment,导致生命周期不对，取消使用onStart，建议使用onBaseStart*/
    final override fun onStart() {
        super.onStart()
    }

    /**取消重写，由于一个Activity对应多个Fragment,导致生命周期不对，取消使用onResume，建议使用onBaseResume*/
    final override fun onResume() {
        super.onResume()
    }

    /**取消重写，由于一个Activity对应多个Fragment,导致生命周期不对，取消使用onPause，建议使用onBasePause*/
    final override fun onPause() {
        super.onPause()
        if (isHidden) return
        onBasePause()
    }

    /**取消重写，由于一个Activity对应多个Fragment,导致生命周期不对，取消使用onStop，建议使用onBaseStop*/
    final override fun onStop() {
        super.onStop()
        if (isHidden) return
        onBaseStop()
    }

    @CallSuper
    open fun onBaseStart() {}

    @CallSuper
    open fun onBaseResume() {
        baseActivity?.setClickOpen(true)
    }

    @CallSuper
    open fun onBasePause() {
        baseActivity?.setClickOpen(false)
    }

    @CallSuper
    open fun onBaseStop() {}


}