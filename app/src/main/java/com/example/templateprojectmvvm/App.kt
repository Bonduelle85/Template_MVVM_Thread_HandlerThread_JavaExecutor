package com.example.templateprojectmvvm

import android.app.Application
import com.example.foundation.BaseApplication
import com.example.foundation.model.tasks.ThreadUtils
import com.example.foundation.model.tasks.dispatchers.MainThreadDispatcher
import com.example.foundation.model.tasks.factories.ExecutorServiceTaskFactory
import com.example.foundation.model.tasks.factories.HandlerThreadTaskFactory
import com.example.foundation.model.tasks.factories.ThreadTasksFactory
import com.example.templateprojectmvvm.model.colors.InMemoryColorsRepository
import java.util.concurrent.Executors


/**
 * Here we store instances of model layer classes.
 */
class App : Application(), BaseApplication {

    private val tasksFactory = ThreadTasksFactory()
//    private val tasksFactory = ExecutorServiceTaskFactory(Executors.newCachedThreadPool())
//    private val tasksFactory = HandlerThreadTaskFactory()

    private val threadUtils = ThreadUtils.Default()
    private val dispatcher = MainThreadDispatcher()

    /**
     * Place your repositories here, now we have only 1 repository
     */
    override val singletonScopeDependencies: List<Any> = listOf(
        tasksFactory,
        dispatcher,
        InMemoryColorsRepository(
            tasksFactory,
            threadUtils)
    )

}