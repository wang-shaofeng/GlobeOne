/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.payment_refund

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.CLICKABLE_TEXT
import ph.com.globe.analytics.events.DONE
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.REFUND_SCREEN
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.PaymentRefundFragmentBinding
import ph.com.globe.globeonesuperapp.payment.PaymentNavigationViewModel
import ph.com.globe.globeonesuperapp.utils.COPIED_REFERENCE_NUMBER
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.copyToClipboard
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.payment.doubleToPesos
import ph.com.globe.globeonesuperapp.utils.permissions.registerActivityResultForStoragePermission
import ph.com.globe.globeonesuperapp.utils.permissions.requestStoragePermissionsIfNeededAndPerformSuccessAction
import ph.com.globe.globeonesuperapp.utils.showSnackbar
import ph.com.globe.globeonesuperapp.utils.takeScreenshotFlow
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.payment.GlobePaymentMethod
import javax.inject.Inject

@AndroidEntryPoint
class PaymentRefundFragment :
    NoBottomNavViewBindingFragment<PaymentRefundFragmentBinding>(
        bindViewBy = { PaymentRefundFragmentBinding.inflate(it) }
    ), AnalyticsScreen {

    private val navigationViewModel: PaymentNavigationViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val paymentRefundFragmentArgs: PaymentRefundFragmentArgs by navArgs()

    var requestStorageActivityLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStorageActivityLauncher = registerActivityResultForStoragePermission {
            takeScreenshotFlow(viewBinding)
        }
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:refund screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            this.setDescription()
            btnDone.setOnClickListener {
                if (navigationViewModel.paymentParameters.isLoggedIn) {
                    crossBackstackNavigator.crossNavigateWithoutHistory(
                        BaseActivity.DASHBOARD_KEY,
                        R.id.dashboardFragment
                    )
                } else {
                    crossBackstackNavigator.crossNavigateWithoutHistory(
                        BaseActivity.AUTH_KEY,
                        R.id.dashboardFragment
                    )
                }
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        REFUND_SCREEN, CLICKABLE_TEXT, DONE
                    )
                )
            }
        }
    }

    private fun PaymentRefundFragmentBinding.setDescription() {
        tvWeWereUnable.setText(
            when (paymentRefundFragmentArgs.purchaseType) {
                "load" -> {
                    tvWeWereUnableDescription.setText(
                        when {
                            paymentRefundFragmentArgs.paymentType is GlobePaymentMethod.ThirdPartyPaymentMethod.GCash && paymentRefundFragmentArgs.isSuccessful -> R.string.something_went_wrong_with_the_load_gcash_succeeded
                            paymentRefundFragmentArgs.paymentType is GlobePaymentMethod.ThirdPartyPaymentMethod.GCash && !paymentRefundFragmentArgs.isSuccessful -> R.string.something_went_wrong_with_the_load_gcash_failed
                            else -> R.string.something_went_wrong_with_the_load_credit_debit_card
                        }
                    )
                    R.string.sorry_we_re_unable_to_process_your_load
                }
                "content" -> {
                    tvWeWereUnableDescription.setText(
                        when {
                            paymentRefundFragmentArgs.paymentType is GlobePaymentMethod.ThirdPartyPaymentMethod.GCash && paymentRefundFragmentArgs.isSuccessful -> R.string.something_went_wrong_with_the_content_gcash_succeeded
                            paymentRefundFragmentArgs.paymentType is GlobePaymentMethod.ThirdPartyPaymentMethod.GCash && !paymentRefundFragmentArgs.isSuccessful -> R.string.something_went_wrong_with_the_content_gcash_failed
                            else -> R.string.something_went_wrong_with_the_content_credit_debit_card
                        }
                    )
                    R.string.sorry_we_were_not_able_to_register_your_content
                }
                "paybill" -> {
                    tvWeWereUnableDescription.setText(R.string.you_can_always_try_different_payment_method)
                    R.string.your_payment_declined
                }
                else -> {
                    tvWeWereUnableDescription.setText(
                        when {
                            paymentRefundFragmentArgs.paymentType is GlobePaymentMethod.ThirdPartyPaymentMethod.GCash && paymentRefundFragmentArgs.isSuccessful -> R.string.something_went_wrong_with_the_promo_gcash_succeeded
                            paymentRefundFragmentArgs.paymentType is GlobePaymentMethod.ThirdPartyPaymentMethod.GCash && !paymentRefundFragmentArgs.isSuccessful -> R.string.something_went_wrong_with_the_promo_gcash_failed
                            else -> R.string.something_went_wrong_with_the_promo_credit_debit_card
                        }
                    )
                    R.string.sorry_we_re_unable_to_register_your_promo
                }
            }
        )
        tvRefund.setText(
            if (paymentRefundFragmentArgs.isSuccessful) R.string.refunded
            else R.string.your_refund
        )

        with(navigationViewModel.paymentParameters) {

            tvRefundAmount.text =
                currentAmount?.toDouble()?.doubleToPesos() ?: price.doubleToPesos()

            if (!referenceId.isNullOrEmpty()) {
                tvReferenceNumberValue.text = referenceId

                ivCopyToClipboard.setOnClickListener {
                    logUiActionEvent("Order no. copy option")
                    requireContext().copyToClipboard(referenceId ?: "", COPIED_REFERENCE_NUMBER)

                    val snackbarViewBinding =
                        GlobeSnackbarLayoutBinding
                            .inflate(LayoutInflater.from(requireContext()))
                    snackbarViewBinding.tvGlobeSnackbarTitle.setText(R.string.copied_to_clipboard)
                    snackbarViewBinding.tvGlobeSnackbarDescription.setText(R.string.you_have_copied_the_order_number)

                    showSnackbar(snackbarViewBinding)
                }
            } else {
                groupReceiptId.visibility = View.GONE
            }
        }

        ivDownloadReceipt.setOnClickListener {

            logUiActionEvent("Download receipt option")
            if (requestStorageActivityLauncher != null) {
                requestStoragePermissionsIfNeededAndPerformSuccessAction(
                    requestStorageActivityLauncher!!
                )
            } else {
                takeScreenshotFlow(viewBinding)
            }
        }
    }

    override val logTag = "PaymentRefundFragment"

    override val analyticsScreenName: String = "pay.results_refund"
}
