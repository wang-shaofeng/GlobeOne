/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.errors.payment.LinkingGCashError
import ph.com.globe.errors.payment.PaymentError
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.globeonesuperapp.profile.payment_methods.credit_card.CreditCardItem
import ph.com.globe.globeonesuperapp.profile.payment_methods.gcash.GCashItem
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.withLoadingOverlay
import ph.com.globe.model.payment.CreditCardModel
import ph.com.globe.model.payment.DeletePaymentMethodParams
import ph.com.globe.model.payment.LinkingGCashAccountParams
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isPostpaidBroadband
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val profileDomainManager: ProfileDomainManager,
    private val paymentDomainManager: PaymentDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _linkedCreditCardAccounts = MutableLiveData<List<CreditCardModel>>()
    val linkedCreditCardAccounts: LiveData<List<CreditCardModel>> = _linkedCreditCardAccounts

    private val _linkedGCashAccounts = MutableLiveData<List<EnrolledAccount>>()
    val linkedGCashAccounts: LiveData<List<EnrolledAccount>> = _linkedGCashAccounts

    private val _notLinkedGCashAccounts = MutableLiveData<List<EnrolledAccount>>()
    val notLinkedGCashAccounts: LiveData<List<EnrolledAccount>> = _notLinkedGCashAccounts

    private val _manageCreditCardsResult = MutableLiveData<ManageCreditCardsResult>()
    val manageCreditCardsResult: LiveData<ManageCreditCardsResult> = _manageCreditCardsResult

    private val _manageGCashLinkingResult =
        MutableLiveData<OneTimeEvent<Unit>>()
    val manageGCashLinkingResult: LiveData<OneTimeEvent<Unit>> =
        _manageGCashLinkingResult

    // Used to store the balance when fetched. Reduces the number of get GCash balance calls.
    private val fetchedGCashBalance = HashMap<String, Double>()

    val canRefreshMediatorLiveData: MediatorLiveData<Boolean> = MediatorLiveData()

    init {
        getGCashAccounts()
        getCreditCards()

        canRefreshMediatorLiveData.addSource(linkedCreditCardAccounts) {
            canRefreshMediatorLiveData.value = linkedGCashAccounts.value == null
        }
        canRefreshMediatorLiveData.addSource(linkedGCashAccounts) {
            canRefreshMediatorLiveData.value = linkedCreditCardAccounts.value == null
        }
    }

    fun initGCashPaymentMethod(accounts: List<EnrolledAccount>) {
        if (_linkedGCashAccounts.value == null) {
            _linkedGCashAccounts.value = accounts.filter { it.isGcashLinked }
            _notLinkedGCashAccounts.value = accounts.filter { !it.isGcashLinked }
        }
    }

    fun initCreditCardPaymentMethod(creditCards: List<CreditCardModel>) {
        if (_linkedCreditCardAccounts.value == null)
            _linkedCreditCardAccounts.value = creditCards
    }

    fun getGCashAccounts() {
        viewModelScope.launch {
            profileDomainManager.getEnrolledAccounts()
                .withLoadingOverlay(handler)
                .collect {
                    it.fold({ accounts ->
                        // with the new requirements we are filtering postpaid broadband accounts because they might not have mobile number
                        val accountsForConsideration = accounts.filter { !it.isPostpaidBroadband() }
                        _linkedGCashAccounts.value = accountsForConsideration.filter { it.isGcashLinked }
                        _notLinkedGCashAccounts.value = accountsForConsideration.filter { !it.isGcashLinked }
                        dLog("Fetched enrolled account.")
                    }, { error ->
                        if (error is GetEnrolledAccountsError.UserHasNoEnrolledAccounts) {
                            _linkedGCashAccounts.value = listOf()
                            _notLinkedGCashAccounts.value = listOf()
                        }
                        dLog("Failed to fetch enrolled accounts $it")
                        canRefreshMediatorLiveData.value = true
                        // TODO error handling
                    })
                }
        }
    }

    fun getCreditCards() {
        viewModelScope.launchWithLoadingOverlay(handler) {
            paymentDomainManager.getPaymentMethodUseCase().fold({ creditCards ->
                _linkedCreditCardAccounts.value = creditCards
                dLog("Fetched credit cards.")
            }, {
                dLog("Failed to fetch credit cards $it.")
                canRefreshMediatorLiveData.value = true
                // TODO handle error
            })
        }
    }

    fun removeCreditCard(creditCard: CreditCardItem) {
        handler.handleDialog(
            overlayAndDialogFactories.createRemoveCreditCardDialog {
                val cardToDelete =
                    linkedCreditCardAccounts.value?.find { it.cardSummary == creditCard.cardSummary }
                viewModelScope.launchWithLoadingOverlay(handler) {
                    cardToDelete?.let {
                        paymentDomainManager.deletePaymentMethodUseCase(
                            DeletePaymentMethodParams(it.cardReference)
                        ).fold(
                            {
                                dLog("Successfully removed a credit card.")
                                _linkedCreditCardAccounts.value =
                                    _linkedCreditCardAccounts.value?.minus(cardToDelete)
                                _manageCreditCardsResult.value =
                                    ManageCreditCardsResult.CreditCardDeleted
                            },
                            { error ->
                                dLog("Failed to remove a credit card.")
                                if (error is PaymentError.General)
                                    handler.handleGeneralError(error.error)
                            }
                        )
                    }
                }
            }
        )
    }

    fun removeGCash(gCashItem: GCashItem) {
        handler.handleDialog(
            overlayAndDialogFactories.createRemoveGCashAccountDialog {
                val gCashAccountToDelete =
                    linkedGCashAccounts.value?.find { it.mobileNumber == gCashItem.mobileNumber }
                viewModelScope.launchWithLoadingOverlay(handler) {
                    gCashAccountToDelete?.let {
                        paymentDomainManager.unlinkGCashAccountUseCase(
                            LinkingGCashAccountParams(accountAlias = gCashItem.accountName, "")
                        ).fold(
                            {
                                dLog("Successfully removed a gcash account.")
                                _linkedGCashAccounts.value =
                                    _linkedGCashAccounts.value?.minus(gCashAccountToDelete)
                                _notLinkedGCashAccounts.value =
                                    _notLinkedGCashAccounts.value?.plus(gCashAccountToDelete)
                                _manageGCashLinkingResult.value =
                                    OneTimeEvent(Unit)
                                profileDomainManager.invalidateEnrolledAccounts()
                            },
                            { error ->
                                dLog("Failed to remove a gcash account.")
                                if (error is LinkingGCashError.General)
                                    handler.handleGeneralError(error.error)
                            }
                        )
                    }
                }
            }
        )
    }

    fun getGCashBalanceAsLiveData(gCashItem: GCashItem): LiveData<Double> {
        val gCashBalanceLiveData = MutableLiveData<Double>()
        if (fetchedGCashBalance[gCashItem.accountName]?.let {
                gCashBalanceLiveData.value = it
            } == null) {
            viewModelScope.launchWithLoadingOverlay(handler) {
                paymentDomainManager.getGCashBalance(gCashItem.mobileNumber).fold({
                    dLog("Get GCash balance successful.")
                    gCashBalanceLiveData.value = it.availableAmount.amount.toDouble()
                    fetchedGCashBalance[gCashItem.accountName] =
                        it.availableAmount.amount.toDouble()
                }, {
                    dLog("Failed to get a GCash balance.")
                })
            }
        }
        return gCashBalanceLiveData
    }

    override val logTag: String = "PaymentMethodsViewModel"
}

sealed class ManageCreditCardsResult {
    object CreditCardDeleted : ManageCreditCardsResult()
}
