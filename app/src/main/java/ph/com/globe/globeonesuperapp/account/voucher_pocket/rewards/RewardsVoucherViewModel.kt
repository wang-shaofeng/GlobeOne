/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.voucher_pocket.rewards

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import ph.com.globe.domain.voucher.VoucherDomainManager
import ph.com.globe.errors.voucher.GetLoyaltySubscribersCouponDetailsError
import ph.com.globe.errors.voucher.MarkVouchersAsUsedError
import ph.com.globe.errors.voucher.RetrieveUsedVouchersError
import ph.com.globe.globeonesuperapp.account.voucher_pocket.VouchersViewModel
import ph.com.globe.globeonesuperapp.account.voucher_pocket.VouchersViewModel.SelectedAccountUIModel
import ph.com.globe.globeonesuperapp.account.voucher_pocket.rewards.VoucherPocketPagingState.Data
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.shared_preferences.VOUCHERS_TAP_TO_COPY_BUBBLE_SHOWN_KEY
import ph.com.globe.globeonesuperapp.utils.shared_preferences.VOUCHERS_TAP_TO_REVEAL_BUBBLE_SHOWN_KEY
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.isAccountNumber
import ph.com.globe.model.account.isMobileNumber
import ph.com.globe.model.account.toNumberType
import ph.com.globe.model.account.toSubscribeType
import ph.com.globe.model.voucher.*
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.fold
import ph.com.globe.util.toFormattedStringOrEmpty
import java.util.*
import javax.inject.Inject

/**
 * logic moved from [VouchersViewModel]
 */
