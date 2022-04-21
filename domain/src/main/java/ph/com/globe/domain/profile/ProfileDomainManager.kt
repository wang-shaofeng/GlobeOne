/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.profile.*
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.RegisteredUser
import ph.com.globe.model.profile.response_models.*
import ph.com.globe.util.LfResult

interface ProfileDomainManager {

    suspend fun getRegisteredUser(): Flow<LfResult<RegisteredUser?, GetRegisteredUserError>>

    suspend fun getUserNickname(): Flow<LfResult<String?, GetRegisteredUserError>>

    suspend fun getUserFirstName(): Flow<LfResult<String?, GetRegisteredUserError>>

    suspend fun getEnrolledAccounts(): Flow<LfResult<List<EnrolledAccount>, GetEnrolledAccountsError>>

    suspend fun refreshEnrolledAccounts()

    suspend fun invalidateEnrolledAccounts()

    suspend fun deleteEnrolledAccounts()

    suspend fun deleteEnrolledAccount(primaryMsisdn: String)

    suspend fun getCustomerDetails(params: GetCustomerDetailsParams): LfResult<CustomerDetails, GetCustomerDetailsError>

    suspend fun getCustomerInterests(): LfResult<List<String>, GetCustomerInterestsError>

    suspend fun getERaffleEntries(): LfResult<GetERaffleEntriesResult, GetERaffleEntriesError>

    suspend fun addCustomerInterests(interests: List<String>): LfResult<Boolean, AddCustomerInterestsError>

    suspend fun updateUserProfile(params: UpdateUserProfileRequestParams): LfResult<Unit, UpdateUserProfileError>

    suspend fun sendVerificationEmail(): LfResult<Unit, SendVerificationEmailError>

    suspend fun verifyEmail(verificationCode: String): LfResult<Unit, VerifyEmailError>

    suspend fun checkCompleteKYC(): LfResult<Boolean, CheckCompleteKYCError>
}
