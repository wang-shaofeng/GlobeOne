/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.payment_processing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.addCallback
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.PaymentProcessingFinalFragmentBinding
import ph.com.globe.globeonesuperapp.payment.PaymentNavigationViewModel
import ph.com.globe.globeonesuperapp.utils.COPIED_REFERENCE_NUMBER
import ph.com.globe.globeonesuperapp.utils.copyToClipboard
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.showSnackbar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class PaymentProcessingFinalFragment :
    NoBottomNavViewBindingFragment<PaymentProcessingFinalFragmentBinding>(
        bindViewBy = { PaymentProcessingFinalFragmentBinding.inflate(it) }
    ), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    private val navigationViewModel: PaymentNavigationViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        with(viewBinding) {

            ivCopyToClipboard.setOnClickListener {
                logUiActionEvent("Order no. copy option")
                requireContext().copyToClipboard(tvReferenceNumberValue.text.toString(), COPIED_REFERENCE_NUMBER)

                val snackbarViewBinding =
                    GlobeSnackbarLayoutBinding
                        .inflate(LayoutInflater.from(requireContext()))
                snackbarViewBinding.tvGlobeSnackbarTitle.setText(R.string.copied_to_clipboard)
                snackbarViewBinding.tvGlobeSnackbarDescription.setText(R.string.you_have_copied_the_order_number)

                showSnackbar(snackbarViewBinding)
            }

            btnKeepWaiting.setOnClickListener {
                findNavController().navigateUp()
            }

            if (navigationViewModel.paymentParameters.isLoggedIn) {
                btnGoBack.setText(R.string.go_back_to_home)
                btnGoBack.setOnClickListener {
                    crossBackstackNavigator.crossNavigateWithoutHistory(
                        BaseActivity.DASHBOARD_KEY,
                        R.id.dashboardFragment
                    )
                }
            } else {
                btnGoBack.setText(R.string.go_back_to_shop)
                btnGoBack.setOnClickListener {
                    findNavController().popBackStack(R.id.paymentLandingFragment, true)
                }
            }

            if (!navigationViewModel.paymentParameters.referenceId.isNullOrEmpty()) {
                tvReferenceNumberValue.text = navigationViewModel.paymentParameters.referenceId
            } else {
                clReceiptId.visibility = View.GONE
            }

            // Override back button
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                findNavController().popBackStack(R.id.paymentLandingFragment, false)
            }
        }
    }

    override val logTag = "PaymentProcessingFragment"
    override val analyticsScreenName: String = "pay.processing_final"
}
