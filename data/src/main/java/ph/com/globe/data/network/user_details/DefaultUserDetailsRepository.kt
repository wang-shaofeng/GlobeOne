/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.user_details

import android.content.SharedPreferences
import androidx.core.content.edit
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.data.shared_preferences.token.di.SHARED_PREFS_TOKEN_KEY
import ph.com.globe.encryption.StringAesCrypto
import ph.com.globe.errors.NetworkError
import ph.com.globe.util.LfResult
import javax.inject.Inject
import javax.inject.Named

class DefaultUserDetailsRepository @Inject constructor(
    @Named(SHARED_PREFS_TOKEN_KEY) private val sharedPreferences: SharedPreferences,
    private val stringAesCrypto: StringAesCrypto,
    private val tokenRepository: TokenRepository,
) : UserDetailsRepository {

    override fun getUserEmail(): LfResult<String, NetworkError.UserNotLoggedInError> {
        val email = sharedPreferences.getString(USER_EMAIL_KEY, null)

        return email?.let { LfResult.success(it) }
            ?: LfResult.failure(NetworkError.UserNotLoggedInError)
    }

    override fun setUserEmail(email: String) {
        sharedPreferences.edit { putString(USER_EMAIL_KEY, email) }
    }

    override fun removeEmail() {
        sharedPreferences.edit {
            remove(USER_EMAIL_KEY)
        }
    }

    override fun encryptData(data: String): String {
        val secret = tokenRepository.getSymmetricKey()
        return try {
            if(secret.isNullOrEmpty()) {
                NOT_SET
            } else {
                stringAesCrypto.encrypt(data, secret)
            }
        } catch (encryptFail: Exception) {
            NOT_SET
        }
    }
}

internal const val USER_EMAIL_KEY = "user_email"
internal const val NOT_SET = "{}"
