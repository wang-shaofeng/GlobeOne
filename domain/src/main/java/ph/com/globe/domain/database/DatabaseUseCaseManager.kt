/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.database

import ph.com.globe.domain.database.di.DatabaseComponent
import javax.inject.Inject

class DatabaseUseCaseManager @Inject constructor(
    factory: DatabaseComponent.Factory
) : DatabaseDomainManager {

    val component = factory.create()

    override suspend fun clearAllData() {
        component.provideClearAllDataUseCase().execute()
    }
}
