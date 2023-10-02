package com.example.templateprojectmvvm.views.changecolor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.foundation.model.LoadingResult
import com.example.foundation.model.Result
import com.example.foundation.model.SuccessResult
import com.example.foundation.navigator.Navigator
import com.example.foundation.uiactions.UiActions
import com.example.foundation.views.BaseViewModel
import com.example.templateprojectmvvm.R
import com.example.templateprojectmvvm.model.colors.ColorsRepository
import com.example.templateprojectmvvm.model.colors.NamedColor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class ChangeColorViewModel(
    screen: ChangeColorFragment.Screen,
    private val navigator: Navigator,
    private val uiActions: UiActions,
    private val colorsRepository: ColorsRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(), ColorsAdapter.Listener {

    // Input sources. Входящие источники для фрагмента: цвета, текущий цвет и состояние
    private val _availableColors = MutableStateFlow<Result<List<NamedColor>>>(LoadingResult())
    private val _currentColorId =
        savedStateHandle.getStateFlowCustom("currentColorId", screen.currentColorId)
    private val _inProgress = MutableStateFlow(false)

    // main destination (contains merged values (ViewState) from _availableColors & _currentColorId & _inProgress)
    val viewState: Flow<Result<ViewState>> = combine(
        flow = _availableColors,
        flow2 = _currentColorId,
        flow3 = _inProgress,
        transform = ::mergeSources
    )

    val screenTitle: LiveData<String> = viewState.map { viewState ->
        return@map if (viewState is SuccessResult) {
            val currentColor = viewState.data.colorsList.first { it.selected }
            uiActions.getString(R.string.change_color_screen_title, currentColor.namedColor.name)
        } else {
            uiActions.getString(R.string.change_color_screen_title_default)
        }
    }.asLiveData()

    init {
        load()
    }

    override fun onColorChosen(namedColor: NamedColor) {
        if (_inProgress.value == true) return
        _currentColorId.value = namedColor.id
    }

    fun onSavePressed() = viewModelScope.launch {
        try {
            _inProgress.value = true
            val currentColorId =
                _currentColorId.value ?: throw IllegalStateException("Color ID can not be null")
            val currentColor = colorsRepository.getById(currentColorId)
            colorsRepository.setCurrentColor(currentColor).collect()

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
    private fun mergeSources(
        colors: Result<List<NamedColor>>,
        currentColorId: Long,
        inProgress: Boolean
    ): Result<ViewState> {

        return colors.map { colorsList ->
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