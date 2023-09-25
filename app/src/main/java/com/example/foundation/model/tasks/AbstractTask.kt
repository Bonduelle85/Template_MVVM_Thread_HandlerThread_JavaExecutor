package com.example.foundation.model.tasks

import com.example.foundation.model.ErrorResult
import com.example.foundation.model.FinalResult
import com.example.foundation.model.SuccessResult
import com.example.foundation.model.tasks.dispatchers.Dispatcher
import com.example.foundation.utils.delegates.Await

abstract class AbstractTask<T>: Task<T> {

    private var finalResult by Await<FinalResult<T>>()

    final override fun await(): T {
        val wrapperListener: TaskListener<T> = {finalResult ->
            this.finalResult = finalResult
        }
        doEnqueue(wrapperListener)
        try {
          when(val result = this.finalResult){
              is ErrorResult -> throw result.exception
              is SuccessResult -> return result.data
          }
        } catch (e: Exception){
            if (e is InterruptedException){
                cancel()
                throw CancelledException(e)
            } else {
                throw e
            }
        }
    }

    final override fun enqueue(dispatcher: Dispatcher, listener: TaskListener<T>) {
        val wrapperListener: TaskListener<T> = {finalResult ->
            this.finalResult = finalResult
            dispatcher.dispatch {
                listener(this.finalResult)
            }
        }
        doEnqueue(wrapperListener)
    }

    final override fun cancel() {
        finalResult = ErrorResult(CancelledException())
        doCancel()
    }

    fun executeBody(taskBody: TaskBody<T>, listener: TaskListener<T>){
        try {
            val data = taskBody()
            listener(SuccessResult(data))
        } catch (e: Exception){
            listener(ErrorResult(e))
        }
    }

    abstract fun doEnqueue(listener: TaskListener<T>)

    abstract fun doCancel()
}