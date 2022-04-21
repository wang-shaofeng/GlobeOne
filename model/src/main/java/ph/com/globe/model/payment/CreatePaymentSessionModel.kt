/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.payment

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class CreatePaymentSessionParams(
    val accountNumber: String? = null,
    val emailAddress: String? = null,
    val mobileNumber: String,
    val requestType: String,
    val transactionType: String,
    val paymentType: String,
    val price: String,
    val purchaseType: PurchaseType,
    val returnUrl: String = "", // Used only for CreateAdyenPaymentSessionNetworkCall and
    val adyenSdkToken: String? = null, // defaults to empty string and stays unused when GCash is processed
    val amountAfterDiscount: Double? = null // Used only for load 4% discount
)

sealed class CreatePaymentSessionResult {
    class CreatePaymentSessionSuccess(val token: String) : CreatePaymentSessionResult()
    class CreateAdyenDropInSessionSuccess(
        val createPaymentSessionResult: CreatePaymentSessionSuccess,
        val paymentMethods: String
    ) : CreatePaymentSessionResult()

    class CreateSessionCheckOutUrlSuccess(
        val createPaymentSessionResult: CreatePaymentSessionSuccess,
        val checkoutUrl: String
    ) : CreatePaymentSessionResult()
}

@JsonClass(generateAdapter = true)
data class Transaction(
    val keyword: String? = null,
    val serviceId: String? = null,
    val serviceID: String? = null,
    val param: String? = null,
    val amount: Double? = null,
    val price: String? = null,
    val provisionStatus: String? = null,
    val transactionId: String? = null,
    val wallet: String? = null,
    val voucherCategory: String? = null,
    val voucherDetails: VoucherDetails? = null
) : Serializable

@JsonClass(generateAdapter = true)
data class SingleDetail(
    val key: String?,
    val type: String?,
    val optional: Boolean?
)

@JsonClass(generateAdapter = true)
data class VoucherDetails(
    val serialNumber: String,
    val voucherCode: String? = null,
    val voucherDescription: String,
    val contentPartner: String,
    val validFrom: String,
    val validTo: String,
    val paidAmount: String
) : Serializable

@JsonClass(generateAdapter = true)
data class Refund(
    val amount: Long,
    val status: String
)

fun Refund?.isSuccessful(): Boolean =
    this?.status?.contains("REFUND_SUCCESS") == true

fun CreatePaymentSessionParams.transformToTransaction(): List<Transaction> =
    when (this.purchaseType) {
        is PurchaseType.BuyLoadConsumer -> listOf(
            Transaction(
                amount = this.price.toDouble(),
                keyword = "LD"
            )
        )
        is PurchaseType.BuyLoadRetailer -> listOf(
            Transaction(
                amount = this.price.toDouble(),
                wallet = "A"
            )
        )
        is PurchaseType.BuyGoCreatePromo -> mutableListOf(
            Transaction(
                serviceId = purchaseType.nonChargePromoServiceId,
                param = if (purchaseType.nonChargeServiceParam.isEmpty()) null else purchaseType.nonChargeServiceParam,
                amount = purchaseType.amount.toDouble()
            )
        )
        is PurchaseType.BuyPromo -> mutableListOf(
            Transaction(
                // Here we are using 'nonCharge' parameters as this is only used within the third-party payment systems
                // where we charge a credit card or a wallet but not the user's load directly
                serviceId = purchaseType.nonChargePromoServiceId,
                param = if (purchaseType.nonChargeServiceParam.isEmpty()) null else purchaseType.nonChargeServiceParam,
                amount = purchaseType.amount.toDouble()
            )
        ).also { it.addAll(purchaseType.boosters.transformToTransaction()) }
        is PurchaseType.BuyContentVoucher -> listOf(
            Transaction(
                amount = this.price.toDouble(),
                voucherCategory = purchaseType.denomCategory
            )
        )
        else -> listOf()
    }

private fun List<BoosterInfo>?.transformToTransaction(): List<Transaction> =
    this?.let {
        it.map { booster ->
            Transaction(
                serviceId = booster.nonChargePromoServiceId,
                param = if (booster.nonChargePromoParam.isEmpty()) null else booster.nonChargePromoParam,
                amount = booster.price.toDouble()
            )
        }
    } ?: listOf()

fun GetPaymentSessionAccount.checkIsCompleted(): Boolean {
    // provisioning on their end is not completed until any one of transactions has 'PROCESSING' status
    return status != "PROCESSING" && !transactions.toTransactionResultList().any { it.status == "PROCESSING" }
}

fun List<TransactionResult>?.checkIfAllFailed(): Boolean {
    // if none of the transactions have 'SUCCESS' status we can assume that all have failed
    return this?.any { it.status == "SUCCESS" } != true
}

fun List<TransactionResult>?.getNumOfSuccessfullyProvisionedBoosters(): Int {
    return (this?.count { it.status == "SUCCESS" }
        ?: 0) - 1 // '1' is the the first transaction and it is a promo
}

fun List<TransactionResult>?.checkIfSomeBoostersHaveFailed(): Boolean {
    return this.getNumOfSuccessfullyProvisionedBoosters() != (this?.size
        ?: 0) - 1 // '1' is the the first transaction and it is a promo
}

fun List<TransactionResult>?.checkIfThisBoosterIsPurchased(
    serviceId: String,
    nonChargeServiceId: String,
    productKeyword: String
): Boolean {
    // provisioning on their end is not completed until any one of transactions has 'PROCESSING' status
    return this?.find {
        it.status == "SUCCESS" && (it.serviceID == serviceId || it.serviceID == nonChargeServiceId || it.keyword == productKeyword)
    } != null
}

fun List<Transaction>?.toTransactionResultList(): List<TransactionResult> {
    return this?.map {
        TransactionResult(
            it.transactionId ?: "",
            it.serviceId,
            it.keyword,
            it.param,
            it.amount.toString(),
            it.provisionStatus ?: "",
            it.voucherDetails
        )
    }
        ?: listOf()
}

const val CANCEL_URL = "gcash?status=fail"
const val SUCCESS_URL = "gcash?status=success"
