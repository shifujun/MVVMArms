package com.common.res.immersionbar
//导航栏
/**
 * desc :开启沉浸式接口
 * author：panyy
 * date：2021/04/22
 */
interface BindImmersionBar {
    companion object {
        //0 默认 1沉静 2全屏
        const val DEFAULTBAR = 0
        const val IMMERSIONBAR = 1
        const val FULLSCREEN = 2
    }
    fun getImmersionBarType(): Int
    fun isStatusBarDarkFont(): Boolean
}