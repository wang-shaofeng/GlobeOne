/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.shared_preferences.token

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.NetworkError.NoAccessToken
import ph.com.globe.errors.NetworkError.UserNotLoggedInError
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.util.LfResult

interface TokenRepository {

    fun getDeviceId(): String

    fun getAccessToken(): LfResult<String, NoAccessToken>

    fun setAccessToken(accessToken: String)

    fun removeAccessToken()

    fun getUserToken(): LfResult<String, UserNotLoggedInError>

    fun setUserToken(userToken: String)

    fun removeUserToken()

    fun getOcsToken(): LfResult<String, NetworkError.NoOcsToken>

    fun setOcsToken(ocsToken: String)

    fun removeOcsToken()

    fun setLoginStatus(loginStatus: LoginStatus)

    fun getLoginStatus(): LoginStatus

    fun logoutEvent(): Flow<Boolean>

    fun removeLoginStatus()

    fun sendLogoutEvent(isUserTokenExpired: Boolean)

    fun getTimeWhenUserTokenWasFetched(): Long

    fun setTimeWhenUserTokenWasFetched(time: Long)

    fun clearTimeWhenUserTokenWasFetched()

    fun setSymmetricKey(key: String)

    fun getSymmetricKey(): String?
}
