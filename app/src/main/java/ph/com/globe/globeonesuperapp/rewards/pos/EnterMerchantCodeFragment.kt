/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.pos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.BUTTON
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.NEXT
import ph.com.globe.analytics.events.PAYMENT_SCREEN
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.EnterMerchantCodeFragmentBinding
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject

@AndroidEntryPoint
class EnterMerchantCodeFragment : NoBottomNavViewBindingFragment<EnterMerchantCodeFragmentBinding>({
    EnterMerchantCodeFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val posViewModel by navGraphViewModels<POSViewModel>(R.id.pos_subgraph) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {

            btnNext.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        PAYMENT_SCREEN, BUTTON, NEXT
                    )
                )
                posViewModel.getMerchantUsingMobileNumber(viewBinding.etCode.text.toString())
                viewBinding.tilCode.error = ""
            }

            etCode.addTextChangedListener {
                it.formatCountryCodeIfExists()

                requireContext().hideError(tilCode, etCode)
            }

            etCode.setOnFocusChangeListener { _, _ ->
                refactorCopiedNumber()
            }

            etCode.setOnClickListener {
                refactorCopiedNumber()
            }

            ivBack.setOnClickListener { findNavController().navigateUp() }

            posViewModel.merchantStatus.oneTimeEventObserve(viewLifecycleOwner) {
                it.onSuccess {
                    if (posViewModel.chosenAccount == null) {
                        findNavController().safeNavigate(R.id.action_enterMerchantCodeFragment_to_whichEnrolledAccountFragment)
                    } else {
                        findNavController().safeNavigate(R.id.action_enterMerchantCodeFragment_to_payPointsFragment)
                    }
                }.onFailure {
                    viewBinding.tilCode.error = getString(R.string.invalid_merchant_code)
                }
            }
        }
    }

    private fun refactorCopiedNumber() {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipDataItem = clipboardManager.primaryClip?.getItemAt(0)
        val pastedNumber = clipDataItem?.text.toString().formattedForPhilippines()
        if (pastedNumber.length == 10 || pastedNumber.length == 11)
            clipboardManager.setPrimaryClip(
                ClipData.newPlainText("Phone Number", pastedNumber)
            )
    }

    override val logTag: String = "EnterMerchantCodeFragment"

    override val analyticsScreenName = "pos.enter_code"
}
