package com.example.foundation.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.foundation.model.ErrorResult
import com.example.foundation.model.Result
import com.example.foundation.model.SuccessResult
import com.example.foundation.utils.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


typealias LiveEvent<T> = LiveData<Event<T>>
typealias MutableLiveEvent<T> = MutableLiveData<Event<T>>

/**
 * Base class for all view-models.
 */
open class BaseViewModel: ViewModel() {

    /**
     * Override this method in child classes if you want to listen for results
     * from other screens
     */
    open fun onResult(result: Any) {

    }

    fun <T> into(liveDataResult: MutableLiveData<Result<T>>, block: suspend () -> T){
        viewModelScope.launch {
            try {
                liveDataResult.postValue(SuccessResult(block()))
            } catch (e: Exception){
                liveDataResult.postValue((ErrorResult(e)))
            }
        }
    }

    fun <T> into(stateFlow: MutableStateFlow<Result<T>>, block: suspend () -> T){
        viewModelScope.launch {
            try {
                stateFlow.value = SuccessResult(block())
            } catch (e: Exception){
                stateFlow.value = ErrorResult(e)
            }
        }
    }

    fun <T> SavedStateHandle.getStateFlowCustom(key: String, initialValue: T): MutableStateFlow<T> {
        val savedStateHandle = this
        val mutableFlow = MutableStateFlow(savedStateHandle[key] ?: initialValue)

        viewModelScope.launch{
            mutableFlow.collect{
                savedStateHandle[key] = it
            }
        }

        viewModelScope.launch{
            savedStateHandle.getLiveData<T>(key).asFlow().collect{
                mutableFlow.value = it
            }
        }
        return mutableFlow
    }
}