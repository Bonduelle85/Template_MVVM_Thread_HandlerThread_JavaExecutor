package com.example.foundation.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foundation.model.LoadingResult
import com.example.foundation.model.tasks.Task
import com.example.foundation.model.tasks.TaskListener
import com.example.foundation.utils.Event
import com.example.foundation.model.Result
import com.example.foundation.model.tasks.dispatchers.Dispatcher


typealias LiveEvent<T> = LiveData<Event<T>>
typealias MutableLiveEvent<T> = MutableLiveData<Event<T>>

/**
 * Base class for all view-models.
 */
open class BaseViewModel(
    private val dispatcher: Dispatcher
) : ViewModel() {

    val tasks = mutableSetOf<Task<*>>()

    override fun onCleared() {
        super.onCleared()
        tasks.forEach { it.cancel() }
        tasks.clear()
    }

    /**
     * Override this method in child classes if you want to listen for results
     * from other screens
     */
    open fun onResult(result: Any) {

    }

    fun <T> Task<T>.safeEnqueue(listener: TaskListener<T>? = null){
        tasks.add(this)
        this.enqueue(dispatcher) {
            tasks.remove(this)
            listener?.invoke(it)
        }
    }


    fun <T> Task<T>.into(liveDataResult: MutableLiveData<Result<T>>){
        liveDataResult.value = LoadingResult()
        this.safeEnqueue {
            liveDataResult.value = it
        }
    }
}