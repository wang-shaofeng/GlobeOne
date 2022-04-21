/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.user_details

import ph.com.globe.errors.NetworkError
import ph.com.globe.util.LfResult

interface UserDetailsDomainManager {
    fun getEmail(): LfResult<String, NetworkError.UserNotLoggedInError>

    fun encryptData(data: String): String
}
