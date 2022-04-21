/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.content

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.catalog.CatalogDomainManager
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.model.catalog.ContentSubscriptionStatusParams
import ph.com.globe.model.shop.domain_models.CONTENT_PROMO_METHOD_DCB
import ph.com.globe.model.shop.ContentSubscriptionUIModel
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject

@HiltViewModel
class ContentSubscriptionsViewModel @Inject constructor(
    private val shopDomainManager: ShopDomainManager,
    private val catalogDomainManager: CatalogDomainManager
) : BaseViewModel() {

    private val _contentSubscriptions = MutableLiveData<List<ContentSubscriptionUIModel>>()
    val contentSubscriptions: LiveData<List<ContentSubscriptionUIModel>> = _contentSubscriptions

    private val _subscriptionRemovedEvent = MutableLiveData<OneTimeEvent<Unit>>()
    val subscriptionRemovedEvent: LiveData<OneTimeEvent<Unit>> = _subscriptionRemovedEvent

    fun getContentSubscriptions(msisdn: String) {
        viewModelScope.launch {
            shopDomainManager.fetchOffers()
                .onSuccess { allOffers ->
                    shopDomainManager.getPromoSubscriptionHistory(
                        GetPromoSubscriptionHistoryParams(
                            msisdn,
                            status = "Active"
                        )
                    )
                        .onSuccess { historyResponse ->
                            val subscriptionsUIModels = mutableListOf<ContentSubscriptionUIModel>()
                            historyResponse.result.subscriptions.let { subscriptions ->
                                val contentPromos =
                                    allOffers.filter { it.isContent && it.method != CONTENT_PROMO_METHOD_DCB }

                                subscriptions.forEach { s ->
                                    contentPromos
                                        .find { p -> p.chargePromoId == s.serviceId }
                                        ?.let { promo ->
                                            subscriptionsUIModels.add(
                                                ContentSubscriptionUIModel(
                                                    serviceId = s.serviceId,
                                                    promoName = promo.name ?: "",
                                                    expiryDate = s.expiryDate,
                                                    description = promo.description,
                                                    asset = promo.asset ?: "",
                                                    displayColor = promo.displayColor,
                                                    redirectionLink = promo.partnerRedirectionLink
                                                        ?: ""
                                                )
                                            )
                                        }
                                }
                            }

                            // Get subscription status for each content promo
                            val results = subscriptionsUIModels.map { subscription ->
                                async {
                                    catalogDomainManager.getContentSubscriptionStatus(
                                        ContentSubscriptionStatusParams(
                                            msisdn,
                                            subscription.serviceId
                                        )
                                    ).fold({ statusResult ->
                                        subscription.isActivated = statusResult.activationStatus
                                        dLog("Get content subscription status success")
                                        LfResult.success(subscription)
                                    }, {
                                        dLog("Get content subscription status failure")
                                        LfResult.failure(it, null)
                                    })
                                }
                            }.awaitAll()

                            // Removing subscriptions for which getContentSubscriptionStatus returned an error
                            _contentSubscriptions.value = results.mapNotNull { it.value }

                            dLog("Get promo subscriptions history success")
                        }
                        .onFailure {
                            dLog("Get promo subscriptions history failure")
                        }
                    dLog("Fetching shop offers success")
                }
                .onFailure {
                    dLog("Fetching shop offers failure")
                }
        }
    }

    fun removeContentSubscription(serviceId: String) {
        _contentSubscriptions.value?.toMutableList()?.let { subscriptions ->
            subscriptions
                .indexOfFirst { s -> s.serviceId == serviceId }
                .takeIf { i -> i != -1 }?.let { index ->
                    subscriptions.removeAt(index)
                    _contentSubscriptions.value = subscriptions

                    // Emit content subscription removed event
                    _subscriptionRemovedEvent.value = OneTimeEvent(Unit)
                }
        }
    }

    override val logTag = "ContentUsageViewModel"
}
