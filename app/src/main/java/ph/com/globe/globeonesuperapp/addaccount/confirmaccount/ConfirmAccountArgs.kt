/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.confirmaccount

import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import java.io.Serializable

data class ConfirmAccountArgs(
    val mobileNumber: String? = null,
    val brand: AccountBrand,
    val brandType: AccountBrandType,
    val segment: AccountSegment,
    val referenceId: String,
    val isPremiumAccount: Boolean = false,
    val accountStatus: String? = null,
    val accountNumber: String? = null,
    val landlineNumber: String? = null,
    val accountName: String? = null,
    val verificationType: String? = null
) : Serializable

fun ConfirmAccountArgs.pickMsisdnForEnrolment(): String =
    when {
        mobileNumber != null -> mobileNumber
        accountNumber != null -> accountNumber
        landlineNumber != null -> landlineNumber
        else -> ""
    }
