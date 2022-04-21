/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.remote_config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import ph.com.globe.analytics.events.HIGHEST_TO_LOWEST
import ph.com.globe.analytics.events.LOWEST_TO_HIGHEST
import ph.com.globe.analytics.events.NO_EMAIL_STORED
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.rewards.RewardsDomainManager
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.domain.user_details.UserDetailsDomainManager
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.rewards.RedeemLoyaltyRewardsError
import ph.com.globe.errors.shop.GetPromoSubscriptionHistoryError
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.globeonesuperapp.rewards.RewardFilter.BudgetFilter.Companion.UNLIMITED
import ph.com.globe.globeonesuperapp.rewards.RewardFilter.TypeFilter.SubscriberType.*
import ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts.EnrolledAccountUiModel
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.setOneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.GetAccountBrandParams
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.model.feature_activation.FEATURE_ACTIVATION_DAC_NAME
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isPostpaidBroadband
import ph.com.globe.model.rewards.*
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryParams
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.fold
import ph.com.globe.util.toDateOrNull
import javax.inject.Inject

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val rewardsDomainManager: RewardsDomainManager,
    private val accountDomainManager: AccountDomainManager,
    private val profileDomainManager: ProfileDomainManager,
    private val authDomainManager: AuthDomainManager,
    private val shopDomainManager: ShopDomainManager,
    private val remoteConfigManager: RemoteConfigManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
    private val userDetailsDomainManager: UserDetailsDomainManager
) : BaseViewModel() {

    private val handler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _accountsCount = MutableLiveData(0)
    val accountsCount: LiveData<Int> = _accountsCount

    private val _enrolledAccount = MutableStateFlow<EnrolledAccountWithLoyaltyIdUiModel?>(null)
    val enrolledAccount = _enrolledAccount.asLiveData()

    private val _appliedFilters = MutableSharedFlow<List<RewardFilter>>(1)
    val appliedFilters = _appliedFilters.asLiveData()

    private val _dacEnabled = MutableLiveData(false)
    val dacEnabled: LiveData<Boolean> = _dacEnabled

    val displayRewards = with(rewardsDomainManager) {
        getAllRewards()
            .combine(getRandomFromEachCategory()) { all, randomRewards ->
                val campaignRewards = if (_isFreeGBCampaign)
                    all.filter { it.name.startsWith(FREE_GB_NAME) }
                else emptyList()

                val lastRandomizedItem = all.filter { it.category == RewardsCategory.RAFFLE }
                    .takeIf { it.isNotEmpty() }?.random()
                    ?: randomRewards.takeIf { it.isNotEmpty() }?.random()

                when {
                    campaignRewards.isEmpty() -> randomRewards
                    campaignRewards.size == 1 -> campaignRewards.plus(randomRewards.take(3))
                    campaignRewards.size == 4 ->
                        if (lastRandomizedItem != null) campaignRewards.plus(lastRandomizedItem)
                        else campaignRewards
                    campaignRewards.size >= 5 -> campaignRewards.take(5)
                    else -> campaignRewards.plus(randomRewards.take(5 - campaignRewards.size))
                }
            }
    }.asLiveData(Dispatchers.Default)

    val checkTheseOutCatalogRewards = with(rewardsDomainManager) {
        getAllRewards()
            .combine(_enrolledAccount.flatMapLatest {
                getRandomFromEachCategoryDependsOnPointsAndLoyaltyId(
                    it?.points ?: -1f,
                    it?.loyaltyProgramId ?: LoyaltyProgramId.ALL
                )
            }) { all, randomFromEachCategory ->

                val campaignRewards = if (_isFreeGBCampaign)
                    all.filter { it.name.startsWith(FREE_GB_NAME) }
                else emptyList()

                campaignRewards.takeIf { it.isNotEmpty() } ?: randomFromEachCategory
            }
    }.asLiveData(Dispatchers.Default)

    val threeFreeRandomRewards = _enrolledAccount.flatMapLatest {
        rewardsDomainManager.getFreeRandomRewards(
            3,
            it?.loyaltyProgramId ?: LoyaltyProgramId.ALL
        )
    }.asLiveData(Dispatchers.Default)

    private val _currentCategory = MutableStateFlow(RewardsCategory.NONE)

    private val _sortRewards = MutableStateFlow(SortType.SORT_BY_PRICE_ASC)
    val sortRewards = _sortRewards.asLiveData()

    private val _raffleInProgress = MutableStateFlow(false)

    val rewardsForCategory =
        combine(
            _currentCategory,
            _enrolledAccount
        ) { category, account ->
            category to account
        }.flatMapLatest { (category, account) ->
            rewardsDomainManager.getRewardsForCategoryDependsOnPointsAndLoyaltyProgramId(
                account?.points ?: -1f,
                account?.loyaltyProgramId ?: LoyaltyProgramId.ALL,
                category
            )
                .combine(_raffleInProgress) { list, _ ->
                    // GO-2462 won't filter the raffle
                    list
                }
                .sort(_sortRewards, category)
                .applyFilters(_appliedFilters)
        }.asLiveData(Dispatchers.Default)

    var budgetFilters: List<RewardFilter.BudgetFilter> = listOf()

    var subscriberTypeFilters: List<RewardFilter.TypeFilter> = listOf()

    var cachedAccountsForSelection: List<EnrolledAccountUiModel> = listOf()

    private val _redeemRewardsSuccessful =
        MutableLiveData<OneTimeEvent<Pair<RedeemLoyaltyRewardsResult, EnrolledAccountWithLoyaltyIdUiModel>>>()
    val redeemRewardsSuccessful =
        _redeemRewardsSuccessful as LiveData<OneTimeEvent<Pair<RedeemLoyaltyRewardsResult, EnrolledAccountWithLoyaltyIdUiModel>>>

    private val _redeemRewardsUnsuccessful = MutableLiveData<OneTimeEvent<Boolean>>()
    val redeemRewardsUnsuccessful = _redeemRewardsUnsuccessful as LiveData<OneTimeEvent<Boolean>>

    private val _accountValidation = MutableLiveData<AccountValidation>()
    val accountValidation: LiveData<AccountValidation> = _accountValidation

    private var _lastValidatedMsisdn: String = ""

    private var _isFreeGBCampaign: Boolean = false

    private val _rewardsCatalogStatus: MutableLiveData<RewardsCatalogStatus> = MutableLiveData()
    val rewardsCatalogStatus: LiveData<RewardsCatalogStatus> = _rewardsCatalogStatus

    private val _isRefreshingData: MutableLiveData<Boolean> = MutableLiveData()
    val isRefreshingData: LiveData<Boolean> = _isRefreshingData

    val encryptedUserEmail by lazy {
        userDetailsDomainManager.getEmail().fold({ email ->
            encryptData(email)
        }, {
            NO_EMAIL_STORED
        })
    }

    init {
        fetchRewardsCatalog()

        viewModelScope.launch {
            remoteConfigManager.getFeatureActivationConfig()?.let { activationConfig ->
                val dacRemoteConfigEnabled = activationConfig
                    .find { it.name == FEATURE_ACTIVATION_DAC_NAME }?.isEnabled ?: false

                _dacEnabled.value = dacRemoteConfigEnabled
            }
        }

        initFilters()
        getEnrolledAccountsCount()
        checkIfItIsRaffleInProgress()
    }

    private fun encryptData(data: String): String = userDetailsDomainManager.encryptData(data = data)

    fun fetchRewardsCatalog() {
        // Set loading state
        _rewardsCatalogStatus.value = RewardsCatalogStatus.Loading

        viewModelScope.launch {
            rewardsDomainManager.fetchRewardsCatalog().fold({
                _rewardsCatalogStatus.value = RewardsCatalogStatus.Success
                dLog("Fetching rewards catalog success")
            }, {
                _rewardsCatalogStatus.value = RewardsCatalogStatus.Error
                dLog("Fetching rewards catalog failure")
            })

            // Disable refreshing state
            _isRefreshingData.value = false
        }
    }

    fun getEnrolledAccountsCount() {
        viewModelScope.launch {
            profileDomainManager.getEnrolledAccounts()
                .collect {
                    it.fold({ enrolledAccounts ->
                        dLog("Fetched enrolled accounts.")
                        _accountsCount.value = enrolledAccounts.size
                    }, {
                        dLog("Failed to fetch enrolled accounts $it")
                        _accountsCount.value = 0
                    })
                }
        }
    }

    fun setCurrentTab(category: RewardsCategory) {
        viewModelScope.launch(Dispatchers.Default) { _currentCategory.emit(category) }
    }

    fun sortRewards(sortType: SortType) {
        viewModelScope.launch(Dispatchers.Default) {
            _sortRewards.emit(sortType)
        }
    }

    fun isLoggedIn(): Boolean = authDomainManager.getLoginStatus() != LoginStatus.NOT_LOGGED_IN

    fun setEnrolledAccount(account: EnrolledAccount) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            val (points: GetRewardPointsModel?, brand: AccountBrand?, layoutProgramId: LoyaltyProgramId?) = withContext(
                Dispatchers.IO
            ) {
                val pointsJob = async {
                    rewardsDomainManager.getRewardPoints(
                        account.primaryMsisdn,
                        account.segment.toString()
                    )
                        .successOrNull()
                }

                val brandJob = async {
                    accountDomainManager.getAccountBrand(GetAccountBrandParams(account.primaryMsisdn))
                        .successOrNull()
                }

                val rawBrand = brandJob.await()?.result?.brand

                val loyaltyProgramId = if (account.isPostpaidBroadband()) {
                    LoyaltyProgramId.GAH
                } else {
                    rawBrand?.let {
                        when (it) {
                            AccountBrand.GhpPrepaid -> LoyaltyProgramId.PREPAID
                            AccountBrand.Tm -> LoyaltyProgramId.TM
                            AccountBrand.Hpw -> LoyaltyProgramId.HPW
                            AccountBrand.GhpPostpaid -> LoyaltyProgramId.POSTPAID
                            else -> LoyaltyProgramId.ALL
                        }
                    }
                }

                Triple(pointsJob.await(), rawBrand, loyaltyProgramId)
            }
            if (points != null && layoutProgramId != null && brand != null) {
                _enrolledAccount.emit(
                    EnrolledAccountWithLoyaltyIdUiModel(
                        account,
                        brand,
                        account.segment,
                        points.total,
                        layoutProgramId,
                        points.expirationDate,
                        points.expiringAmount
                    )
                )
            } else {
                _enrolledAccount.emit(null)
                handler.handleGeneralError(GeneralError.General)
            }
        }
    }

    fun validateReward(rewardItem: RewardsCatalogItem) {
        _enrolledAccount.value?.let { account ->
            if (account.enrolledAccount.primaryMsisdn == _lastValidatedMsisdn)
                return

            // Save last validated number
            _lastValidatedMsisdn = account.enrolledAccount.primaryMsisdn

            // Check the loyalty program id
            if (account.loyaltyProgramId !in rewardItem.loyaltyProgramIds) {
                _accountValidation.value = AccountValidation.NotEligibleNumber
            } else if (rewardItem.name.startsWith(FREE_GB_NAME)) { //
                // Free GB campaign flow
                if (account.loyaltyProgramId == LoyaltyProgramId.GAH) {
                    _accountValidation.value = AccountValidation.BrandTypeGAH
                } else {
                    // Continue validation with subscriptions history API
                    getPromoSubscriptionHistory(account.enrolledAccount.primaryMsisdn)
                }
            } else {
                // Regular reward flow
                if (account.points < rewardItem.pointsCost.toInt()) {
                    _accountValidation.value = AccountValidation.NotEligibleNumber
                } else {
                    _accountValidation.value = AccountValidation.AvailableToRedeem
                }
            }
        }
    }

    private fun getPromoSubscriptionHistory(mobileNumber: String) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            shopDomainManager.getPromoSubscriptionHistory(
                GetPromoSubscriptionHistoryParams(mobileNumber)
            ).fold(
                { response ->
                    val freeRewardServiceIds = if (BuildConfig.FLAVOR_servers == "staging") {
                        listOf("10139", "10140", "10141", "10142", "10143")
                    } else {
                        listOf("8432", "8433", "8434", "8435", "8436")
                    }

                    val alreadyRedeemed = response.result.subscriptions
                        .map { it.serviceId }.any { serviceId ->
                            freeRewardServiceIds.contains(serviceId)
                        }

                    _accountValidation.value = if (alreadyRedeemed) {
                        AccountValidation.AlreadyRedeemed
                    } else {
                        AccountValidation.AvailableToRedeem
                    }
                },
                { error ->
                    when (error) {
                        is GetPromoSubscriptionHistoryError.NoSubscriptionsFound -> {
                            // User is able to redeem a free reward with empty subscription history
                            _accountValidation.value = AccountValidation.AvailableToRedeem
                        }
                        else -> {
                            _accountValidation.value = AccountValidation.Failure
                            handler.handleGeneralError(GeneralError.General)
                        }
                    }
                }
            )
        }
    }

    fun clearAccountValidation() {
        _accountValidation.value = AccountValidation.NoValidation
        _lastValidatedMsisdn = ""
    }

    private fun initFilters() {
        budgetFilters = listOf(
            RewardFilter.BudgetFilter(5, 10, "5-10 points", false, false),
            RewardFilter.BudgetFilter(11, 50, "11-50 points", false, false),
            RewardFilter.BudgetFilter(51, 100, "51-100 points", false, false),
            RewardFilter.BudgetFilter(101, 300, "101-300 points", false, false),
            RewardFilter.BudgetFilter(301, UNLIMITED, "301 points and above", false, false)
        )

        subscriberTypeFilters = listOf(
            RewardFilter.TypeFilter(
                GLOBE_PREPAID,
                "Globe Prepaid",
                false,
                false
            ),
            RewardFilter.TypeFilter(
                RewardFilter.TypeFilter.SubscriberType.TM,
                "TM",
                false,
                false
            ),
            RewardFilter.TypeFilter(
                HPW,
                "Home Prepaid WiFi",
                false,
                false
            ),
            RewardFilter.TypeFilter(
                HOME_POSTPAID,
                "Home Postpaid",
                false,
                false
            )
        )

        viewModelScope.launch {
            _appliedFilters.emit(emptyList())
        }
    }

    fun applySelectedFilters() {
        budgetFilters.forEach { it.applied = it.checked }
        subscriberTypeFilters.forEach { it.applied = it.checked }

        viewModelScope.launch {
            val filters = budgetFilters + subscriberTypeFilters

            val appliedFilters = filters.filter { it.applied }

            _appliedFilters.emit(appliedFilters)
        }
    }

    fun removeUnnecessarilyCheckFlag() {
        budgetFilters.forEach { it.checked = it.applied }
        subscriberTypeFilters.forEach { it.checked = it.applied }
    }

    fun redeemReward(rewardItem: RewardsCatalogItem) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            val (number, loyaltyId) = _enrolledAccount.first()
                ?.let { it.enrolledAccount.primaryMsisdn to it.loyaltyProgramId }
                ?: "" to LoyaltyProgramId.ALL
            rewardsDomainManager.redeemLoyaltyRewards(number, rewardItem, loyaltyId).fold({
                dLog("redeem Loyalty Rewards success")
                val newPoints = rewardsDomainManager.getRewardPoints(
                    msisdn = number,
                    segment = (_enrolledAccount.value?.segment ?: AccountSegment.Mobile).toString()
                )
                    .successOrNull()?.total
                val oldPoints = _enrolledAccount.first()?.points ?: 0f
                _enrolledAccount.emit(
                    _enrolledAccount.first()?.copy(
                        points = newPoints ?: (oldPoints + it.loyaltyPoints[0].toFloat())
                    )
                )
                _redeemRewardsSuccessful.setOneTimeEvent(it to _enrolledAccount.first() as EnrolledAccountWithLoyaltyIdUiModel)
            }, {
                dLog("redeem Loyalty Rewards failure")
                when (it) {
                    is RedeemLoyaltyRewardsError.General -> _redeemRewardsUnsuccessful.setOneTimeEvent(
                        true
                    )
                    else -> handler.handleDialog(
                        overlayAndDialogFactories.createRedeemUnsuccessfulDialog(R.string.you_may_have_reached_the_maximum_number)
                    )
                }
            })
        }
    }

    private fun checkIfItIsRaffleInProgress() = viewModelScope.launch {
        val currentTime = System.currentTimeMillis()
        val raffleInProgress = remoteConfigManager.getRafflesConfig()?.any {
            it.startDate.toDateOrNull()?.time?.let { it < currentTime } == true
                    && it.endDate.toDateOrNull()?.time?.let { currentTime < it } == true
        } == true
        _raffleInProgress.emit(raffleInProgress)
    }

    override val logTag = "RewardsViewModel"
}

