/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.payment

import com.squareup.moshi.JsonClass
import ph.com.globe.model.payment.ThirdPartyPaymentResult.AdyenResult
import ph.com.globe.model.payment.ThirdPartyPaymentResult.AdyenResult.*
import ph.com.globe.model.payment.ThirdPartyPaymentResult.GeneralResult.GeneralResultAuthorised
import ph.com.globe.model.payment.ThirdPartyPaymentResult.GeneralResult.GeneralResultRefused
import ph.com.globe.model.util.JsonObjectAsString

data class GetPaymentSessionParams(
    val tokenPaymentId: String
)

@JsonClass(generateAdapter = true)
data class GetPaymentSessionResult(
    val tokenPaymentId: String,
    val paymentSession: String?,
    val checkoutUrl: String?,
    val accounts: List<GetPaymentSessionAccount>,
    @JsonObjectAsString val paymentMethods: String?,
    val merchantAccount: String?,
    val paymentResult: PaymentResult?
)

@JsonClass(generateAdapter = true)
data class GetPaymentSessionAccount(
    val status: String,
    val statusRemarks: String?,
    val transactions: List<Transaction>?,
    val refund: Refund?
)

@JsonClass(generateAdapter = true)
data class PaymentResult(
    val resultCode: String?,
    @JsonObjectAsString val action: String?,
    val details: List<SingleDetail>?,
    val paymentData: String?,
)

sealed class ThirdPartyPaymentResult {
    abstract val result: String

    abstract fun isFinal(): Boolean

    sealed class AdyenResult : ThirdPartyPaymentResult() {
        override fun isFinal() = this in failedTransactionTypes || this in successTransactionTypes

        object AdyenResultAuthorised : AdyenResult() {
            override val result: String
                get() = "ADYEN_AUTHORISED"
        }

        object AdyenResultRefused : AdyenResult() {
            override val result: String
                get() = "ADYEN_REFUSED"
        }

        object AdyenResultReceived : AdyenResult() {
            override val result: String
                get() = "ADYEN_RECEIVED"
        }

        object AdyenResultCancelled : AdyenResult() {
            override val result: String
                get() = "ADYEN_CANCELLED"
        }

        object AdyenResultError : AdyenResult() {
            override val result: String
                get() = "ADYEN_ERROR"
        }

        object AdyenResultNoConnection : AdyenResult() {
            override val result: String
                get() = "ADYEN_NO_CONNECTION"
        }

        companion object {
            val failedTransactionTypes
                get() = listOf(
                    AdyenResultError,
                    AdyenResultCancelled,
                    AdyenResultRefused,
                    AdyenResultNoConnection
                )
            val successTransactionTypes
                get() = listOf(
                    AdyenResultAuthorised
                )
        }
    }

    sealed class GCashResult : ThirdPartyPaymentResult() {
        override fun isFinal() = this in failedTransactionTypes || this in successTransactionTypes

        object GCashResultAuthorised : GCashResult() {
            override val result: String
                get() = "GCASH_AUTHORISED"
        }

        object GCashResultRefused : GCashResult() {
            override val result: String
                get() = "GCASH_REFUSED"
        }

        companion object {
            val failedTransactionTypes
                get() = listOf(
                    GCashResultRefused
                )
            val successTransactionTypes
                get() = listOf(
                    GCashResultAuthorised
                )
        }
    }

    sealed class GeneralResult : ThirdPartyPaymentResult() {
        override fun isFinal() = this in failedTransactionTypes || this in successTransactionTypes

        object GeneralResultAuthorised : GeneralResult() {
            override val result: String
                get() = "Authorised"
        }

        object GeneralResultRefused : GeneralResult() {
            override val result: String
                get() = "Refused"
        }

        companion object {
            val failedTransactionTypes
                get() = listOf(
                    GeneralResultRefused
                )
            val successTransactionTypes
                get() = listOf(
                    GeneralResultAuthorised
                )
        }
    }

    operator fun compareTo(other: ThirdPartyPaymentResult): Int =
        if (this.result == other.result) 0
        else -1

    companion object {
        val failedTransactionTypes: List<ThirdPartyPaymentResult>
            get() = listOf(
                AdyenResult.failedTransactionTypes,
                GCashResult.failedTransactionTypes,
                GeneralResult.failedTransactionTypes
            ).flatten()
        val successTransactionTypes: List<ThirdPartyPaymentResult>
            get() = listOf(
                AdyenResult.successTransactionTypes,
                GCashResult.successTransactionTypes,
                GeneralResult.successTransactionTypes
            ).flatten()
    }
}


fun String?.toAdyenResult(): AdyenResult? =
    when (this) {
        AdyenResultAuthorised.result, GeneralResultAuthorised.result -> AdyenResultAuthorised
        AdyenResultRefused.result, GeneralResultRefused.result -> AdyenResultRefused
        AdyenResultCancelled.result -> AdyenResultCancelled
        AdyenResultError.result -> AdyenResultError
        AdyenResultReceived.result -> AdyenResultReceived
        AdyenResultNoConnection.result -> AdyenResultNoConnection
        else -> null
    }

fun GetPaymentSessionResult.isAdyenTransactionCompleted(): Boolean =
    containsActionObject() || transactionSuccessful() || transactionFailed()

fun GetPaymentSessionResult.transactionSuccessful(): Boolean =
    accounts[0].status in ThirdPartyPaymentResult.successTransactionTypes.map { it.result }

fun GetPaymentSessionResult.transactionFailed(): Boolean =
    accounts[0].status in ThirdPartyPaymentResult.failedTransactionTypes.map { it.result }

fun GetPaymentSessionResult.containsActionObject(): Boolean =
    // The function indicates weather there is an action object in the response as it should be used by the DropIn sdk
    paymentResult?.action != null
