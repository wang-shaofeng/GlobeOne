/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.dashboard

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.remote_config.RemoteConfigManager
import com.globe.inappupdate.usecases.PersonalizedCampaignUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.billings.BillingsDomainManager
import ph.com.globe.domain.group.GroupDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.rush.RushDomainManager
import ph.com.globe.errors.account.DeleteEnrolledAccountError
import ph.com.globe.errors.group.AccountDetailsGroupsError
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.globeonesuperapp.utils.AppConstants.PLATINUM
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.DashboardBubblesHelper
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.applyPostpaidActiveSubscription
import ph.com.globe.globeonesuperapp.utils.shared_preferences.*
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.*
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageRequest
import ph.com.globe.model.account.network_models.applyGroupData
import ph.com.globe.model.account.network_models.applySubscriptionUsage
import ph.com.globe.model.billings.network_models.applyBillingDetails
import ph.com.globe.model.billings.network_models.createPostpaidBillingDetailsParams
import ph.com.globe.model.group.domain_models.AccountDetailsGroupsParams
import ph.com.globe.model.personalized_campaign.PersonalizedCampaignConfig
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isPostpaid
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.model.util.AccountStatus.*
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.*
import java.util.*
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val profileDomainManager: ProfileDomainManager,
    private val accountDomainManager: AccountDomainManager,
    private val groupDomainManager: GroupDomainManager,
    private val rushDomainManager: RushDomainManager,
    private val remoteConfigManager: RemoteConfigManager,
    private val billingsDomainManager: BillingsDomainManager,
    private val sharedPreferences: SharedPreferences,
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
    private val personalizedCampaignUseCase: PersonalizedCampaignUseCase
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _accountsStatus = MutableLiveData<AccountsStatus>()
    val accountsStatus: LiveData<AccountsStatus> = _accountsStatus

    private val _accountsUpdateTrigger = MutableLiveData<AccountsUpdateTrigger>()
    val accountsUpdateTrigger: LiveData<AccountsUpdateTrigger> = _accountsUpdateTrigger

    val currentUiModels = mutableListOf<UsageUIModel>()

    private val accountBrandAsyncCalls = mutableListOf<Deferred<Unit>>()

    private val _nickname = MutableLiveData<NicknameUiModel>()
    val nickname: LiveData<NicknameUiModel> = _nickname

    private val _bubbleVisibilityState = MutableLiveData(false)
    val bubbleVisibilityState: LiveData<Boolean> = _bubbleVisibilityState

    private val profileBubbleShown: Boolean
        get() = sharedPreferences.getBoolean(DASHBOARD_PROFILE_BUBBLE_SHOWN_KEY, false)

    private val _greeting: MutableLiveData<String> = MutableLiveData()
    val greeting: LiveData<String> = _greeting

    private val _shouldUpdateGreeting: MutableLiveData<Boolean> = MutableLiveData()
    val shouldUpdateGreeting: LiveData<Boolean> = _shouldUpdateGreeting

    private val lastTimeGreetingWasUpdated: Long
        get() = sharedPreferences.getLong(DASHBOARD_GREETING_LAST_UPDATED_TIME, 0)

    private val _shouldShowSpinwheel: MutableLiveData<Boolean> = MutableLiveData()
    val shouldShowSpinwheel: LiveData<Boolean> = _shouldShowSpinwheel

    private val _spinwheelUrlResult: MutableLiveData<OneTimeEvent<String>> = MutableLiveData()
    val spinwheelUrlResult: LiveData<OneTimeEvent<String>> = _spinwheelUrlResult

    private val _accountRemovedEvent: MutableLiveData<OneTimeEvent<Int>> = MutableLiveData()
    val accountRemovedEvent: LiveData<OneTimeEvent<Int>> = _accountRemovedEvent

    private val _isRefreshingData: MutableLiveData<Boolean> = MutableLiveData()
    val isRefreshingData: LiveData<Boolean> = _isRefreshingData

    private val bubblesHelper by lazy { DashboardBubblesHelper(sharedPreferences) }

    val personalizedCampaigns = mutableListOf<PersonalizedCampaignConfig>()

    private val mutex = Mutex()

    init {
        if (!profileBubbleShown) {
            showProfileBubble()
        }
        _greeting.value = sharedPreferences.getString(DASHBOARD_GREETING, "")

        if (System.currentTimeMillis() - lastTimeGreetingWasUpdated > GREETING_CHANGE_PERIOD)
            _shouldUpdateGreeting.value = true

        getEnrolledAccountsWithConsumption()
    }

    private fun getEnrolledAccountsWithConsumption() {
        _accountsStatus.value = AccountsStatus(
            loading = true,
            error = false,
            empty = false
        )
        viewModelScope.launch(Dispatchers.Default) {
            accountDomainManager.setAccountsLoadingState(AccountsLoadingState.Loading)
            profileDomainManager.getEnrolledAccounts()
                .collectLatest {
                    it.fold({ accounts ->

                        if (accounts.equalToUiModels() && !isRefreshing())
                            return@fold

                        _accountsStatus.postValue(
                            AccountsStatus(
                                loading = false,
                                error = false,
                                empty = accounts.isEmpty()
                            )
                        )
                        // Disable refreshing state
                        _isRefreshingData.postValue(false)

                        sharedPreferences.edit()
                            .putInt(RATING_ENROLLED_ACCOUNTS_COUNT, accounts.size)
                            .apply()

                        mutex.withLock {
                            currentUiModels.clear()
                            currentUiModels.addAll(accounts.map { it.toLoadingUsageUIModel() })
                        }

                        _accountsUpdateTrigger.postValue(
                            AccountsUpdateTrigger(
                                updateAll = true,
                                updateIndex = -1
                            )
                        )
                        if (accounts.isNotEmpty()) {
                            val currentDayAndMonth =
                                Calendar.getInstance().time.toFormattedStringOrEmpty(GlobeDateFormat.HidingSpinwheel)
                            if (currentDayAndMonth == sharedPreferences.getString(
                                    SPINWHEEL_HIDE_DATE,
                                    ""
                                )
                            ) {
                                _shouldShowSpinwheel.postValue(false)
                            } else {
                                async {
                                    val rushCampaignInfo = remoteConfigManager.getRushData()
                                    rushCampaignInfo?.let {
                                        val show = rushDomainManager.shouldShowSpinwheelButton(it)
                                        _shouldShowSpinwheel.postValue(show)
                                    }
                                }
                            }
                        }

                        for (i in accounts.indices) {

                            // Brand and gift
                            async {
                                if (accounts[i].isPostpaid()) {
                                    currentUiModels[i].brand = AccountBrand.GhpPostpaid
                                    currentUiModels[i].brandFetched = true
                                } else {
                                    accountDomainManager.getAccountBrand(
                                        GetAccountBrandParams(
                                            accounts[i].primaryMsisdn
                                        )
                                    ).fold({
                                        currentUiModels[i].brand = it.result.brand
                                        currentUiModels[i].brandFetched = true
                                    }, {
                                        currentUiModels[i].brandFetched = true
                                    })
                                }

                                currentUiModels[i].brand?.let { brand ->
                                    personalizedCampaignUseCase.execute(brand)
                                        .onSuccess { campaigns ->
                                            accountDomainManager.getCustomerCampaignPromo(
                                                accounts[i].primaryMsisdn,
                                                currentUiModels[i].segment.toString(),
                                                campaigns
                                            ).onSuccess { list ->
                                                currentUiModels[i].hasGift = list.isNotEmpty()
                                            }
                                        }
                                    currentUiModels[i].giftFetched = true
                                    tryItemUiUpdate(i)
                                } ?: run {
                                    currentUiModels[i].giftFetched = true
                                    tryItemUiUpdate(i)
                                }
                            }.let { asyncCall ->
                                accountBrandAsyncCalls.add(asyncCall)
                            }

                            // Balance and usages
                            if (accounts[i].isPostpaid()) {

                                async {
                                    billingsDomainManager.getBillingsDetails(
                                        createPostpaidBillingDetailsParams(accounts[i])
                                    ).fold({ billingDetails ->
                                        if (currentUiModels[i].balanceFetched ||
                                            currentUiModels[i].accountStatus is Inactive
                                        ) {
                                            currentUiModels[i].balanceFetched = true
                                            tryItemUiUpdate(i)
                                        } else {
                                            currentUiModels[i].applyBillingDetails(
                                                billingDetails
                                            )
                                            currentUiModels[i].balanceFetched = true
                                            tryItemUiUpdate(i)
                                        }
                                        dLog("Fetching billing details success")
                                    }, {
                                        currentUiModels[i].balanceFetched = true
                                        tryItemUiUpdate(i)
                                        dLog("Fetching billing details failure")
                                    })
                                }

                                async {
                                    accountDomainManager.getAccountDetails(
                                        GetAccountDetailsParams(
                                            accounts[i].primaryMsisdn, accounts[i].segment
                                        )
                                    ).fold({ accountInfo ->
                                        val status = accountInfo.statusDescription.extractStatus()
                                        currentUiModels[i].accountStatus = status

                                        when (status) {
                                            is Active, Disconnected -> {

                                                if (status is Disconnected) {
                                                    currentUiModels[i].apply {
                                                        isLoading = false
                                                        usageFetched = true
                                                    }
                                                    return@async
                                                }

                                                when (accounts[i].segment) {
                                                    AccountSegment.Mobile -> {

                                                        // Check for platinum account
                                                        accountDomainManager.getMobilePlanDetails(
                                                            GetPlanDetailsParams(
                                                                accounts[i].primaryMsisdn,
                                                                AccountSegment.Mobile
                                                            )
                                                        ).onSuccess { planDetails ->
                                                            if (planDetails.plan.planType == PLATINUM) {
                                                                currentUiModels[i].setPlatinumAccountFlags()
                                                                tryItemUiUpdate(i)
                                                                return@async
                                                            }
                                                        }

                                                        async {
                                                            accountDomainManager.getPostpaidPromoSubscriptionUsage(
                                                                GetPostpaidPromoSubscriptionUsageRequest(
                                                                    accounts[i].primaryMsisdn
                                                                )
                                                            ).fold({ subscriptionUsage ->
                                                                currentUiModels[i].applySubscriptionUsage(
                                                                    subscriptionUsage
                                                                )

                                                                accountDomainManager.getPostpaidActivePromoSubscription(
                                                                    GetPostpaidActivePromoSubscriptionRequest(
                                                                        accounts[i].primaryMsisdn
                                                                    )
                                                                ).fold({ activeSubscription ->
                                                                    currentUiModels[i].applyPostpaidActiveSubscription(
                                                                        activeSubscription
                                                                    )
                                                                    tryItemUiUpdate(i)
                                                                    dLog("Fetching postpaid active promo subscription success")
                                                                }, {
                                                                    currentUiModels[i].apply {
                                                                        isLoading = false
                                                                        usageFetched = true
                                                                        error.errorActivePromoSubscription =
                                                                            true
                                                                    }
                                                                    tryItemUiUpdate(i)
                                                                    dLog("Fetching postpaid active promo subscription failure")
                                                                })
                                                                dLog("Fetching postpaid promo subscription usage success")
                                                            }, {
                                                                currentUiModels[i].apply {
                                                                    isLoading = false
                                                                    usageFetched = true
                                                                    error.errorPromoSubscriptionUsage =
                                                                        true
                                                                }
                                                                tryItemUiUpdate(i)
                                                                dLog("Fetching postpaid promo subscription usage failure")
                                                            })
                                                        }
                                                    }
                                                    // Data usage fetching temporary commented for postpaid broadband
                                                    AccountSegment.Broadband -> {
                                                        currentUiModels[i].apply {
                                                            isLoading = false
                                                            usageFetched = true
                                                        }
//                                                        async {
//                                                            accountDomainManager.getAccountAccessType(
//                                                                GetAccountAccessTypeParams(accounts[i].primaryMsisdn)
//                                                            ).fold({ accessTypeResult ->
//                                                                if (accessTypeResult.isUnlimitedData()) {
//                                                                    currentUiModels[i].setUnlimitedDataFlags()
//                                                                } else {
//                                                                    accountDomainManager.getUsageConsumptionReports(
//                                                                        GetUsageConsumptionReportsParams(
//                                                                            accounts[i].segment,
//                                                                            accounts[i].brand,
//                                                                            accounts[i].primaryMsisdn
//                                                                        )
//                                                                    )
//                                                                        .fold({ usageConsumptionResult ->
//                                                                            currentUiModels[i].applyUsageConsumptionResult(
//                                                                                usageConsumptionResult
//                                                                            )
//                                                                            tryItemUiUpdate(i)
//                                                                            dLog("Fetching usage consumption reports success")
//                                                                        }, {
//                                                                            currentUiModels[i].apply {
//                                                                                isLoading = false
//                                                                                usageFetched = true
//                                                                                error.errorAccessType =
//                                                                                    true
//                                                                            }
//                                                                            tryItemUiUpdate(i)
//                                                                            dLog("Fetching usage consumption reports failure")
//                                                                        })
//                                                                }
//                                                                dLog("Fetching access type success")
//                                                            }, {
//                                                                currentUiModels[i].apply {
//                                                                    isLoading = false
//                                                                    usageFetched = true
//                                                                    error.errorAccessType = true
//                                                                }
//                                                                tryItemUiUpdate(i)
//                                                                dLog("Fetching access type failure")
//                                                            })
//                                                        }
                                                    }
                                                }
                                            }
                                            is Inactive -> {
                                                currentUiModels[i].apply {
                                                    isLoading = false
                                                    balanceFetched = true
                                                    balance = null
                                                    postpaidPaymentStatus = null
                                                    usageFetched = true
                                                    giftFetched = true
                                                }
                                                tryItemUiUpdate(i)
                                            }
                                        }
                                        dLog("Fetching account details success")
                                    }, {
                                        currentUiModels[i].apply {
                                            isLoading = false
                                            error.errorAccountDetails = true
                                            balanceFetched = true
                                            balance = null
                                            postpaidPaymentStatus = null
                                            usageFetched = true
                                            giftFetched = true
                                        }
                                        tryItemUiUpdate(i)
                                        dLog("Fetching account details failure")
                                    })
                                }
                            } else {
                                async {
                                    accountDomainManager.getPrepaidPromoSubscriptionUsage(
                                        GetPrepaidPromoSubscriptionUsageRequest(accounts[i].primaryMsisdn)
                                    ).fold(
                                        { subscriptionUsage ->
                                            currentUiModels[i].applySubscriptionUsage(
                                                subscriptionUsage
                                            )

                                            groupDomainManager.retrieveGroupsAccountDetails(
                                                AccountDetailsGroupsParams(
                                                    accounts[i].primaryMsisdn,
                                                    accounts[i].accountAlias
                                                )
                                            ).first().fold(
                                                {
                                                    it?.groups?.let { groups ->
                                                        for (group in groups) {
                                                            currentUiModels[i].applyGroupData(
                                                                group.total,
                                                                group.left
                                                            )
                                                        }
                                                    }
                                                    dLog("Fetching account groups success")
                                                }, {
                                                    currentUiModels[i].isLoading = false
                                                    currentUiModels[i].error.errorGroupUsage = true
                                                    currentUiModels[i].usageFetched = true
                                                    tryItemUiUpdate(i)
                                                    dLog("Fetching account groups failure")
                                                }
                                            )

                                            accountDomainManager.getPrepaidPromoActiveSubscription(
                                                GetPrepaidPromoActiveSubscriptionRequest(accounts[i].primaryMsisdn)
                                            ).fold(
                                                { activeSubscription ->
                                                    currentUiModels[i].applyActiveSubscription(
                                                        activeSubscription
                                                    )
                                                    currentUiModels[i].usageFetched = true
                                                    tryItemUiUpdate(i)
                                                    dLog("Fetching prepaid promo active subscription success")
                                                }, {
                                                    currentUiModels[i].isLoading = false
                                                    currentUiModels[i].error.errorActivePromoSubscription =
                                                        true
                                                    currentUiModels[i].usageFetched = true
                                                    tryItemUiUpdate(i)
                                                    dLog("Fetching prepaid promo active subscription failure")
                                                }
                                            )
                                            dLog("Fetching prepaid promo subscriber usage success")
                                        }, {
                                            currentUiModels[i].isLoading = false
                                            currentUiModels[i].error.errorActivePromoSubscription =
                                                false
                                            currentUiModels[i].usageFetched = true
                                            tryItemUiUpdate(i)
                                            dLog("Fetching prepaid promo subscriber usage failure")
                                        }
                                    )
                                }

                                async {
                                    accountDomainManager.inquirePrepaidBalance(accounts[i].primaryMsisdn)
                                        .fold({
                                            currentUiModels[i].balance = it.balance
                                            currentUiModels[i].balanceFetched = true
                                            tryItemUiUpdate(i)
                                            dLog("Fetching balance success")
                                        }, {
                                            currentUiModels[i].balanceFetched = true
                                            tryItemUiUpdate(i)
                                            dLog("Fetching balance failure")
                                        })
                                }
                            }
                            if (i == accounts.lastIndex) {
                                initAsyncCallsFinishedListener()
                            }
                        }
                    }, {
                        accountDomainManager.setAccountsLoadingState(AccountsLoadingState.Failure)
                        if (it is GetEnrolledAccountsError.UserHasNoEnrolledAccounts) {
                            currentUiModels.clear()
                            _accountsUpdateTrigger.postValue(
                                AccountsUpdateTrigger(
                                    updateAll = true,
                                    updateIndex = -1
                                )
                            )
                        }
                        _accountsStatus.postValue(
                            AccountsStatus(
                                loading = false,
                                error = it !is GetEnrolledAccountsError.UserHasNoEnrolledAccounts,
                                empty = it is GetEnrolledAccountsError.UserHasNoEnrolledAccounts
                            )
                        )
                        // Disable refreshing state
                        _isRefreshingData.postValue(false)
                    })
                }
        }
    }

    private fun initAsyncCallsFinishedListener() {
        viewModelScope.launch {
            accountBrandAsyncCalls.awaitAll()

            currentUiModels.mapNotNull {
                it.brand?.let { brand ->
                    PersistentBrandModel(
                        it.accountName,
                        it.primaryMsisdn,
                        brand
                    )
                }
            }.let { persistentBrands ->
                accountDomainManager.apply {
                    storeBrands(persistentBrands)
                    setAccountsLoadingState(AccountsLoadingState.Loaded)
                }
                accountBrandAsyncCalls.clear()
            }
        }
    }

    fun getCustomerNickname() = viewModelScope.launch {
        profileDomainManager.getUserNickname().collect {
            it.fold(
                { nickname ->
                    _nickname.value = nickname?.nonEmptyOrNull()?.let {
                        NicknameUiModel.Data(it)
                    } ?: NicknameUiModel.Empty
                    dLog("Profile fetching success")
                }, {
                    _nickname.value = NicknameUiModel.Error
                    dLog("Profile fetching failure")
                }
            )
        }
    }

    private fun tryItemUiUpdate(i: Int) {
        if (currentUiModels[i].isReadyForUiUpdate())
            _accountsUpdateTrigger.postValue(
                AccountsUpdateTrigger(
                    updateAll = false,
                    updateIndex = i
                )
            )
    }

    fun reloadItem(i: Int, msisdn: String, accountAlias: String) {
        viewModelScope.launch {

            currentUiModels[i].error = UsageError()
            currentUiModels[i].setLoadingFlags()
            currentUiModels[i].usageFetched = true
            tryItemUiUpdate(i)

            async {
                accountDomainManager.getPrepaidPromoSubscriptionUsage(
                    GetPrepaidPromoSubscriptionUsageRequest(msisdn.formattedForPhilippines())
                ).fold(
                    { subscriptionUsage ->
                        currentUiModels[i].applySubscriptionUsage(
                            subscriptionUsage
                        )

                        groupDomainManager.retrieveGroupsAccountDetails(
                            AccountDetailsGroupsParams(
                                msisdn.formattedForPhilippines(),
                                accountAlias
                            )
                        ).first().fold(
                            {
                                it?.groups?.let { groups ->
                                    for (group in groups) {
                                        currentUiModels[i].applyGroupData(
                                            group.total,
                                            group.left
                                        )
                                    }
                                }
                                dLog("Fetching account groups success")
                            },
                            { error ->
                                currentUiModels[i].isLoading = false
                                currentUiModels[i].error.errorGroupService =
                                    error !is AccountDetailsGroupsError.MobileNumberNotFound
                                currentUiModels[i].usageFetched = true
                                tryItemUiUpdate(i)
                                dLog("Fetching account groups failure")
                            }
                        )

                        accountDomainManager.getPrepaidPromoActiveSubscription(
                            GetPrepaidPromoActiveSubscriptionRequest(msisdn.formattedForPhilippines())
                        ).fold(
                            { activeSubscription ->
                                currentUiModels[i].applyActiveSubscription(
                                    activeSubscription
                                )
                                currentUiModels[i].usageFetched = true
                                tryItemUiUpdate(i)
                                dLog("Fetching prepaid promo active subscription success")
                            }, {
                                currentUiModels[i].isLoading = false
                                currentUiModels[i].error.errorActivePromoSubscription = true
                                currentUiModels[i].usageFetched = true
                                tryItemUiUpdate(i)
                                dLog("Fetching prepaid promo active subscription failure")
                            }
                        )
                        dLog("Fetching prepaid promo subscriber usage success")
                    }, {
                        currentUiModels[i].isLoading = false
                        currentUiModels[i].error.errorActivePromoSubscription = false
                        currentUiModels[i].usageFetched = true
                        tryItemUiUpdate(i)
                        dLog("Fetching prepaid promo subscriber usage failure")
                    }
                )
            }

            async {
                accountDomainManager.inquirePrepaidBalance(msisdn.formattedForPhilippines())
                    .fold({
                        currentUiModels[i].balance = it.balance
                        currentUiModels[i].balanceFetched = true
                        tryItemUiUpdate(i)
                        dLog("Fetching balance success")
                    }, {
                        currentUiModels[i].balanceFetched = true
                        tryItemUiUpdate(i)
                        dLog("Fetching balance failure")
                    })
            }

            async {
                accountDomainManager.getAccountBrand(
                    GetAccountBrandParams(
                        msisdn.formattedForPhilippines()
                    )
                ).onSuccess {
                    currentUiModels[i].brand = it.result.brand

                    personalizedCampaignUseCase.execute(it.result.brand).onSuccess { campaigns ->
                        accountDomainManager.getCustomerCampaignPromo(
                            msisdn,
                            currentUiModels[i].segment.toString(),
                            campaigns
                        ).onSuccess { list ->
                            currentUiModels[i].hasGift = list.isNotEmpty()
                        }
                    }
                }
                currentUiModels[i].giftFetched = true
                currentUiModels[i].brandFetched = true

                tryItemUiUpdate(i)
            }.let { asyncCall ->
                accountBrandAsyncCalls.add(asyncCall)
            }
        }
    }

    private fun showProfileBubble() {
        viewModelScope.launch {
            delay(BUBBLE_INITIAL_DELAY)
            _bubbleVisibilityState.value = true
            delay(BUBBLE_VISIBILITY_DURATION)
            _bubbleVisibilityState.value = false

            // Update bubble state in preferences
            sharedPreferences.edit().putBoolean(DASHBOARD_PROFILE_BUBBLE_SHOWN_KEY, true)
                .apply()
        }
    }

    fun setNewGreeting(greeting: String) {
        _greeting.value = greeting
        sharedPreferences.edit()
            .putString(DASHBOARD_GREETING, greeting)
            .putLong(DASHBOARD_GREETING_LAST_UPDATED_TIME, System.currentTimeMillis())
            .apply()
    }

    fun refreshAccounts(manualRefresh: Boolean = false) {
        viewModelScope.launch {
            _isRefreshingData.value = manualRefresh
            profileDomainManager.refreshEnrolledAccounts()
        }
    }

    fun removeAccount(usageModel: UsageUIModel) {
        handler.handleDialog(overlayAndDialogFactories.createRemoveInactiveAccountDialog {
            viewModelScope.launchWithLoadingOverlay(handler) {
                accountDomainManager.deleteEnrolledAccount(usageModel.enrolledAccount.accountAlias)
                    .fold({
                        // Remove account from database
                        profileDomainManager.deleteEnrolledAccount(usageModel.primaryMsisdn)

                        // Update UI
                        currentUiModels.indexOf(usageModel).let { removedIndex ->
                            currentUiModels.removeAt(removedIndex)
                            _accountRemovedEvent.value = OneTimeEvent(removedIndex)
                        }
                        dLog("Account removal success")
                    }, {
                        dLog("Account removal failure")
                        if (it is DeleteEnrolledAccountError.General)
                            handler.handleGeneralError(it.error)
                    })
            }
        })
    }

    fun checkIfBubbleAvailable(position: Int, showBubbleCallback: () -> Unit) {
        if (position in 0..currentUiModels.size && bubblesHelper.isBubbleAvailableToShow(
                currentUiModels[position].primaryMsisdn
            )
        )
            showBubbleCallback.invoke()
    }

    fun getSpinwheelUrl() {
        viewModelScope.launch {
            remoteConfigManager.getRushData()?.let { rushData ->
                rushDomainManager.getSpinwheelUrl(rushData)?.let {
                    _spinwheelUrlResult.postValue(OneTimeEvent(it))
                }
            }
        }
    }

    fun hideSpinwheel() {
        val currentDayAndMonth =
            Calendar.getInstance().time.toFormattedStringOrEmpty(GlobeDateFormat.HidingSpinwheel)
        sharedPreferences.edit().putString(SPINWHEEL_HIDE_DATE, currentDayAndMonth).apply()
        _shouldShowSpinwheel.postValue(false)
    }

    private fun isRefreshing() = _isRefreshingData.value == true

    /**
     * Method to compare fetched enrolled accounts with current UI models.
     * Order is ignored, comparing executes only by content.
     * */
    private fun List<EnrolledAccount>.equalToUiModels(): Boolean {
        if (size != currentUiModels.size) return false

        val accountsFromUiModels = currentUiModels.map { it.enrolledAccount }
        return !any { !accountsFromUiModels.contains(it) }
    }

    override val logTag = "DashboardViewModel"

}

sealed class EarnedTicketsResult {

    data class EarnedTicketsSuccessfully(
        val profileName: String,
        val numOfTickets: Int
    ) : EarnedTicketsResult()

    object EarnedTicketsUnsuccessfully : EarnedTicketsResult()
}

sealed class NicknameUiModel {
    object Empty : NicknameUiModel()
    object Error : NicknameUiModel()
    data class Data(val name: String) : NicknameUiModel()
}

data class AccountsStatus(
    var loading: Boolean,
    var error: Boolean,
    var empty: Boolean
)

data class AccountsUpdateTrigger(
    var updateAll: Boolean,
    var updateIndex: Int
)

private const val BUBBLE_INITIAL_DELAY = 500L
private const val BUBBLE_VISIBILITY_DURATION = 3000L
private const val GREETING_CHANGE_PERIOD = 10800000L
