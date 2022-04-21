/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.shared_preferences.token

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import ph.com.globe.data.shared_preferences.token.di.SHARED_PREFS_TOKEN_KEY
import ph.com.globe.encryption.CryptoOperationFailed
import ph.com.globe.encryption.StringCrypto
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.NetworkError.NoAccessToken
import ph.com.globe.errors.NetworkError.UserNotLoggedInError
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.model.auth.toEmailVerificationStatus
import ph.com.globe.model.auth.toInteger
import ph.com.globe.util.LfResult
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class DefaultTokenRepository @Inject constructor(
    @Named(SHARED_PREFS_TOKEN_KEY) private val sharedPreferences: SharedPreferences,
    private val stringCrypto: StringCrypto
) : TokenRepository {

    private val logoutEvent = Channel<Boolean>(Channel.BUFFERED)

    override fun getDeviceId(): String {
        return sharedPreferences.getString(DEVICE_ID_KEY, null) ?: UUID.randomUUID().toString()
            .also {
                sharedPreferences.edit().putString(DEVICE_ID_KEY, it).apply()
            }
    }

    override fun getAccessToken(): LfResult<String, NoAccessToken> {

        val encryptedAccessToken = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
            ?: return LfResult.failure(NoAccessToken)

        return try {
            val accessToken = stringCrypto.decrypt(encryptedAccessToken)
            accessToken.let { LfResult.success(it) }
        } catch (e: CryptoOperationFailed) {
            removeAccessToken()
            LfResult.failure(NoAccessToken)
        }
    }

    override fun setAccessToken(accessToken: String) {
        try {
            val encryptedAccessToken = stringCrypto.encrypt(accessToken)
            sharedPreferences.edit()
                .putString(ACCESS_TOKEN_KEY, encryptedAccessToken)
                .apply()

        } catch (e: CryptoOperationFailed) {
            removeAccessToken()
        }
    }

    override fun removeAccessToken() {
        sharedPreferences.edit()
            .remove(ACCESS_TOKEN_KEY)
            .apply()
    }

    override fun getUserToken(): LfResult<String, UserNotLoggedInError> {
        val encryptedUserToken = sharedPreferences.getString(USER_TOKEN_KEY, null)
            ?: return LfResult.failure(UserNotLoggedInError)

        return try {
            val userToken = stringCrypto.decrypt(encryptedUserToken)
            LfResult.success(userToken)
        } catch (e: CryptoOperationFailed) {
            removeUserToken()
            LfResult.failure(UserNotLoggedInError)
        }
    }

    override fun setUserToken(userToken: String) {
        try {
            val encryptedUserToken = stringCrypto.encrypt(userToken)
            sharedPreferences.edit()
                .putString(USER_TOKEN_KEY, encryptedUserToken)
                .apply()
        } catch (e: CryptoOperationFailed) {
            removeUserToken()
        }
    }

    override fun removeUserToken() {
        sharedPreferences.edit()
            .remove(USER_TOKEN_KEY)
            .apply()
    }

    override fun getOcsToken(): LfResult<String, NetworkError.NoOcsToken> {
        val encryptedUserToken = sharedPreferences.getString(OCS_TOKEN_KEY, null)
            ?: return LfResult.failure(NetworkError.NoOcsToken)

        return try {
            val userToken = stringCrypto.decrypt(encryptedUserToken)
            LfResult.success(userToken)
        } catch (e: CryptoOperationFailed) {
            removeUserToken()
            LfResult.failure(NetworkError.NoOcsToken)
        }
    }

    override fun setOcsToken(ocsToken: String) {
        try {
            val encryptedUserToken = stringCrypto.encrypt(ocsToken)
            sharedPreferences.edit()
                .putString(OCS_TOKEN_KEY, encryptedUserToken)
                .apply()
        } catch (e: CryptoOperationFailed) {
            removeUserToken()
        }
    }

    override fun removeOcsToken() {
        sharedPreferences.edit()
            .remove(OCS_TOKEN_KEY)
            .apply()
    }

    override fun setLoginStatus(loginStatus: LoginStatus) {
        sharedPreferences.edit()
            .putInt(LOGIN_STATUS, loginStatus.toInteger())
            .apply()
    }

    override fun getLoginStatus(): LoginStatus =
        sharedPreferences.getInt(LOGIN_STATUS, LoginStatus.NOT_LOGGED_IN.toInteger())
            .toEmailVerificationStatus()

    override fun logoutEvent(): Flow<Boolean> = logoutEvent.receiveAsFlow()

    override fun removeLoginStatus() {
        sharedPreferences.edit()
            .remove(LOGIN_STATUS)
            .apply()
    }

    override fun sendLogoutEvent(isUserTokenExpired: Boolean) {
        logoutEvent.offer(isUserTokenExpired)
    }

    override fun getTimeWhenUserTokenWasFetched(): Long =
        sharedPreferences.getLong(TIME_WHEN_USER_TOKEN_WAS_FETCHED, 0)

    override fun clearTimeWhenUserTokenWasFetched() {
        sharedPreferences.edit {
            remove(TIME_WHEN_USER_TOKEN_WAS_FETCHED)
        }
    }

    override fun setTimeWhenUserTokenWasFetched(time: Long) {
        sharedPreferences.edit {
            putLong(TIME_WHEN_USER_TOKEN_WAS_FETCHED, time)
        }
    }

    override fun setSymmetricKey(key: String) {
        sharedPreferences.edit {
            putString(SYMMETRIC_KEY, key)
        }
    }

    override fun getSymmetricKey(): String? =
        sharedPreferences.getString(SYMMETRIC_KEY, null)
}

internal const val DEVICE_ID_KEY = "device_id"
internal const val ACCESS_TOKEN_KEY = "access_token"
internal const val USER_TOKEN_KEY = "user_token"
internal const val OCS_TOKEN_KEY = "ocs_token"
internal const val LOGIN_STATUS = "login_status"
internal const val TIME_WHEN_USER_TOKEN_WAS_FETCHED = "time_when_user_token_was_fetched"
internal const val SYMMETRIC_KEY = "symmetric_key"
