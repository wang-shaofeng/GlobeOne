/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.email_verification

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.BUTTON
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.SIGN_IN_TO_GLOBE_ONE
import ph.com.globe.analytics.events.VERIFICATION_SCREEN
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.EmailVerificationStatusFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class EmailVerificationStatusFragment :
    NoBottomNavViewBindingFragment<EmailVerificationStatusFragmentBinding>({
        EmailVerificationStatusFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    private val args by navArgs<EmailVerificationStatusFragmentArgs>()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    lateinit var backstackNavigator: CrossBackstackNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            if (args.isSuccess) {
                btnSignInToGlobeOne.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Conversion,
                            VERIFICATION_SCREEN, BUTTON, SIGN_IN_TO_GLOBE_ONE
                        )
                    )
                    findNavController().safeNavigate(EmailVerificationStatusFragmentDirections.actionEmailVerificationStatusFragmentToNavigationAddAccount(true))
                }
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { } // block onbackpress
            } else {
                lavEmailVerifiedStatus.setAnimation(R.raw.failure)

                tvEmailVerifiedStatus.text = getString(R.string.sorry_something_went_wrong)
                tvEmailVerifiedStatusDescription.text = getString(R.string.please_try_again)

                btnSignInToGlobeOne.text = getString(R.string.back)

                btnSignInToGlobeOne.setOnClickListener { findNavController().navigateUp() }
            }
        }
    }

    override val logTag: String = "EmailVerificationStatusFragment"

    override val analyticsScreenName = "email_verification.status"
}
