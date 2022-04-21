/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.profile.ProfileDataManager
import ph.com.globe.errors.profile.*
import ph.com.globe.model.profile.response_models.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkProfileManager @Inject constructor(
    factory: ProfileComponent.Factory
) : ProfileDataManager {

    private val profileComponent: ProfileComponent = factory.create()

    override suspend fun getRegisteredUser(): LfResult<GetRegisteredUserResponseResult, GetRegisteredUserError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetRegisteredUserNetworkCall().execute()
        }

    override suspend fun getEnrolledAccounts(): LfResult<List<EnrolledAccountJson>, GetEnrolledAccountsError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetEnrolledAccountsNetworkCall().execute()
        }

    override suspend fun getCustomerDetails(params: GetCustomerDetailsParams): LfResult<CustomerDetails, GetCustomerDetailsError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetCustomerDetailsNetworkCall().execute(params)
        }

    override suspend fun getCustomerInterests(): LfResult<List<String>, GetCustomerInterestsError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetCustomerInterestsNetworkCall().execute()
        }

    override suspend fun getERaffleEntries(): LfResult<GetERaffleEntriesResult, GetERaffleEntriesError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideGetERaffleEntriesNetworkCall().execute()
        }

    override suspend fun addCustomerInterests(interests: List<String>): LfResult<Boolean, AddCustomerInterestsError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideAddCustomerInterestsNetworkCall().execute(interests)
        }

    override suspend fun sendVerificationEmail(): LfResult<Unit, SendVerificationEmailError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideSendVerificationEmailNetworkCall().execute()
        }

    override suspend fun updateUserProfile(params: UpdateUserProfileRequestParams): LfResult<Unit, UpdateUserProfileError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideUpdateUserProfileNetworkCall().execute(params)
        }

    override suspend fun verifyEmail(verificationCode: String): LfResult<Unit, VerifyEmailError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideVerifyEmailNetworkCall().execute(verificationCode)
        }

    override suspend fun checkCompleteKYC(): LfResult<Boolean, CheckCompleteKYCError> =
        withContext(Dispatchers.IO) {
            profileComponent.provideCheckCompleteKYCNetworkCall().execute()
        }
}
