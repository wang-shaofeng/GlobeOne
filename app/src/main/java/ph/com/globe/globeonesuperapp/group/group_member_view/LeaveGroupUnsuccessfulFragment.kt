/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group.group_member_view

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.databinding.LeaveGroupUnsuccessfulFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

class LeaveGroupUnsuccessfulFragment :
    NoBottomNavViewBindingFragment<LeaveGroupUnsuccessfulFragmentBinding>({
        LeaveGroupUnsuccessfulFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.btnGoBack.setOnClickListener {
            logCustomEvent(
                analyticsEventsProvider.provideEvent(
                    EventCategory.Engagement,
                    ACCOUNT_DETAILS_SCREEN, BUTTON, BACK
                )
            )
            findNavController().navigateUp()
        }
    }

    override val logTag = "LeaveGroupUnsuccessfulFragment"

    override val analyticsScreenName = "group.leave_unsuccessful"
}
