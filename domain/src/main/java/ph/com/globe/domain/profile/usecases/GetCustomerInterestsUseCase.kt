/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile.usecases

import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.errors.profile.GetCustomerInterestsError
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetCustomerInterestsUseCase @Inject constructor(private val profileManager: ProfileDataManager) {

    suspend fun execute(): LfResult<List<String>, GetCustomerInterestsError> =
        profileManager.getCustomerInterests()

}
