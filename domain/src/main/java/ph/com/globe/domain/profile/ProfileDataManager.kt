/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.profile

import ph.com.globe.errors.profile.*
import ph.com.globe.model.profile.response_models.*
import ph.com.globe.util.LfResult

interface ProfileDataManager {

    suspend fun getRegisteredUser(): LfResult<GetRegisteredUserResponseResult, GetRegisteredUserError>

    suspend fun getEnrolledAccounts(): LfResult<List<EnrolledAccountJson>, GetEnrolledAccountsError>

    suspend fun getCustomerDetails(params: GetCustomerDetailsParams): LfResult<CustomerDetails, GetCustomerDetailsError>

    suspend fun getCustomerInterests(): LfResult<List<String>, GetCustomerInterestsError>

    suspend fun getERaffleEntries(): LfResult<GetERaffleEntriesResult, GetERaffleEntriesError>

    suspend fun addCustomerInterests(interests: List<String>): LfResult<Boolean, AddCustomerInterestsError>

    suspend fun updateUserProfile(params: UpdateUserProfileRequestParams): LfResult<Unit, UpdateUserProfileError>

    suspend fun sendVerificationEmail(): LfResult<Unit, SendVerificationEmailError>

    suspend fun verifyEmail(verificationCode: String): LfResult<Unit, VerifyEmailError>

    suspend fun checkCompleteKYC(): LfResult<Boolean, CheckCompleteKYCError>
}
