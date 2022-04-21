/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.adyen

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PaymentAdyenFragmentBinding
import ph.com.globe.globeonesuperapp.payment.PaymentNavigationViewModel
import ph.com.globe.globeonesuperapp.payment.adyen.util.GlobeDropInComponent
import ph.com.globe.globeonesuperapp.payment.payment_processing.ProcessingEntryPoint
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.payment.*
import ph.com.globe.model.payment.ThirdPartyPaymentResult.AdyenResult.*
import javax.inject.Inject


@AndroidEntryPoint
class AdyenPaymentFragment :
    NoBottomNavViewBindingFragment<PaymentAdyenFragmentBinding>(
        bindViewBy = { PaymentAdyenFragmentBinding.inflate(it) }
    ) {

    private val viewModel: AdyenPaymentViewModel by viewModels()

    private val paymentNavigationViewModel: PaymentNavigationViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)

    private val adyenArguments: AdyenPaymentFragmentArgs by navArgs()

    @Inject
    lateinit var globeDropInComponent: GlobeDropInComponent

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val jsonObject = JSONObject(adyenArguments.paymentMethodJson)

        globeDropInComponent.startPayment(
            paymentNavigationViewModel.paymentParameters.currentAmount
                ?: paymentNavigationViewModel.paymentParameters.totalAmount,
            jsonObject,
            this@AdyenPaymentFragment
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        globeDropInComponent.handleOnActivityResult(
            requestCode,
            resultCode,
            data,
            successAction = {
                findNavController().safeNavigate(
                    AdyenPaymentFragmentDirections.actionAdyenPaymentFragmentToPaymentProcessingFragment(
                        ProcessingEntryPoint.AdyenPaymentProcessing
                    )
                )
            },
            cancelAction = { findNavController().navigateUp() },
            errorAction = { navigateOnPaymentFailed() },
            noInternetAction = {
                navigateOnPaymentFailed(
                    getString(R.string.looks_like_you_are_offline),
                    getString(R.string.please_check_your_internet_connection_and_try_again)
                )
            },
            cardRefusedAction = { navigateOnPaymentFailed(errorMessageInfo = getString(R.string.credit_card_refused_error)) }
        )
    }

    private fun navigateOnPaymentFailed(
        errorMessage: String? = null,
        errorMessageInfo: String? = null,
        error: Exception? = null
    ) {
        findNavController().safeNavigate(
            AdyenPaymentFragmentDirections.actionAdyenPaymentFragmentToPaymentFailedFragment(
                message = errorMessage ?: getString(R.string.your_payment_declined),
                messageInfo = errorMessageInfo,
                messageDebug = error.toString()
            )
        )
    }

    override val logTag = "AdyenPaymentFragment"
}
