/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rewards

import androidx.core.graphics.PathSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ph.com.globe.domain.rewards.RewardsDataManager
import ph.com.globe.errors.rewards.*
import ph.com.globe.model.rewards.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkRewardsManager @Inject constructor(
    factory: RewardsComponent.Factory
) : RewardsDataManager {

    private val rewardsComponent = factory.create()

    override suspend fun getRewardPoints(
        msisdn: String,
        segment: String
    ): LfResult<GetRewardPointsResponse, GetRewardPointsError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideGetRewardPointsNetworkCall().execute(msisdn, segment)
        }

    override suspend fun getConversionQualification(params: GetConversionQualificationParams): LfResult<GetConversionQualificationResult, GetConversionQualificationError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideGetConversionQualificationNetworkCall().execute(params)
        }

    override suspend fun addDataConversion(requestBody: AddDataConversionRequest): LfResult<String, AddDataConversionError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideAddDataConversionNetworkCall().execute(requestBody)
        }

    override suspend fun getDataConversionDetails(conversionId: String): LfResult<GetDataConversionDetailsResult, GetDataConversionDetailsError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideGetDataConversionDetailsNetworkCall().execute(conversionId)
        }

    override suspend fun fetchRewardsCatalog(): LfResult<List<RewardsCatalogItem>, FetchRewardsCatalogError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideRewardsCatalogNetworkCall().execute()
        }

    override fun getRandomFromEachCategory(): Flow<List<RewardsCatalogItem>> =
        rewardsComponent.provideRewardsRepository().getRandomFromEachCategory()

    override suspend fun setRewardsCatalog(list: List<RewardsCatalogItem>) {
        rewardsComponent.provideRewardsRepository().setRewardsCatalog(list)
    }

    override fun getFreeRandomRewards(
        num: Int,
        loyaltyProgramId: LoyaltyProgramId
    ): Flow<List<RewardsCatalogItem>> =
        rewardsComponent.provideRewardsRepository().getFreeRandomRewards(num, loyaltyProgramId)

    override fun getRewards(): Flow<List<RewardsCatalogItem>> =
        rewardsComponent.provideRewardsRepository().getRewards()

    override fun getRandomFromEachCategoryDependsOnPoints(
        points: Float,
        loyaltyProgramId: LoyaltyProgramId
    ) = rewardsComponent.provideRewardsRepository()
        .getRandomFromEachCategoryDependsOnPoints(points, loyaltyProgramId)

    override suspend fun getLoyaltyCustomerProfile(mobileNumber: String): LfResult<LoyaltyCustomerProfileModel, LoyaltyCustomerProfileError> =
        rewardsComponent.provideGetLoyaltyCustomerProfileCall().execute(mobileNumber)

    override suspend fun redeemLoyaltyRewards(
        mobileNumber: String,
        rewardsCatalogItem: RewardsCatalogItem,
        loyaltyProgramId: LoyaltyProgramId
    ): LfResult<RedeemLoyaltyRewardsResult, RedeemLoyaltyRewardsError> =
        rewardsComponent.provideRedeemLoyaltyRewardsNetworkCall()
            .execute(mobileNumber, rewardsCatalogItem, loyaltyProgramId)

    override suspend fun getMerchantDetailsUsingUUID(uuid: String): LfResult<GetMerchantDetailsResult, GetMerchantDetailsError> =
        rewardsComponent.provideGetMerchantDetailsNetworkCall().executeUsingUUID(uuid)

    override suspend fun getMerchantDetailsUsingMobileNumber(mobileNumber: String): LfResult<GetMerchantDetailsResult, GetMerchantDetailsError> =
        rewardsComponent.provideGetMerchantDetailsNetworkCall()
            .executeUsingMobileNumber(mobileNumber)

    override suspend fun redeemPoints(
        msisdn: String,
        merchantNumber: String,
        amount: Float
    ): LfResult<RedeemPointsResult, RedeemPointsError> =
        rewardsComponent.provideRedeemPointsNetworkCall()
            .execute(msisdn, merchantNumber, amount)
}
