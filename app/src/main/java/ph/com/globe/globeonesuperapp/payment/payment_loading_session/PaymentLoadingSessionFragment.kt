package ph.com.globe.globeonesuperapp.payment.payment_loading_session

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PaymentProcessingFragmentBinding
import ph.com.globe.globeonesuperapp.payment.PaymentNavigationViewModel
import ph.com.globe.globeonesuperapp.payment.adyen.util.GlobeDropInComponent
import ph.com.globe.globeonesuperapp.payment.setCurrentTransactionsResult
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.payment.*
import javax.inject.Inject


@AndroidEntryPoint
class PaymentLoadingSessionFragment :
    NoBottomNavViewBindingFragment<PaymentProcessingFragmentBinding>(
        bindViewBy = { PaymentProcessingFragmentBinding.inflate(it) }
    ) {

    private val viewModel: PaymentLoadingSessionViewModel by viewModels()

    private val paymentNavigationViewModel: PaymentNavigationViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)

    private val paymentLoadingArguments: PaymentLoadingSessionFragmentArgs by navArgs()

    @Inject
    lateinit var globeDropInComponent: GlobeDropInComponent

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.tvHeaderTitle.setText(R.string.just_a_moment_three_dots)

        with(viewModel) {
            with(paymentNavigationViewModel) {
                if (paymentLoadingArguments.paymentType == GlobePaymentMethod.ThirdPartyPaymentMethod.GCash) {
                    viewBinding.tvHeaderMessage.setText(R.string.we_re_waiting_for_gcash_respond)
                    createGCashPaymentSession(
                        CreatePaymentSessionParams(
                            accountNumber = paymentParameters.accountNumber,
                            emailAddress = paymentParameters.emailAddress,
                            mobileNumber = paymentParameters.primaryMsisdn,
                            requestType = paymentParameters.paymentType,
                            transactionType = paymentParameters.transactionType,
                            paymentType = GCASH,
                            price = paymentParameters.totalAmount.toString(),
                            purchaseType = paymentParameters.purchaseType,
                            amountAfterDiscount = paymentParameters.currentAmount
                        )
                    )
                    checkoutUrl.observe(viewLifecycleOwner, {
                        if (!checkoutUrl.value.isNullOrEmpty()) {
                            findNavController().safeNavigate(
                                PaymentLoadingSessionFragmentDirections.actionPaymentLoadingSessionFragmentToGCashPaymentFragment(
                                    checkoutUrl.value.toString()
                                )
                            )
                        }
                    })
                    paymentResultGcash.observe(viewLifecycleOwner, {
                        it.handleEvent { result ->
                            when (result) {
                                is PaymentLoadingSessionViewModel.GCashPaymentResult.GenericPaymentError -> {
                                    navigateOnPaymentFailed()
                                }
                                is PaymentLoadingSessionViewModel.GCashPaymentResult.PaymentSessionCreated -> {
                                    paymentNavigationViewModel.paymentParameters.setCurrentTransactionsResult(
                                        referenceId = result.tokenPaymentId,
                                        paymentMethod = GlobePaymentMethod.ThirdPartyPaymentMethod.GCash
                                    )
                                }
                            }
                        }
                    })
                } else if (paymentLoadingArguments.paymentType == GlobePaymentMethod.ThirdPartyPaymentMethod.Adyen) {
                    viewBinding.tvHeaderMessage.setText(R.string.we_re_waiting_your_payment_service_respond)
                    createAdyenPaymentSession(
                        CreatePaymentSessionParams(
                            accountNumber = paymentParameters.accountNumber,
                            emailAddress = paymentParameters.emailAddress,
                            mobileNumber = paymentParameters.primaryMsisdn,
                            requestType = paymentParameters.paymentType,
                            transactionType = paymentParameters.transactionType,
                            paymentType = ADYEN_DROPIN,
                            price = paymentParameters.totalAmount.toString(),
                            purchaseType = paymentParameters.purchaseType,
                            returnUrl = globeDropInComponent.getReturnUrl(),
                            amountAfterDiscount = paymentParameters.currentAmount
                        )
                    )
                    paymentResultAdyen.observe(viewLifecycleOwner, {
                        it.handleEvent { result ->
                            when (result) {
                                is PaymentLoadingSessionViewModel.AdyenPaymentResult.GenericPaymentError -> {
                                    navigateOnPaymentFailed()
                                }
                                is PaymentLoadingSessionViewModel.AdyenPaymentResult.PaymentSessionCreated -> {
                                    paymentNavigationViewModel.paymentParameters.setCurrentTransactionsResult(
                                        referenceId = result.tokenPaymentId,
                                        paymentMethod = GlobePaymentMethod.ThirdPartyPaymentMethod.Adyen
                                    )
                                }
                            }
                        }
                    })
                    paymentMethodsJSON.observe(
                        viewLifecycleOwner,
                        { paymentMethodsJSON ->
                            if (paymentMethodsJSON.toString().isNotEmpty()) {
                                findNavController().safeNavigate(
                                    PaymentLoadingSessionFragmentDirections.actionPaymentLoadingSessionFragmentToAdyenPaymentFragment(
                                        paymentMethodsJSON.toString()
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
    }

    private fun navigateOnPaymentFailed(error: Exception? = null) {
        findNavController().safeNavigate(
            PaymentLoadingSessionFragmentDirections.actionPaymentLoadingSessionFragmentToPaymentFailedFragment(
                message = getString(R.string.your_payment_declined),
                messageDebug = error.toString()
            )
        )
    }
    override val logTag = "GCashPaymentFragment"
}
