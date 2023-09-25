package com.example.foundation.model.tasks

import com.example.foundation.model.FinalResult
import com.example.foundation.model.tasks.dispatchers.Dispatcher


typealias TaskListener<T> = (FinalResult<T>) -> Unit

class CancelledException(
    originException: Exception? = null
): Exception(originException)

interface Task<T> {

    fun await(): T

    /**
     * Listeners are coled only in main Thread
     */

    fun enqueue(dispatcher: Dispatcher, listener: TaskListener<T>)

    fun cancel()
}