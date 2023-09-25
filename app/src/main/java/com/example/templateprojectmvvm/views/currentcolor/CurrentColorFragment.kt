package com.example.templateprojectmvvm.views.currentcolor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.foundation.model.ErrorResult
import com.example.foundation.model.LoadingResult
import com.example.foundation.model.SuccessResult
import com.example.foundation.views.BaseFragment
import com.example.foundation.views.BaseScreen
import com.example.foundation.views.screenViewModel
import com.example.templateprojectmvvm.databinding.FragmentCurrentColorBinding
import com.example.templateprojectmvvm.databinding.ResultLayoutBinding


class CurrentColorFragment : BaseFragment() {

    // no arguments for this screen
    class Screen : BaseScreen

    override val viewModel by screenViewModel<CurrentColorViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentCurrentColorBinding.inflate(inflater, container, false)

        viewModel.currentColor.observe(viewLifecycleOwner) { result ->
            when(result){
                is LoadingResult -> {
                    binding.resultLayout.progressBar.visibility = View.VISIBLE
                    binding.resultLayout.errorContainer.visibility = View.GONE
                    binding.colorContainer.visibility = View.GONE
                    binding.changeColorButton.visibility = View.GONE
                }
                is ErrorResult -> {
                    binding.resultLayout.progressBar.visibility = View.GONE
                    binding.resultLayout.errorContainer.visibility = View.VISIBLE
                    binding.colorContainer.visibility = View.GONE
                    binding.changeColorButton.visibility = View.GONE
                }
                is SuccessResult -> {
                    binding.resultLayout.progressBar.visibility = View.GONE
                    binding.resultLayout.errorContainer.visibility = View.GONE
                    binding.colorContainer.visibility = View.VISIBLE
                    binding.changeColorButton.visibility = View.VISIBLE
                    binding.colorView.setBackgroundColor(result.data.value)
                }
            }
        }

        binding.changeColorButton.setOnClickListener {
            viewModel.changeColor()
        }

        binding.resultLayout.tryAgainButton.setOnClickListener {
            viewModel.tryAgain()
        }

        return binding.root
    }
}