/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.rewards

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ph.com.globe.domain.rewards.di.RewardsComponent
import ph.com.globe.errors.rewards.*
import ph.com.globe.model.rewards.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class RewardsUseCaseManager @Inject constructor(
    factory: RewardsComponent.Factory
) : RewardsDomainManager {

    private val rewardsComponent: RewardsComponent = factory.create()

    override suspend fun getRewardPoints(msisdn: String, segment: String) =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideGetRewardPointsUseCase().execute(msisdn, segment)
        }

    override suspend fun getConversionQualification(): LfResult<List<QualificationDetails>, GetConversionQualificationError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideGetConversionQualificationUseCase().execute()
        }

    override suspend fun addDataConversion(requestBody: AddDataConversionRequest): LfResult<String, AddDataConversionError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideAddDataConversionUseCase().execute(requestBody)
        }

    override suspend fun getDataConversionDetails(conversionId: String): LfResult<GetDataConversionDetailsResult, GetDataConversionDetailsError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideGetDataConversionDetailsUseCase().execute(conversionId)
        }

    override suspend fun fetchRewardsCatalog(): LfResult<List<RewardsCatalogItem>, FetchRewardsCatalogError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideFetchRewardsCatalogUseCase().execute()
        }

    override fun getRandomFromEachCategory(): Flow<List<RewardsCatalogItem>> =
        rewardsComponent.provideRandomRewardsFromEachCategoryUseCase().get()

    override fun getFreeRandomRewards(
        num: Int,
        loyaltyProgramId: LoyaltyProgramId
    ): Flow<List<RewardsCatalogItem>> =
        rewardsComponent.provideFreeRandomRewardsUseCase().get(num, loyaltyProgramId)

    override fun getRewardsForCategoryDependsOnPointsAndLoyaltyProgramId(
        points: Float,
        loyaltyProgramId: LoyaltyProgramId,
        category: RewardsCategory
    ): Flow<List<RewardsCatalogItem>> =
        rewardsComponent.provideRewardsForCategoryUseCase().get(points, loyaltyProgramId, category)

    override fun getRandomFromEachCategoryDependsOnPointsAndLoyaltyId(
        points: Float,
        loyaltyProgramId: LoyaltyProgramId
    ): Flow<List<RewardsCatalogItem>> =
        rewardsComponent.provideRandomFromEachCategoryDependsOnPointsUseCase()
            .get(points, loyaltyProgramId)

    override suspend fun getLoyaltyProgramIdFromSpecificMsisdn(mobileNumber: String): LfResult<LoyaltyProgramId, LoyaltyCustomerProfileError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideGetLoyaltyProgramIdFromSpecificMsisdnUseCase()
                .execute(mobileNumber)
        }

    override fun getAllRewards(): Flow<List<RewardsCatalogItem>> =
        rewardsComponent.provideGetAllRewardsUseCase().get()

    override suspend fun redeemLoyaltyRewards(
        mobileNumber: String,
        rewardsCatalogItem: RewardsCatalogItem,
        loyaltyProgramId: LoyaltyProgramId
    ): LfResult<RedeemLoyaltyRewardsResult, RedeemLoyaltyRewardsError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideRedeemLoyaltyRewardsUseCase()
                .execute(mobileNumber, rewardsCatalogItem, loyaltyProgramId)
        }

    override suspend fun getMerchantDetailsUsingUUID(uuid: String): LfResult<GetMerchantDetailsResult, GetMerchantDetailsError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideGetMerchantDetailsUseCase().executeUsingUUID(uuid)
        }

    override suspend fun getMerchantDetailsUsingMobileNumber(mobileNumber: String): LfResult<GetMerchantDetailsResult, GetMerchantDetailsError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideGetMerchantDetailsUseCase()
                .executeUsingMobileNumber(mobileNumber)
        }

    override suspend fun redeemPoints(
        msisdn: String,
        merchantNumber: String,
        amount: Float
    ): LfResult<RedeemPointsResult, RedeemPointsError> =
        withContext(Dispatchers.IO) {
            rewardsComponent.provideRedeemPointsUseCase()
                .execute(msisdn, merchantNumber, amount)
        }
}
