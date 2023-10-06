package com.example.templateprojectmvvm

import com.example.foundation.SingletonScopeDependencies
import com.example.templateprojectmvvm.model.colors.InMemoryColorsRepository
import com.example.templateprojectmvvm.model.coroutinrs.DefaultDispatcher
import com.example.templateprojectmvvm.model.coroutinrs.IODispatcher
import kotlinx.coroutines.Dispatchers

object Initializer {

    fun initDependencies(){

        // Place your repositories here, now we have only 1 repository
        SingletonScopeDependencies.init { applicationContext ->
            val idDispatcher = IODispatcher(Dispatchers.IO)
            val defaultDispatcher = DefaultDispatcher(Dispatchers.Default)

            return@init listOf(
                InMemoryColorsRepository(idDispatcher)
            )
        }
    }
}