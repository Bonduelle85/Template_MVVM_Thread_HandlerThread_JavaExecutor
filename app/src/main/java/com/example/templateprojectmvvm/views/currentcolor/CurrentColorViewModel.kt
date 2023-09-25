package com.example.templateprojectmvvm.views.currentcolor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foundation.model.ErrorResult
import com.example.foundation.model.LoadingResult
import com.example.foundation.model.Result
import com.example.foundation.model.SuccessResult
import com.example.foundation.model.takeSuccess
import com.example.foundation.model.tasks.dispatchers.Dispatcher
import com.example.foundation.navigator.Navigator
import com.example.foundation.uiactions.UiActions
import com.example.foundation.views.BaseViewModel
import com.example.templateprojectmvvm.R
import com.example.templateprojectmvvm.model.colors.ColorListener
import com.example.templateprojectmvvm.model.colors.ColorsRepository
import com.example.templateprojectmvvm.model.colors.NamedColor
import com.example.templateprojectmvvm.views.changecolor.ChangeColorFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CurrentColorViewModel(
    private val navigator: Navigator,
    private val uiActions: UiActions,
    private val colorsRepository: ColorsRepository,
    dispatcher: Dispatcher
) : BaseViewModel(dispatcher) {

    private val _currentColor = MutableLiveData<Result<NamedColor>>(LoadingResult()) // изначально в ЛайвДате LoadingResult - показываем прогрессБар.
    val currentColor: LiveData<Result<NamedColor>> = _currentColor

    private val colorListener: ColorListener = {
        _currentColor.postValue(SuccessResult(it))
    }

    // --- example of listening results via model layer

    init {
        colorsRepository.addListener(colorListener)
        load()
    }

    override fun onCleared() {
        super.onCleared()
        colorsRepository.removeListener(colorListener)
    }

    // --- example of listening results directly from the screen

    override fun onResult(result: Any) {
        super.onResult(result)
        if (result is NamedColor) {
            val message = uiActions.getString(R.string.changed_color, result.name)
            uiActions.toast(message)
        }
    }


    fun changeColor() {
        val currentColor = currentColor.value.takeSuccess() ?: return
        val screen = ChangeColorFragment.Screen(currentColor.id)
        navigator.launch(screen)
    }
    // метод для повторного запуска асинхронной операции
    fun tryAgain(){
      load()
    }

    private fun load(){
        colorsRepository.getCurrentColor().into(_currentColor)
    }
}