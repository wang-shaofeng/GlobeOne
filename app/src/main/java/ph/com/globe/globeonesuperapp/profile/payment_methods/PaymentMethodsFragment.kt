/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ProfilePaymentMethodsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class PaymentMethodsFragment :
    NoBottomNavViewBindingFragment<ProfilePaymentMethodsFragmentBinding>(bindViewBy = {
        ProfilePaymentMethodsFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val paymentMethodsViewModel: PaymentMethodsViewModel by navGraphViewModels(R.id.payment_methods_subgraph) { defaultViewModelProviderFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:payment method screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setLightStatusBar()

        with(paymentMethodsViewModel) {
            with(viewBinding) {
                wfProfile.onBack {
                    findNavController().navigateUp()
                }
                clGCash.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            PAYMENT_OPTIONS_SCREEN, CLICKABLE_TEXT, GCASH
                        )
                    )
                    if (linkedGCashAccounts.value?.size == 0) {
                        findNavController().safeNavigate(R.id.action_payment_methods_fragment_to_linkGCashFragment)
                    } else if (!linkedGCashAccounts.value.isNullOrEmpty()) {
                        findNavController().safeNavigate(R.id.action_payment_methods_fragment_to_manageGCashMethodFragment)
                    }
                }
                clCreditCard.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            PAYMENT_OPTIONS_SCREEN, CLICKABLE_TEXT, CREDIT_CARD
                        )
                    )
                    if (!linkedCreditCardAccounts.value.isNullOrEmpty()) {
                        findNavController().safeNavigate(R.id.action_payment_methods_fragment_to_manageCreditCardsMethod)
                    }
                }
                canRefreshMediatorLiveData.observe(viewLifecycleOwner, {
                    ibRefresh.visibility = if (it) View.VISIBLE else View.GONE
                })
                ibRefresh.setOnClickListener {
                    ibRefresh.visibility = View.GONE
                    getCreditCards()
                    getGCashAccounts()
                }
                linkedCreditCardAccounts.observe(viewLifecycleOwner, {
                    if (it.isEmpty()) {
                        tvCreditCardNumberAccounts.text =
                            getString(R.string.you_haven_t_linked_a_card_yet)
                        ivCreditCardAction.visibility = View.GONE
                    } else {
                        tvCreditCardNumberAccounts.text =
                            getString(R.string.there_are_n_linked_accounts, it.size.toString())
                        ivCreditCardAction.visibility = View.VISIBLE
                    }
                    tvCreditCardNumberAccounts.isEnabled = true
                })
                linkedGCashAccounts.observe(viewLifecycleOwner, {
                    ivGCashAction.visibility = View.VISIBLE
                    if (it.isEmpty()) {
                        tvGCashNumberAccounts.text =
                            getString(R.string.you_haven_t_linked_an_account_yet)
                        ivGCashAction.setImageResource(R.drawable.ic_plus_normal)
                    } else {
                        tvGCashNumberAccounts.text =
                            getString(R.string.there_are_n_linked_accounts, it.size.toString())
                        ivGCashAction.setImageResource(R.drawable.ic_arrow_right)
                    }
                    tvGCashNumberAccounts.isEnabled = true
                })
            }
        }
    }

    override val logTag = "PaymentMethodsFragment"

    override val analyticsScreenName = "profile.payment_methods"
}
