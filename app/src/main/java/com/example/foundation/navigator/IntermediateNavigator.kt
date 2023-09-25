package com.example.foundation.navigator

import com.example.foundation.utils.ResourceActions
import com.example.foundation.views.BaseScreen


/**
 * Mediator that holds nav actions in the queue if real navigator is not active.
 */
class IntermediateNavigator : Navigator {

    private val targetNavigator = ResourceActions<Navigator>()

    override fun launch(screen: BaseScreen) = targetNavigator {
        it.launch(screen)
    }

    override fun goBack(result: Any?) = targetNavigator {
        it.goBack(result)
    }

    fun setTarget(navigator: Navigator?) {
        targetNavigator.resource = navigator
    }

    fun clear() {
        targetNavigator.clear()
    }

}