sealed class RewardFilter(
    open val title: String,
    open var checked: Boolean,
    open var applied: Boolean
) {
    data class BudgetFilter(
        val min: Int,
        val max: Int,
        override val title: String,
        override var checked: Boolean,
        override var applied: Boolean
    ) : RewardFilter(title, checked, applied) {
        companion object {
            const val UNLIMITED = -1
        }
    }

    data class TypeFilter(
        val type: SubscriberType,
        override val title: String,
        override var checked: Boolean,
        override var applied: Boolean
    ) : RewardFilter(title, checked, applied) {

        enum class SubscriberType {
            GLOBE_PREPAID, TM, HOME_POSTPAID, HPW;
        }
    }
}

data class EnrolledAccountWithLoyaltyIdUiModel(
    val enrolledAccount: EnrolledAccount,
    val brand: AccountBrand,
    val segment: AccountSegment,
    val points: Float,
    val loyaltyProgramId: LoyaltyProgramId,
    val expirationDate: String?,
    val expirationAmount: String?
)

enum class SortType {
    SORT_BY_PRICE_ASC, SORT_BY_PRICE_DESC, NONE;

    companion object {
        fun toSortType(num: Int) = when (num) {
            0 -> SORT_BY_PRICE_ASC
            1 -> SORT_BY_PRICE_DESC
            else -> NONE
        }

        fun toAnalyticsTextValue(num: Int) = when (num) {
            0 -> LOWEST_TO_HIGHEST
            1 -> HIGHEST_TO_LOWEST
            else -> ""
        }
    }
}

