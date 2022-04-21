/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile.usecases

import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.errors.profile.GetERaffleEntriesError
import ph.com.globe.model.profile.response_models.GetERaffleEntriesResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetERaffleEntriesUseCase @Inject constructor(private val profileManager: ProfileDataManager)  {
    suspend fun execute(): LfResult<GetERaffleEntriesResult, GetERaffleEntriesError> =
        profileManager.getERaffleEntries()
}
