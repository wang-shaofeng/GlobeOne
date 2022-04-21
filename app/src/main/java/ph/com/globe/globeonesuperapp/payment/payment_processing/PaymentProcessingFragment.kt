/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.payment_processing

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PaymentProcessingFragmentBinding
import ph.com.globe.globeonesuperapp.payment.PaymentNavigationViewModel
import ph.com.globe.globeonesuperapp.payment.adyen.AdyenPaymentResult
import ph.com.globe.globeonesuperapp.payment.adyen.AdyenPaymentViewModel
import ph.com.globe.globeonesuperapp.payment.charge_to_load.ChargeToLoadPaymentResult
import ph.com.globe.globeonesuperapp.payment.charge_to_load.ChargeToLoadViewModel
import ph.com.globe.globeonesuperapp.payment.gcash.GCashPaymentResult
import ph.com.globe.globeonesuperapp.payment.gcash.GCashPaymentViewModel
import ph.com.globe.globeonesuperapp.payment.payment_processing.ProcessingPaymentResult.*
import ph.com.globe.globeonesuperapp.payment.setCurrentTransactionsResult
import ph.com.globe.globeonesuperapp.utils.OTP_KEY_PROVISION_CONTENT_PROMO
import ph.com.globe.globeonesuperapp.utils.convertToClassicNumberFormat
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import ph.com.globe.model.payment.GlobePaymentMethod
import ph.com.globe.model.payment.PurchaseParams
import ph.com.globe.model.payment.PurchaseType.*
import ph.com.globe.model.payment.checkIfAllFailed
import ph.com.globe.model.util.brand.AccountBrand

