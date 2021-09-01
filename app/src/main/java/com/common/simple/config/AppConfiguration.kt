package com.common.simple.config

import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import androidx.fragment.app.FragmentManager
import com.common.core.base.delegate.BaseApplicationLifecycle
import com.common.core.config.CoreConfigModule
import com.common.core.di.module.ConfigModule

class AppConfiguration : CoreConfigModule() {
    override fun applyOptions(context: Context?, builder: ConfigModule.Builder?) {
    }


    override fun injectAppLifecycle(context: Context, lifecycles: MutableList<BaseApplicationLifecycle>) {
        lifecycles.add(AppLifecyclesImpl())
    }

    override fun injectActivityLifecycle(context: Context, lifecycles: MutableList<ActivityLifecycleCallbacks>) {
        lifecycles.add(AppActivityLifecycleCallbacksImpl())
    }

    override fun injectFragmentLifecycle(context: Context, lifecycles: MutableList<FragmentManager.FragmentLifecycleCallbacks>) {

    }
}