package com.common.res.template

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.common.core.base.mvvm.BaseVMActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity模板示例
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
@AndroidEntryPoint
class TemplateActivity : BaseVMActivity<TemplateViewModel>(){

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun getLayoutId(): Int {
        return 0
    }

}