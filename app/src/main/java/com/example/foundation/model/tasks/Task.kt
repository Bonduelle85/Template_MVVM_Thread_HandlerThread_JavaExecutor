package com.example.foundation.model.tasks

import com.example.foundation.model.ErrorResult
import com.example.foundation.model.FinalResult
import com.example.foundation.model.SuccessResult
import com.example.foundation.model.tasks.dispatchers.Dispatcher
import com.example.foundation.model.tasks.dispatchers.ImmediateDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


typealias TaskListener<T> = (FinalResult<T>) -> Unit

class CancelledException(
    originException: Exception? = null
) : Exception(originException)

interface Task<T> {

    fun await(): T

    /**
     * Listeners are coled only in main Thread
     */

    fun enqueue(dispatcher: Dispatcher, listener: TaskListener<T>)

    fun cancel()

    suspend fun suspend(): T = suspendCancellableCoroutine { continuation ->
        enqueue(ImmediateDispatcher()) { finalResult ->
            continuation.invokeOnCancellation { cancel() }
            when (finalResult) {
                is SuccessResult -> continuation.resume(finalResult.data)
                is ErrorResult -> continuation.resumeWithException(finalResult.exception)
            }
        }
    }
}