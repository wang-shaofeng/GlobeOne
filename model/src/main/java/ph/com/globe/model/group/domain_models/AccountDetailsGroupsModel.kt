/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.group.domain_models

import ph.com.globe.model.shop.domain_models.AppItem
import java.io.Serializable

data class AccountDetailsGroupsParams(
    val primaryMsisdn: String,
    val accountAlias: String
)

data class AccountDetailsGroups(
    val groups: List<UsageItem>?
)

data class UsageItem(
    val title: String,
    val category: String = "",
    val left: Int = 0,
    val total: Int = 0,
    val expiration: String = "",
    val accountNumber: String = "",
    val accountName: String = "",
    val accountRole: String = "",
    val skelligWallet: String = "",
    val skelligCategory: String = "",
    val groupOwnerMobileNumber: String = "",
    val isUnlimited: Boolean = false,
    val addOnData: Boolean = false,
    val addOnDataType: String = "",
    val includedPromos: List<UsagePromo> = emptyList(),
    val apps: List<AppItem>? = null,
    val used: Int = -1
) : Serializable

data class UsagePromo(
    val promoName: String,
    val dataRemaining: Int,
    val dataTotal: Int,
    val endDate: String
) : Serializable

const val BUCKET_NAME_ALL_ACCESS_DATA = "All Access Data"
const val BUCKET_NAME_MAIN_ALL_ACCESS_DATA = "Main all-access data"
const val BUCKET_NAME_ONE_TIME_ALL_ACCESS = "One-time all-access"
const val BUCKET_NAME_RECURRING_ALL_ACCESS = "Recurring all-access"
const val BUCKET_NAME_OTHERS = "Others"

const val DATA_TYPE_ONE_TIME_ACCESS = "one_time_access_data"
const val DATA_TYPE_RECURRING_ACCESS = "recurring_access_data"

const val PR_GOSURF_DVB = "PR_GOSURF_DVB"
