/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.shared_preferences.token

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.NetworkError.NoAccessToken
import ph.com.globe.errors.NetworkError.UserNotLoggedInError
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.util.LfResult
import java.util.*

object InMemoryTokenRepository : TokenRepository {

    private var deviceId: String? = null
    private var accessTokenStorage: String? = null
    private var userTokenStorage: String? = null
    private var ocsTokenStorage: String? = null
    private var loginStatus: LoginStatus? = null
    private var timeWhenUserTokenWasFetched: Long = 0L
    private var symmetricKey: String? = null

    override fun getDeviceId(): String {
        return if (deviceId != null) {
            deviceId!!
        } else {
            UUID.randomUUID().toString().also { deviceId = it }
        }
    }

    override fun getAccessToken(): LfResult<String, NoAccessToken> {
        return if (accessTokenStorage != null) {
            LfResult.success(accessTokenStorage!!)
        } else {
            LfResult.failure(NoAccessToken)
        }
    }

    override fun setAccessToken(accessToken: String) {
        accessTokenStorage = accessToken
    }

    override fun removeAccessToken() {
        accessTokenStorage = null
    }

    override fun getUserToken(): LfResult<String, UserNotLoggedInError> {
        return if (userTokenStorage != null) {
            LfResult.success(userTokenStorage!!)
        } else {
            LfResult.failure(UserNotLoggedInError)
        }
    }

    override fun setUserToken(userToken: String) {
        userTokenStorage = userToken
    }

    override fun removeUserToken() {
        userTokenStorage = null
    }

    override fun getOcsToken(): LfResult<String, NetworkError.NoOcsToken> =
        if (ocsTokenStorage != null) {
            LfResult.success(ocsTokenStorage!!)
        } else {
            LfResult.failure(NetworkError.NoOcsToken)
        }

    override fun setOcsToken(ocsToken: String) {
        ocsTokenStorage = ocsToken
    }

    override fun removeOcsToken() {
        ocsTokenStorage = null
    }

    override fun setLoginStatus(loginStatus: LoginStatus) {
        this.loginStatus = loginStatus
    }

    override fun getLoginStatus(): LoginStatus = loginStatus ?: LoginStatus.NOT_LOGGED_IN


    override fun logoutEvent(): Flow<Boolean> = flow { }

    override fun removeLoginStatus() {
        loginStatus = null
    }

    override fun sendLogoutEvent(isUserTokenExpired: Boolean) {
        removeLoginStatus()

    }

    override fun getTimeWhenUserTokenWasFetched(): Long = timeWhenUserTokenWasFetched

    override fun setTimeWhenUserTokenWasFetched(time: Long) {
        timeWhenUserTokenWasFetched = time
    }

    override fun clearTimeWhenUserTokenWasFetched() {
        timeWhenUserTokenWasFetched = 0
    }

    override fun setSymmetricKey(key: String) {
        symmetricKey = key
    }

    override fun getSymmetricKey(): String? = symmetricKey
}
