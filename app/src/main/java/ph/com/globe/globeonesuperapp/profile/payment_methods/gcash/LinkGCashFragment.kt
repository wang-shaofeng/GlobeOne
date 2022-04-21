/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods.gcash

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.ADD_ACCOUNT
import ph.com.globe.analytics.events.BUTTON
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.PAYMENT_OPTIONS_SCREEN
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.FROM_DASHBOARD
import ph.com.globe.globeonesuperapp.databinding.ProfileLinkGCashFragmentBinding
import ph.com.globe.globeonesuperapp.profile.payment_methods.PaymentMethodsViewModel
import ph.com.globe.globeonesuperapp.utils.OTP_KEY_G_CASH_LINK
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LinkGCashFragment :
    NoBottomNavViewBindingFragment<ProfileLinkGCashFragmentBinding>(bindViewBy = {
        ProfileLinkGCashFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    private val paymentMethodsViewModel: PaymentMethodsViewModel by navGraphViewModels(R.id.payment_methods_subgraph) { defaultViewModelProviderFactory }

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private lateinit var selectedAccount: LinkGCashAccountItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLightStatusBar()
        with(viewBinding) {
            rvAccounts.isNestedScrollingEnabled = false
            rvLinkedAccounts.isNestedScrollingEnabled = false
            wfChooseAnAccount.onBack {
                findNavController().popBackStack(R.id.payment_methods_fragment, false)
            }
            with(paymentMethodsViewModel) {
                if (notLinkedGCashAccounts.value?.size != 0) {
                    val unlinkedAccountAdapter = LinkGCashAccountAdapter({ account ->
                        selectedAccount = account
                        verifyOtpViewModel.sendOtp(
                            selectedAccount.accountMsisdn,
                            listOf(OTP_KEY_G_CASH_LINK)
                        )
                    })
                    rvAccounts.adapter = unlinkedAccountAdapter
                    unlinkedAccountAdapter.submitList(notLinkedGCashAccounts.value?.map { it.toLinkGCashAccountItem() })
                } else {
                    clUnlinked.visibility = View.GONE
                }

                if (linkedGCashAccounts.value?.size != 0) {
                    val linkedAccountAdapter = LinkGCashAccountAdapter(isLinked = true)
                    rvLinkedAccounts.adapter = linkedAccountAdapter
                    linkedAccountAdapter.submitList(linkedGCashAccounts.value?.map { it.toLinkGCashAccountItem() })
                    tvUnlikedAccountsTitle.visibility = View.VISIBLE
                } else {
                    clLinked.visibility = View.GONE
                    vHorizontalLine.visibility = View.GONE
                }

                if (linkedGCashAccounts.value?.size == 0 && notLinkedGCashAccounts.value?.size == 0) {
                    incEmptyState.root.visibility = View.VISIBLE
                    incEmptyState.btnAddGCashAccount.setOnClickListener {
                        logCustomEvent(
                            analyticsEventsProvider.provideEvent(
                                EventCategory.Engagement,
                                PAYMENT_OPTIONS_SCREEN, BUTTON, ADD_ACCOUNT
                            )
                        )
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.ADD_ACCOUNT_KEY,
                            R.id.addAccountNumberFragment,
                            bundleOf(FROM_DASHBOARD to true)
                        )
                    }
                }
            }
        }

        verifyOtpViewModel.sendOtpResult.observe(viewLifecycleOwner) {
            it.handleEvent { result ->
                when (result) {
                    is VerifyOtpViewModel.SendOtpResult.SentOtpSuccess -> {
                        findNavController().safeNavigate(
                            LinkGCashFragmentDirections.actionLinkGCashFragmentToLinkGCashOtpFragment(
                                result, selectedAccount.accountName
                            )
                        )
                    }
                    else -> Unit
                }
            }
        }
    }

    override val logTag: String = "LinkGCashFragment"

    override val analyticsScreenName = "profile.link_gcash_account"
}
