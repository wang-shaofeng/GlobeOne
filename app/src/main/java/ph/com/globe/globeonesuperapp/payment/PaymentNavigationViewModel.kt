/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.BoosterItem
import ph.com.globe.globeonesuperapp.utils.payment.*
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.model.payment.BoosterInfo
import ph.com.globe.model.payment.GlobePaymentMethod
import ph.com.globe.model.payment.PurchaseType
import ph.com.globe.model.payment.TransactionResult
import ph.com.globe.model.shop.domain_models.Validity
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountBrandType
import javax.inject.Inject

@HiltViewModel
class PaymentNavigationViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    lateinit var paymentParameters: PaymentParameters

    private val _landingScreenSetup = MutableLiveData<PurchaseType>()
    val landingScreenSetup: LiveData<PurchaseType> = _landingScreenSetup

    fun initiatePaymentLandingScreen() {
        _landingScreenSetup.value = paymentParameters.purchaseType
    }

    init {
        savedStateHandle.get<PaymentParameters>("paymentParameter")?.let {
            paymentParameters = it
        }
    }

    override fun onCleared() {
        if (::paymentParameters.isInitialized)
            savedStateHandle.set("paymentParameter", paymentParameters)
        super.onCleared()
    }

    fun initPaymentWithParameters(paymentParams: PaymentParameters) {
        paymentParameters = paymentParams
        paymentParameters.purchaseType = when (paymentParameters.paymentType) {
            BUY_PROMO -> {
                if (paymentParams.paymentName == GO_CREATE) {
                    PurchaseType.BuyGoCreatePromo(
                        displayName = paymentParams.paymentName,
                        chargePromoServiceId = paymentParameters.chargePromoId ?: "",
                        nonChargePromoServiceId = paymentParameters.nonChargePromoId ?: "",
                        amount = paymentParams.amount.toString(),
                        chargeServiceParam = paymentParams.chargePromoParam ?: "",
                        nonChargeServiceParam = paymentParams.nonChargePromoParam ?: ""
                    )
                } else {
                    when (paymentParameters.shareablePromo) {
                        true -> {
                            PurchaseType.BuyShareablePromo(
                                displayName = paymentParams.paymentName,
                                chargePromoServiceId = paymentParameters.chargePromoId ?: "",
                                nonChargePromoServiceId = paymentParameters.nonChargePromoId ?: "",
                                productKeyword = paymentParameters.apiProvisioningKeyword ?: "",
                                shareKeyword = paymentParameters.shareKeyword ?: "",
                                amount = paymentParams.amount,
                                chargeServiceParam = paymentParams.chargePromoParam ?: "",
                                nonChargeServiceParam = paymentParams.nonChargePromoParam ?: "",
                                boosters = paymentParams.selectedBoosters?.map {
                                    BoosterInfo(
                                        chargePromoServiceId = it.serviceId,
                                        nonChargePromoServiceId = it.nonChargeServiceId,
                                        productKeyword = it.productKeyword,
                                        price = it.boosterPrice,
                                        provisionByServiceId = it.provisionByServiceId,
                                        chargePromoParam = it.chargeBoosterParam,
                                        nonChargePromoParam = it.nonChargeBoosterParam,
                                    )
                                },
                                shareFee = paymentParams.shareFee,
                                provisionByServiceId = paymentParams.provisionByServiceId
                            )
                        }
                        false -> {
                            PurchaseType.BuyNonShareablePromo(
                                displayName = paymentParams.paymentName,
                                chargePromoServiceId = paymentParameters.chargePromoId ?: "",
                                nonChargePromoServiceId = paymentParameters.nonChargePromoId ?: "",
                                productKeyword = paymentParameters.apiProvisioningKeyword ?: "",
                                shareKeyword = paymentParameters.shareKeyword ?: "",
                                amount = paymentParams.amount,
                                chargeServiceParam = paymentParams.chargePromoParam ?: "",
                                nonChargeServiceParam = paymentParams.nonChargePromoParam ?: "",
                                boosters = paymentParams.selectedBoosters?.map {
                                    BoosterInfo(
                                        chargePromoServiceId = it.serviceId,
                                        nonChargePromoServiceId = it.nonChargeServiceId,
                                        productKeyword = it.productKeyword,
                                        price = it.boosterPrice,
                                        provisionByServiceId = it.provisionByServiceId,
                                        chargePromoParam = it.chargeBoosterParam,
                                        nonChargePromoParam = it.nonChargeBoosterParam,
                                    )
                                },
                                provisionByServiceId = paymentParameters.provisionByServiceId
                            )
                        }
                    }
                }
            }
            BUY_CONTENT -> {
                PurchaseType.BuyContentRegular(
                    amount = "${paymentParameters.amount}",
                    provisionByServiceId = paymentParameters.provisionByServiceId,
                    chargePromoServiceId = paymentParameters.chargePromoId ?: "",
                    chargeServiceParam = paymentParameters.chargePromoParam ?: "",
                    productKeyword = paymentParams.apiProvisioningKeyword ?: ""
                )
            }
            BUY_VOUCHER -> {
                PurchaseType.BuyContentVoucher(
                    displayName = paymentParams.paymentName,
                    partnerName = paymentParams.partnerName ?: "",
                    denomCategory = paymentParams.denomCategory ?: "",
                    brand = paymentParams.brand,
                    productDescription = paymentParams.productDescription ?: "",
                    amount = "${paymentParameters.amount}"
                )
            }
            PAY_BILLS -> {
                PurchaseType.PayBill(paymentParameters.amount.toString())
            }
            else -> {
                if (paymentParameters.isRetailer) {
                    PurchaseType.BuyLoadRetailer("${paymentParameters.amount}")
                } else {
                    PurchaseType.BuyLoadConsumer(
                        "${paymentParameters.amount}",
                        "${paymentParameters.currentAmount}"
                    )
                }
            }
        }
        paymentParameters.isLoggedIn =
            authDomainManager.getLoginStatus() != LoginStatus.NOT_LOGGED_IN
    }
}

