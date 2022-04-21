/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile.db

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.profile.GetRegisteredUserError
import ph.com.globe.model.profile.domain_models.RegisteredUser
import ph.com.globe.util.LfResult

interface RegisteredUserRepo {

    suspend fun fetchRegisteredUser(): LfResult<Unit, GetRegisteredUserError>

    suspend fun checkFreshnessAndUpdate(): LfResult<Unit, GetRegisteredUserError>

    suspend fun getRegisteredUser(): Flow<RegisteredUser?>

    suspend fun getUserNickname(): Flow<String?>

    suspend fun getFirstName(): Flow<String?>

    suspend fun refreshRegisteredUser()

    suspend fun invalidateRegisteredUser()

    suspend fun deleteRegisteredUser()

    suspend fun deleteMetadata()
}
