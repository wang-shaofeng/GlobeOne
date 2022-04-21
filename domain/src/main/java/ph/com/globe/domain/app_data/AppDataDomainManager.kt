/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.app_data

import ph.com.globe.errors.profile.GetRegisteredUserError
import ph.com.globe.util.LfResult

interface AppDataDomainManager {

    suspend fun fetchRegisteredUser(): LfResult<Unit, GetRegisteredUserError>

    suspend fun clearDatabase()

    suspend fun refreshAccountDetailsData(msisdn: String)
}
