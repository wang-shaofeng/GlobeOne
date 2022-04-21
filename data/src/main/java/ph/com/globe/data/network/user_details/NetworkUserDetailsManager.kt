/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.user_details

import ph.com.globe.domain.user_details.UserDetailsDataManager
import ph.com.globe.errors.NetworkError
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkUserDetailsManager @Inject constructor(
    private val userDetailsRepository: UserDetailsRepository
) : UserDetailsDataManager {
    override fun getEmail(): LfResult<String, NetworkError.UserNotLoggedInError> =
        userDetailsRepository.getUserEmail()

    override fun setEmail(email: String) {
        userDetailsRepository.setUserEmail(email)
    }

    override fun encryptData(data: String): String {
        return userDetailsRepository.encryptData(data)
    }
}
