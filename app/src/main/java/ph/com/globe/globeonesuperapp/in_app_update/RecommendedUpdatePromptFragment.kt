/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.in_app_update

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.databinding.RecommendedUpdatePromptFragmentBinding
import ph.com.globe.globeonesuperapp.splash.SplashFragment.Companion.CANCEL_KEY
import ph.com.globe.globeonesuperapp.splash.SplashFragment.Companion.IN_APP_UPDATE_RECOMMENDED_KEY
import ph.com.globe.globeonesuperapp.splash.SplashFragment.Companion.IN_APP_UPDATE_RECOMMENDED_KEY_VALUE
import ph.com.globe.globeonesuperapp.splash.SplashFragment.Companion.UPDATE_KEY
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class RecommendedUpdatePromptFragment :
    NoBottomNavViewBindingFragment<RecommendedUpdatePromptFragmentBinding>(bindViewBy = {
        RecommendedUpdatePromptFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:autoupdate screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {
            btnGoToDashboard.setOnClickListener {
                setFragmentResult(
                    IN_APP_UPDATE_RECOMMENDED_KEY,
                    bundleOf(IN_APP_UPDATE_RECOMMENDED_KEY_VALUE to CANCEL_KEY)
                )
                findNavController().navigateUp()
            }
            btnUpdateOnAppStore.setOnClickListener {
                setFragmentResult(
                    IN_APP_UPDATE_RECOMMENDED_KEY,
                    bundleOf(IN_APP_UPDATE_RECOMMENDED_KEY_VALUE to UPDATE_KEY)
                )
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { }
    }

    override val logTag: String = "RecommendedUpdatePromptFragment"

    override val analyticsScreenName: String = "autoupdate.mandatory"
}
