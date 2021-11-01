package com.common.template.ui.vm

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import com.common.core.base.mvvm.BaseViewModel
import com.common.res.adapter.BaseAdapter

class StatusViewModel @ViewModelInject constructor(
    application: Application
) : BaseViewModel(application) {

    var statusList = MutableLiveData<ArrayList<String>>()

    fun getData(pageStart: Int) {
        val arrayListOf: ArrayList<String> = arrayListOf()
        for (index in pageStart until pageStart + BaseAdapter.PAGE_SIZE) {
            arrayListOf.add("这是第" + index.toString() + "个条目")
        }
        statusList.postValue(arrayListOf)
    }
}