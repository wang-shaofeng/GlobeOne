/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.itemdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.remote_config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.credit.CreditDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.errors.credit.GetCreditInfoError
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel.RaffleBannerResult.HideRaffleBanner
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel.RaffleBannerResult.ShowRaffleBanner
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel.RaffleBannerResult.ShowRaffleBanner.BannerType.BUY_HPW
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel.RaffleBannerResult.ShowRaffleBanner.BannerType.ELIGIBLE
import ph.com.globe.globeonesuperapp.shop.util.NumberValidation
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.withLoadingOverlay
import ph.com.globe.model.auth.OtpType
import ph.com.globe.model.auth.SendOtpParams
import ph.com.globe.model.credit.GetCreditInfoParams
import ph.com.globe.model.credit.LoanPromoParams
import ph.com.globe.model.credit.LoanPromoRequest
import ph.com.globe.model.shop.domain_models.*
import ph.com.globe.model.util.brand.*
import ph.com.globe.util.fold
import ph.com.globe.util.toDateOrNull
import javax.inject.Inject

@HiltViewModel
class ShopItemDetailsViewModel @Inject constructor(
    shopDomainManager: ShopDomainManager,
    private val authDomainManager: AuthDomainManager,
    private val creditDomainManager: CreditDomainManager,
    private val remoteConfigManager: RemoteConfigManager,
    private val profileDomainManager: ProfileDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _loanProcessResult = MutableLiveData<OneTimeEvent<ProcessLoanResult>>()
    val loanProcessResult: LiveData<OneTimeEvent<ProcessLoanResult>> = _loanProcessResult

    private val _subscribeResult = MutableLiveData<OneTimeEvent<SubscribeResult>>()
    val subscribeResult: LiveData<OneTimeEvent<SubscribeResult>> = _subscribeResult

    private val _phoneNumberLoanValidityResult = MutableLiveData<OneTimeEvent<GetLoanInfoResult>>()
    val phoneNumberLoanValidityResult: LiveData<OneTimeEvent<GetLoanInfoResult>> =
        _phoneNumberLoanValidityResult

    private val _showHideRaffleBanner = MutableLiveData<OneTimeEvent<RaffleBannerResult>>()
    val showHideRaffleBanner: LiveData<OneTimeEvent<RaffleBannerResult>> = _showHideRaffleBanner

    private val allOffersList = shopDomainManager.getAllOffers()

    private val boosterIds = MutableSharedFlow<List<String>>(1)
    private lateinit var boosters: List<BoosterItem>
    private val _boostersLiveData = MutableLiveData<List<BoosterItem>>()
    val boostersLiveData: LiveData<List<BoosterItem>> = _boostersLiveData

    private var numberOfSelectedBoosters = 0
    private var boostersPriceSum = 0
    private val _boostersPriceSumLiveData = MutableLiveData<OneTimeEvent<Int>>()
    val boostersPriceSumLiveData: LiveData<OneTimeEvent<Int>> = _boostersPriceSumLiveData

    private val boostersApplied = mutableListOf<BoosterItem>()

    private lateinit var singleSelectFreebies: List<FreebieSingleSelectItemUiModel>
    private val _singleSelectFreebiesLiveData =
        MutableLiveData<List<FreebieSingleSelectItemUiModel>>()
    val singleSelectFreebiesLiveData: LiveData<List<FreebieSingleSelectItemUiModel>> =
        _singleSelectFreebiesLiveData

    private var freebieSelected = true
    private var selectedFreebieWithChargingParam = ""
    private var selectedFreebieWithoutChargingParam = ""
    private var selectedFreebieNoneChargeId = ""
    private var selectedFreebieKeyword = ""
    private var freebieType = ""
    private var freebieName = ""

    private var mobileNumberNotEmpty = false
    private val _enableButton = MutableLiveData(OneTimeEvent(false))
    val enableButton: LiveData<OneTimeEvent<Boolean>> = _enableButton

    private lateinit var loanPromoRequest: LoanPromoRequest
    private lateinit var price: String
    private lateinit var fee: String
    private lateinit var loanName: String
    private var validity: Validity? = null

    private var numberValidation: NumberValidation? = null
    private var mobileNumberBrandValid = false

    init {
        viewModelScope.launch {
            allOffersList.findBoosters(boosterIds).collect {
                _boostersLiveData.value = it
            }
        }
    }

    fun showHideRaffleBanner(isRafflePromo: Boolean) =
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val raffleInProgress = remoteConfigManager.getRafflesConfig()?.any {
                it.startDate.toDateOrNull()?.time?.let { it < currentTime } == true
                        && it.endDate.toDateOrNull()?.time?.let { currentTime < it } == true
            } == true

            if (isRafflePromo && raffleInProgress) {
                profileDomainManager.getEnrolledAccounts()
                    .withLoadingOverlay(handler)
                    .collect {
                        it.fold({ enrolledAccountsList ->
                            dLog("Fetched enrolled accounts.")
                            val bannerType =
                                if (enrolledAccountsList.any { it.segment == AccountSegment.Broadband }) ELIGIBLE else BUY_HPW
                            _showHideRaffleBanner.value = OneTimeEvent(ShowRaffleBanner(bannerType))
                        }, {
                            dLog("Failed to fetch enrolled accounts $it")
                            _showHideRaffleBanner.value = OneTimeEvent(HideRaffleBanner)
                        })
                    }
            } else {
                _showHideRaffleBanner.value = OneTimeEvent(HideRaffleBanner)
            }
        }

    fun resetSelection() {
        freebieSelected = true
        selectedFreebieNoneChargeId = ""
        selectedFreebieWithChargingParam = ""
        selectedFreebieWithoutChargingParam = ""
        selectedFreebieKeyword = ""
        freebieType = ""
        freebieName = ""
    }

    fun checkBrand(validation: NumberValidation, shopItem: ShopItem) {
        if (!shopItem.isBrandCorrect(validation.brand)) {
            _subscribeResult.value = OneTimeEvent(SubscribeResult.SubscribeNotPossible)
            mobileNumberBrandValid = false
        } else {
            if (shopItem.freebie?.items == null || ((selectedFreebieNoneChargeId.isNotEmpty() && selectedFreebieWithChargingParam.isNotEmpty()) || selectedFreebieKeyword.isNotEmpty()))
                _subscribeResult.value = OneTimeEvent(SubscribeResult.SubscribePossible)
            mobileNumberBrandValid = true
        }
        numberValidation = validation
    }

    fun hasFreebiesWithSingleSelect(freebie: FreebieItem) {
        freebieSelected = freebie.items.isNullOrEmpty()
        freebie.items?.let {
            this.singleSelectFreebies = it.map { FreebieSingleSelectItemUiModel(it, false) }
            _singleSelectFreebiesLiveData.value = this.singleSelectFreebies
        }
        _enableButton.value = OneTimeEvent(freebieSelected && mobileNumberNotEmpty)
    }

    fun selectFreebie(
        freebieTitle: String,
        freebieServiceChargeParam: String,
        freebieServiceNonChargeParam: String,
        freebieNoneChargeId: String,
        freebieKeyword: String,
        type: String
    ) {
        freebieSelected = true
        selectedFreebieNoneChargeId = freebieNoneChargeId
        selectedFreebieWithChargingParam = freebieServiceChargeParam
        selectedFreebieWithoutChargingParam = freebieServiceNonChargeParam
        selectedFreebieKeyword = freebieKeyword
        freebieName = freebieTitle
        freebieType = type
        singleSelectFreebies = singleSelectFreebies.map {
            it.copy(
                selected = it.freebieSingleSelectItem.title == freebieTitle
                        && it.freebieSingleSelectItem.serviceNoneChargeId == freebieNoneChargeId
                        && it.freebieSingleSelectItem.serviceChargeParam == freebieServiceChargeParam
                        && it.freebieSingleSelectItem.serviceNonChargeParam == freebieServiceNonChargeParam
                        && it.freebieSingleSelectItem.apiProvisioningKeyword == freebieKeyword
                        && it.freebieSingleSelectItem.type == type
            )
        }
        _singleSelectFreebiesLiveData.value = singleSelectFreebies

        _enableButton.value = OneTimeEvent(freebieSelected && mobileNumberBrandValid)
    }

    fun mobileNumberEditTextChange(mobileNumber: String) {
        mobileNumberNotEmpty = mobileNumber.isNotEmpty()
        _enableButton.value = OneTimeEvent(freebieSelected && mobileNumberNotEmpty)
    }

    fun clearBoosters() {
        boostersPriceSum = 0
        numberOfSelectedBoosters = 0
        boostersApplied.clear()
        if (this::boosters.isInitialized)
            boosters.forEach { it.selected = false;it.enabled = true }
        _boostersPriceSumLiveData.value = OneTimeEvent(boostersPriceSum)
    }

    fun toggleBooster(serviceId: String, price: String, selected: Boolean) {
        boosters.find { it.serviceId == serviceId }?.let {
            if (selected) {
                numberOfSelectedBoosters++
                boostersApplied.add(it)
                boostersPriceSum += price.toInt()
            } else {
                numberOfSelectedBoosters--
                boostersApplied.remove(it)
                boostersPriceSum -= price.toInt()
            }
        }

        boosters.forEach {
            it.selected =
                if (it.serviceId == serviceId) selected else it.selected
            it.enabled =
                if (numberOfSelectedBoosters == 3)
                    (if (it.serviceId == serviceId) selected
                    else it.selected)
                else true
        }

        _boostersLiveData.reemitValue()
        _boostersPriceSumLiveData.value = OneTimeEvent(boostersPriceSum)
    }

    fun filterBoosters(ids: List<String>) {
        viewModelScope.launch { boosterIds.emit(ids) }
    }

    private fun Flow<List<ShopItem>>.findBoosters(ids: Flow<List<String>>) =
        this.combine(ids) { shopItems, listOfIds ->
            val boosterItems = mutableListOf<BoosterItem>()

            for (id in listOfIds) {
                for (item in shopItems) {
                    if (item.sections.any { it?.id ?: "" == id }) {
                        boosterItems.add(item.toBoosterItem())
                    }
                }
            }

            boosters = boosterItems
            return@combine boosterItems
        }

    fun subscribe(
        shopItem: ShopItem,
        loggedIn: Boolean,
        isEnrolledAccount: Boolean = false,
        checkMobileNumberBrand: Boolean = true
    ) {
        if (checkMobileNumberBrand && !mobileNumberBrandValid) {
            _subscribeResult.value = OneTimeEvent(SubscribeResult.SubscribeNotPossible)
            return
        }

        if (shopItem.loanable) {
            loanPromoRequest =
                LoanPromoRequest(
                    shopItem.apiProvisioningKeyword,
                    "CXS-${System.currentTimeMillis()}",
                    numberValidation?.number ?: ""
                )
            this.price = shopItem.price
            this.fee = shopItem.fee
            this.loanName = shopItem.name
            this.validity = shopItem.validity
            if (loggedIn && isEnrolledAccount)
                _phoneNumberLoanValidityResult.value = OneTimeEvent(GetLoanInfoResult.LoanLoggedIn)
            else sendOtp(numberValidation?.number ?: "", numberValidation?.brandType)
        } else if (shopItem.isContent) {
            _subscribeResult.value =
                OneTimeEvent(SubscribeResult.SubscribeContentSuccess)
        } else {
            _subscribeResult.value =
                OneTimeEvent(
                    SubscribeResult.SubscribePromoSuccess(
                        boostersApplied = boostersApplied,
                        nonChargeId = selectedFreebieNoneChargeId,
                        chargeParam = selectedFreebieWithChargingParam,
                        nonChargeParam = selectedFreebieWithoutChargingParam,
                        apiProvisioningKeyword = selectedFreebieKeyword,
                        freebieType = freebieType,
                        freebieName = freebieName
                    )
                )
        }
    }

    private fun sendOtp(phoneNumber: String, brand: AccountBrandType?) =
        viewModelScope.launchWithLoadingOverlay(handler) {
            brand?.let {
                authDomainManager.sendOtp(
                    SendOtpParams(
                        type = OtpType.SMS,
                        msisdn = phoneNumber,
                        categoryIdentifier = listOf(OTP_KEY_GET_CREDIT_INFO, OTP_KEY_LOAN_PROMO),
                        brandType = brand,
                        segment = AccountSegment.Mobile
                    )
                ).fold({
                    dLog("Otp sent successfully.")
                    _phoneNumberLoanValidityResult.value =
                        OneTimeEvent(
                            GetLoanInfoResult.OtpSentSuccessfully(
                                phoneNumber,
                                brand,
                                it.referenceId
                            )
                        )
                }, {
                    dLog("Otp sending failed.")
                    println(it)
                })
            } ?: run {
                dLog("No brand.")
                _phoneNumberLoanValidityResult.value = OneTimeEvent(GetLoanInfoResult.NoBrand)
            }
        }

    fun processLoan(otpReferenceId: String?) = viewModelScope.launch {
        creditDomainManager.getCreditInfo(
            GetCreditInfoParams(otpReferenceId, numberValidation?.number ?: "")
        ).fold({
            dLog("User has loan")
            _loanProcessResult.value = OneTimeEvent(ProcessLoanResult.HasLoan)
        }, {
            dLog("User doesn't have loan")
            when (it) {
                GetCreditInfoError.NoLoan -> {
                    creditDomainManager.loanPromo(
                        LoanPromoParams(otpReferenceId, loanPromoRequest)
                    ).fold({
                        dLog("Successful loan")
                        _loanProcessResult.value =
                            OneTimeEvent(
                                ProcessLoanResult.SuccessfulLoan(
                                    numberValidation?.number ?: "",
                                    loanName,
                                    validity,
                                    price,
                                    fee
                                )
                            )
                    }, {
                        dLog("Unsuccessful loan")
                        _loanProcessResult.value =
                            OneTimeEvent(ProcessLoanResult.UnsuccessfulLoan)
                    })
                }
                else -> _loanProcessResult.value = OneTimeEvent(ProcessLoanResult.UnsuccessfulLoan)
            }
        })
    }

    fun cancelAddingAccount(yesCallback: () -> Unit, noCallback: () -> Unit) {
        handler.handleDialog(
            overlayAndDialogFactories.createAddAccountMobileNumberCancelDialog(
                yesCallback,
                noCallback
            )
        )
    }

    fun showNoBrandError(okCallback: () -> Unit) {
        handler.handleDialog(
            overlayAndDialogFactories.createNoBrandErrorDialog(okCallback)
        )
    }

    data class FreebieSingleSelectItemUiModel(
        val freebieSingleSelectItem: FreebieSingleSelectItem,
        var selected: Boolean
    )

    sealed class RaffleBannerResult {

        object HideRaffleBanner : RaffleBannerResult()

        data class ShowRaffleBanner(
            val bannerType: BannerType
        ) : RaffleBannerResult() {

            enum class BannerType {
                BUY_HPW, ELIGIBLE
            }
        }
    }

    sealed class SubscribeResult {
        object SubscribePossible : SubscribeResult()

        object SubscribeNotPossible : SubscribeResult()

        data class SubscribePromoSuccess(
            val boostersApplied: List<BoosterItem>,
            val nonChargeId: String,
            val chargeParam: String,
            val nonChargeParam: String,
            val apiProvisioningKeyword: String,
            val freebieType: String,
            val freebieName: String
        ) : SubscribeResult()

        object SubscribeContentSuccess : SubscribeResult()
    }

    sealed class ProcessLoanResult {

        data class SuccessfulLoan(
            val sentTo: String,
            val loanName: String,
            val validity: Validity?,
            val amount: String,
            val serviceFee: String
        ) : ProcessLoanResult()

        object HasLoan : ProcessLoanResult()

        object UnsuccessfulLoan : ProcessLoanResult()
    }

    sealed class GetLoanInfoResult {

        data class OtpSentSuccessfully(
            val phoneNumber: String,
            val brandType: AccountBrandType,
            val referenceId: String
        ) : GetLoanInfoResult()

        object LoanLoggedIn : GetLoanInfoResult()

        object NoBrand : GetLoanInfoResult()
    }

    override val logTag = "ShopPromoDetailsViewModel"
}
