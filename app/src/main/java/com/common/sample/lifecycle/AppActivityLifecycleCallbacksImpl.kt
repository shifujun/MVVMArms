package com.common.sample.lifecycle

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.common.core.base.BaseActivity
import com.common.res.immersionbar.BindFullScreen
import com.common.res.immersionbar.BindImmersionBar
import com.common.sample.R
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar

class AppActivityLifecycleCallbacksImpl : ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

}