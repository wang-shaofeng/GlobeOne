/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.model.auth.LoginStatus
import javax.inject.Inject

class RemoveUserDataIfRefreshUserTokenExpiredUseCase @Inject constructor(private val authDataManager: AuthDataManager) {
    fun execute() {
        if (authDataManager.getLoginStatus() == LoginStatus.VERIFIED) {
            val timeInMs = authDataManager.getTimeWhenUserTokenWasFetched()

            val currentTime = System.currentTimeMillis()

            if (timeInMs + TIME_30_DAYS_MINUS_30_MIN_BUFFER <= currentTime) {
                authDataManager.removeUserData()
            }
        }
    }
}

private const val TIME_30_DAYS_MINUS_30_MIN_BUFFER: Long =
    1000L * 60L * 60L * 24L * 30L - 1000L * 60L * 30L
