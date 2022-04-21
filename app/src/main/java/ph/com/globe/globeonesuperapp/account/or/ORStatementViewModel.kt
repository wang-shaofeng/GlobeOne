/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.or

import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.domain.billings.BillingsDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.billings.network_models.GetBillingsStatementsParams
import ph.com.globe.model.billings.network_models.GetBillingsStatementsPdfParams
import ph.com.globe.model.billings.network_models.ResponseType
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class ORStatementViewModel @Inject constructor(
    private val billingsDomainManager: BillingsDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    lateinit var selectedEnrolledAccount: EnrolledAccount
    var receiptId: String
    var verificationToken: String? = null

    init {
        with(savedStateHandle) {
            get<EnrolledAccount>("enrolledAccount")?.let { selectedEnrolledAccount = it }
            receiptId = get<String>("receiptId") ?: ""
            verificationToken = get<String>("verificationToken")
        }
    }

    private val _orStatementStatus = MutableLiveData<OneTimeEvent<ORStatementStatus>>()
    val orStatementStatus: LiveData<OneTimeEvent<ORStatementStatus>> = _orStatementStatus

    fun downloadBillPdf(failureCallback: () -> Unit) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            when (selectedEnrolledAccount.segment) {
                AccountSegment.Mobile -> {
                    if (receiptId.isEmpty() || verificationToken.isNullOrEmpty()) {
                        billingsDomainManager.getBillingsStatements(
                            GetBillingsStatementsParams(
                                mobileNumber = selectedEnrolledAccount.mobileNumber ?: "",
                                segment = AccountSegment.Mobile,
                                pageSize = 1
                            )
                        ).fold({ billingStatementList ->
                            billingStatementList.first().id?.let { billingStatementId ->
                                getMobileBillingStatement(
                                    billingStatementId,
                                    billingStatementList.first().verificationToken,
                                    failureCallback
                                )
                            }
                        }, {
                            _orStatementStatus.value = OneTimeEvent(ORStatementStatus.Error)
                        })
                    } else
                        getMobileBillingStatement(receiptId, verificationToken, failureCallback)
                }
                AccountSegment.Broadband -> {
                    billingsDomainManager.getBillingsStatementsPdf(
                        GetBillingsStatementsPdfParams(
                            responseType = ResponseType.JsonFormat,
                            landlineNumber = selectedEnrolledAccount.landlineNumber,
                            segment = AccountSegment.Broadband
                        )
                    ).fold({ pdfResponse ->
                        try {
                            val decodedPdf = Base64.decode(pdfResponse.result, Base64.DEFAULT)
                            _orStatementStatus.value = OneTimeEvent(
                                ORStatementStatus.Success(
                                    "globe_bill_${selectedEnrolledAccount.landlineNumber}.pdf",
                                    decodedPdf
                                )
                            )
                        } catch (e: java.lang.IllegalArgumentException) {
                            handleDownloadError(failureCallback)
                        }
                    }, {
                        handleDownloadError(failureCallback)
                    })
                }
            }
        }
    }

    private suspend fun getMobileBillingStatement(
        receiptId: String,
        verificationToken: String?,
        failureCallback: () -> Unit
    ) {
        billingsDomainManager.getBillingsStatementsPdf(
            GetBillingsStatementsPdfParams(
                responseType = ResponseType.JsonFormat,
                billingStatementId = receiptId,
                mobileNumber = selectedEnrolledAccount.mobileNumber,
                segment = AccountSegment.Mobile,
                verificationToken = verificationToken
            )
        ).fold({ pdfResponse ->
            try {
                val decodedPdf = Base64.decode(pdfResponse.result, Base64.DEFAULT)
                _orStatementStatus.value = OneTimeEvent(
                    ORStatementStatus.Success(
                        pdfName = "globe_bill_${selectedEnrolledAccount.landlineNumber}.pdf",
                        pdfByteArray = decodedPdf
                    )
                )
            } catch (e: IllegalArgumentException) {
                handleDownloadError(failureCallback)
            }
        }, {
            handleDownloadError(failureCallback)
        })
    }

    private fun handleDownloadError(failureCallback: () -> Unit) {
        _orStatementStatus.value = OneTimeEvent(ORStatementStatus.Error)
        handler.handleDialog(
            overlayAndDialogFactories.createAccountDetailsFailedDialog(
                failureCallback
            )
        )
    }

    sealed class ORStatementStatus {

        data class Success(
            val pdfName: String,
            val pdfByteArray: ByteArray
        ) : ORStatementStatus()

        object Error : ORStatementStatus()
    }

    override val logTag = "ORStatementViewModel"
}
