package com.example.foundation.model

import java.lang.IllegalStateException

// для читаемости создадим тип Маппер - лямбда
typealias Mapper<Input, Output> = (Input) -> Output


/**
 * Base class which represents result of some async operation
 */
sealed class Result<T> {
    /**
     * Convert this result of type T into another result of type R:
     * - error result of type T is converted to error result of type R with the same exception
     * - pending result of type T is converted to pending result of type R
     * - success result of type T is converted to success result of type R, where conversion
     *   of ([SuccessResult.data] from T to R is conducted by [mapper]
     */
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
/**
 * Operation has been finished
 */
sealed class FinalResult<T>: Result<T>()

/**
 * Operation has finished successfully
 */
class SuccessResult<T>(
    val data: T
    ): FinalResult<T>()

class ErrorResult<T>(
    val exception: Exception
): FinalResult<T>()

/**
 * Operation is in progress
 */
class LoadingResult<T>: Result<T>()

// метод для получения результата при условии
fun <T> Result<T>?.takeSuccess(): T? = if (this is SuccessResult) data else null