@HiltViewModel
class RewardsVoucherViewModel @Inject constructor(
    private val voucherDomainManager: VoucherDomainManager,
    private val requestTapToCopyBubbleTimer: RequestTapToCopyBubbleTimer,
    private val requestTapToRevealBubbleTimer: RequestTapToRevealBubbleTimer,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel(), RequestTapToCopyBubbleReceiver, RequestTapToRevealBubbleReceiver {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private var selectedAccount: SelectedAccountUIModel = SelectedAccountUIModel("", "", false)

    private val _vouchers = MutableLiveData<List<VoucherPocketPagingState>>()
    val vouchers: LiveData<List<VoucherPocketPagingState>> = _vouchers

    private var currentDataNum = 0 // requested data num
    private var applicableDataNum = 0
    private var currentJob: Job? = null

    fun accountChanged(account: SelectedAccountUIModel) {
        selectedAccount = account
        reload()
    }

    fun revealVoucher(coupon: Coupon) = viewModelScope.launchWithLoadingOverlay(handler) {
        _vouchers.value?.let { list ->
            var coupons = list.map {
                if (it is Data)
                    it.copy(
                        coupon = it.coupon.copy(
                            isLoading = it.coupon.coupon.couponId == coupon.couponId
                        )
                    )
                else it
            }
            _vouchers.value = coupons

            voucherDomainManager.markVoucherAsUsed(
                MarkVouchersAsUsedParams(
                    mobileNumber = selectedAccount.msisdn.takeIf { it.isMobileNumber() },
                    accountNumber = selectedAccount.msisdn.takeIf { it.isAccountNumber() },
                    requestParams = MarkVoucherAsUsedRequest(
                        listOf(
                            Voucher(
                                coupon.couponId.toString(),
                                coupon.couponNumber,
                                coupon.couponType,
                                coupon.expiryDate
                            )
                        )
                    )
                )
            ).fold(
                {
                    val showTapToCopyBubble = sharedPreferences.getBoolean(
                        VOUCHERS_TAP_TO_COPY_BUBBLE_SHOWN_KEY,
                        true
                    ) && !COUPONS_WITH_ACTIVATE_NOW_LINK.contains(coupon.couponType)
                    if (showTapToCopyBubble) {
                        sharedPreferences.edit()
                            .putBoolean(VOUCHERS_TAP_TO_COPY_BUBBLE_SHOWN_KEY, false)
                            .apply()
                    }

                    coupons = coupons.map { voucherPocket ->
                        if (voucherPocket is Data)
                            voucherPocket.copy(
                                coupon = voucherPocket.coupon.copy(
                                    used = if (voucherPocket.coupon.coupon.couponId == coupon.couponId) true else voucherPocket.coupon.used,
                                    isLoading = false,
                                    showTapToCopyBubble = if (voucherPocket.coupon.coupon.couponId == coupon.couponId) showTapToCopyBubble else false
                                )
                            )
                        else voucherPocket
                    }
                    _vouchers.value = coupons
                    requestTapToCopyBubbleTimer.startCountDown(this)
                }, { markVouchersAsUserError ->
                    coupons = coupons.map { voucherPocket ->
                        if (voucherPocket is Data)
                            voucherPocket.copy(
                                coupon = voucherPocket.coupon.copy(isLoading = false)
                            )
                        else voucherPocket
                    }
                    _vouchers.value = coupons

                    if (markVouchersAsUserError is MarkVouchersAsUsedError.General)
                        handler.handleGeneralError(markVouchersAsUserError.error)
                }
            )
        }
    }

    fun reload() {
        _vouchers.value = listOf(VoucherPocketPagingState.SkeletonLoading)
        currentDataNum = 0
        applicableDataNum = 0

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
        if (_vouchers.value?.lastOrNull() is VoucherPocketPagingState.Loading) {
            _vouchers.value = clearLoadingStates(_vouchers.value) +
                    VoucherPocketPagingState.Loading(false, currentDataNum)
        }

        voucherDomainManager.getLoyaltySubscribersCouponDetails(
            GetLoyaltySubscribersCouponDetailsParams(
                subscriberId = (selectedAccount.msisdn),
                subscriberType = (selectedAccount.msisdn).toNumberType()
                    .toSubscribeType(),
                channel = "Z",
                expiryDateFrom = getExpiryDate(),
                offset = currentDataNum, // offset = offset + limit
                limit = COUPONS_LIST_LIMIT
            )
        ).fold(
            { couponDetails ->
                voucherDomainManager.retrieveUsedVouchers(
                    RetrieveUsedVouchersParams(
                        mobileNumber = selectedAccount.msisdn.takeIf { it.isMobileNumber() },
                        accountNumber = selectedAccount.msisdn.takeIf { it.isAccountNumber() },
                    )
                ).fold(
                    { retrieveUsedVoucherResponse ->
                        val coupons = couponDetails.coupons ?: emptyList()
                        val totalRecordCount =
                            couponDetails.details?.totalRecordCount?.toIntOrNull() ?: 0

                        // filter applicable coupons for display, not applicable data omitted
                        val applicableCoupons = coupons.filter {
                            it.couponStatus == CouponStatus.VISIBLE
                                    && (it.couponDescription.startsWith(DESCRIPTION_VOUCHER) || it.couponDescription.startsWith(
                                DESCRIPTION_SOFT_BENEFIT
                            ))
                        }

                        currentDataNum += coupons.size // requested data num
                        applicableDataNum += applicableCoupons.size // applicable data num

                        /**
                         * applicable data num equals 0 and had requested all record data, it is real empty for UI display.
                         * And the old code was't add reach-end item when code filter early applicable data.
                         */
                        val isEmpty = applicableDataNum == 0 && currentDataNum == totalRecordCount

                        when {
                            isEmpty -> setEmptyState()
                            else -> {
                                addNewVouchers(currentDataNum, applicableCoupons.filter {
                                    it.couponStatus == CouponStatus.VISIBLE
                                            && (it.couponDescription.startsWith(DESCRIPTION_VOUCHER) || it.couponDescription.startsWith(
                                        DESCRIPTION_SOFT_BENEFIT
                                    ))
                                }.map { coupon ->
                                    val voucher =
                                        retrieveUsedVoucherResponse.result.vouchers?.find {
                                            it.id == coupon.couponId.toString()
                                                    && it.code == coupon.couponNumber
                                                    && it.type == coupon.couponType
                                        }

                                    val showTapToRevealBubble = sharedPreferences.getBoolean(
                                        VOUCHERS_TAP_TO_REVEAL_BUBBLE_SHOWN_KEY,
                                        true
                                    ) && voucher == null

                                    if (showTapToRevealBubble) {
                                        sharedPreferences.edit()
                                            .putBoolean(
                                                VOUCHERS_TAP_TO_REVEAL_BUBBLE_SHOWN_KEY,
                                                false
                                            )
                                            .apply()
                                    }

                                    CouponUIModel(
                                        coupon,
                                        voucher != null,
                                        COUPONS_WITH_ACTIVATE_NOW_LINK.contains(coupon.couponType),
                                        coupon.couponDescription.contains(DESCRIPTION_SOFT_BENEFIT),
                                        false,
                                        showTapToRevealBubble
                                    )
                                })
                                // if got the total data, add reach-end item
                                if (currentDataNum == totalRecordCount) {
                                    addReachEnd()
                                }
                            }
                        }

                        _vouchers.value?.let { coupons ->
                            if (coupons.any { it is Data && it.coupon.showTapToCopyBubble }) {
                                requestTapToCopyBubbleTimer.startCountDown(this)
                            }

                            if (coupons.any { it is Data && it.coupon.showTapToRevealBubble }) {
                                requestTapToRevealBubbleTimer.startCountDown(this)
                            }
                        }
                    }, {
                        addErrorState()

                        if (it is RetrieveUsedVouchersError.General)
                            handler.handleGeneralError(it.error)
                    }
                )
            }, {
                addErrorState()

                if (it is GetLoyaltySubscribersCouponDetailsError.General)
                    handler.handleGeneralError(it.error)
            }
        )
    }

    private fun addErrorState() {
        _vouchers.value =
            clearLoadingStates(_vouchers.value) + VoucherPocketPagingState.Error
    }

    private fun setEmptyState() {
        _vouchers.value = listOf(VoucherPocketPagingState.Empty)
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
                    value = data + VoucherPocketPagingState.ReachEnd
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
        vouchers: List<CouponUIModel>
    ) {
        _vouchers.value =
            clearLoadingStates(_vouchers.value) + vouchers.map {
                Data(it)
            } + VoucherPocketPagingState.Loading(true, currentOffset)
    }

    private fun clearLoadingStates(list: List<VoucherPocketPagingState>?) =
        list?.mapNotNull {
            if (it is Data) it else null
        } ?: emptyList()

    private fun getExpiryDate(): String {
        val millis = Calendar.getInstance().also {
            it.timeInMillis = System.currentTimeMillis()
            it.set(Calendar.HOUR, 0)
            it.set(Calendar.MINUTE, 0)
            it.set(Calendar.SECOND, 0)
            it.set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return millis.toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
    }

    override fun countDownTapToCopyFinished() {
        _vouchers.value?.let {
            _vouchers.value = it.map {
                if (it is Data)
                    it.copy(
                        coupon = it.coupon.copy(
                            showTapToCopyBubble = false
                        )
                    )
                else it
            }
        }
    }

    override fun countDownTapToRevealFinished() {
        _vouchers.value?.let {
            _vouchers.value = it.map {
                if (it is Data)
                    it.copy(
                        coupon = it.coupon.copy(
                            showTapToRevealBubble = false
                        )
                    )
                else it
            }
        }
    }

    override val logTag = "VouchersViewModel"
}

sealed class VoucherPocketPagingState(val id: Int) {
    object SkeletonLoading : VoucherPocketPagingState(0)
    data class Loading(val trigger: Boolean, val oldOffset: Int) : VoucherPocketPagingState(1)
    object ReachEnd : VoucherPocketPagingState(2)
    object Empty : VoucherPocketPagingState(3)
    object Error : VoucherPocketPagingState(4)
    data class Data(val coupon: CouponUIModel) : VoucherPocketPagingState(5)
}

data class CouponUIModel(
    val coupon: Coupon,
    val used: Boolean,
    val couponWithLink: Boolean,
    val softBenefit: Boolean,
    var showTapToCopyBubble: Boolean,
    var showTapToRevealBubble: Boolean,
    var isLoading: Boolean = false
)

const val COUPONS_LIST_LIMIT = 10
val COUPONS_WITH_ACTIVATE_NOW_LINK = listOf(
    "GRAB100",
    "GRABFOOD150",
    "LAZ100",
    "LAZADA300",
    "LAZADA500",
    "RAZERGOLD100",
    "SHOPEE100",
    "ZALORAV300",
    "ZALORAV500",
    "RAZERGOLD20"
)
