/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.filter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ph.com.globe.analytics.events.HIGHEST_TO_LOWEST
import ph.com.globe.analytics.events.LOWEST_TO_HIGHEST
import ph.com.globe.analytics.events.MOST_POPULAR
import ph.com.globe.globeonesuperapp.shop.promo.filter.ShopItemFilter.*
import ph.com.globe.globeonesuperapp.shop.promo.filter.ShopItemFilter.BudgetFilter.Companion.UNLIMITED
import ph.com.globe.globeonesuperapp.utils.AppConstants.ALL_PROMOS_TAB
import ph.com.globe.globeonesuperapp.utils.AppConstants.GLOBE_PREPAID_TAB
import ph.com.globe.globeonesuperapp.utils.AppConstants.HOME_WIFI_PREPAID_TAB
import ph.com.globe.globeonesuperapp.utils.AppConstants.TM_TAB
import ph.com.globe.model.shop.domain_models.SectionItem
import ph.com.globe.model.shop.domain_models.ShopItem

fun Flow<List<ShopItem>>.sort(_sortType: Flow<SortType>) =
    this.combine(_sortType) { list, sortType ->
        list.sort(sortType)
    }

fun List<ShopItem>.sort(sort: SortType) = when (sort) {
    SortType.SORT_BY_POPULARITY -> this.sortedBy { it.popularity }
    SortType.SORT_BY_PRICE_ASC -> this.sortedBy { it.price.toIntOrNull() }
    SortType.SORT_BY_PRICE_DESC -> this.sortedByDescending { it.price.toIntOrNull() }
    SortType.NONE -> this
}

data class Section(
    val name: String?,
    val sectionList: List<ShopItem>
)

fun List<ShopItem>.splitOnSectionsWithMax8Items(): List<Section> {
    var sections = mutableListOf<SectionItem>()

    this.forEach { shopItem ->
        sections.addAll(shopItem.sections.filter { it.isSection })
    }

    sections = sections.distinct().toMutableList()
    sections.sortBy { it.sortPriority }
    sections = sections.take(3).toMutableList()

    val s1 =
        this.filter { it.sections.any { it.name == sections.getOrNull(0)?.name ?: "" } }
    val s2 =
        this.filter { it.sections.any { it.name == sections.getOrNull(1)?.name ?: "" } }
    val s3 =
        this.filter { it.sections.any { it.name == sections.getOrNull(2)?.name ?: "" } }

    val s1Name = if (s1.isNotEmpty()) sections.getOrNull(0)?.name else null
    val s2Name = if (s2.isNotEmpty()) sections.getOrNull(1)?.name else null
    val s3Name = if (s3.isNotEmpty()) sections.getOrNull(2)?.name else null

    var s1Index = 0
    var s2Index = 0
    var s3Index = 0

    val s11 = mutableListOf<ShopItem>()
    val s22 = mutableListOf<ShopItem>()
    val s33 = mutableListOf<ShopItem>()
    var count = 0
    var i = 0
    while ((s1.size + s2.size + s3.size) > count && count != 8) {
        val section = i % 3

        if (section == 0) {
            if (s1.size > s1Index) {
                s11.add(s1[s1Index++])
                count++
            }
        } else if (section == 1) {
            if (s2.size > s2Index) {
                s22.add(s2[s2Index++])
                count++
            }
        } else if (section == 2) {
            if (s3.size > s3Index) {
                s33.add(s3[s3Index++])
                count++
            }
        }

        i++
    }

    return arrayListOf(Section(s1Name, s11), Section(s2Name, s22), Section(s3Name, s33))
}

