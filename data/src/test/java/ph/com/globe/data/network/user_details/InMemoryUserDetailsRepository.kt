/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.user_details

import ph.com.globe.errors.NetworkError
import ph.com.globe.util.LfResult

object InMemoryUserDetailsRepository : UserDetailsRepository {

    private var emailStorage: String? = null

    override fun getUserEmail(): LfResult<String, NetworkError.UserNotLoggedInError> =
        emailStorage?.let { LfResult.success(it) }
            ?: LfResult.failure(NetworkError.UserNotLoggedInError)


    override fun setUserEmail(email: String) {
        emailStorage = email
    }

    override fun removeEmail() {
        emailStorage = null
    }

    override fun encryptData(data: String): String {
        return "EncryptedData"
    }
}
