/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.gcash

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PaymentGcashFragmentBinding
import ph.com.globe.globeonesuperapp.payment.payment_processing.ProcessingEntryPoint
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.web_view_components.GlobeGCashInterceptor
import ph.com.globe.globeonesuperapp.web_view_components.InterceptingGlobeWebViewClient
import ph.com.globe.globeonesuperapp.web_view_components.interceptingWebComponent
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess

@AndroidEntryPoint
class GCashPaymentFragment :
    NoBottomNavViewBindingFragment<PaymentGcashFragmentBinding>(
        bindViewBy = { PaymentGcashFragmentBinding.inflate(it) }
    ) {

    private val viewModel: GCashPaymentViewModel by viewModels()

    private val gCashArguments: GCashPaymentFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            lifecycleScope.launch {
                viewBinding.webView.interceptingWebComponent(
                    InterceptingGlobeWebViewClient(urlInterceptor = GlobeGCashInterceptor())
                )
                    .showAndWaitForResult(
                        url = gCashArguments.checkoutUrl
                    )
                    .onSuccess {
                        findNavController().safeNavigate(
                            GCashPaymentFragmentDirections.actionGCashPaymentFragmentToPaymentProcessingFragment(
                                ProcessingEntryPoint.GCashPaymentProcessing
                            )
                        )
                    }
                    .onFailure {
                        navigateOnPaymentFailed()
                    }
            }
            viewBinding.webView.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                    // we are only handling cases when user presses the system back button
                    if (viewBinding.webView.copyBackForwardList().currentIndex <= INITIAL_GCASH_PAGE_INDEX) {
                        // if we are at the initial page or any page before initial one we go back to the previous fragment
                        findNavController().navigateUp()
                    } else {
                        // else we explicitly call viewBinding.webView.goBack() since the system don't handle 'false' as expected
                        // (default behaviour misbehaves)
                        viewBinding.webView.goBack()
                    }
                    // returning true since we overrode the default behaviour
                    true
                } else {
                    // returning false so the webView will proceed with the default behaviour
                    false
                }
            }
            paymentResult.observe(viewLifecycleOwner, {
                it.handleEvent { result ->
                    when (result) {
                        is GCashPaymentResult.GenericPaymentError -> {
                            navigateOnPaymentFailed()
                        }
                    }
                }
            })
        }
    }

    private fun navigateOnPaymentFailed(error: Exception? = null) {
        findNavController().safeNavigate(
            GCashPaymentFragmentDirections.actionGCashPaymentFragmentToPaymentFailedFragment(
                message = getString(R.string.your_payment_declined),
                messageDebug = error.toString()
            )
        )
    }

    private companion object {
        private const val INITIAL_GCASH_PAGE_INDEX = 1
    }

    override val logTag = "GCashPaymentFragment"
}
