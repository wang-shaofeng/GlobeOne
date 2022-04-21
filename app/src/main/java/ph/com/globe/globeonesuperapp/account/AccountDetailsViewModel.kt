/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.usecases.PersonalizedCampaignUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.billings.BillingsDomainManager
import ph.com.globe.domain.credit.CreditDomainManager
import ph.com.globe.domain.database.DatabaseDomainManager
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.rewards.RewardsDomainManager
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.account.DeleteEnrolledAccountError
import ph.com.globe.errors.account.ModifyEnrolledAccountError
import ph.com.globe.errors.account.PurchaseCampaignPromoError
import ph.com.globe.errors.credit.GetCreditInfoError
import ph.com.globe.errors.payment.GetGcashBalanceError
import ph.com.globe.errors.rewards.GetRewardPointsError
import ph.com.globe.errors.shop.GetAllOffersError
import ph.com.globe.globeonesuperapp.account.personalized_campaigns.PersonalizedCampaignsLoadingFragmentArgs
import ph.com.globe.globeonesuperapp.rewards.pos.EnrolledAccountWithPoints
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.AppConstants.PLATINUM
import ph.com.globe.globeonesuperapp.utils.balance.toFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.balance.toRewardsExpiringAmountFormat
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.*
import ph.com.globe.model.billings.domain_models.BillingsDetails
import ph.com.globe.model.billings.domain_models.getPostpaidPaymentStatus
import ph.com.globe.model.billings.network_models.createPostpaidBillingDetailsParams
import ph.com.globe.model.credit.GetCreditInfoParams
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isBroadband
import ph.com.globe.model.profile.domain_models.isPostpaidMobile
import ph.com.globe.model.shop.ContentSubscriptionUIModel
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.model.util.AccountStatus
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.fold
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import ph.com.globe.util.toDateOrNull
import javax.inject.Inject

