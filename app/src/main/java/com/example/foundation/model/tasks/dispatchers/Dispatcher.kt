package com.example.foundation.model.tasks.dispatchers

interface Dispatcher {
    fun dispatch(block: () -> Unit)
}