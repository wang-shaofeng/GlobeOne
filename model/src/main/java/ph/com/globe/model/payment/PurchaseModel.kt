/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.payment

import com.squareup.moshi.JsonClass
import ph.com.globe.model.profile.response_models.CustomerDetails
import ph.com.globe.model.util.brand.AccountBrand
import java.io.Serializable

sealed class PurchaseResult {
    class ShareALoadPromoResult(val createServiceIdResult: CreateServiceIdResult) : PurchaseResult()
    class GeneralResult(val multiplePurchasePromoResult: MultiplePurchasePromoResult) :
        PurchaseResult()
}

data class PurchaseParams(
    val sourceNumber: String = "",
    val targetNumber: String,
    val purchaseType: PurchaseType,
    val chargeToLoad: Boolean = false,
    val otpReferenceId: String? = null
)

sealed class PurchaseType(val amount: String) : Serializable {
    abstract val simpleName: String

    open class BuyLoad(amount: String) : PurchaseType(amount) {
        override val simpleName: String = "load"
    }

    class BuyLoadConsumer(amount: String, amountAfterDiscount: String? = null) : BuyLoad(amount)
    class BuyLoadRetailer(amount: String) : BuyLoad(amount)

    open class BuyContent(amount: String) : PurchaseType(amount) {
        override val simpleName: String = "content"
    }

    class BuyContentRegular(
        amount: String,
        val provisionByServiceId: Boolean,
        val chargePromoServiceId: String,
        val chargeServiceParam: String,
        val productKeyword: String
    ) : BuyContent(amount)

    class BuyContentVoucher(
        val displayName: String,
        val partnerName: String,
        val denomCategory: String,
        val brand: AccountBrand?,
        val productDescription: String,
        amount: String,
        var customerDetails: CustomerDetails? = null
    ) : BuyContent(amount)

    open class BuyPromo(
        val displayName: String,
        val chargePromoServiceId: String,
        val nonChargePromoServiceId: String,
        val shareKeyword: String,
        val productKeyword: String,
        amount: Double,
        val chargeServiceParam: String,
        val nonChargeServiceParam: String,
        val boosters: List<BoosterInfo>? = null,
        val provisionByServiceId: Boolean,
        val isGoPlus: Boolean = displayName.contains("""Go\+""".toRegex(RegexOption.IGNORE_CASE))
    ) :
        PurchaseType(amount.toString()) {
        override val simpleName: String = "promo"
    }

    class BuyShareablePromo(
        displayName: String,
        chargePromoServiceId: String,
        nonChargePromoServiceId: String,
        productKeyword: String,
        shareKeyword: String,
        amount: Double,
        chargeServiceParam: String,
        nonChargeServiceParam: String,
        boosters: List<BoosterInfo>? = null,
        val shareFee: Double,
        provisionByServiceId: Boolean
    ) :
        BuyPromo(
            displayName,
            chargePromoServiceId,
            nonChargePromoServiceId,
            shareKeyword,
            productKeyword,
            amount,
            chargeServiceParam,
            nonChargeServiceParam,
            boosters,
            provisionByServiceId
        )

    class BuyNonShareablePromo(
        displayName: String,
        chargePromoServiceId: String,
        nonChargePromoServiceId: String,
        productKeyword: String,
        shareKeyword: String,
        amount: Double,
        chargeServiceParam: String,
        nonChargeServiceParam: String,
        boosters: List<BoosterInfo>? = null,
        provisionByServiceId: Boolean
    ) :
        BuyPromo(
            displayName,
            chargePromoServiceId,
            nonChargePromoServiceId,
            shareKeyword,
            productKeyword,
            amount,
            chargeServiceParam,
            nonChargeServiceParam,
            boosters,
            provisionByServiceId
        )

    class BuyGoCreatePromo(
        val displayName: String,
        val chargePromoServiceId: String,
        val nonChargePromoServiceId: String,
        val chargeServiceParam: String,
        val nonChargeServiceParam: String,
        amount: String
    ) : PurchaseType(amount) {
        override val simpleName: String = "gocreate"
    }

    class PayBill(amount: String) : PurchaseType(amount) {
        override val simpleName: String = "paybill"
    }
}

/**
 * Provision information of a single booster.
 */
