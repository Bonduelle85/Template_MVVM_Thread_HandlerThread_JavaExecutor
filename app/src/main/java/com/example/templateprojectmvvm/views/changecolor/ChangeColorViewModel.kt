package com.example.templateprojectmvvm.views.changecolor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.foundation.model.ErrorResult
import com.example.foundation.model.FinalResult
import com.example.foundation.model.LoadingResult
import com.example.foundation.model.Result
import com.example.foundation.model.SuccessResult
import com.example.foundation.model.tasks.factories.TasksFactory
import com.example.foundation.model.tasks.dispatchers.Dispatcher
import com.example.foundation.navigator.Navigator
import com.example.foundation.uiactions.UiActions
import com.example.foundation.views.BaseViewModel
import com.example.templateprojectmvvm.R
import com.example.templateprojectmvvm.model.colors.ColorsRepository
import com.example.templateprojectmvvm.model.colors.NamedColor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch


class ChangeColorViewModel(
    screen: ChangeColorFragment.Screen,
    private val navigator: Navigator,
    private val uiActions: UiActions,
    private val colorsRepository: ColorsRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(), ColorsAdapter.Listener {

    // Input sources. Входящие источники для фрагмента: цвета, текущий цвет и состояние
    private val _availableColors = MutableLiveData<Result<List<NamedColor>>>(LoadingResult())
    private val _currentColorId = savedStateHandle.getLiveData("currentColorId", screen.currentColorId)
    private val _inProgress = MutableLiveData<Boolean>(false)           // false потому что изначально !_inProgress

    // main destination (contains merged values (ViewState) from _availableColors & _currentColorId & _inProgress)
    private val _viewState = MediatorLiveData<Result<ViewState>>()
    val viewState: LiveData<Result<ViewState>> = _viewState

    // side destination, also the same result can be achieved by using map() function. (Transformations устарел)
    val screenTitle: LiveData<String> = viewState.map { viewState ->
        if (viewState is SuccessResult) {
            val currentColor = viewState.data.colorsList.first { it.selected }
            uiActions.getString(R.string.change_color_screen_title, currentColor.namedColor.name)
        } else {
            uiActions.getString(R.string.change_color_screen_title_default)
        }
    }

    init {
        load()
        // initializing MediatorLiveData
        _viewState.addSource(_availableColors) { mergeSources() }
        _viewState.addSource(_currentColorId) { mergeSources() }
        _viewState.addSource(_inProgress) { mergeSources() }
    }

    override fun onColorChosen(namedColor: NamedColor) {
        if (_inProgress.value == true) return
        _currentColorId.value = namedColor.id
    }

    fun onSavePressed() = viewModelScope.launch {
        try {
            _inProgress.postValue(true)
            val currentColorId =
                _currentColorId.value ?: throw IllegalStateException("Color ID can not be null")
            val currentColor = colorsRepository.getById(currentColorId)
            colorsRepository.setCurrentColor(currentColor)

            navigator.goBack(currentColor)
        } catch (e: Exception) {
            if (e !is CancellationException) uiActions.toast(uiActions.getString(R.string.error_happened))
        } finally {
            _inProgress.value = false
        }

    }

    fun onCancelPressed() {
        navigator.goBack()
    }

    /**
     * [MediatorLiveData] can listen other LiveData instances (even more than 1)
     * and combine their values.
     * Here we listen the list of available colors ([_availableColors] live-data) + current color id
     * ([_currentColorId] live-data), then we use both of these values in order to create a list of
     * [NamedColorListItem], it is a list to be displayed in RecyclerView.
     */
    private fun mergeSources() {
        val colors = _availableColors.value ?: return
        val currentColorId = _currentColorId.value ?: return
        val inProgress = _inProgress.value ?: return

        _viewState.value = colors.map { colorsList ->
            ViewState(
                colorsList = colorsList.map { NamedColorListItem(it, currentColorId == it.id) },
                saveButtonIsShown = !inProgress,
                cancelButtonIsShown = !inProgress,
                saveProgressBarIsShown = inProgress
            )
        }
    }

    fun tryAgain() {
        load()
    }
    private fun load() = into(_availableColors) { colorsRepository.getAvailableColors() }

    // Класс для отображения состояние фрагментаЖ список цветов и видимость элементов (кнопки и бар)
    data class ViewState(
        val colorsList: List<NamedColorListItem>,
        val saveButtonIsShown: Boolean,
        val cancelButtonIsShown: Boolean,
        val saveProgressBarIsShown: Boolean,
    )
}