fun Flow<List<ShopItem>>.applyFilters(
    selectedFiltersFlow: Flow<List<ShopItemFilter>>
) = this.combine(selectedFiltersFlow) { promos, selectedFilters ->
    if (selectedFilters.isEmpty()) return@combine promos

    return@combine promos.filter {
        var shouldAddBudget = false
        var shouldAddValidity = false
        var shouldAddType = false
        var shouldAddFunction = false
        var shouldAddPromoType = false

        var hasAnyBudgetFilter = false
        var hasAnyValidityFilter = false
        var hasAnyTypeFilter = false
        var hasAnyFunctionFilter = false
        var hasAnyPromoTypeFilter = false

        for (filter in selectedFilters) {
            when (filter) {
                is BudgetFilter -> {
                    hasAnyBudgetFilter = true
                    val price = it.price.toInt()
                    shouldAddBudget = shouldAddBudget ||
                            price >= filter.min && (filter.max == UNLIMITED || price <= filter.max)
                }
                is ValidityFilter -> {
                    hasAnyValidityFilter = true
                    shouldAddValidity =
                        shouldAddValidity || it.validity?.days.toString() == filter.value
                }
                is TypeFilter -> {
                    hasAnyTypeFilter = true
                    shouldAddType = shouldAddType || it.types.contains(filter.value)
                }
                is FunctionFilter -> {
                    hasAnyFunctionFilter = true
                    shouldAddFunction = shouldAddFunction || it.functions.contains(filter.value)
                }
                is PromoTypeFilter -> {
                    hasAnyPromoTypeFilter = true
                    shouldAddPromoType = shouldAddPromoType || it.promoType.contains(filter.value)
                }
            }
        }

        (shouldAddBudget || !hasAnyBudgetFilter) && (shouldAddValidity || !hasAnyValidityFilter) && (shouldAddType || !hasAnyTypeFilter) && (shouldAddFunction || !hasAnyFunctionFilter) && (shouldAddPromoType || !hasAnyPromoTypeFilter)
    }
}

sealed class ShopItemFilter(
    open val title: String,
    open val tabs: List<Int> = listOf(
        ALL_PROMOS_TAB,
        GLOBE_PREPAID_TAB,
        TM_TAB,
        HOME_WIFI_PREPAID_TAB
    ),
    open var selected: Boolean = false
) {

    abstract fun toFilterType(): FilterType

    data class BudgetFilter(
        val min: Int,
        val max: Int,
        override val title: String
    ) : ShopItemFilter(title) {
        companion object {
            const val UNLIMITED = -1
        }

        override fun toFilterType() = FilterType.BUDGET_FILTER
    }

    data class ValidityFilter(
        val value: String,
        override val title: String
    ) : ShopItemFilter(title) {

        override fun toFilterType() = FilterType.VALIDITY_FILTER
    }

    data class TypeFilter(
        val value: String,
        override val title: String
    ) : ShopItemFilter(title) {

        override fun toFilterType() = FilterType.TYPE_FILTER
    }

    data class FunctionFilter(
        val value: String,
        override val title: String
    ) : ShopItemFilter(title) {

        override fun toFilterType() = FilterType.FUNCTION_FILTER
    }

    data class PromoTypeFilter(
        val value: String,
        override val title: String,
        override val tabs: List<Int> = listOf(
            ALL_PROMOS_TAB,
            GLOBE_PREPAID_TAB,
            TM_TAB,
            HOME_WIFI_PREPAID_TAB
        )
    ) : ShopItemFilter(title, tabs) {

        override fun toFilterType() = FilterType.PROMOTYPE_FILTER
    }
}

enum class FilterType {
    BUDGET_FILTER, VALIDITY_FILTER, TYPE_FILTER, FUNCTION_FILTER, PROMOTYPE_FILTER, NONE;
}

enum class SortType {
    SORT_BY_POPULARITY, SORT_BY_PRICE_ASC, SORT_BY_PRICE_DESC, NONE;

    companion object {
        fun toSortType(num: Int) = when (num) {
            0 -> SORT_BY_POPULARITY
            1 -> SORT_BY_PRICE_ASC
            2 -> SORT_BY_PRICE_DESC
            else -> NONE
        }

        fun toAnalyticsTextValue(num: Int) = when (num) {
            0 -> MOST_POPULAR
            1 -> LOWEST_TO_HIGHEST
            2 -> HIGHEST_TO_LOWEST
            else -> ""
        }
    }
}
