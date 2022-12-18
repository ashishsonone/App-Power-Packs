package com.sononehouse.powerpacks.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val mutableUrl = MutableLiveData<String>()
    val selectedUrl: LiveData<String> get() = mutableUrl

    fun setUrl(url: String) {
        mutableUrl.value = url
    }
}