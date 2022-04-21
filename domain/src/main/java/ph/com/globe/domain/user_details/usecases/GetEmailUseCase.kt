/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.user_details.usecases

import ph.com.globe.domain.user_details.UserDetailsDataManager
import javax.inject.Inject

class GetEmailUseCase @Inject constructor(
    private val userDetailsDataManager: UserDetailsDataManager
) {
    fun get() = userDetailsDataManager.getEmail()
}