@AndroidEntryPoint
class PaymentProcessingFragment :
    NoBottomNavViewBindingFragment<PaymentProcessingFragmentBinding>(
        bindViewBy = { PaymentProcessingFragmentBinding.inflate(it) }
    ) {

    private val appDataViewModel: AppDataViewModel by activityViewModels()

    private val navigationViewModel: PaymentNavigationViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)

    private val adyenPaymentViewModel: AdyenPaymentViewModel by viewModels()

    private val gCashPaymentViewModel: GCashPaymentViewModel by viewModels()

    private val chargeToLoadViewModel: ChargeToLoadViewModel by viewModels()

    private val processingViewModel: PaymentProcessingViewModel by viewModels()

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    private val processingArguments: PaymentProcessingFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLightStatusBar()

        with(navigationViewModel.paymentParameters) {
            when (val entryPoint = processingArguments.entryPoint) {
                is ProcessingEntryPoint.CheckBalanceSufficiency -> {
                    processingViewModel.checkPrepaidBalanceSufficiency(
                        primaryMsisdn.convertToClassicNumberFormat(),
                        purchaseType.amount
                    )
                }
                is ProcessingEntryPoint.ProvisionContentPromoWithOTP -> {
                    processingViewModel.provisionContentPromo(
                        PurchaseParams(
                            sourceNumber = primaryMsisdn.convertToClassicNumberFormat(),
                            targetNumber = navigationViewModel.paymentParameters.primaryMsisdn,
                            purchaseType = purchaseType,
                            chargeToLoad = true,
                            otpReferenceId = entryPoint.otpReferenceId
                        )
                    )
                }
                is ProcessingEntryPoint.AdyenPaymentProcessing -> {
                    processingViewModel.startProcessingTransitions()
                    adyenPaymentViewModel.checkAdyenPaymentAndTryPurchase()
                    adyenPaymentViewModel.paymentResult.observe(viewLifecycleOwner, {
                        it.handleEvent { result ->
                            when (result) {
                                is AdyenPaymentResult.BillPaymentError -> {
                                    navigateOnPaymentFailed(messageInfo = getString(R.string.credit_card_refused_error))
                                }
                                is AdyenPaymentResult.GenericPaymentError -> {
                                    navigateOnPaymentFailed()
                                }
                                is AdyenPaymentResult.PurchaseSuccessful -> {
                                    appDataViewModel.refreshDataAfterTransaction(primaryMsisdn)
                                    setCurrentTransactionsResult(
                                        result.transactionsResult,
                                        GlobePaymentMethod.ThirdPartyPaymentMethod.Adyen,
                                        result.refundSuccessful
                                    )
                                    if (currentTransactionsResult.checkIfAllFailed() && purchaseType !is PayBill) {
                                        findNavController().safeNavigate(
                                            PaymentProcessingFragmentDirections.actionPaymentProcessingFragmentToPaymentRefundFragment(
                                                GlobePaymentMethod.ThirdPartyPaymentMethod.Adyen,
                                                purchaseType.simpleName,
                                                result.refundSuccessful
                                            )
                                        )
                                    } else {
                                        findNavController().safeNavigate(R.id.action_paymentProcessingFragment_to_paymentSuccessfulFragment)
                                    }
                                }
                                is AdyenPaymentResult.PurchaseFailed -> {
                                    navigateOnPurchaseFailed()
                                }
                            }
                        }
                    })
                }
                is ProcessingEntryPoint.GCashPaymentProcessing -> {
                    processingViewModel.startProcessingTransitions()
                    gCashPaymentViewModel.checkGCashPaymentAndTryPurchase()
                    gCashPaymentViewModel.paymentResult.observe(viewLifecycleOwner, {
                        it.handleEvent { result ->
                            when (result) {
                                is GCashPaymentResult.GenericPaymentError -> {
                                    navigateOnPaymentFailed(messageInfo = getString(R.string.you_can_always_try_different_payment_method))
                                }
                                is GCashPaymentResult.PurchaseSuccessful -> {
                                    appDataViewModel.refreshDataAfterTransaction(primaryMsisdn)
                                    setCurrentTransactionsResult(
                                        transactions = result.transactionsResult,
                                        paymentMethod = GlobePaymentMethod.ThirdPartyPaymentMethod.GCash,
                                        refundSuccessful = result.refundSuccessful,
                                        referenceId = result.tokenPaymentId
                                    )
                                    if (currentTransactionsResult.checkIfAllFailed() && purchaseType !is PayBill) {
                                        findNavController().safeNavigate(
                                            PaymentProcessingFragmentDirections.actionPaymentProcessingFragmentToPaymentRefundFragment(
                                                paymentType = GlobePaymentMethod.ThirdPartyPaymentMethod.GCash,
                                                purchaseType = purchaseType.simpleName,
                                                isSuccessful = result.refundSuccessful
                                            )
                                        )
                                    } else {
                                        findNavController().safeNavigate(R.id.action_paymentProcessingFragment_to_paymentSuccessfulFragment)
                                    }
                                }
                            }
                        }
                    })
                }
                is ProcessingEntryPoint.ChargeToLoadPurchasePromo -> {
                    if (purchaseType is BuyGoCreatePromo) {
                        chargeToLoadViewModel.tryPurchaseGoCreatePromo(
                            sourceNumber = entryPoint.mobileNumber,
                            purchaseType = entryPoint.purchaseType,
                            otpReferenceId = entryPoint.otpReferenceId,
                            totalAmount = purchaseType.amount
                        )
                    } else {
                        chargeToLoadViewModel.tryPurchasePromo(
                            sourceNumber = entryPoint.mobileNumber,
                            purchaseType = entryPoint.purchaseType,
                            otpReferenceId = entryPoint.otpReferenceId
                        )
                    }
                    chargeToLoadViewModel.paymentResult.observe(viewLifecycleOwner, {
                        it.handleEvent { result ->
                            when (result) {
                                is ChargeToLoadPaymentResult.PurchaseSuccessful -> {
                                    appDataViewModel.refreshDataAfterTransaction(primaryMsisdn)
                                    navigationViewModel.paymentParameters.setCurrentTransactionsResult(
                                        transactions = result.result.promos,
                                        paymentMethod = GlobePaymentMethod.ChargeToLoad,
                                        referenceId = result.result.promos[0].transactionId
                                            .replace("superapp", "GLAP", ignoreCase = true)
                                    )
                                    if (navigationViewModel.paymentParameters.currentTransactionsResult.checkIfAllFailed()) {
                                        findNavController().safeNavigate(
                                            PaymentProcessingFragmentDirections.actionPaymentProcessingFragmentToPaymentFailedFragment(
                                                getString(R.string.sorry_something_went_wrong)
                                            )
                                        )
                                    } else {
                                        findNavController().safeNavigate(
                                            PaymentProcessingFragmentDirections.actionPaymentProcessingFragmentToPaymentSuccessfulFragment(
                                                isHpwAndChargeToLoad = entryPoint.brand == AccountBrand.Hpw
                                            )
                                        )
                                    }
                                }
                                is ChargeToLoadPaymentResult.PurchaseFailed -> findNavController().safeNavigate(
                                    PaymentProcessingFragmentDirections.actionPaymentProcessingFragmentToPaymentFailedFragment(
                                        getString(R.string.your_payment_declined)
                                    )
                                )
                                is ChargeToLoadPaymentResult.NotEnoughBalance -> {
                                    when (navigationViewModel.paymentParameters.purchaseType) {
                                        is BuyPromo, is BuyGoCreatePromo -> {
                                            navigateOnPaymentFailed(
                                                getString(R.string.insufficient_funds_buy_promo),
                                                getString(R.string.insufficient_funds_buy_promo_info)
                                            )
                                        }
                                        is BuyContent -> {
                                            navigateOnPaymentFailed(
                                                getString(R.string.insufficient_funds_buy_content),
                                                getString(R.string.insufficient_funds_buy_content_info)
                                            )
                                        }
                                        else -> {
                                            navigateOnPaymentFailed(
                                                getString(R.string.insufficient_funds),
                                                getString(R.string.insufficient_funds_info)
                                            )
                                        }
                                    }
                                }
                                is ChargeToLoadPaymentResult.GenericPaymentError -> findNavController().safeNavigate(
                                    PaymentProcessingFragmentDirections.actionPaymentProcessingFragmentToPaymentFailedFragment(
                                        getString(R.string.your_payment_declined)
                                    )
                                )
                            }
                        }
                    })
                }
            }

            processingViewModel.processingResult.observe(viewLifecycleOwner, {
                it.handleEvent { result ->
                    when (result) {
                        is SufficientBalance -> {
                            when (purchaseType) {
                                is BuyContent -> {
                                    // If the user is not logged-in or selected non enrolled account, we need to use OTP verification
                                    // Otherwise we can directly continue purchase flow
                                    if (!isLoggedIn || !isEnrolledAccount) {
                                        verifyOtpViewModel.sendOtp(
                                            primaryMsisdn, listOf(
                                                OTP_KEY_PROVISION_CONTENT_PROMO
                                            ),
                                            outsideUIProcessing = true
                                        )
                                    } else {
                                        processingViewModel.provisionContentPromo(
                                            PurchaseParams(
                                                sourceNumber = navigationViewModel.paymentParameters.primaryMsisdn,
                                                targetNumber = navigationViewModel.paymentParameters.primaryMsisdn,
                                                purchaseType = purchaseType,
                                                chargeToLoad = true
                                            )
                                        )
                                    }
                                }
                                // Can be used to observe processing result for other purchase types
                                else -> {
                                }
                            }
                        }
                        is ProvisionContentPromoSuccessful -> {
                            appDataViewModel.refreshDataAfterTransaction(primaryMsisdn)
                            findNavController().safeNavigate(R.id.action_paymentProcessingFragment_to_paymentSuccessfulFragment)
                        }
                        is ProvisionContentPromoFailed -> {
                            navigateOnPaymentFailed()
                        }
                        is NotEnoughBalance -> {
                            when (navigationViewModel.paymentParameters.purchaseType) {
                                is BuyPromo, is BuyGoCreatePromo -> {
                                    navigateOnPaymentFailed(
                                        getString(R.string.insufficient_funds_buy_promo),
                                        getString(R.string.insufficient_funds_buy_promo_info)
                                    )
                                }
                                is BuyContent -> {
                                    navigateOnPaymentFailed(
                                        getString(R.string.insufficient_funds_buy_content),
                                        getString(R.string.insufficient_funds_buy_content_info)
                                    )
                                }
                                else -> {
                                    navigateOnPaymentFailed(
                                        getString(R.string.insufficient_funds),
                                        getString(R.string.insufficient_funds_info)
                                    )
                                }
                            }
                        }
                        is GeneralProcessingError -> {
                            navigateOnPaymentFailed()
                        }
                    }
                }
            })
        }

        verifyOtpViewModel.sendOtpResult.observe(viewLifecycleOwner, {
            it.handleEvent { result ->
                when (result) {
                    is VerifyOtpViewModel.SendOtpResult.SentOtpSuccess -> {
                        findNavController().safeNavigate(
                            PaymentProcessingFragmentDirections.actionPaymentProcessingFragmentToChargeToLoadFragment(
                                result
                            )
                        )
                    }
                    else -> {
                        // This case includes 'SentOtpFailure' result
                        navigateOnPaymentFailed()
                    }
                }
            }
        })

        processingViewModel.processingIterations.observe(viewLifecycleOwner) {
            it.handleEvent { iteration ->
                when (iteration) {
                    is ProcessingIterations.InitialProcessingIteration -> {
                        viewBinding.lavProcessing.setNewAnimation(R.raw.processing_first)
                        viewBinding.tvHeaderTitle.setText(R.string.we_re_waiting_your_payment_service)
                        viewBinding.tvHeaderMessage.setText(R.string.this_may_take_a_while)
                    }
                    is ProcessingIterations.JustAMomentProcessingIteration -> {
                        viewBinding.lavProcessing.setNewAnimation(R.raw.processing_second)
                        viewBinding.tvHeaderTitle.setText(R.string.just_a_moment_three_dots)
                        viewBinding.tvHeaderMessage.setText(R.string.we_re_finishing_your_payment)
                    }
                    is ProcessingIterations.HangInThereProcessingIteration -> {
                        viewBinding.lavProcessing.setNewAnimation(R.raw.processing_third)
                        viewBinding.tvHeaderTitle.setText(R.string.hang_in_there)
                        viewBinding.tvHeaderMessage.setText(R.string.thanks_for_being_patient)
                    }
                    is ProcessingIterations.FinalProcessingIteration -> {
                        // here the polling reached it's timeout so we explicitly stop the polling
                        // so we can re start it properly if the user chooses 'keep waiting'
                        adyenPaymentViewModel.stopPolling()
                        gCashPaymentViewModel.stopPolling()
                        findNavController().safeNavigate(R.id.action_paymentProcessingFragment_to_paymentProcessingFinalFragment)
                    }
                }
            }
        }

        // Disable back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
    }

    private fun navigateOnPaymentFailed(message: String? = null, messageInfo: String? = null) {
        findNavController().safeNavigate(
            PaymentProcessingFragmentDirections.actionPaymentProcessingFragmentToPaymentFailedFragment(
                message = (message ?: getString(R.string.your_payment_declined)),
                messageInfo = (messageInfo ?: getString(R.string.please_try_again))
            )
        )
    }

    private fun navigateOnPurchaseFailed(error: Exception? = null) {
        findNavController().safeNavigate(
            PaymentProcessingFragmentDirections.actionPaymentProcessingFragmentToPaymentFailedFragment(
                message = getString(R.string.your_purchase_failed),
                messageInfo = getString(R.string.your_purchase_failed_info),
                messageDebug = error.toString()
            )
        )
    }

    private fun LottieAnimationView.setNewAnimation(animationResId: Int) {
        setAnimation(animationResId)
        repeatCount = LottieDrawable.INFINITE
        playAnimation()
    }

    override val logTag = "PaymentProcessingFragment"
}
