/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards

import kotlinx.coroutines.flow.Flow
import ph.com.globe.errors.rewards.*
import ph.com.globe.model.rewards.*
import ph.com.globe.util.LfResult

interface RewardsDataManager {

    suspend fun getRewardPoints(
        msisdn: String,
        segment: String
    ): LfResult<GetRewardPointsResponse, GetRewardPointsError>

    suspend fun getConversionQualification(params: GetConversionQualificationParams): LfResult<GetConversionQualificationResult, GetConversionQualificationError>

    suspend fun addDataConversion(requestBody: AddDataConversionRequest): LfResult<String, AddDataConversionError>

    suspend fun getDataConversionDetails(conversionId: String): LfResult<GetDataConversionDetailsResult, GetDataConversionDetailsError>

    suspend fun fetchRewardsCatalog(): LfResult<List<RewardsCatalogItem>, FetchRewardsCatalogError>

    fun getRandomFromEachCategory(): Flow<List<RewardsCatalogItem>>

    suspend fun setRewardsCatalog(list: List<RewardsCatalogItem>)

    fun getFreeRandomRewards(
        num: Int,
        loyaltyProgramId: LoyaltyProgramId
    ): Flow<List<RewardsCatalogItem>>

    fun getRewards(): Flow<List<RewardsCatalogItem>>

    fun getRandomFromEachCategoryDependsOnPoints(
        points: Float,
        loyaltyProgramId: LoyaltyProgramId
    ): Flow<List<RewardsCatalogItem>>

    suspend fun getLoyaltyCustomerProfile(mobileNumber: String): LfResult<LoyaltyCustomerProfileModel, LoyaltyCustomerProfileError>

    suspend fun redeemLoyaltyRewards(
        mobileNumber: String,
        rewardsCatalogItem: RewardsCatalogItem,
        loyaltyProgramId: LoyaltyProgramId
    ): LfResult<RedeemLoyaltyRewardsResult, RedeemLoyaltyRewardsError>

    suspend fun getMerchantDetailsUsingUUID(uuid: String): LfResult<GetMerchantDetailsResult, GetMerchantDetailsError>

    suspend fun getMerchantDetailsUsingMobileNumber(mobileNumber: String): LfResult<GetMerchantDetailsResult, GetMerchantDetailsError>

    suspend fun redeemPoints(
        msisdn: String,
        merchantNumber: String,
        amount: Float
    ): LfResult<RedeemPointsResult, RedeemPointsError>
}