data class BoosterInfo(
    val chargePromoServiceId: String,
    val nonChargePromoServiceId: String,
    val productKeyword: String,
    val provisionByServiceId: Boolean,
    val price: String,
    val chargePromoParam: String,
    val nonChargePromoParam: String
) : Serializable

fun PurchaseType.getShareFee(): Double =
    when (this) {
        is PurchaseType.BuyLoad -> HARDCODED_SHARELOAD_FEE
        is PurchaseType.BuyShareablePromo -> this.shareFee
        else -> 0.0
    }

@JsonClass(generateAdapter = true)
data class PurchasePromoRequest(
    val mobileNumber: String,
    val serviceID: String
)

@JsonClass(generateAdapter = true)
data class MultiplePurchasePromoRequest(
    val mobileNumber: String,
    val promos: List<Transaction>
)

@JsonClass(generateAdapter = true)
data class MultiplePurchasePromoResponse(
    val result: MultiplePurchasePromoResult,
)

@JsonClass(generateAdapter = true)
data class MultiplePurchasePromoResult(
    val promos: List<TransactionResult>,
) : Serializable

@JsonClass(generateAdapter = true)
data class TransactionResult(
    val transactionId: String,
    val serviceID: String?,
    val keyword: String?,
    val param: String?,
    val price: String?,
    val status: String,
    val voucherDetails: VoucherDetails? = null
) : Serializable

@JsonClass(generateAdapter = true)
data class PurchaseLoadConsumerRequest(
    val keyword: String = "LD",
    val amount: String
)

@JsonClass(generateAdapter = true)
data class CreateServiceOrderLoadRequest(
    val sourceNumber: String,
    val targetNumber: String,
    val amount: Double
)

@JsonClass(generateAdapter = true)
data class CreateServiceOrderPromoRequest(
    val sourceNumber: String,
    val targetNumber: String,
    val productKeyword: String,
    val amount: Double
)

@JsonClass(generateAdapter = true)
data class PurchaseLoadRetailerRequest(
    val wallet: String = "A",
    val amount: String
)

@JsonClass(generateAdapter = true)
data class PurchaseLoadResponse(
    val result: TransactionIdJson
)

@JsonClass(generateAdapter = true)
data class TransactionIdJson(
    val transactionId: Long
)

data class CreateServiceOrderParameters(
    val sourceNumber: String,
    val targetNumber: String,
    val purchaseType: PurchaseType
)

@JsonClass(generateAdapter = true)
data class CreateServiceIdResult(
    val referenceId: String
)

fun PurchaseParams.transformToTransactions(): List<Transaction> =
    when (this.purchaseType) {
        is PurchaseType.BuyPromo -> mutableListOf(
            if (purchaseType.provisionByServiceId) {
                Transaction(
                    // Here we are using 'charge' parameters as this is only used within the charge to load flow
                    // where we always want to charge to the load
                    serviceID = purchaseType.chargePromoServiceId,
                    price = purchaseType.chargeServiceParam
                )
            } else {
                Transaction(
                    keyword = purchaseType.productKeyword
                )
            }
        ).also { it.addAll(purchaseType.boosters.transformToTransaction()) }
        is PurchaseType.BuyGoCreatePromo -> {
            listOf(
                Transaction(
                    serviceID = purchaseType.chargePromoServiceId,
                    price = purchaseType.chargeServiceParam
                )
            )
        }
        is PurchaseType.BuyContentRegular -> {
            listOf(
                if (purchaseType.provisionByServiceId) {
                    Transaction(
                        // Here we are using 'charge' parameters as this is only used within the charge to load flow
                        // where we always want to charge to the load
                        serviceID = purchaseType.chargePromoServiceId,
                        price = purchaseType.chargeServiceParam
                    )
                } else {
                    Transaction(
                        keyword = purchaseType.productKeyword
                    )
                }
            )
        }
        else -> listOf()
    }

private fun List<BoosterInfo>?.transformToTransaction(): List<Transaction> =
    this?.let {
        it.map { booster ->
            if (booster.provisionByServiceId) {
                Transaction(
                    serviceID = booster.chargePromoServiceId,
                    param = booster.chargePromoParam.ifEmpty { null },
                    price = booster.price
                )
            } else {
                Transaction(
                    keyword = booster.productKeyword
                )
            }
        }
    } ?: listOf()

const val HARDCODED_SHARELOAD_FEE = 1.0
