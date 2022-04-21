/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.rewards.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import ph.com.globe.model.rewards.FREE_GB_NAME
import ph.com.globe.model.rewards.LoyaltyProgramId
import ph.com.globe.model.rewards.RewardsCatalogItem
import javax.inject.Inject
import kotlin.random.Random

class DefaultRewardsRepository @Inject constructor() : RewardsRepository {

    private val rewardsCatalogFlow = MutableSharedFlow<List<RewardsCatalogItem>>(1)

    override suspend fun setRewardsCatalog(list: List<RewardsCatalogItem>) {
        rewardsCatalogFlow.emit(list)
    }

    override fun getRandomFromEachCategory(): Flow<List<RewardsCatalogItem>> =
        rewardsCatalogFlow.map { list ->
            list.filter { !it.name.startsWith(FREE_GB_NAME) }.groupBy { it.category }
                .mapValues { entry ->
                    val r = Random.nextInt(0, entry.value.size)
                    entry.value[r]
                }.values.toList()
        }

    override fun getFreeRandomRewards(
        num: Int,
        loyaltyProgramId: LoyaltyProgramId
    ): Flow<List<RewardsCatalogItem>> =
        rewardsCatalogFlow.map {
            val freeRewards =
                it.filter { it.pointsCost == "0" && (loyaltyProgramId in it.loyaltyProgramIds || loyaltyProgramId == LoyaltyProgramId.ALL) }

            val list = freeRewards.toMutableList()
            val randomRewards = arrayListOf<RewardsCatalogItem>()

            for (i in 0 until num) {
                if (list.size == 0) break

                val index = Random.nextInt(list.size)
                val randomItem = list.removeAt(index)
                randomRewards.add(randomItem)
            }

            return@map randomRewards
        }

    override fun getRewards(): Flow<List<RewardsCatalogItem>> = rewardsCatalogFlow

    override fun getRandomFromEachCategoryDependsOnPoints(
        points: Float,
        loyaltyProgramId: LoyaltyProgramId
    ): Flow<List<RewardsCatalogItem>> =
        rewardsCatalogFlow.map { list ->
            list.filter {
                (it.pointsCost.toInt() <= points || points == -1f) &&
                        (loyaltyProgramId in it.loyaltyProgramIds || loyaltyProgramId == LoyaltyProgramId.ALL)
            }
                .groupBy { it.category }.mapValues { entry ->
                    val r = Random.nextInt(0, entry.value.size)
                    entry.value[r]
                }.values.toList()
        }
}
