/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.profile_info

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.DataScope
import ph.com.globe.data.db.needsUpdate
import ph.com.globe.data.db.util.ParameterlessRepoUpdater
import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.domain.profile.db.RegisteredUserRepo
import ph.com.globe.errors.profile.GetRegisteredUserError
import ph.com.globe.model.profile.domain_models.RegisteredUser
import ph.com.globe.model.profile.response_models.GetRegisteredUserResponseResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

/**
 * Repository for [RegisteredUser]s. Exposes [getRegisteredUser] function, which will try to fetch
 * registered user from [registeredUserDao], and [checkFreshnessAndUpdate] function which will check
 * if there are stored registered user or the data is not stale, if it is stale it will start the update
 * via [fetchRegisteredUser] function.
 * Also there are functions [getUserNickname] which will get the nickname of the user and [getFirstName] which will
 * get the first name of the user.
 */
@DataScope
class RegisteredUserRepository @Inject constructor(
    private val registeredUserDao: GlobeRegisteredUserQueryDao,
    private val profileDataManager: ProfileDataManager,
    private val parameterlessRepoUpdater: ParameterlessRepoUpdater<GetRegisteredUserResponseResult, GetRegisteredUserError>
) : RegisteredUserRepo, HasLogTag {

    override suspend fun fetchRegisteredUser(): LfResult<Unit, GetRegisteredUserError> {
        parameterlessRepoUpdater.update(profileDataManager::getRegisteredUser) { registeredUserResponse ->
            registeredUserDao.clearInsert(registeredUserResponse.toEntity())
        }?.let {
            return LfResult.failure(it)
        }

        return LfResult.success(Unit)
    }

    override suspend fun checkFreshnessAndUpdate(): LfResult<Unit, GetRegisteredUserError> {
        val registeredUserWithFreshness = registeredUserDao.getFreshness().first()
        if (registeredUserWithFreshness.needsUpdate()) {
            return fetchRegisteredUser()
        }

        return LfResult.success(Unit)
    }

    override suspend fun getRegisteredUser(): Flow<RegisteredUser?> =
        registeredUserDao.getRegisteredUser().map { it?.toDomain() }

    override suspend fun getUserNickname(): Flow<String?> = registeredUserDao.getNickname()

    override suspend fun getFirstName(): Flow<String?> = registeredUserDao.getFirstName()

    override suspend fun refreshRegisteredUser() {
        registeredUserDao.staleRow()
    }

    override suspend fun invalidateRegisteredUser() {
        registeredUserDao.invalidRow()
    }

    override suspend fun deleteRegisteredUser() {
        registeredUserDao.deleteRegisteredUser()
    }

    override suspend fun deleteMetadata() {
        registeredUserDao.deleteMetadata()
    }

    override val logTag = "RegisteredUserRepository"
}
