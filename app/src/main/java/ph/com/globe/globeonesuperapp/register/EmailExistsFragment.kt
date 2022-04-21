/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.register

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.EmailExistsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class EmailExistsFragment : NoBottomNavViewBindingFragment<EmailExistsFragmentBinding>(
    bindViewBy = { EmailExistsFragmentBinding.inflate(it) }
) {

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    lateinit var analyticsLogger: GlobeAnalyticsLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:register email exist screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.btnLogin.setOnClickListener {
            findNavController().popBackStack(R.id.selectSignMethodFragment, false)
        }
        viewBinding.btnTryUsingAnotherEmail.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override val logTag = "EmailExistsFragment"
}
