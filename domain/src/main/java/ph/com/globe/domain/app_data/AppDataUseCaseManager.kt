/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.app_data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.app_data.di.AppDataComponent
import ph.com.globe.errors.profile.GetRegisteredUserError
import ph.com.globe.util.LfResult
import javax.inject.Inject

class AppDataUseCaseManager @Inject constructor(
    factory: AppDataComponent.Factory
) : AppDataDomainManager {

    private val appDataComponent: AppDataComponent = factory.create()

    override suspend fun fetchRegisteredUser(): LfResult<Unit, GetRegisteredUserError> =
        withContext(Dispatchers.IO) {
            appDataComponent.provideFetchRegisteredUserUseCase().execute()
        }

    override suspend fun clearDatabase() = withContext(Dispatchers.IO) {
        appDataComponent.provideClearDatabaseUseCase().execute()
    }

    override suspend fun refreshAccountDetailsData(msisdn: String) {
        appDataComponent.provideRefreshAccountDetailsDataUseCase().execute(msisdn)
    }
}
