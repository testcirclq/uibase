package com.yanxuwen.lib_common.Listener

import android.content.Intent

/**
 * 作者：严旭文 on 2016/6/4.
 * 邮箱：420255048@qq.com
 */
interface OnFragmentResultListener {
    fun onFragmentResult(requestCode: Int, resultCode: Int, data: Intent)
}
