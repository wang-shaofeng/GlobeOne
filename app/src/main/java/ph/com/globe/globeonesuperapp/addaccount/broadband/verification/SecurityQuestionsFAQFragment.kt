/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.verification

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.databinding.SecurityQuestionsFaqFragmentBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class SecurityQuestionsFAQFragment :
    NoBottomNavViewBindingFragment<SecurityQuestionsFaqFragmentBinding>(
        bindViewBy = {
            SecurityQuestionsFaqFragmentBinding.inflate(it)
        }
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewBinding) {
            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }
            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }
            flQuestionRoot1.setOnClickListener {
                TransitionManager.beginDelayedTransition(this.root, ChangeBounds())
                changeSectionExpandedState(clOpenedQuestion1, clUnopenedQuestion1)
            }
            flQuestionRoot2.setOnClickListener {
                TransitionManager.beginDelayedTransition(this.root, ChangeBounds())
                changeSectionExpandedState(clOpenedQuestion2, clUnopenedQuestion2)
            }
            flQuestionRoot3.setOnClickListener {
                TransitionManager.beginDelayedTransition(this.root, ChangeBounds())
                changeSectionExpandedState(clOpenedQuestion3, clUnopenedQuestion3)
            }
            flQuestionRoot4.setOnClickListener {
                TransitionManager.beginDelayedTransition(this.root, ChangeBounds())
                changeSectionExpandedState(clOpenedQuestion4, clUnopenedQuestion4)
            }
        }
    }

    private fun changeSectionExpandedState(unopened: View, opened: View) {
        unopened.isVisible = !unopened.isVisible
        opened.isVisible = !opened.isVisible
    }

    override val logTag = "SecurityQuestionsFAQFragment"
}
