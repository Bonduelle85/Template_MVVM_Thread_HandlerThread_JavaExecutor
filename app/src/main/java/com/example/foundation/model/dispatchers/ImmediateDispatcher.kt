package com.example.foundation.model.dispatchers

class ImmediateDispatcher: Dispatcher {
    override fun dispatch(block: () -> Unit) {
        block()
    }
}