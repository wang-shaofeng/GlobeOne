/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.enrolled_accounts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.DataScope
import ph.com.globe.data.db.needsUpdate
import ph.com.globe.data.db.util.ParameterlessRepoUpdater
import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.domain.profile.db.EnrolledAccountsRepo
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.response_models.EnrolledAccountJson
import ph.com.globe.util.LfResult
import javax.inject.Inject

/**
 * Repository for [EnrolledAccount]s. Exposes [getAllEnrolledAccounts] method, which will try to fetch
 * enrolled accounts from [enrolledAccountDao] first, and if there are no stored enrolled accounts or
 * the data that was stored is stale, it will start the update via [parameterlessRepoUpdater]'s
 * update method.
 */
@DataScope
class EnrolledAccountRepository @Inject constructor(
    private val enrolledAccountDao: GlobeEnrolledAccountsQueryDao,
    private val profileDataManager: ProfileDataManager,
    private val parameterlessRepoUpdater: ParameterlessRepoUpdater<List<EnrolledAccountJson>, GetEnrolledAccountsError>
) : EnrolledAccountsRepo, HasLogTag {

    override suspend fun getAllEnrolledAccounts(): Flow<LfResult<List<EnrolledAccount>, GetEnrolledAccountsError>> =
        enrolledAccountDao.getAllEnrolledAccounts()
            .map { enrolledAccountsWithFreshness ->

                // Starting the repo update if the data that is currently stored is stale
                if (enrolledAccountsWithFreshness.needsUpdate()) {
                    parameterlessRepoUpdater.update(profileDataManager::getEnrolledAccounts) { enrolledJsonList ->
                        enrolledAccountDao.clearInsert(enrolledJsonList.map { it.toEntity() })
                    }?.let {
                        // The update has failed, returning the error
                        return@map LfResult.failure<List<EnrolledAccount>, GetEnrolledAccountsError>(
                            it
                        )
                    }
                }

                // Returning the data that is read (both if the update has been started or not)
                LfResult.success(enrolledAccountsWithFreshness.data!!.map { it.toDomain() })
            }

    override suspend fun refreshEnrolledAccounts() {
        enrolledAccountDao.staleRow()
    }

    override suspend fun invalidateEnrolledAccounts() {
        enrolledAccountDao.invalidRow()
    }

    override suspend fun deleteEnrolledAccounts() {
        enrolledAccountDao.deleteEnrolledAccounts()
    }

    override suspend fun deleteEnrolledAccount(primaryMsisdn: String) {
        enrolledAccountDao.deleteEnrolledAccount(primaryMsisdn)
    }

    override suspend fun deleteMetadata() {
        enrolledAccountDao.deleteMetadata()
    }

    override val logTag = "EnrolledAccountRepository"
}
