package com.example.templateprojectmvvm.model.colors

import com.example.foundation.model.Repository
import com.example.foundation.model.tasks.Task

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

    fun getCurrentColor(): Task<NamedColor>

    /**
     * Set the specified color as current
     */

    fun setCurrentColor(color: NamedColor): Task<Unit>


    /**
     * Get the list of all available colors that may be chosen by the user.
     */
    fun getAvailableColors(): Task<List<NamedColor>>

    /**
     * Get the color content by its ID
     */
    fun getById(id: Long): Task<NamedColor>

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