/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.model.account.DataUsageStatus
import ph.com.globe.model.group.domain_models.AccountDetailsGroupsParams
import ph.com.globe.model.group.domain_models.UsageItem
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isPostpaidMobile
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryParams
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.model.shop.domain_models.isBrandCorrect
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class DataUsageViewModel @Inject constructor(
    private val accountDomainManager: AccountDomainManager,
    private val shopDomainManager: ShopDomainManager
) : BaseViewModel() {

    private val _promoAllOffersList = shopDomainManager.getPromos()

    private val _dataUsageItems: MutableList<UsageItem> = mutableListOf()

    private val _tryTheseOut = MutableLiveData<List<ShopItem>>()
    val tryTheseOut: LiveData<List<ShopItem>> = _tryTheseOut

    private val _subscriptionsHistory = MutableLiveData<List<ShopItem>>()
    val subscriptionsHistory: LiveData<List<ShopItem>> = _subscriptionsHistory

    private val _dataUsageStatus: MutableLiveData<DataUsageStatus> = MutableLiveData()
    val dataUsageStatus: LiveData<DataUsageStatus> = _dataUsageStatus

    fun fetchDataUsage(enrolledAccount: EnrolledAccount, accountAlias: String) {
        viewModelScope.launch(Dispatchers.Main) {

            // Clear previously fetched usages
            _dataUsageItems.clear()

            async { getAccountData(enrolledAccount, accountAlias) }

            if (!enrolledAccount.isPostpaidMobile()) {
                async { getSubscriptionHistory(enrolledAccount.primaryMsisdn) }
            }
        }
    }

    fun setDataUsageStatus(status: DataUsageStatus) {
        _dataUsageStatus.value = status
    }

    private suspend fun getAccountData(enrolledAccount: EnrolledAccount, accountAlias: String) {
        val params = AccountDetailsGroupsParams(
            enrolledAccount.primaryMsisdn,
            accountAlias
        )
        val dataUsagesFlow = if (enrolledAccount.isPostpaidMobile()) {
            accountDomainManager.getPostpaidAccountDataUsageItems(params)
        } else {
            accountDomainManager.getAccountDataUsageItems(params)
        }
        dataUsagesFlow.first().fold(
            { items ->
                _dataUsageItems.addAll(items)

                _dataUsageStatus.value = _dataUsageItems.takeIf { it.isNotEmpty() }?.let {
                    DataUsageStatus.Success(it)
                } ?: DataUsageStatus.Empty
            }, {
                dLog("Failed to fetch account details data")
            }
        )
    }

    fun showTryTheseOutItem(accountBrand: AccountBrand) {
        viewModelScope.launch {
            _promoAllOffersList.collect { offers ->
                _tryTheseOut.value = offers.filter { shopItem ->
                    shopItem.isBrandCorrect(accountBrand)
                }.filter { filterItem ->
                    filterItem.sections.any { section ->
                        section.sortPriority == 1 && section.isSection
                    }
                }
            }
        }
    }

    private suspend fun getSubscriptionHistory(msisdn: String) {
        shopDomainManager.getPromoSubscriptionHistory(
            GetPromoSubscriptionHistoryParams(
                msisdn,
                "Inactive"
            )
        ).fold({ historyItems ->
            _promoAllOffersList.collect { offers ->
                _subscriptionsHistory.value = offers.filter { shopItem ->
                    historyItems.result.subscriptions.any { it.promoName == shopItem.name }
                }
            }
            dLog("GetPromoSubscriptionHistory success")
        }, {
            dLog("GetPromoSubscriptionHistory failure")
        })
    }

    override val logTag = "DataUsageViewModel"
}

const val ACCESS_DATA_INFO_URL = "https://www.globe.com.ph/help/mobile-internet/fup.html#gref"
