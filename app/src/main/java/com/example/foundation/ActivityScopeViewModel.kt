package com.example.foundation

import androidx.lifecycle.ViewModel
import com.example.foundation.navigator.IntermediateNavigator
import com.example.foundation.navigator.Navigator
import com.example.foundation.uiactions.UiActions


/**
 * Implementation of [Navigator] and [UiActions].
 * It is based on activity view-model because instances of [Navigator] and [UiActions]
 * should be available from fragments' view-models (usually they are passed to the view-model constructor).
 */
class ActivityScopeViewModel(
    val uiActions: UiActions,
    val navigator: IntermediateNavigator
) : ViewModel(),
    Navigator by navigator,
    UiActions by uiActions {

    override fun onCleared() {
        super.onCleared()
        navigator.clear()
    }

}