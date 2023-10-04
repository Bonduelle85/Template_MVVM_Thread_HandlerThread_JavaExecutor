package com.example.templateprojectmvvm.views.changecolor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.foundation.model.ErrorResult
import com.example.foundation.model.LoadingResult
import com.example.foundation.model.SuccessResult
import com.example.foundation.model.takeSuccess
import com.example.foundation.views.BaseFragment
import com.example.foundation.views.BaseScreen
import com.example.foundation.views.HasScreenTitle
import com.example.foundation.views.screenViewModel
import com.example.templateprojectmvvm.R
import com.example.templateprojectmvvm.databinding.FragmentChangeColorBinding
import kotlinx.coroutines.launch


/**
 * Screen for changing color.
 * 1) Displays the list of available colors
 * 2) Allows choosing the desired color
 * 3) Chosen color is saved only after pressing "Save" button
 * 4) The current choice is saved via [SavedStateHandle] (see [ChangeColorViewModel])
 */
class ChangeColorFragment : BaseFragment(), HasScreenTitle {

    /**
     * This screen has 1 argument: color ID to be displayed as selected.
     */
    class Screen(
        val currentColorId: Long
    ) : BaseScreen

    override val viewModel by screenViewModel<ChangeColorViewModel>()

    /**
     * Example of dynamic screen title
     */
    override fun getScreenTitle(): String? = viewModel.screenTitle.value

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentChangeColorBinding.inflate(inflater, container, false)

        val adapter = ColorsAdapter(viewModel)
        setupLayoutManager(binding, adapter)

        binding.saveButton.setOnClickListener { viewModel.onSavePressed() }
        binding.cancelButton.setOnClickListener { viewModel.onCancelPressed() }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.viewState.collect{ result ->
                    when (result){
                        is SuccessResult -> {
                            val viewState = result.takeSuccess()
                            if (viewState == null){
                                throw NullPointerException()
                            } else {
                                adapter.items = viewState.colorsList
                                binding.saveButton.visibility = if (viewState.saveButtonIsShown) View.VISIBLE else View.GONE
                                binding.cancelButton.visibility = if (viewState.cancelButtonIsShown) View.VISIBLE else View.GONE

                                binding.saveProgressGroup.visibility = if (viewState.saveProgressBarIsShown) View.VISIBLE else View.GONE

                                binding.saveProgressBar.progress = viewState.saveProgressPercentage
                                binding.savingPercentageTextView.text = viewState.saveProgressPercentageMessage
                            }
                            binding.resultLayout.progressBar.visibility = View.GONE
                            binding.resultLayout.errorContainer.visibility = View.GONE

                        }
                        is LoadingResult ->{
                            binding.resultLayout.errorContainer.visibility = View.GONE
                            binding.saveProgressGroup.visibility = View.GONE
                        }
                        is ErrorResult -> {
                            binding.resultLayout.errorContainer.visibility = View.VISIBLE
                            binding.resultLayout.progressBar.visibility = View.GONE
                        }
                }

                }
            }
        }

        viewModel.screenTitle.observe(viewLifecycleOwner) {
            // if screen title is changed -> need to notify activity about updates
            notifyScreenUpdates()
        }

        binding.resultLayout.tryAgainButton.setOnClickListener {
            viewModel.tryAgain()
            binding.resultLayout.progressBar.visibility = View.VISIBLE
        }

        return binding.root
    }

    private fun setupLayoutManager(binding: FragmentChangeColorBinding, adapter: ColorsAdapter) {
        // waiting for list width
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val width = binding.root.width
                val itemWidth = resources.getDimensionPixelSize(R.dimen.item_width)
                val columns = width / itemWidth
                binding.colorsRecyclerView.adapter = adapter
                binding.colorsRecyclerView.layoutManager = GridLayoutManager(requireContext(), columns)
            }
        })
    }
}