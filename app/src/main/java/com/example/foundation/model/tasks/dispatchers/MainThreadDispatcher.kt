package com.example.foundation.model.tasks.dispatchers

import android.os.Handler
import android.os.Looper

class MainThreadDispatcher: Dispatcher {

    private val handler = Handler(Looper.getMainLooper())

    override fun dispatch(block: () -> Unit) {
        if (Thread.currentThread().id == Looper.getMainLooper().thread.id){
            block()
        } else{
            handler.post(block)
        }
    }

}