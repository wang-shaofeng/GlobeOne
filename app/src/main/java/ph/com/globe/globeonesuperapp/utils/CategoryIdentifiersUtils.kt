/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

const val OTP_KEY_MULTIPLE_PURCHASE_PROMO = "MultiplePurchasePromo"
const val OTP_KEY_PROVISION_CONTENT_PROMO = "ProvisionContentPromo"
const val OTP_KEY_GET_CUSTOMER_DETAILS = "GetCustomerDetails"
const val OTP_KEY_G_CASH_LINK = "LinkGcashAccount"
const val OTP_KEY_GET_CREDIT_INFO = "GetCreditInfo"
const val OTP_KEY_LOAN_PROMO = "LoanPromo"
const val OTP_KEY_ENROLL_ACCOUNT = "EnrollAccount"
const val OTP_KEY_GET_PLAN_DETAILS = "GetPlanDetails"
const val OTP_KEY_GET_ACCOUNT_DETAILS = "GetAccountDetails"

val OTP_KEY_SET_ENROLL_ACCOUNT = listOf(OTP_KEY_ENROLL_ACCOUNT, OTP_KEY_GET_PLAN_DETAILS, OTP_KEY_GET_ACCOUNT_DETAILS)
val OTP_KEY_SET_BORROW = listOf(OTP_KEY_GET_CREDIT_INFO, OTP_KEY_LOAN_PROMO)
