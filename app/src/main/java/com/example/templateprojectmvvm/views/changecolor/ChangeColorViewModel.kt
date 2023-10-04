package com.example.templateprojectmvvm.views.changecolor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.foundation.model.LoadingResult
import com.example.foundation.model.Result
import com.example.foundation.model.SuccessResult
import com.example.foundation.navigator.Navigator
import com.example.foundation.uiactions.UiActions
import com.example.foundation.views.BaseViewModel
import com.example.templateprojectmvvm.R
import com.example.templateprojectmvvm.model.EmptyProgress
import com.example.templateprojectmvvm.model.PercentageProgress
import com.example.templateprojectmvvm.model.Progress
import com.example.templateprojectmvvm.model.colors.ColorsRepository
import com.example.templateprojectmvvm.model.colors.NamedColor
import com.example.templateprojectmvvm.model.coroutinrs.finiteShareIn
import com.example.templateprojectmvvm.model.getPercentage
import com.example.templateprojectmvvm.model.isInProgress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
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
    private val _instanceSaveInProgress = MutableStateFlow<Progress>(EmptyProgress)
    private val _sampledSaveInProgress = MutableStateFlow<Progress>(EmptyProgress)


    // main destination (contains merged values (ViewState) from _availableColors & _currentColorId & _inProgress)
    val viewState: Flow<Result<ViewState>> = combine(
        flow = _availableColors,
        flow2 = _currentColorId,
        flow3 = _instanceSaveInProgress,
        flow4 = _sampledSaveInProgress,
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
        if (_instanceSaveInProgress.value.isInProgress()) return
        _currentColorId.value = namedColor.id
    }

    fun onSavePressed() = viewModelScope.launch {
        try {
            _instanceSaveInProgress.value = PercentageProgress.START
            _sampledSaveInProgress.value = PercentageProgress.START

            val currentColorId = _currentColorId.value
            val currentColor = colorsRepository.getById(currentColorId)

            val flow = colorsRepository.setCurrentColor(currentColor).finiteShareIn(this)

            val instantJob = async {
                flow.collect{ percentage ->
                    _instanceSaveInProgress.value = PercentageProgress(percentage)
                }
            }

            val sampledJob = async {
                flow.sample(400).collect{ percentage ->
                    _sampledSaveInProgress.value = PercentageProgress(percentage)
                }
            }

            instantJob.await()
            sampledJob.await()

            navigator.goBack(currentColor)
        } catch (e: Exception) {
            if (e !is CancellationException) uiActions.toast(uiActions.getString(R.string.error_happened))
        } finally {
            _instanceSaveInProgress.value = EmptyProgress
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
        instanceSaveInProgress: Progress,
        sampledSaveInProgress: Progress
    ): Result<ViewState> {

        return colors.map { colorsList ->
            ViewState(
                colorsList = colorsList.map { NamedColorListItem(it, currentColorId == it.id) },
                saveButtonIsShown = !instanceSaveInProgress.isInProgress(),
                cancelButtonIsShown = !instanceSaveInProgress.isInProgress(),
                saveProgressBarIsShown = instanceSaveInProgress.isInProgress(),
                saveProgressPercentage = instanceSaveInProgress.getPercentage(),
                saveProgressPercentageMessage = uiActions.getString(R.string.percentage_value, sampledSaveInProgress.getPercentage())
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
        val saveProgressPercentage: Int,
        val saveProgressPercentageMessage: String,

    )
}