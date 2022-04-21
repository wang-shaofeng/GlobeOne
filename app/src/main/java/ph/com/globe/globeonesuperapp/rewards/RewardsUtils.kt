/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ph.com.globe.model.SearchItem
import ph.com.globe.model.rewards.LoyaltyProgramId
import ph.com.globe.model.rewards.RewardsCatalogItem
import ph.com.globe.model.rewards.RewardsCategory

fun Flow<List<RewardsCatalogItem>>.sort(_sortType: Flow<SortType>, category: RewardsCategory) =
    this.combine(_sortType) { list, sortType ->
        if (category == RewardsCategory.DONATION)
            list.odettePrioritySort(sortType)
        else
            list.sort(sortType)
    }

fun List<RewardsCatalogItem>.sort(sort: SortType) = when (sort) {
    SortType.SORT_BY_PRICE_ASC ->
        this.sortedBy { it.pointsCost.toIntOrNull() }
    SortType.SORT_BY_PRICE_DESC ->
        this.sortedByDescending { it.pointsCost.toIntOrNull() }
    else -> this
}

fun List<RewardsCatalogItem>.odettePrioritySort(sort: SortType) = when (sort) {
    SortType.SORT_BY_PRICE_ASC ->
        this.sortedBy { it.pointsCost.toIntOrNull()?.minus(it.getOdettePriority()) }
    SortType.SORT_BY_PRICE_DESC ->
        this.sortedByDescending { it.pointsCost.toIntOrNull()?.plus(it.getOdettePriority()) }
    else -> this
}

fun List<SearchItem>.odettePrioritySort() =
    this.sortedBy {
        if (it is RewardsCatalogItem)
            (it.pointsCost.toIntOrNull() ?: NO_PRIORITY) - it.getOdettePriority()
        else
            NO_PRIORITY
    }

// TODO read me: Odette typhoon related changes. To be reverted after January the 4th. Consult @Shao about this.
fun SearchItem.getOdettePriority(): Int {
    return when {
        this.name.contains("Odette") -> ODETTE_SORT_PRIORITY
        else -> return NO_PRIORITY
    }.plus(
        when {
            this.name.contains("GMA", true) -> GMA_FOUNDATION_SORT_PRIORITY
            this.name.contains("Rise against hunger", true) -> RAH_FOUNDATION_PRIORITY
            this.name.contains("Ayala", true) -> AYALA_FOUNDATION_SORT_PRIORITY
            else -> NO_PRIORITY
        }
    )
}

private const val ODETTE_SORT_PRIORITY = 10000
private const val GMA_FOUNDATION_SORT_PRIORITY = 100003
private const val RAH_FOUNDATION_PRIORITY = 100002
private const val AYALA_FOUNDATION_SORT_PRIORITY = 100001
private const val NO_PRIORITY = 0

fun Flow<List<RewardsCatalogItem>>.applyFilters(appliedFilters: Flow<List<RewardFilter>>) =
    combine(appliedFilters) { rewards, filters ->
        if (filters.isEmpty()) rewards
        else rewards.filter {
            var shouldAddBudget = false
            var shouldAddType = false

            var hasAnyBudgetFilter = false
            var hasAnyTypeFilter = false

            for (filter in filters) {
                when (filter) {
                    is RewardFilter.BudgetFilter -> {
                        hasAnyBudgetFilter = true
                        val rewardPoints = it.pointsCost.toInt()
                        shouldAddBudget = shouldAddBudget ||
                                rewardPoints >= filter.min && (filter.max == RewardFilter.BudgetFilter.UNLIMITED || rewardPoints <= filter.max)
                    }

                    is RewardFilter.TypeFilter -> {
                        hasAnyTypeFilter = true
                        shouldAddType = shouldAddType || when (filter.type) {
                            RewardFilter.TypeFilter.SubscriberType.GLOBE_PREPAID -> LoyaltyProgramId.PREPAID in it.loyaltyProgramIds
                            RewardFilter.TypeFilter.SubscriberType.TM -> LoyaltyProgramId.TM in it.loyaltyProgramIds
                            RewardFilter.TypeFilter.SubscriberType.HOME_POSTPAID -> LoyaltyProgramId.POSTPAID in it.loyaltyProgramIds
                            RewardFilter.TypeFilter.SubscriberType.HPW -> LoyaltyProgramId.HPW in it.loyaltyProgramIds
                        }
                    }
                }
            }

            (shouldAddBudget || !hasAnyBudgetFilter) && (shouldAddType || !hasAnyTypeFilter)
        }
    }
