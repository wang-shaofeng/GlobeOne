/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.analytics.events.custom

import ph.com.globe.analytics.events.*


abstract class PaymentAnalytics(
    private val page: String,
    private val type: String,
    private val status: String,
    private val promo_id: String?,
    private val promo_name: String?,
    private val load_wallet: String?,
    private val target: String,
    private val msisdn: String,
    private val amount: String,
) : AnalyticsEvent {

    override fun prepareParamsBundle(): Map<String, String> =
        mapOf(
            PAGE_KEY to page,
            TYPE to type,
            ACTION_KEY to ACTION_CLICK,
            "MSISDN" to msisdn,
            "status" to status,
            TARGET_KEY to target,
            "load_wallet" to (load_wallet ?: ""),
            "promo_name" to (promo_name ?: ""),
            "promo_id" to (promo_id ?: ""),
            "amount" to amount,
        )
}

class PaymentStart(
    page: String,
    type: String,
    status: String,
    promo_id: String?,
    promo_name: String?,
    load_wallet: String?,
    target: String,
    msisdn: String,
    amount: String,
) : PaymentAnalytics(
    page,
    type,
    status,
    promo_id,
    promo_name,
    load_wallet,
    target,
    msisdn,
    amount
) {
    override val eventName = "payment_start"
}

class PaymentSuccess(
    page: String,
    type: String,
    status: String,
    promo_id: String?,
    promo_name: String?,
    load_wallet: String?,
    target: String,
    msisdn: String,
    amount: String,
    private val discount: String?,
    private val orderNumber: String?,
    private val paymentMethod: String
) : PaymentAnalytics(
    page,
    type,
    status,
    promo_id,
    promo_name,
    load_wallet,
    target,
    msisdn,
    amount
) {

    override val eventName = "payment_success"

    override fun prepareParamsBundle(): Map<String, String> =
        super.prepareParamsBundle().plus(
            mapOf(
                "discount" to (discount ?: ""),
                "order" to (orderNumber ?: ""),
                "payment_method" to paymentMethod
            )
        )
}

class PaymentFailed(
    page: String,
    type: String,
    status: String,
    promo_id: String?,
    promo_name: String?,
    load_wallet: String?,
    target: String,
    msisdn: String,
    amount: String,
    private val paymentMethod: String
) : PaymentAnalytics(
    page,
    type,
    status,
    promo_id,
    promo_name,
    load_wallet,
    target,
    msisdn,
    amount
) {

    override val eventName = "payment_failed"

    override fun prepareParamsBundle(): Map<String, String> =
        super.prepareParamsBundle().plus(
            mapOf(
                "payment_method" to paymentMethod
            )
        )
}
