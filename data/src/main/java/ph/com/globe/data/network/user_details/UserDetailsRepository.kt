/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.user_details

import ph.com.globe.errors.NetworkError
import ph.com.globe.util.LfResult

interface UserDetailsRepository {

    fun getUserEmail(): LfResult<String, NetworkError.UserNotLoggedInError>

    fun setUserEmail(email: String)

    fun removeEmail()

    fun encryptData(data: String): String
}
