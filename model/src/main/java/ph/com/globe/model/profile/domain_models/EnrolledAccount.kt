/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.profile.domain_models

import ph.com.globe.model.util.brand.*
import java.io.Serializable

data class EnrolledAccount(
    val primaryMsisdn: String,
    val mobileNumber: String? = null,
    val accountNumber: String? = null,
    val landlineNumber: String? = null,
    val accountAlias: String,
    val brandType: AccountBrandType,
    val segment: AccountSegment,
    val channel: List<String>,
    val isGcashLinked: Boolean
) : Serializable

fun EnrolledAccount.isPrepaid(): Boolean =
    brandType == AccountBrandType.Prepaid

fun EnrolledAccount.isPostpaid(): Boolean =
    brandType == AccountBrandType.Postpaid

fun EnrolledAccount.isBroadband(): Boolean =
    segment == AccountSegment.Broadband

fun EnrolledAccount.isMobile(): Boolean =
    segment == AccountSegment.Mobile

fun EnrolledAccount.isPostpaidBroadband(): Boolean =
    isPostpaid() && isBroadband()

fun EnrolledAccount.isPostpaidMobile(): Boolean =
    isPostpaid() && isMobile()
