/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ph.com.globe.domain.profile.di.ProfileComponent
import ph.com.globe.errors.profile.*
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.RegisteredUser
import ph.com.globe.model.profile.response_models.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class ProfileUseCaseManager @Inject constructor(
    factory: ProfileComponent.Factory
) : ProfileDomainManager {

    private val profileComponent: ProfileComponent = factory.create()

    override suspend fun getRegisteredUser(): Flow<LfResult<RegisteredUser?, GetRegisteredUserError>> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetRegisteredUserUseCase().execute()
        }

    override suspend fun getUserNickname(): Flow<LfResult<String?, GetRegisteredUserError>> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetUserNicknameUseCase().execute()
        }

    override suspend fun getUserFirstName(): Flow<LfResult<String?, GetRegisteredUserError>> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetUserFirstNameUseCase().execute()
        }

    override suspend fun getEnrolledAccounts(): Flow<LfResult<List<EnrolledAccount>, GetEnrolledAccountsError>> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetEnrolledAccountsUseCase().execute()
        }

    override suspend fun refreshEnrolledAccounts() {
        profileComponent.provideRefreshEnrolledAccountsUseCase().execute()
    }

    override suspend fun invalidateEnrolledAccounts() {
        profileComponent.provideInvalidateEnrolledAccountsUseCase().execute()
    }

    override suspend fun deleteEnrolledAccounts() {
        profileComponent.provideDeleteEnrolledAccountsUseCase().execute()
    }

    override suspend fun deleteEnrolledAccount(primaryMsisdn: String) {
        profileComponent.provideDeleteEnrolledAccountUseCase().execute(primaryMsisdn)
    }

    override suspend fun getCustomerDetails(params: GetCustomerDetailsParams): LfResult<CustomerDetails, GetCustomerDetailsError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetCustomerDetailsUseCase().execute(params)
        }

    override suspend fun getCustomerInterests(): LfResult<List<String>, GetCustomerInterestsError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetCustomerInterestsUseCase().execute()
        }

    override suspend fun getERaffleEntries(): LfResult<GetERaffleEntriesResult, GetERaffleEntriesError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetERaffleEntriesUseCase().execute()
        }

    override suspend fun addCustomerInterests(interests: List<String>): LfResult<Boolean, AddCustomerInterestsError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideAddCustomerInterestsUseCase().execute(interests)
        }

    override suspend fun updateUserProfile(params: UpdateUserProfileRequestParams): LfResult<Unit, UpdateUserProfileError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideUpdateUserProfileUseCase().execute(params)
        }

    override suspend fun sendVerificationEmail(): LfResult<Unit, SendVerificationEmailError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideSendVerificationEmailUseCase().execute()
        }

    override suspend fun verifyEmail(verificationCode: String): LfResult<Unit, VerifyEmailError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideVerifyEmailUseCase().execute(verificationCode)
        }

    override suspend fun checkCompleteKYC(): LfResult<Boolean, CheckCompleteKYCError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideCheckCompleteKYCUseCase().execute()
        }
}
