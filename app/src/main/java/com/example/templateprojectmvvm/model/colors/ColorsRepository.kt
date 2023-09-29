package com.example.templateprojectmvvm.model.colors

import com.example.foundation.model.Repository

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

    suspend fun setCurrentColor(color: NamedColor)


    /**
     * Get the list of all available colors that may be chosen by the user.
     */
    suspend fun getAvailableColors(): List<NamedColor>

    /**
     * Get the color content by its ID
     */
    suspend fun getById(id: Long): NamedColor

    /**
     * Listen for the current color changes.
     * The listener is triggered immediately with the current value when calling this method.
     */
    fun addListener(listener: ColorListener)

    /**
     * Stop listening for the current color changes
     */
    fun removeListener(listener: ColorListener)

}