@HiltViewModel
class AccountDetailsViewModel @Inject constructor(
    private val accountDomainManager: AccountDomainManager,
    private val rewardsDomainManager: RewardsDomainManager,
    private val creditDomainManager: CreditDomainManager,
    private val paymentDomainManager: PaymentDomainManager,
    private val profileDomainManager: ProfileDomainManager,
    private val shopDomainManager: ShopDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
    private val databaseDomainManager: DatabaseDomainManager,
    private val billingsDomainManager: BillingsDomainManager,
    private val personalizedCampaignUseCase: PersonalizedCampaignUseCase
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private var aliasUpdated = false
    var accountName = ""
    private val _accountAlias: MutableLiveData<String> = MutableLiveData()
    val accountAlias: LiveData<String> = _accountAlias

    var selectedEnrolledAccount: EnrolledAccount =
        EnrolledAccount(
            "",
            "",
            "",
            "",
            "",
            AccountBrandType.Prepaid,
            AccountSegment.Mobile,
            listOf(),
            false
        )
        set(value) {
            field = value
            _selectedEnrolledAccountLiveData.value = field
        }

    private val _selectedEnrolledAccountLiveData = MutableLiveData<EnrolledAccount>()
    val selectedEnrolledAccountLiveData =
        _selectedEnrolledAccountLiveData as LiveData<EnrolledAccount>

    private val _accountDetails = MutableLiveData<GetAccountDetailsResult>()
    val accountDetails: LiveData<GetAccountDetailsResult> = _accountDetails

    // region Brand
    val brandValue: AccountBrand? get() = _brandStatus.value.getBrandSafely()
    private val _brandStatus: MutableLiveData<BrandStatus> = MutableLiveData()
    val brandStatus: LiveData<BrandStatus> = _brandStatus

    private val _brandLoadedEvent: MutableLiveData<OneTimeEvent<Unit>> = MutableLiveData()
    val brandLoadedEvent: LiveData<OneTimeEvent<Unit>> = _brandLoadedEvent
    // endregion

    private val _postpaidBillStatus = MutableLiveData<BillStatus>()
    val postpaidBillStatus: LiveData<BillStatus> = _postpaidBillStatus

    private val _billingDetails = MutableLiveData<BillingsDetails>()
    val billingDetails: LiveData<BillingsDetails> = _billingDetails

    // region Balances
    private val _loadBalanceStatus: MutableLiveData<BalanceStatus> = MutableLiveData()
    val loadBalanceStatus: LiveData<BalanceStatus> = _loadBalanceStatus

    private val _rewardPointsStatus: MutableLiveData<BalanceStatus> = MutableLiveData()
    val rewardPointsStatus: LiveData<BalanceStatus> = _rewardPointsStatus

    private val _gCashBalanceStatus: MutableLiveData<BalanceStatus> = MutableLiveData()
    val gCashBalanceStatus: LiveData<BalanceStatus> = _gCashBalanceStatus

    private val _loanBalanceStatus: MutableLiveData<BalanceStatus> = MutableLiveData()
    val loanBalanceStatus: LiveData<BalanceStatus> = _loanBalanceStatus
    // endregion

    private val _accountStatus = MutableStateFlow<AccountStatus?>(null)
    val accountStatus: LiveData<AccountStatus?> = _accountStatus.asLiveData(Dispatchers.Default)

    private val _posAvailable = MutableStateFlow(true)
    val posAvailable = _posAvailable.combine(_accountStatus) { posAvailable, accountStatus ->
        posAvailable && accountStatus != AccountStatus.Inactive
    }.asLiveData(Dispatchers.Default)

    private val _accountDeleted: MutableLiveData<OneTimeEvent<Unit>> = MutableLiveData()
    val accountDeleted: LiveData<OneTimeEvent<Unit>> = _accountDeleted

    private val _callsUsages = MutableLiveData<List<AccountDetailsUsageUIModel>?>()
    val callsUsages: LiveData<List<AccountDetailsUsageUIModel>?> = _callsUsages

    private val _textUsages = MutableLiveData<List<AccountDetailsUsageUIModel>>()
    val textUsages: LiveData<List<AccountDetailsUsageUIModel>> = _textUsages

    private val _postpaidContentItems = MutableLiveData<List<ContentSubscriptionUIModel>>()
    val postpaidContentItems: LiveData<List<ContentSubscriptionUIModel>> = _postpaidContentItems

    private val _postpaidOfferDescription = MutableLiveData<String>()
    val postpaidOfferDescription: LiveData<String> = _postpaidOfferDescription

    private val _customerCampaignPromo: MutableLiveData<List<AvailableCampaignPromosModelWithBrand>> =
        MutableLiveData()
    val customerCampaignPromo: LiveData<List<AvailableCampaignPromosModelWithBrand>> =
        _customerCampaignPromo

    private val _pullFreebieSuccessfulStatus: MutableLiveData<OneTimeEvent<Boolean>> =
        MutableLiveData()
    val pullFreebieSuccessfulStatus: LiveData<OneTimeEvent<Boolean>> = _pullFreebieSuccessfulStatus

    private val _subscriptionsDataLoadedEvent = MutableLiveData<OneTimeEvent<Int>>()
    val subscriptionsDataLoadedEvent: LiveData<OneTimeEvent<Int>> = _subscriptionsDataLoadedEvent

    private val _exclusivePromoModels = MutableSharedFlow<List<AvailableCampaignPromosModel>>(1)
    private val _offers =
        shopDomainManager.getAllOffers(false).combine(_exclusivePromoModels) { list, models ->
            list.mapNotNull { shopItem ->
                models.find { shopItem.nonChargePromoId == it.skuId1 || shopItem.chargePromoId == it.skuId1 }
                    ?.let {
                        Pair(shopItem, it)
                    }
            }
        }

    val offers = _offers.asLiveData(Dispatchers.Default)

    private val _openPOS = MutableLiveData<OneTimeEvent<EnrolledAccountWithPoints>>()
    val openPOS: LiveData<OneTimeEvent<EnrolledAccountWithPoints>> = _openPOS

    private val _isRefreshingData: MutableLiveData<Boolean> = MutableLiveData()
    val isRefreshingData: LiveData<Boolean> = _isRefreshingData

    private val _dataIsChanged: MutableLiveData<OneTimeEvent<Boolean>> = MutableLiveData()
    val dataIsChanged: LiveData<OneTimeEvent<Boolean>> = _dataIsChanged

    fun setAccountAlias(alias: String?) {
        if (!aliasUpdated)
            _accountAlias.value = alias
        accountName = alias ?: ""
    }

    private suspend fun getBrand(msisdn: String, segment: AccountSegment) {
        // Set loading state
        _brandStatus.value = BrandStatus.Loading

        accountDomainManager.getAccountBrand(GetAccountBrandParams(msisdn))
            .fold({ response ->
                response.result.brand.let { brand ->

                    fun onBrandLoaded(value: UIAccountBrand) {
                        _brandStatus.value = BrandStatus.Success(value)
                        _brandLoadedEvent.value = OneTimeEvent(Unit)
                    }

                    fun handlePlatinumPlanType(planType: String?) {
                        onBrandLoaded(
                            if (planType == PLATINUM) UIAccountBrand.PlatinumBrand(brand) else UIAccountBrand.RegularBrand(
                                brand
                            )
                        )
                    }

                    if (brand == AccountBrand.GhpPostpaid) {
                        val params = GetPlanDetailsParams(msisdn, segment)
                        when (segment) {
                            AccountSegment.Mobile -> {
                                accountDomainManager.getMobilePlanDetails(params).fold({
                                    handlePlatinumPlanType(it.plan.planType)
                                }, {
                                    onBrandLoaded(UIAccountBrand.RegularBrand(brand))
                                })
                            }
                            AccountSegment.Broadband -> {
                                accountDomainManager.getBroadbandPlanDetails(params).fold({
                                    handlePlatinumPlanType(it.plan.planType)
                                }, {
                                    onBrandLoaded(UIAccountBrand.RegularBrand(brand))
                                })
                            }
                        }
                    } else {
                        onBrandLoaded(UIAccountBrand.RegularBrand(brand))
                    }
                }
                dLog("Brand fetching success")
            }, {
                _brandStatus.value = BrandStatus.Empty
                dLog("Brand fetching failure")
            })
    }

    private suspend fun getPrepaidBalance(msisdn: String) {
        // Set loading state
        _loadBalanceStatus.value = BalanceStatus.Loading

        accountDomainManager.inquirePrepaidBalance(msisdn).fold({ balanceResponse ->
            _loadBalanceStatus.value = BalanceStatus.Success(
                balanceResponse.balance,
                balanceResponse.expiryDate,
                expiringAmount = balanceResponse.balance.toFormattedDisplayBalance()
            )
            dLog("Prepaid balance fetching success")
        }, {
            _loadBalanceStatus.value = BalanceStatus.Empty()
            dLog("Prepaid balance fetching failure")
        })
    }

    private suspend fun getRewardPoints(msisdn: String, segment: String) {
        // Set loading state
        _rewardPointsStatus.value = BalanceStatus.Loading

        rewardsDomainManager.getRewardPoints(msisdn, segment).fold({ rewardPoints ->
            _rewardPointsStatus.value = BalanceStatus.Success(
                rewardPoints.total,
                rewardPoints.expirationDate,
                expiringAmount = rewardPoints.expiringAmount.toRewardsExpiringAmountFormat()
            )
            dLog("Reward points fetching success")
        }, {
            _rewardPointsStatus.value = BalanceStatus.Empty()
            _posAvailable.value = it !is GetRewardPointsError.SubscriberAccountIsNotRegistered
            dLog("Reward points fetching failure")
        })
    }

    private suspend fun getGCashBalance(msisdn: String) {
        // Set loading state
        _gCashBalanceStatus.value = BalanceStatus.Loading

        paymentDomainManager.getGCashBalance(msisdn).fold({
            _gCashBalanceStatus.value = BalanceStatus.Success(
                it.availableAmount.amount
            )
            dLog("GCash balance fetching success")
        }, {
            _gCashBalanceStatus.value = BalanceStatus.Empty(
                linkGCash = it is GetGcashBalanceError.NoGCashAccount || it is GetGcashBalanceError.GCashNotLinked
            )
            dLog("GCash balance fetching failure")
        })
    }

    private suspend fun getLoanedCredit(msisdn: String) {
        // Set loading state
        _loanBalanceStatus.value = BalanceStatus.Loading

        creditDomainManager.getCreditInfo(GetCreditInfoParams(null, msisdn)).fold({ response ->
            _loanBalanceStatus.value =
                response.result.loanedAmount.toFloatOrNull()?.let { balance ->
                    BalanceStatus.Success(
                        balance
                    )
                } ?: BalanceStatus.Empty()
            dLog("Loaned credit fetching success")
        }, {
            _loanBalanceStatus.value =
                if (it is GetCreditInfoError.NoLoan) {
                    BalanceStatus.Success(0f)
                } else {
                    BalanceStatus.Empty()
                }
            dLog("Loaned credit fetching failure")
        })
    }

    private suspend fun getPersonalizedCampaignPromo(
        msisdn: String,
        brand: AccountBrand,
        segment: AccountSegment
    ) = personalizedCampaignUseCase.execute(brand).onSuccess { campaigns ->

        accountDomainManager.getCustomerCampaignPromo(msisdn, segment.toString(), campaigns)
            .onSuccess { promos ->
                val finalList = mutableListOf<AvailableCampaignPromosModel>()

                promos.forEach { item ->
                    val selectedCampConf =
                        campaigns.firstOrNull { config -> config.campaign_id == item.channel }
                    selectedCampConf?.let { selectedConfig ->
                        finalList.add(
                            item.copy(
                                promoType = updateCampaignFlow(selectedConfig.campaign_flow),
                                bannerUrl = selectedConfig.banner_URL,
                                buttonLabel = selectedConfig.primaryCTA,
                                availMode = selectedConfig.avail_mode.toInt()
                            )
                        )
                    }
                }

                _customerCampaignPromo.value =
                    finalList.map { AvailableCampaignPromosModelWithBrand(it, brand) }
            }
    }

    private fun updateCampaignFlow(type: String): AvailableCampaignPromosModel.PersonalizedCampaignsPromoType {
        return when (type) {
            CF_EXCLUSIVE_OFFERS -> AvailableCampaignPromosModel.PersonalizedCampaignsPromoType.EXCLUSIVE_OFFERS
            CF_FREEBIE_OR_SURPRISE -> AvailableCampaignPromosModel.PersonalizedCampaignsPromoType.FREEBIE_OR_SURPRISE
            CF_SIM_SAMPLER -> AvailableCampaignPromosModel.PersonalizedCampaignsPromoType.SIM_SAMPLER
            else -> AvailableCampaignPromosModel.PersonalizedCampaignsPromoType.NONE
        }
    }

    private suspend fun retrieveSubscriberUsage(msisdn: String) =
        accountDomainManager.getPrepaidPromoActiveSubscription(
            GetPrepaidPromoActiveSubscriptionRequest(msisdn.formattedForPhilippines())
        ).fold({ result ->
            result.activePromoSubscriptions?.callUsage?.let {
                _callsUsages.postValue(it.map { it.toAccountDetailsUsagesModel() }
                    .filter { it.bucketId != DEFA_BUCKET_ID })
            } ?: kotlin.run {
                _callsUsages.postValue(emptyList())
            }
            result.activePromoSubscriptions?.textUsage?.let {
                _textUsages.postValue(it.map { it.toAccountDetailsUsagesModel() }
                    .filter { it.bucketId != DEFA_BUCKET_ID })
            } ?: kotlin.run {
                _textUsages.postValue(emptyList())
            }
            dLog("Fetching subscriber usage success")
        }, {
            dLog("Fetching subscriber usage failure")
        })

    private suspend fun getAccountDetails(
        msisdn: String,
        segment: AccountSegment,
        failureCallback: (() -> Unit)?
    ) =
        accountDomainManager.getAccountDetails(GetAccountDetailsParams(msisdn, segment)).fold(
            { result ->
                _accountDetails.value = result
                _accountStatus.value = result.statusDescription.extractStatus()
                dLog("Fetching account details success")
            }, {
                failureCallback?.let { callback ->
                    handler.handleDialog(
                        overlayAndDialogFactories.createAccountDetailsFailedDialog(
                            callback
                        )
                    )
                }

                dLog("Fetching account details failure")
            }
        )

    private suspend fun getPostpaidActivePromoSubscriptions(msisdn: String) =
        accountDomainManager.getPostpaidActivePromoSubscription(
            GetPostpaidActivePromoSubscriptionRequest(msisdn)
        ).fold({ response ->
            _postpaidContentItems.value = response.getContentSubscriptions()
            _postpaidOfferDescription.value = response.getOfferDescription()
            _callsUsages.value = response.getPlanUsages()
            dLog("Fetching postpaid active promo subscriptions success")
        }, {
            _postpaidContentItems.value = emptyList()
            _callsUsages.value = emptyList()
            dLog("Fetching postpaid active promo subscriptions failure")
        })

    fun fetchData(accountDetailsFailureCallback: (() -> Unit)? = null) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                with(selectedEnrolledAccount) {

                    async {
                        getBrand(primaryMsisdn, segment)

                        _brandStatus.value?.let { status ->
                            if (status is BrandStatus.Success) {
                                getPersonalizedCampaignPromo(
                                    selectedEnrolledAccount.primaryMsisdn,
                                    status.uiAccountBrand.brand,
                                    selectedEnrolledAccount.segment
                                )
                            }
                        }
                    }

                    async {
                        getRewardPoints(
                            primaryMsisdn, segment.toString()
                        )
                    }

                    if (brandType == AccountBrandType.Prepaid) {
                        // Prepaid specific calls
                        async { getPrepaidBalance(primaryMsisdn) }
                        async { getLoanedCredit(primaryMsisdn) }
                        async { retrieveSubscriberUsage(primaryMsisdn) }
                    } else {
                        // Postpaid specific calls
                        async {
                            getAccountDetails(
                                primaryMsisdn,
                                segment,
                                accountDetailsFailureCallback
                            )
                        }

                        async { getBillingDetails() }

                        if (isPostpaidMobile()) {
                            async { getPostpaidActivePromoSubscriptions(primaryMsisdn) }
                        }
                    }

                    // MobileNumber specific calls
                    if (primaryMsisdn.isMobileNumber()) {
                        async { getGCashBalance(primaryMsisdn) }
                    }
                }
            }

            // Disable refreshing state
            _isRefreshingData.value = false
        }
    }

    fun modifyAccount(aliasNew: String) = viewModelScope.launchWithLoadingOverlay(handler) {
        accountAlias.value?.let { oldAlias ->
            accountDomainManager.modifyEnrolledAccount(
                oldAlias,
                ModifyEnrolledAccountRequest(aliasNew)
            ).fold({
                _accountAlias.postValue(aliasNew)
                aliasUpdated = true
                accountName = aliasNew
                profileDomainManager.invalidateEnrolledAccounts()
                _dataIsChanged.setOneTimeEvent(true)
                dLog("Account updating success")
            }, {
                dLog("Account updating failure")
                if (it is ModifyEnrolledAccountError.General)
                    handler.handleGeneralError(it.error)
            })
        }
    }

    fun removeAccount(inactiveAccount: Boolean = false) {
        val removeAccountJob = {
            viewModelScope.launchWithLoadingOverlay(handler) {
                accountAlias.value?.let { alias ->
                    accountDomainManager.deleteEnrolledAccount(alias).fold({
                        databaseDomainManager.clearAllData()
                        _accountDeleted.postValue(OneTimeEvent(Unit))
                        dLog("Account deleting success")
                    }, {
                        dLog("Account deleting failure")
                        if (it is DeleteEnrolledAccountError.General)
                            handler.handleGeneralError(it.error)
                    })
                }
            }
        }
        handler.handleDialog(
            if (inactiveAccount) {
                overlayAndDialogFactories.createRemoveInactiveAccountDialog {
                    removeAccountJob.invoke()
                }
            } else {
                overlayAndDialogFactories.createRemoveAccountDialog {
                    removeAccountJob.invoke()
                }
            }
        )
    }

    fun pullFreebie(campaignPromoArgs: PersonalizedCampaignsLoadingFragmentArgs) {
        viewModelScope.launch {
            with(campaignPromoArgs.availableCampaignPromosModel) {
                accountDomainManager.purchaseCampaignPromo(
                    channel,
                    mobileNumber,
                    customerParameter1,
                    maId,
                    availMode
                )
            }.onFailure {
                dLog("Pull freebie failure")
                _pullFreebieSuccessfulStatus.value = OneTimeEvent(false)
                if (it is PurchaseCampaignPromoError.General)
                    handler.handleGeneralError(it.error)
            }.onSuccess {
                dLog("Pull freebie success")
                fetchData()
                _pullFreebieSuccessfulStatus.value = OneTimeEvent(true)
            }
        }
    }

    fun onSubscriptionsDataLoaded(tabPosition: Int) {
        _subscriptionsDataLoadedEvent.value = OneTimeEvent(tabPosition)
    }

    fun fetchOffersData(availableCampaignPromosModels: List<AvailableCampaignPromosModel>) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            _exclusivePromoModels.emit(availableCampaignPromosModels)
            shopDomainManager.fetchOffers().onFailure {
                dLog("fetch offers failure")
                if (it is GetAllOffersError.General)
                    handler.handleGeneralError(it.error)
            }.onSuccess {
                dLog("fetch offers success")
            }
        }
    }

    fun tryToOpenPOS() {
        when (val balance = _rewardPointsStatus.value) {
            is BalanceStatus.Success -> {
                navigateToPOS(balance)
            }
            else -> viewModelScope.launch {
                // Try to fetch reward points one more time
                getRewardPoints(
                    selectedEnrolledAccount.primaryMsisdn,
                    selectedEnrolledAccount.segment.toString()
                )

                when (val fetchedBalance = _rewardPointsStatus.value) {
                    is BalanceStatus.Success -> {
                        navigateToPOS(fetchedBalance)
                    }
                    else -> handler.handleGeneralError(GeneralError.General)
                }
            }
        }
    }

    private fun navigateToPOS(rewardPoints: BalanceStatus.Success) {
        with(rewardPoints) {
            _openPOS.postOneTimeEvent(
                EnrolledAccountWithPoints(
                    selectedEnrolledAccount,
                    brandStatus.value.getBrandSafely(),
                    balance,
                    expiryDate,
                    expiringAmount
                )
            )
        }
    }

    private fun getBillingDetails() {
        _postpaidBillStatus.value = BillStatus.Loading
        viewModelScope.launch {
            billingsDomainManager.getBillingsDetails(
                createPostpaidBillingDetailsParams(selectedEnrolledAccount)
            ).fold({
                _billingDetails.value = it
                _postpaidBillStatus.value = BillStatus.Success(
                    it.getPostpaidPaymentStatus(),
                    false,
                    it.dueDate.toDateOrNull(),
                    it.outstandingBalance
                )
            }, {
                _postpaidBillStatus.value = BillStatus.Error
            })
        }
    }

    fun isICCBSAccount() =
        selectedEnrolledAccount.isBroadband() && accountDetails.value?.source == AppConstants.ICCBS

    override val logTag: String = "AccountDetailsViewModel"
}

