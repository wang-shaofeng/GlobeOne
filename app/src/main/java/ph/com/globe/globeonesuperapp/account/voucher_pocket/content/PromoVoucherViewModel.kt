/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.voucher_pocket.content

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import ph.com.globe.domain.voucher.VoucherDomainManager
import ph.com.globe.errors.voucher.GetPromoVouchersError
import ph.com.globe.errors.voucher.MarkVouchersAsUsedError
import ph.com.globe.errors.voucher.RetrieveUsedVouchersError
import ph.com.globe.globeonesuperapp.account.voucher_pocket.VouchersViewModel
import ph.com.globe.globeonesuperapp.account.voucher_pocket.content.PromoVoucherPagingState.Data
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.shared_preferences.VOUCHERS_TAP_TO_COPY_BUBBLE_SHOWN_KEY
import ph.com.globe.globeonesuperapp.utils.shared_preferences.VOUCHERS_TAP_TO_REVEAL_BUBBLE_SHOWN_KEY
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.isAccountNumber
import ph.com.globe.model.account.isMobileNumber
import ph.com.globe.model.voucher.*
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class PromoVoucherViewModel @Inject constructor(
    private val voucherDomainManager: VoucherDomainManager,
    private val requestTapToCopyBubbleTimer: RequestTapToCopyBubbleTimer,
    private val requestTapToRevealBubbleTimer: RequestTapToRevealBubbleTimer,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel(), RequestTapToCopyBubbleReceiver, RequestTapToRevealBubbleReceiver {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private var selectedAccount: VouchersViewModel.SelectedAccountUIModel =
        VouchersViewModel.SelectedAccountUIModel("", "", false)

    private val _vouchers = MutableLiveData<List<PromoVoucherPagingState>>()
    val vouchers: LiveData<List<PromoVoucherPagingState>> = _vouchers

    private var currentPageNum = 1
    private var dataTotalCount = 0
    private var currentJob: Job? = null

    fun accountChanged(account: VouchersViewModel.SelectedAccountUIModel) {
        selectedAccount = account
        if (account.isContentAvailable) {
            reload()
        } else {
            addComingSoonState()
        }
    }

    fun revealVoucher(promoVoucher: PromoVoucher) =
        viewModelScope.launchWithLoadingOverlay(handler) {
            _vouchers.value?.let { voucherList ->
                var coupons = voucherList.map { voucherItem ->
                    if (voucherItem is Data)
                        voucherItem.copy(
                            voucher = voucherItem.voucher.copy(
                                isLoading = voucherItem.voucher.voucher.serialNumber == promoVoucher.serialNumber
                            )
                        )
                    else voucherItem
                }
                _vouchers.value = coupons

                voucherDomainManager.markVoucherAsUsed(
                    MarkVouchersAsUsedParams(
                        mobileNumber = selectedAccount.msisdn.takeIf { it.isMobileNumber() },
                        accountNumber = selectedAccount.msisdn.takeIf { it.isAccountNumber() },
                        requestParams = MarkVoucherAsUsedRequest(
                            listOf(
                                Voucher(
                                    promoVoucher.serialNumber,
                                    promoVoucher.code,
                                    promoVoucher.category,
                                    promoVoucher.validityEndDate
                                )
                            )
                        )
                    )
                ).fold(
                    {
                        val showTapToCopyBubble = sharedPreferences.getBoolean(
                            VOUCHERS_TAP_TO_COPY_BUBBLE_SHOWN_KEY,
                            true
                        )
                        if (showTapToCopyBubble) {
                            sharedPreferences.edit()
                                .putBoolean(VOUCHERS_TAP_TO_COPY_BUBBLE_SHOWN_KEY, false)
                                .apply()
                        }

                        coupons = coupons.map {
                            if (it is Data)
                                it.copy(
                                    voucher = it.voucher.copy(
                                        used = if (it.voucher.voucher.serialNumber == promoVoucher.serialNumber) true else it.voucher.used,
                                        isLoading = false,
                                        showTapToCopyBubble = if (it.voucher.voucher.serialNumber == promoVoucher.serialNumber) showTapToCopyBubble else false
                                    )
                                )
                            else it
                        }
                        _vouchers.value = coupons
                        requestTapToCopyBubbleTimer.startCountDown(this)
                    }, {
                        coupons = coupons.map { voucherItem ->
                            if (voucherItem is Data)
                                voucherItem.copy(
                                    voucher = voucherItem.voucher.copy(isLoading = false)
                                )
                            else voucherItem
                        }
                        _vouchers.value = coupons

                        if (it is MarkVouchersAsUsedError.General)
                            handler.handleGeneralError(it.error)
                    }
                )
            }
        }

    fun reload() {
        _vouchers.value = listOf(PromoVoucherPagingState.SkeletonLoading)
        currentPageNum = 1
        dataTotalCount = 0

        viewModelScope.launch {
            if (currentJob?.isActive == true) currentJob?.cancelAndJoin()

            loadMore()
        }
    }

    fun loadMore() {
        currentJob = viewModelScope.launch { nextFetch() }
    }

    private suspend fun nextFetch() {
        // Reset trigger flag in Loading
        if (_vouchers.value?.lastOrNull() is PromoVoucherPagingState.Loading) {
            _vouchers.value = clearLoadingStates(_vouchers.value) +
                    PromoVoucherPagingState.Loading(false, currentPageNum)
        }

        voucherDomainManager.getPromoVouchers(
            GetPromoVouchersParams(
                mobileNumber = selectedAccount.msisdn,
                pageNumber = currentPageNum,
                pageSize = COUPONS_LIST_LIMIT
            )
        ).fold(
            { promoVouchersResult ->
                voucherDomainManager.retrieveUsedVouchers(
                    RetrieveUsedVouchersParams(
                        mobileNumber = selectedAccount.msisdn.takeIf { it.isMobileNumber() },
                        accountNumber = selectedAccount.msisdn.takeIf { it.isAccountNumber() },
                    )
                ).fold(
                    { retrieveUsedVoucherResponse ->
                        val vouchers = promoVouchersResult.vouchers ?: emptyList()
                        val totalPage = promoVouchersResult.totalPages.toIntOrNull() ?: 0
                        val pageNumber = promoVouchersResult.pageNumber.toIntOrNull() ?: 0

                        dataTotalCount += vouchers.size
                        if (currentPageNum < totalPage) {
                            currentPageNum++
                        }

                        /**
                         * data total count equals 0 and had requested all record data
                         */
                        val isEmpty = dataTotalCount == 0 && pageNumber == totalPage
                        when {
                            isEmpty -> setEmptyState()
                            else -> {
                                addNewVouchers(currentPageNum, vouchers.map { voucher ->
                                    val usedVoucher =
                                        retrieveUsedVoucherResponse.result.vouchers?.find {
                                            it.id == voucher.serialNumber
                                                    && it.code == voucher.code
                                                    && it.type == voucher.category
                                        }

                                    val showTapToRevealBubble = sharedPreferences.getBoolean(
                                        VOUCHERS_TAP_TO_REVEAL_BUBBLE_SHOWN_KEY,
                                        true
                                    ) && usedVoucher == null

                                    if (showTapToRevealBubble) {
                                        sharedPreferences.edit()
                                            .putBoolean(
                                                VOUCHERS_TAP_TO_REVEAL_BUBBLE_SHOWN_KEY,
                                                false
                                            )
                                            .apply()
                                    }

                                    PromoVoucherItemModel(
                                        voucher,
                                        usedVoucher != null,
                                        false,
                                        showTapToRevealBubble
                                    )
                                })
                                // if got the total data, add reach-end item
                                if (pageNumber == totalPage) {
                                    addReachEnd()
                                }
                            }
                        }

                        _vouchers.value?.let { coupons ->
                            if (coupons.any { it is Data && it.voucher.showTapToCopyBubble }) {
                                requestTapToCopyBubbleTimer.startCountDown(this)
                            }

                            if (coupons.any { it is Data && it.voucher.showTapToRevealBubble }) {
                                requestTapToRevealBubbleTimer.startCountDown(this)
                            }
                        }
                    }, {
                        addErrorState()

                        if (it is RetrieveUsedVouchersError.General)
                            handler.handleGeneralError(it.error)
                    }
                )
            }, { error ->
                if (error is GetPromoVouchersError.PromoVouchersNotFound) {
                    setEmptyState()
                } else {
                    addErrorState()
                    if (error is GetPromoVouchersError.General)
                        handler.handleGeneralError(error.error)
                }
            }
        )
    }

    private fun addErrorState() {
        _vouchers.value =
            clearLoadingStates(_vouchers.value) + PromoVoucherPagingState.Error
    }

    private fun setEmptyState() {
        _vouchers.value = listOf(PromoVoucherPagingState.Empty)
    }

    private fun addReachEnd() {
        with(_vouchers) {
            val data = clearLoadingStates(value)
            when {
                data.isEmpty() -> {
                    // If data item is empty, set empty state.
                    setEmptyState()
                }
                data.size > 5 -> {
                    // If the data count > 5，show data + reach-end.
                    value = data + PromoVoucherPagingState.ReachEnd
                }
                else -> {
                    // If the data count <= 5，show data
                    value = data
                }
            }
        }
    }

    private fun addNewVouchers(
        currentOffset: Int,
        vouchers: List<PromoVoucherItemModel>
    ) {
        _vouchers.value =
            clearLoadingStates(_vouchers.value) + vouchers.map {
                Data(it)
            } + PromoVoucherPagingState.Loading(true, currentOffset)
    }

    private fun addComingSoonState() {
        _vouchers.value = listOf(PromoVoucherPagingState.ComingSoon)
    }

    private fun clearLoadingStates(list: List<PromoVoucherPagingState>?) =
        list?.mapNotNull {
            if (it is Data) it else null
        } ?: emptyList()

    override fun countDownTapToCopyFinished() {
        _vouchers.value?.let { voucherList ->
            _vouchers.value = voucherList.map { voucherItem ->
                if (voucherItem is Data)
                    voucherItem.copy(
                        voucher = voucherItem.voucher.copy(
                            showTapToCopyBubble = false
                        )
                    )
                else voucherItem
            }
        }
    }

    override fun countDownTapToRevealFinished() {
        _vouchers.value?.let { voucherList ->
            _vouchers.value = voucherList.map { voucherItem ->
                if (voucherItem is Data)
                    voucherItem.copy(
                        voucher = voucherItem.voucher.copy(
                            showTapToRevealBubble = false
                        )
                    )
                else voucherItem
            }
        }
    }

    override val logTag = "VouchersViewModel"
}

sealed class PromoVoucherPagingState(val id: Int) {
    object SkeletonLoading : PromoVoucherPagingState(0)
    data class Loading(val trigger: Boolean, val oldOffset: Int) : PromoVoucherPagingState(1)
    object ReachEnd : PromoVoucherPagingState(2)
    object Empty : PromoVoucherPagingState(3)
    object Error : PromoVoucherPagingState(4)
    data class Data(val voucher: PromoVoucherItemModel) : PromoVoucherPagingState(5)
    object ComingSoon : PromoVoucherPagingState(6)
}

data class PromoVoucherItemModel(
    val voucher: PromoVoucher,
    val used: Boolean,
    var showTapToCopyBubble: Boolean,
    var showTapToRevealBubble: Boolean,
    var isLoading: Boolean = false
)

const val COUPONS_LIST_LIMIT = 10
