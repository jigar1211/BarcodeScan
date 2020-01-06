package com.bynq.wallet.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimeLineViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is timeline Fragment"
    }
    val text: LiveData<String> = _text
}