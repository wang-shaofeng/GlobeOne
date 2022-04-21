/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.usecases.PersonalizedCampaignUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ph.com.globe.analytics.events.NO_EMAIL_STORED
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.database.DatabaseDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.user_details.UserDetailsDomainManager
import ph.com.globe.errors.account.EnrollAccountsError
import ph.com.globe.errors.auth.LogoutError
import ph.com.globe.errors.profile.GetEnrolledAccountsError
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel.GetAccountsForMigrationResult.GetAccountsForMigrationSuccess
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel.GetAccountsForMigrationResult.NoAccounts
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.convertToPrefixNumberFormat
import ph.com.globe.globeonesuperapp.utils.shared_preferences.GET_STARTED_BUBBLE_SHOWN_KEY
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.*
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.fold
import ph.com.globe.util.onFailure
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class AddAccountMoreAccountsViewModel @Inject constructor(
    private val accountDomainManager: AccountDomainManager,
    private val profileDomainManager: ProfileDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
    private val sharedPreferences: SharedPreferences,
    private val authDomainManager: AuthDomainManager,
    private val databaseDomainManager: DatabaseDomainManager,
    private val personalizedCampaignUseCase: PersonalizedCampaignUseCase,
    private val userDetailsDomainManager: UserDetailsDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    var numberToPrefill: String? = null

    private val _getMigrationAccountsResult =
        MutableLiveData<OneTimeEvent<GetAccountsForMigrationResult>>()
    val getMigrationAccountsResult: LiveData<OneTimeEvent<GetAccountsForMigrationResult>> =
        _getMigrationAccountsResult

    private val _checkNumberResultMobile = MutableLiveData<OneTimeEvent<CheckNumberResult>>()
    val checkNumberResultMobile: LiveData<OneTimeEvent<CheckNumberResult>> =
        _checkNumberResultMobile

    private val _checkNumberResultBroadband = MutableLiveData<OneTimeEvent<CheckNumberResult>>()
    val checkNumberResultBroadband: LiveData<OneTimeEvent<CheckNumberResult>> =
        _checkNumberResultBroadband

    private val _checkNameResult = MutableLiveData<OneTimeEvent<CheckNameResult>>()
    val checkNameResult: LiveData<OneTimeEvent<CheckNameResult>> = _checkNameResult

    private val _accountsResult = MutableLiveData<AccountsResult>()

    val filteredAccounts = MutableLiveData<List<EnrollAccountUI>>()

    private val _enrollAccountsResult = MutableLiveData<OneTimeEvent<EnrollAccountsResult>>()
    val enrollAccountsResult: LiveData<OneTimeEvent<EnrollAccountsResult>> = _enrollAccountsResult

    private val _enableProceedButton = MutableLiveData<OneTimeEvent<Boolean>>()
    val enableProceedButton: LiveData<OneTimeEvent<Boolean>> = _enableProceedButton

    private val _saveChangesResult = MutableLiveData<OneTimeEvent<SaveChangesResult>>()
    val saveChangesResult: LiveData<OneTimeEvent<SaveChangesResult>> = _saveChangesResult

    // TODO remove the migration flow from the app
    /*private val _enrollMigratedAccountsResult =
        MutableLiveData<OneTimeEvent<EnrollMigratedAccountsResult>>()
    val enrollMigratedAccountsResult: LiveData<OneTimeEvent<EnrollMigratedAccountsResult>> =
        _enrollMigratedAccountsResult*/

    private var accountsToEnroll = mutableListOf<EnrollAccountUI>()

    var hasPromo = false

    var newUser = false

    var dacFlow = false

    val encryptedUserEmail by lazy {
        userDetailsDomainManager.getEmail().fold({ email ->
            encryptData(email)
        }, {
            NO_EMAIL_STORED
        })
    }

    init {
        viewModelScope.launch {
            profileDomainManager.getEnrolledAccounts()
                // TODO handle this when applicable -> .withLoadingOverlay(handler)
                .collect { enrolledAccountsResult ->
                    enrolledAccountsResult.fold({ response ->
                        val list = response.map { account ->
                            EnrollAccountRequest(
                                accountNumber = account.accountNumber,
                                mobileNumber = account.mobileNumber,
                                landlineNumber = account.landlineNumber,
                                accountAlias = account.accountAlias,
                                brand = account.brandType.toString(),
                                segment = account.segment.toString(),
                                channel = account.channel
                            )
                        }
                        _accountsResult.value = AccountsResult.AccountsSuccessResult(list)
                        dLog("Enrolled accounts fetched")
                    }, { enrolledAccountError ->
                        when (enrolledAccountError) {
                            is GetEnrolledAccountsError.General -> handler.handleGeneralError(
                                enrolledAccountError.error
                            )
                            GetEnrolledAccountsError.UserHasNoEnrolledAccounts -> {
                                dLog("No enrolled accounts")
                                _accountsResult.value = AccountsResult.UserHasNoEnrolledAccounts
                            }
                            else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                        }
                    })
                }
        }
    }

    private fun encryptData(data: String): String {
        return userDetailsDomainManager.encryptData(data = data)
    }

    fun checkNumber(msisdn: String, segment: AccountSegment) {
        if (msisdn.isEmpty()) {
            if (segment == AccountSegment.Mobile) _checkNumberResultMobile.value =
                OneTimeEvent(CheckNumberResult.NumberFieldEmpty)
            else _checkNumberResultBroadband.value =
                OneTimeEvent(CheckNumberResult.NumberFieldEmpty)
            return
        }

        if (msisdn.isInvalidFormat()) {
            _checkNumberResultBroadband.value =
                OneTimeEvent(CheckNumberResult.InvalidNumberFormat)
            return
        }

        if (_accountsResult.value is AccountsResult.AccountsSuccessResult) {
            for (account in (_accountsResult.value as AccountsResult.AccountsSuccessResult).accountRequests) {
                account.let {
                    if (it.accountNumber == msisdn || it.landlineNumber == msisdn || it.mobileNumber == msisdn) {
                        dLog("Number exists")
                        if (segment == AccountSegment.Mobile) _checkNumberResultMobile.value =
                            OneTimeEvent(CheckNumberResult.SameNumberExists)
                        else _checkNumberResultBroadband.value =
                            OneTimeEvent(CheckNumberResult.SameNumberExists)
                        return
                    }
                }
            }
        }

        for (account in accountsToEnroll) {
            account.enrollAccount.msisdn.let {
                if (it.convertToPrefixNumberFormat() == msisdn) {
                    dLog("Number exists")
                    if (segment == AccountSegment.Mobile) _checkNumberResultMobile.value =
                        OneTimeEvent(CheckNumberResult.SameNumberExists)
                    else _checkNumberResultBroadband.value =
                        OneTimeEvent(CheckNumberResult.SameNumberExists)
                    return
                }
            }
        }

        dLog("Unique number")
        if (segment == AccountSegment.Mobile) _checkNumberResultMobile.value =
            OneTimeEvent(CheckNumberResult.UniqueNumber(msisdn))
        else _checkNumberResultBroadband.value =
            OneTimeEvent(CheckNumberResult.UniqueNumber(msisdn))
    }

    fun checkName(accountName: String) {
        if (accountName.isEmpty()) {
            _checkNameResult.value = OneTimeEvent(CheckNameResult.NameFieldEmpty)
            return
        }

        if (_accountsResult.value is AccountsResult.AccountsSuccessResult) {
            for (account in (_accountsResult.value as AccountsResult.AccountsSuccessResult).accountRequests) {
                if (account.accountAlias == accountName) {
                    dLog("Name exists")
                    _checkNameResult.value = OneTimeEvent(CheckNameResult.SameNameExists)
                    return
                }
            }
        }

        for (account in accountsToEnroll) {
            if (account.enrollAccount.accountAlias == accountName) {
                dLog("Name exists")
                _checkNameResult.value = OneTimeEvent(CheckNameResult.SameNameExists)
                return
            }
        }

        dLog("Unique name")
        _checkNameResult.value = OneTimeEvent(CheckNameResult.UniqueName(accountName))
    }

    fun filterAccounts(position: Int = 0) {
        filteredAccounts.value =
            accountsToEnroll.filter { position.tabPositionToFilter(it.enrollAccount) }
    }

    // TODO remove the migration flow from the app
    /*fun migrateOldAccounts() = viewModelScope.launchWithLoadingOverlay(handler) {
        if (accountsToEnroll.any { it.isMigrationAccount }) {
            accountDomainManager.enrollMigratedAccounts(
                EnrollMigratedAccountsParams(
                    accountsToEnroll.filter { it.isMigrationAccount }
                        .map {
                            EnrollMigratedAccountsJson(
                                accountNumber = if (it.enrollAccount.msisdn.toNumberType() is NumberType.AccountNumber) it.enrollAccount.msisdn else null,
                                landlineNumber = if (it.enrollAccount.msisdn.toNumberType() is NumberType.LandlineNumber) it.enrollAccount.msisdn else null,
                                mobileNumber = if (it.enrollAccount.msisdn.toNumberType() is NumberType.MobileNumber) it.enrollAccount.msisdn else null,
                                accountAlias = it.enrollAccount.accountAlias,
                                brand = it.enrollAccount.brandType.toString(),
                                brandDetail = it.enrollAccount.brandType.toString(),
                                segment = it.enrollAccount.segment.toString(),
                                channel = it.enrollAccount.channel
                            )
                        })
            ).fold(
                {
                    // Refresh enrolled accounts has to be in both if and else because of the
                    // enrolling migrated accounts in if
                    dLog("Successfully enrolled migrated accounts")
                    for (accountResult in it.result)
                        accountResult.account.pickPrimaryMsisdn().let { number ->
                            checkForPromos(accountResult.account.brandType, number)
                        }
                    _enrollMigratedAccountsResult.value =
                        OneTimeEvent(EnrollMigratedAccountsSuccess)
                },
                {
                    dLog("Failed to enroll migrated accounts.")
                    when (it) {
                        is EnrollMigratedAccountsError.General -> handler.handleGeneralError(it.error)
                        else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                    }

                }
            )
        } else {
            // Refresh enrolled accounts has to be in both if and else because of the
            // enrolling migrated accounts in if
            _enrollMigratedAccountsResult.value = OneTimeEvent(AddAccountSuccess)
        }
    }*/

    fun getMigratedAccounts(email: String, isSocialLogin: Boolean) =
        viewModelScope.launchWithLoadingOverlay(handler) {
            accountDomainManager.getMigratedAccounts(
                GetMigratedAccountsParams(
                    email,
                    if (isSocialLogin) "All" else "globeOne"
                )
            ).fold({ response ->
                accountsToEnroll = response.map {
                    EnrollAccountUI(
                        enrollAccount = EnrollAccountParams(
                            referenceId = "",
                            msisdn = it.primaryMsisdn,
                            accountAlias = it.accountAlias,
                            brandType = it.brandType,
                            segment = it.segment,
                            channel = it.channel
                        ),
                        isMigrationAccount = true,
                        duplicatedNameError = false,
                        duplicatedNumberError = false,
                        tooLongNameError = false
                    )
                } as MutableList<EnrollAccountUI>

                updateAccounts()
                _getMigrationAccountsResult.value = OneTimeEvent(GetAccountsForMigrationSuccess)
                dLog("Enrolled accounts fetched")
            }, {
                when (it) {
                    GetEnrolledAccountsError.UserHasNoEnrolledAccounts -> {
                        dLog("No enrolled accounts")
                        filteredAccounts.value = emptyList()
                        _getMigrationAccountsResult.value = OneTimeEvent(NoAccounts)
                    }
                    is GetEnrolledAccountsError.General -> handler.handleGeneralError(it.error)
                    else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                }
            })
        }

    private fun updateAccounts() {
        accountsToEnroll.forEach { uiAccount1 ->
            accountsToEnroll.forEach { uiAccount2 ->
                if (uiAccount1.enrollAccount != uiAccount2.enrollAccount) {
                    if (uiAccount1.enrollAccount.accountAlias == uiAccount2.enrollAccount.accountAlias) {
                        uiAccount1.duplicatedNameError = true
                        uiAccount2.duplicatedNameError = true
                    }

                    if ((uiAccount1.enrollAccount.msisdn == uiAccount2.enrollAccount.msisdn && uiAccount1.enrollAccount.msisdn.isNotBlank() && uiAccount2.enrollAccount.msisdn.isNotBlank())) {
                        uiAccount1.duplicatedNumberError = true
                        uiAccount2.duplicatedNumberError = true
                    }
                }
            }

            if (uiAccount1.enrollAccount.accountAlias.length > 15)
                uiAccount1.tooLongNameError = true
        }

        _enableProceedButton.value =
            OneTimeEvent(accountsToEnroll.find { it.duplicatedNumberError || it.duplicatedNameError || it.tooLongNameError } == null)

        filteredAccounts.value = accountsToEnroll
    }

    fun saveChanges(enrolledAccount: EnrollAccountUI, newAccountName: String) {
        accountsToEnroll.find { it.enrollAccount.accountAlias == newAccountName }?.let {
            _saveChangesResult.value = OneTimeEvent(SaveChangesResult.SaveChangesFailure)
            return
        }
        accountsToEnroll.find {
            it.enrollAccount.accountAlias == enrolledAccount.enrollAccount.accountAlias && it != enrolledAccount
        }?.duplicatedNameError = false
        accountsToEnroll.find { it == enrolledAccount }?.let {
            it.enrollAccount.accountAlias = newAccountName
            it.duplicatedNameError = false
        }
        updateAccounts()
        _saveChangesResult.value = OneTimeEvent(SaveChangesResult.SaveChangesSuccess)
    }

    fun enrollAccount(
        msisdn: String,
        brand: AccountBrand,
        brandType: AccountBrandType,
        segment: AccountSegment,
        referenceId: String,
        accountAlias: String,
        verificationType: String? = null
    ) = viewModelScope.launchWithLoadingOverlay(handler) {
        val enrollAccountParams = EnrollAccountParams(
            referenceId,
            msisdn,
            accountAlias,
            brandType,
            segment,
            arrayListOf(ENROLL_ACCOUNT_CHANNEL),
            verificationType
        )
        accountDomainManager.enrollAccounts(enrollAccountParams)
            .fold({
                accountsToEnroll.add(
                    EnrollAccountUI(
                        enrollAccountParams,
                        false,
                        false,
                        false,
                        false
                    )
                )

                invalidateAccounts()
                checkForPromos(brand, msisdn)

                _enrollAccountsResult.value =
                    OneTimeEvent(EnrollAccountsResult.EnrollAccountsSuccess)
                dLog("Enrolled account.")

            }, {
                when (it) {
                    is EnrollAccountsError.General -> handler.handleGeneralError(it.error)
                    else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                }
            })
    }

    private suspend fun checkForPromos(brand: AccountBrand, msisdn: String) {
        personalizedCampaignUseCase.execute(brand).fold(
            { availableCampaign ->
                accountDomainManager.getCustomerCampaignPromo(
                    msisdn,
                    brand.name,
                    availableCampaign
                ).fold({ promo ->
                    hasPromo = promo.isNotEmpty()
                }, {
                    hasPromo = false
                })
            },
            {
                hasPromo = false
            }
        )
    }

    fun numberOfMobileAccountsAdded() =
        accountsToEnroll.filter { it.enrollAccount.segment == AccountSegment.Mobile }.size

    fun numberOfBroadbandAccountsAdded() =
        accountsToEnroll.filter { it.enrollAccount.segment == AccountSegment.Broadband }.size

    // TODO delete API call should be added when available
    fun deleteAccount(account: EnrollAccountUI, currentTab: Int) {
        accountsToEnroll.remove(account)
        filterAccounts(currentTab)
        accountsToEnroll.find {
            it.enrollAccount.msisdn == account.enrollAccount.msisdn
        }?.duplicatedNumberError = false
        updateAccounts()
    }

    private suspend fun invalidateAccounts() {
        databaseDomainManager.clearAllData()
    }

    fun createDeleteAccountDialog(yesCallback: () -> Unit, noCallback: () -> Unit) {
        handler.handleDialog(
            overlayAndDialogFactories.createDeleteAccountDialog(
                yesCallback,
                noCallback
            )
        )
    }

    fun skipAddingAccount(yesCallback: () -> Unit, noCallback: () -> Unit) {
        handler.handleDialog(
            overlayAndDialogFactories.createAddAccountMobileNumberSkipDialog(
                yesCallback,
                noCallback
            )
        )
    }

    fun logout() {
        handler.handleDialog(
            overlayAndDialogFactories.createLogoutDialog {
                viewModelScope.launchWithLoadingOverlay(handler) {
                    // Update bubble state on 'Get started' screen
                    sharedPreferences.edit().putBoolean(GET_STARTED_BUBBLE_SHOWN_KEY, false).apply()
                    // Make logout
                    authDomainManager.logout().onFailure {
                        if (it is LogoutError.General) {
                            handler.handleGeneralError(it.error)
                        }
                    }
                }
            }
        )
    }

    data class EnrollAccountUI(
        val enrollAccount: EnrollAccountParams,
        val isMigrationAccount: Boolean,
        var duplicatedNameError: Boolean,
        var duplicatedNumberError: Boolean,
        var tooLongNameError: Boolean
    ) : Serializable

    // TODO remove the migration flow from the app
    /*sealed class EnrollMigratedAccountsResult {

        object EnrollMigratedAccountsSuccess : EnrollMigratedAccountsResult()

        object AddAccountSuccess : EnrollMigratedAccountsResult()
    }*/

    sealed class SaveChangesResult {

        object SaveChangesSuccess : SaveChangesResult()

        object SaveChangesFailure : SaveChangesResult()
    }

    sealed class EnrollAccountsResult {
        object EnrollAccountsSuccess : EnrollAccountsResult()
    }

    sealed class GetAccountsForMigrationResult {
        object NoAccounts : GetAccountsForMigrationResult()
        object GetAccountsForMigrationSuccess : GetAccountsForMigrationResult()
    }

    override val logTag = "AddAccountMoreAccountsViewModel"
}

private fun Int.tabPositionToFilter(item: EnrollAccountParams) =
    when (this) {
        1 -> item.segment == AccountSegment.Mobile
        2 -> item.segment == AccountSegment.Broadband
        else -> true
    }
