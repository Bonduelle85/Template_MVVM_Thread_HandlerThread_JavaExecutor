package com.example.templateprojectmvvm


import android.app.Application
import com.example.foundation.BaseApplication
import com.example.templateprojectmvvm.model.colors.InMemoryColorsRepository
import com.example.templateprojectmvvm.model.coroutinrs.DefaultDispatcher
import com.example.templateprojectmvvm.model.coroutinrs.IODispatcher
import kotlinx.coroutines.Dispatchers


/**
 * Here we store instances of model layer classes.
 */
class App : Application(), BaseApplication {

    val idDispatcher = IODispatcher(Dispatchers.IO)
    val defaultDispatcher = DefaultDispatcher(Dispatchers.Default)

    /**
     * Place your repositories here, now we have only 1 repository
     */
    override val singletonScopeDependencies: List<Any> = listOf(
        InMemoryColorsRepository(idDispatcher)
    )

}