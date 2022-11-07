package com.prosabdev.fluidmusic.viewmodels.explore

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prosabdev.fluidmusic.models.SongItem
import com.prosabdev.fluidmusic.utils.ConstantValues
import java.util.Objects

abstract class BaseExploreViewModel : ViewModel() {
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

    open fun requestLoadDataAsync(activity : Activity, minToUpdateDataList : Int = 10){
        Log.i(ConstantValues.TAG, "BaseExploreViewModel request to load data")
        Log.i(ConstantValues.TAG, "BaseExploreViewModel nothing to do")
    }
    fun getIsLoading(): LiveData<Boolean> {
        return mIsLoading
    }
    fun setIsLoading(value : Boolean) {
        this.mMutableIsLoading.value = true
    }
    fun getIsLoadingInBackground(): LiveData<Boolean> {
        return mIsLoadingInBackground
    }
    fun setIsLoadingInBackground(value : Boolean) {
        this.mMutableIsLoadingInBackground.value = true
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
    fun getSongList(): LiveData<ArrayList<Any>> {
        return mDataList
    }
    fun setDataList(dataList: ArrayList<Any>)  {
        mMutableDataList.value = dataList
    }
}