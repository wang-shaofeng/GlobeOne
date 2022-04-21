/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rush

import ph.com.globe.model.rush.GetRushAccessTokenResponseModel
import javax.inject.Singleton

@Singleton
class RushTokenManager {

    private lateinit var storedRushAccessToken: GetRushAccessTokenResponseModel

    fun getStoredRushAccessToken(): GetRushAccessTokenResponseModel? {
        if (!this::storedRushAccessToken.isInitialized) return null
        val currentMillis = System.currentTimeMillis()
        val tokenLifecycleThreshold = storedRushAccessToken.expires_in / 3 * 2 * 1000
        val tokenRefreshThreshold =
            storedRushAccessToken.created_at?.plus(tokenLifecycleThreshold)
                ?: return null
        return if (currentMillis < tokenRefreshThreshold) storedRushAccessToken else null
    }

    fun storeRushAccessToken(tokenToStore: GetRushAccessTokenResponseModel) {
        storedRushAccessToken = tokenToStore
    }
}
