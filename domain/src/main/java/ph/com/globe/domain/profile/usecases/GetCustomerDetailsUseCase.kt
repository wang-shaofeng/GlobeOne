/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile.usecases

import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.errors.profile.GetCustomerDetailsError
import ph.com.globe.model.profile.response_models.CustomerDetails
import ph.com.globe.model.profile.response_models.GetCustomerDetailsParams
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetCustomerDetailsUseCase @Inject constructor(private val profileManager: ProfileDataManager) {

    suspend fun execute(params: GetCustomerDetailsParams): LfResult<CustomerDetails, GetCustomerDetailsError> =
        profileManager.getCustomerDetails(params)
}
