/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.payment

import android.content.res.Resources
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.payment.PaymentParameters
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.BoosterItem
import ph.com.globe.globeonesuperapp.utils.convertToClassicNumberFormat
import ph.com.globe.model.shop.domain_models.Validity
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.util.isNoExpiry

fun checkoutToPaymentParams(
    paymentType: String,
    transactionType: String = NON_BILL,
    mobileNumber: String,
    accountNumber: String? = null,
    accountName: String? = null,
    emailAddress: String? = null,
    amount: Double,
    promoNonChargeId: String? = null,
    promoChargeId: String? = null,
    chargePromoParam: String? = null,
    nonChargePromoParam: String? = null,
    apiProvisioningKeyword: String? = null,
    shareKeyword: String? = null,
    shareFee: Double = 0.0,
    paymentName: String,
    validity: Validity? = null,
    price: Double,
    discount: Double? = null,
    isGroupDataPromo: Boolean = false,
    shareable: Boolean = false,
    selectedBoosters: List<BoosterItem>? = null,
    skelligWallet: String? = null,
    skelligCategory: String? = null,
    provisionByServiceId: Boolean,
    isEnrolledAccount: Boolean = false,
    isVoucher: Boolean = false,
    partnerName: String? = null,
    partnerRedirectionLink: String? = null,
    brandType: AccountBrandType? = null,
    brand: AccountBrand? = null,
    denomCategory: String? = null,
    productDescription: String? = null,
    displayColor: String? = null,
    monitoredInApp: Boolean = false,
    isExclusivePromo: Boolean = false,
    availMode: Int = 0,
    accountStatus: String? = null,
    billingFullPayment: Boolean? = null,
    isRetailer: Boolean = false,
    isFreebieVoucher: Boolean = false,
    freebieName: String = "",
    // only used for the load 4% discount off
    currentAmount: Double? = null
): PaymentParameters {
    return PaymentParameters(
        paymentType = paymentType,
        transactionType = transactionType,
        // if there is an account number present it is always of a higher priority than mobile number
        primaryMsisdn = accountNumber ?: mobileNumber.convertToClassicNumberFormat(),
        accountNumber = accountNumber,
        accountName = accountName,
        emailAddress = emailAddress,
        amount = amount,
        nonChargePromoId = promoNonChargeId,
        chargePromoId = promoChargeId,
        chargePromoParam = chargePromoParam,
        nonChargePromoParam = nonChargePromoParam,
        apiProvisioningKeyword = apiProvisioningKeyword,
        isGroupDataPromo = isGroupDataPromo,
        shareKeyword = shareKeyword,
        shareFee = shareFee,
        paymentName = paymentName,
        validity = validity,
        price = price,
        totalAmount = price - (discount
            ?: 0.0) + (selectedBoosters?.sumOf { it.boosterPrice.toDouble() }
            ?: 0.0),
        discount = discount ?: 0.0,
        shareablePromo = shareable,
        // we are copying the list here because it is being cleared on the shop
        selectedBoosters = selectedBoosters?.map { it.copy() },
        skelligWallet = skelligWallet,
        skelligCategory = skelligCategory,
        provisionByServiceId = provisionByServiceId,
        isEnrolledAccount = isEnrolledAccount,
        isVoucher = isVoucher,
        partnerName = partnerName,
        partnerRedirectionLink = partnerRedirectionLink,
        brandType = brandType,
        brand = brand,
        denomCategory = denomCategory,
        productDescription = productDescription,
        displayColor = displayColor,
        monitoredInApp = monitoredInApp,
        isExclusivePromo = isExclusivePromo,
        availMode = availMode,
        accountStatus = accountStatus,
        billingFullPayment = billingFullPayment,
        isRetailer = isRetailer,
        isFreebieVoucher = isFreebieVoucher,
        freebieName = freebieName,
        currentAmount = currentAmount
    )
}

fun String.pesosToDouble() =
    substringAfter(PRICE_PREFIX)
        // in cases of existing comma (,) on values higher than a thousand
        .replace(",", "")
        .toDoubleOrNull()
        ?: 0.0

fun String.pesosToInt() =
    substringAfter(PRICE_PREFIX)
        // in cases of existing comma (,) on values higher than a thousand
        .replace(",", "")
        .toIntOrNull()
        ?: 0

fun Int?.intToPesos(): String = "$PRICE_PREFIX$this"

fun Double?.doubleToPesos(): String = "$PRICE_PREFIX$this"

fun Float?.floatToPesos(): String = "$PRICE_PREFIX$this"

fun Double?.toPesosWithDecimal(): String = "$PRICE_PREFIX${String.format("%.02f", this)}"

fun String.stringToPesos(): String = "$PRICE_PREFIX$this"

fun Resources.setValidityText(value: Validity?): String = when {
    value == null -> ""
    value.days == 1 -> this.getString(R.string.valid_for_a_day, value.days.toString())
    value.days > 1 -> this.getString(R.string.valid_for_days, value.days.toString())
    value.days == 0 && value.hours == 1 -> this.getString(
        R.string.valid_for_an_hour,
        value.hours.toString()
    )
    value.days == 0 && value.hours > 1 -> this.getString(
        R.string.valid_for_hours,
        value.hours.toString()
    )
    else -> ""
}

fun Resources.setValidityTextEx(value: Validity?): String = when {
    value == null || value.isNoExpiry() -> this.getString(R.string.valid_for_no_expiry)
    else -> getString(
        R.string.valid_for,
        setValidityText(value)
    )
}

// Types of payments
const val BUY_LOAD = "BuyLoad"
const val BUY_PROMO = "BuyPromo"
const val BUY_CONTENT = "BuyContent"
const val BUY_VOUCHER = "BuyVoucher"
const val PAY_BILLS = "PayBills"

const val GO_CREATE = "GoCREATE"

// Types of transactions
const val NON_BILL = "N"
const val BILL = "G"

const val PRICE_PREFIX = "P"