data class AvailableCampaignPromosModelWithBrand(
    val availableCampaignPromosModel: AvailableCampaignPromosModel,
    val brand: AccountBrand
)

// Navigation requires Parcelable object for passing array, so this is helper class for that
@Parcelize
data class AvailableCampaignPromosModelParcelable(
    val maId: String,
    val promoMechanics: String,
    val customerParameter1: String,
    val channel: String,
    val mobileNumber: String,
    val skuId1: String?,
    val description: String,
    val offerValidDays: String?,
    val promoType: AvailableCampaignPromosModel.PersonalizedCampaignsPromoType?,
    val availMode: Int = 0
) : Parcelable {
    fun toModel() = AvailableCampaignPromosModel(
        maId,
        promoMechanics,
        customerParameter1,
        channel,
        mobileNumber,
        skuId1,
        description,
        offerValidDays,
        promoType,
        availMode = availMode
    )
}

fun AvailableCampaignPromosModel.toParcelableModel() =
    AvailableCampaignPromosModelParcelable(
        maId,
        promoMechanics,
        customerParameter1,
        channel,
        mobileNumber,
        skuId1,
        description,
        benefitsSkuDays,
        promoType,
        availMode
    )

// campaign flow types
private const val CF_EXCLUSIVE_OFFERS = "exclusive_offers"
private const val CF_FREEBIE_OR_SURPRISE = "freebie_or_surprise"
private const val CF_SIM_SAMPLER = "sim_sampler"