fun PaymentParameters.setCurrentTransactionsResult(
    transactions: List<TransactionResult>? = null,
    paymentMethod: GlobePaymentMethod? = null,
    refundSuccessful: Boolean? = null,
    referenceId: String? = null
) {
    currentTransactionsResult = transactions
    paymentMethod?.let { this.globePaymentMethod = it }
    refundSuccessful?.let { this.refundSuccessful = it }
    referenceId?.let { this.referenceId = it }
}

@Parcelize
data class PaymentParameters(
    val primaryMsisdn: String,
    val accountNumber: String? = null,
    val accountName: String? = null,
    val emailAddress: String? = null,
    val transactionType: String,
    val paymentType: String,
    val paymentName: String,
    val amount: Double,
    val discount: Double,
    val price: Double,
    val totalAmount: Double,
    val validity: Validity? = null,
    val nonChargePromoId: String? = null,
    val chargePromoId: String? = null,
    val chargePromoParam: String? = null,
    val nonChargePromoParam: String? = null,
    val isGroupDataPromo: Boolean,
    val shareablePromo: Boolean,
    val apiProvisioningKeyword: String? = null,
    val shareKeyword: String? = null,
    val shareFee: Double = 0.0,
    var isLoggedIn: Boolean = false,
    var purchaseType: PurchaseType = PurchaseType.BuyLoad(amount.toString()),
    var globePaymentMethod: GlobePaymentMethod? = null,
    val selectedBoosters: List<BoosterItem>? = null,
    val skelligWallet: String?,
    val skelligCategory: String?,
    var currentTransactionsResult: List<TransactionResult>? = null,
    var currentPaymentMethod: String = "",
    var refundSuccessful: Boolean? = null,
    var referenceId: String? = null,
    val provisionByServiceId: Boolean,
    val isEnrolledAccount: Boolean = false,
    val isVoucher: Boolean = false,
    val partnerName: String? = null,
    val partnerRedirectionLink: String? = null,
    val brandType: AccountBrandType? = null,
    val brand: AccountBrand? = null,
    val denomCategory: String? = null,
    val productDescription: String? = null,
    val displayColor: String? = null,
    val monitoredInApp: Boolean = false,
    val isExclusivePromo: Boolean = false,
    val availMode: Int = 0,
    val accountStatus: String? = null,
    val billingFullPayment: Boolean? = null,
    val isRetailer: Boolean = false,
    val isFreebieVoucher: Boolean = false,
    val freebieName: String = "",
    // only used for the load 4% discount off
    val currentAmount: Double? = null
) : Parcelable

fun PaymentParameters.canDisplayChargeToLoad(): Boolean =
    // If we are buying load for Consumer we should always have charge to load option available
    ((purchaseType is PurchaseType.BuyLoadConsumer) ||
            // If we are buying regular content promo
            (purchaseType is PurchaseType.BuyContentRegular) ||
            // If we are buying GoCreate promo and chargePromoId is not not or empty
            (purchaseType is PurchaseType.BuyGoCreatePromo && canPurchasePromoWithChargingTheLoad()) ||
            // If we are buying a promo with serviceId then the chargePromoId has to be present on all the items (promo && boosters)
            // Or if we are buying the promo with the keyword and the keyword has to be present
            // Or if the promo is shareable and there is a non empty shareKeyword we can display the Charge to Load option
            ((purchaseType is PurchaseType.BuyPromo) && (canPurchasePromoWithChargingTheLoad() || (shareablePromo && !shareKeyword.isNullOrBlank()))))

fun PaymentParameters.canDisplayGCash(): Boolean =
// If we are buying a voucher content promo
    // Or if we are are following the flows described below with C/D card
    ((purchaseType is PurchaseType.BuyContentVoucher) || canDisplayCreditDebitCard())

fun PaymentParameters.canDisplayCreditDebitCard(): Boolean =
    // If we are buying load we should always have C/D card & GCash options available
    ((purchaseType is PurchaseType.BuyLoad) ||
            // If we are paying bill
            (purchaseType is PurchaseType.PayBill) ||
            // If we are buying GoCreate promo and nonChargePromoId is not null or empty
            (purchaseType is PurchaseType.BuyGoCreatePromo && canPurchasePromoWithoutChargingTheLoad()) ||
            // Or if we are buying the promo and the nonChargePromoId is present we can display C/D card & GCash options
            ((purchaseType is PurchaseType.BuyPromo) && (canPurchasePromoWithoutChargingTheLoad())))

fun PaymentParameters.canOnlyChargeToOwnLoad(): Boolean =
    // We can charge only our own load if the boosters are selected
    selectedBoosters?.isNotEmpty() == true
            // or the promo is not shareable
            || purchaseType is PurchaseType.BuyNonShareablePromo
            // or we are buying the content
            || purchaseType is PurchaseType.BuyContent
            // or we are buying the GoCreate promo
            || purchaseType is PurchaseType.BuyGoCreatePromo

fun PaymentParameters.canPurchasePromoWithChargingTheLoad(): Boolean =
    (provisionByServiceId && (!chargePromoId.isNullOrEmpty() && selectedBoosters?.any { it.serviceId.isEmpty() } != true)) || (!provisionByServiceId && !apiProvisioningKeyword.isNullOrEmpty())

fun PaymentParameters.canPurchasePromoWithoutChargingTheLoad(): Boolean =
    ((!nonChargePromoId.isNullOrEmpty() && selectedBoosters?.any { it.nonChargeServiceId.isEmpty() } != true))
