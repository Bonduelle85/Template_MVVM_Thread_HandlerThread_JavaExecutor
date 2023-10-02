package com.example.templateprojectmvvm.model.colors

import com.example.foundation.model.Repository
import kotlinx.coroutines.flow.Flow

typealias ColorListener = (NamedColor) -> Unit

/**
 * Repository interface example.
 *
 * Provides access to the available colors and current selected color.
 */
interface ColorsRepository : Repository {

    /**
     * Get the color selected content
     */

    suspend fun getCurrentColor(): NamedColor

    /**
     * Set the specified color as current
     */

    fun setCurrentColor(color: NamedColor): Flow<Int>


    /**
     * Get the list of all available colors that may be chosen by the user.
     */
    suspend fun getAvailableColors(): List<NamedColor>

    /**
     * Get the color content by its ID
     */
    suspend fun getById(id: Long): NamedColor

    fun listenCurrentColor(): Flow<NamedColor>
}