/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rating

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.RatingFragmentBinding
import ph.com.globe.globeonesuperapp.databinding.SuggestionItemLayoutBinding
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.openPlayStore
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class RatingFragment : NoBottomNavViewBindingFragment<RatingFragmentBinding>(bindViewBy = {
    RatingFragmentBinding.inflate(it)
}), AnalyticsScreen {
    var iconsList = listOf<ImageView>()
    var animationsList = listOf<LottieAnimationView>()

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    private val ratingViewModel: RatingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDarkStatusBar()

        with(viewBinding) {
            with(ratingViewModel) {

                iconsList = listOf(ivRating1, ivRating2, ivRating3, ivRating4, ivRating5)
                animationsList = listOf(lavRating1, lavRating2, lavRating3, lavRating4, lavRating5)

                ivRating1.setOnClickListener {
                    selectRating(1)
                    mlRoot.transitionToEnd()
                    clPositiveRating.transitionToStart()
                    clNegativeRating.transitionToEnd()
                    etSuggestion.isEnabled = true
                    btnSubmitFeedback.isEnabled = true
                    btnNoThanks.isEnabled = false
                }

                ivRating2.setOnClickListener {
                    selectRating(2)
                    mlRoot.transitionToEnd()
                    clPositiveRating.transitionToStart()
                    clNegativeRating.transitionToEnd()
                    etSuggestion.isEnabled = true
                    btnSubmitFeedback.isEnabled = true
                    btnNoThanks.isEnabled = false
                }

                ivRating3.setOnClickListener {
                    selectRating(3)
                    mlRoot.transitionToEnd()
                    clPositiveRating.transitionToStart()
                    clNegativeRating.transitionToEnd()
                    etSuggestion.isEnabled = true
                    btnSubmitFeedback.isEnabled = true
                    btnNoThanks.isEnabled = false
                }

                ivRating4.setOnClickListener {
                    selectRating(4)
                    clNegativeRating.transitionToStart()
                    clPositiveRating.transitionToEnd()
                    mlRoot.transitionToEnd()
                    etSuggestion.isEnabled = false
                    btnSubmitFeedback.isEnabled = false
                    btnNoThanks.isEnabled = true
                }

                ivRating5.setOnClickListener {
                    selectRating(5)
                    clNegativeRating.transitionToStart()
                    clPositiveRating.transitionToEnd()
                    mlRoot.transitionToEnd()
                    etSuggestion.isEnabled = false
                    btnSubmitFeedback.isEnabled = false
                    btnNoThanks.isEnabled = true
                }

                improvementOptionsFetched.observe(viewLifecycleOwner, { fetched ->
                    if (fetched)
                        improvementOptionsList.map { it.toChip() }
                            .forEach { cgNegativeSuggestions.addView(it) }
                })

                ivClose.setOnClickListener {
                    saveLastRatingParameters(true)
                    logCustomEvent(
                        getRatingAnalyticsEvent(etSuggestion.text.toString(), EXIT_X)
                    )
                    findNavController().navigateUp()
                }

                btnRate.setOnClickListener {
                    saveLastRatingParameters(false)
                    context?.openPlayStore("ph.com.globe.globeonesuperapp")
                    logCustomEvent(
                        getRatingAnalyticsEvent("", EXIT_APP_STORE)
                    )
                }

                btnNoThanks.setOnClickListener {
                    saveLastRatingParameters(false)
                    logCustomEvent(
                        getRatingAnalyticsEvent(etSuggestion.text.toString(), EXIT_NO_THANKS)
                    )
                    findNavController().navigateUp()
                }

                btnSubmitFeedback.setOnClickListener {
                    saveLastRatingParameters(false)
                    logCustomEvent(
                        getRatingAnalyticsEvent(etSuggestion.text.toString(), EXIT_SUBMIT_RATING)
                    )
                    findNavController().safeNavigate(R.id.action_ratingFragment_to_ratingFeedbackSuccessFragment)
                }

                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            saveLastRatingParameters(true)
                            findNavController().navigateUp()
                        }
                    }
                )
            }
        }
    }

    fun selectRating(ratingIndex: Int) {
        for (index in 0..4) {
            animationsList[index].visibility =
                if (index == ratingIndex - 1) View.VISIBLE else View.INVISIBLE
            iconsList[index].visibility =
                if (index != ratingIndex - 1) View.VISIBLE else View.INVISIBLE
        }
        ratingViewModel.currentRating = ratingIndex
    }

    private fun ImprovementOptionUI.toChip(): View {
        val chip = SuggestionItemLayoutBinding.inflate(layoutInflater)
        chip.tvSuggestion.text = title
        chip.ivSelector.setImageResource(if (applied) R.drawable.ic_blue_selected else R.drawable.ic_gray_broken_circle)
        chip.clSuggestionLayout.isSelected = applied
        chip.root.setOnClickListener {
            applied = !applied
            chip.selectSuggestion(applied)
        }
        return chip.root
    }

    private fun SuggestionItemLayoutBinding.selectSuggestion(selected: Boolean) {
        if (selected) ivSelector.setImageResource(R.drawable.ic_blue_selected)
        else ivSelector.setImageResource(R.drawable.ic_gray_broken_circle)
        clSuggestionLayout.isSelected = selected
    }

    override val logTag = "RatingFragment"
    override val analyticsScreenName = "app_rating"

}

const val EXIT_X = "x"
const val EXIT_APP_STORE = "app_store"
const val EXIT_NO_THANKS = "no_thanks"
const val EXIT_SUBMIT_RATING = "submit_rating"