sealed class AccountValidation {
    object AvailableToRedeem : AccountValidation()
    object NotEligibleNumber : AccountValidation()
    object AlreadyRedeemed : AccountValidation()
    object BrandTypeGAH : AccountValidation()
    object NoValidation : AccountValidation()
    object Failure : AccountValidation()
}

sealed class AccountBrandParcelable : Parcelable {
    @Parcelize
    object GhpPrepaid : AccountBrandParcelable(), Parcelable

    @Parcelize
    object GhpPostpaid : AccountBrandParcelable(), Parcelable

    @Parcelize
    object Hpw : AccountBrandParcelable(), Parcelable

    @Parcelize
    object Tm : AccountBrandParcelable(), Parcelable
}

fun AccountBrandParcelable.toAccountBrand(): AccountBrand =
    when (this) {
        is AccountBrandParcelable.GhpPostpaid -> AccountBrand.GhpPostpaid
        is AccountBrandParcelable.GhpPrepaid -> AccountBrand.GhpPrepaid
        is AccountBrandParcelable.Hpw -> AccountBrand.Hpw
        is AccountBrandParcelable.Tm -> AccountBrand.Tm
    }

fun AccountBrand.toAccountBrandParcelable(): AccountBrandParcelable =
    when (this) {
        is AccountBrand.GhpPostpaid -> AccountBrandParcelable.GhpPostpaid
        is AccountBrand.GhpPrepaid -> AccountBrandParcelable.GhpPrepaid
        is AccountBrand.Hpw -> AccountBrandParcelable.Hpw
        is AccountBrand.Tm -> AccountBrandParcelable.Tm
    }
