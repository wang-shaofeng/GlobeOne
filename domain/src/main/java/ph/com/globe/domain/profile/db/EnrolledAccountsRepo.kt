/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile.db

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.util.LfResult

interface EnrolledAccountsRepo {

    suspend fun getAllEnrolledAccounts(): Flow<LfResult<List<EnrolledAccount>, GetEnrolledAccountsError>>

    suspend fun refreshEnrolledAccounts()

    suspend fun invalidateEnrolledAccounts()

    suspend fun deleteEnrolledAccounts()

    suspend fun deleteEnrolledAccount(primaryMsisdn: String)

    suspend fun deleteMetadata()
}
