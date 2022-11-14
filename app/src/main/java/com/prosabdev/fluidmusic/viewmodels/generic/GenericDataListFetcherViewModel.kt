package com.prosabdev.fluidmusic.viewmodels.generic

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prosabdev.fluidmusic.utils.ConstantValues

open class GenericDataListFetcherViewModel : ViewModel() {
    private val mMutableIsLoading = MutableLiveData<Boolean>(false)
    private val mMutableIsLoadingInBackground = MutableLiveData<Boolean>(false)
    private val mMutableDataRequestCounter = MutableLiveData<Int>(0)
    private val mMutableLastLoadedPosition = MutableLiveData<Int>(0)
    private val mMutableDataList = MutableLiveData<ArrayList<Any>>(null)

    private val mIsLoading : LiveData<Boolean> get() = mMutableIsLoading
    private val mIsLoadingInBackground : LiveData<Boolean> get() = mMutableIsLoadingInBackground
    private val mDataLoadedCounter : LiveData<Int> get() = mMutableDataRequestCounter
    private val mLastLoadedPosition: LiveData<Int> get() = mMutableLastLoadedPosition
    private val mDataList: LiveData<ArrayList<Any>> get() = mMutableDataList

    open fun requestLoadDataAsync(
        context: Context,
        startCursor: Int = 0,
        maxDataCount: Int = 50
    ){
        Log.i(ConstantValues.TAG, "GenericDataListFetcherViewModel request to load data")
        Log.i(ConstantValues.TAG, "GenericDataListFetcherViewModel nothing to do")
    }

    protected fun getDataList(): LiveData<ArrayList<Any>> {
        return mDataList
    }
    protected fun setDataList(dataList: ArrayList<Any>)  {
        mMutableDataList.value = dataList
    }

    fun getIsLoading(): LiveData<Boolean> {
        return mIsLoading
    }
    fun setIsLoading(value : Boolean) {
        this.mMutableIsLoading.value = value
    }
    fun getIsLoadingInBackground(): LiveData<Boolean> {
        return mIsLoadingInBackground
    }
    fun setIsLoadingInBackground(value : Boolean) {
        this.mMutableIsLoadingInBackground.value = value
    }
    fun getDataRequestCounter(): LiveData<Int> {
        return mDataLoadedCounter
    }
    fun setDataRequestCounter(value : Int) {
        mMutableDataRequestCounter.value = value
    }
    fun getLastLoadedPosition(): LiveData<Int> {
        return mLastLoadedPosition
    }
    fun setLastLoadedPosition(value : Int) {
        mMutableLastLoadedPosition.value = value
    }
}