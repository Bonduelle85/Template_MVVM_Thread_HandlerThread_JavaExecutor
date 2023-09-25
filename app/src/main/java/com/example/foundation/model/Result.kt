package com.example.foundation.model

import java.lang.IllegalStateException

// для читаемости создадим тип Маппер - лямбда
typealias Mapper<Input, Output> = (Input) -> Output

sealed class Result<T> {
    // функция для конвертации данных
    fun <R> map(mapper: Mapper<T, R>? = null): Result<R> {
        return when (this) {
            is SuccessResult -> {
                if (mapper == null) throw IllegalStateException("Mapper should not be NULL for success result")
                SuccessResult(mapper(this.data))
            }

            is ErrorResult -> ErrorResult(this.exception)
            is LoadingResult -> LoadingResult()
        }
    }
}

sealed class FinalResult<T>: Result<T>()

class SuccessResult<T>(
    val data: T
    ): FinalResult<T>()

class ErrorResult<T>(
    val exception: Exception
): FinalResult<T>()

class LoadingResult<T>: Result<T>()

// метод для получения результата при условии
fun <T> Result<T>?.takeSuccess(): T? = if (this is SuccessResult) data else null


