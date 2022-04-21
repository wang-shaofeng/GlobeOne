/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.util

// Statuses of an account
const val ACCOUNT_STATUS_ACTIVE = "Active"
const val ACCOUNT_STATUS_INACTIVE = "Inactive"
const val ACCOUNT_STATUS_DISCONNECTED = "Disconnected"
const val ACCOUNT_STATUS_MIGRATED = "Migrated"

sealed class AccountStatus {
    object Active : AccountStatus()
    object Disconnected : AccountStatus()
    object Inactive : AccountStatus()
}

fun String.convertToAccountStatusString(): String? =
    when {
        contains(ACCOUNT_STATUS_INACTIVE, true) -> ACCOUNT_STATUS_INACTIVE
        contains(ACCOUNT_STATUS_ACTIVE, true) -> ACCOUNT_STATUS_ACTIVE
        contains(ACCOUNT_STATUS_DISCONNECTED, true) -> ACCOUNT_STATUS_DISCONNECTED
        else -> null
    }

fun String.convertToAccountStatus(): AccountStatus? =
    when (convertToAccountStatusString()) {
        ACCOUNT_STATUS_ACTIVE -> AccountStatus.Active
        ACCOUNT_STATUS_INACTIVE -> AccountStatus.Inactive
        ACCOUNT_STATUS_DISCONNECTED -> AccountStatus.Disconnected
        else -> null
    }
