package com.example.foundation.model.tasks.factories

import com.example.foundation.model.tasks.AbstractTask
import com.example.foundation.model.tasks.SynchronizedTask
import com.example.foundation.model.tasks.Task
import com.example.foundation.model.tasks.TaskBody
import com.example.foundation.model.tasks.TaskListener
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class ExecutorServiceTaskFactory(
    val executorService: ExecutorService
): TasksFactory {

    override fun <T> asyncCreate(body: TaskBody<T>): Task<T> {
        return SynchronizedTask(ExecutorServiceTask(body))
    }



    private inner class ExecutorServiceTask<T>(
        private val body: TaskBody<T>
    ) : AbstractTask<T>() {

        private var future: Future<*>? = null

        override fun doEnqueue(listener: TaskListener<T>) {
            future = executorService.submit{
                executeBody(body, listener)
            }
        }

        override fun doCancel() {
            future?.cancel(true)
        }
    }
}