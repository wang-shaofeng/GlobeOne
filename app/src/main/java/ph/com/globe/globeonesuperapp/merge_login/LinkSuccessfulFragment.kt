/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.merge_login

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.LinkSuccessfulFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class LinkSuccessfulFragment :
    NoBottomNavViewBindingFragment<LinkSuccessfulFragmentBinding>(bindViewBy = {
        LinkSuccessfulFragmentBinding.inflate(it)
    }) {
    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    lateinit var analyticsLogger: GlobeAnalyticsLogger

    private val args: LinkSuccessfulFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:link successful screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        viewBinding.tvHeaderMessage.text =
            getString(R.string.your_account_now_linked, args.recentProviderText)

        viewBinding.btnGoHome.setOnClickListener {
            crossBackstackNavigator.crossNavigateWithoutHistory(
                BaseActivity.DASHBOARD_KEY,
                R.id.dashboardFragment
            )
        }
    }

    override val logTag = "LinkSuccessfulFragment"
}
