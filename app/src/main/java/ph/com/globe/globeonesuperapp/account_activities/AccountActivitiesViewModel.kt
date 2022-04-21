/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.recyclerview.widget.DiffUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.setOneTimeEvent
import ph.com.globe.model.account_activities.AccountRewardsTransaction
import ph.com.globe.model.billings.domain_models.BillingStatement
import ph.com.globe.model.payment.Payment
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import javax.inject.Inject

@HiltViewModel
class AccountActivitiesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val _enrolledAccount =
        savedStateHandle.getLiveData<EnrolledAccount>(ENROLLED_ACCOUNT_KEY)
    val enrolledAccount = _enrolledAccount as LiveData<EnrolledAccount>

    private val _scrollToTop: MutableLiveData<OneTimeEvent<Unit>> = MutableLiveData()
    val scrollToTop: LiveData<OneTimeEvent<Unit>> = _scrollToTop

    fun setEnrolledAccount(enrolledAccount: EnrolledAccount) {
        _enrolledAccount.postValue(enrolledAccount)
    }

    fun scrollToTop() = _scrollToTop.setOneTimeEvent(Unit)

    companion object {
        const val ENROLLED_ACCOUNT_KEY = "enrolledAccount"
    }

    override val logTag: String = "AccountActivityViewModel"
}

sealed class AccountActivityPagingState(val id: Int) {
    object SkeletonLoading : AccountActivityPagingState(0)
    data class Loading(val trigger: Boolean, val oldOffset: Int) : AccountActivityPagingState(1)
    object ReachEnd : AccountActivityPagingState(2)
    object Empty : AccountActivityPagingState(3)
    object Error : AccountActivityPagingState(4)
    data class Rewards(val transaction: AccountRewardsTransaction) : AccountActivityPagingState(5)
    data class PrepaidLedger(val prepaidLedger: PrepaidLedgerTransactionItem) :
        AccountActivityPagingState(6)

    data class BillStatement(val billingStatement: BillingStatement) : AccountActivityPagingState(7)
    data class BillPayment(val billPayment: Payment) : AccountActivityPagingState(8)
}

object AccountActivitiesDiffUtil : DiffUtil.ItemCallback<AccountActivityPagingState>() {
    override fun areItemsTheSame(
        oldItem: AccountActivityPagingState,
        newItem: AccountActivityPagingState
    ): Boolean =
        oldItem is AccountActivityPagingState.Loading && newItem is AccountActivityPagingState.Loading && oldItem.id == newItem.id ||
                oldItem is AccountActivityPagingState.SkeletonLoading && newItem is AccountActivityPagingState.SkeletonLoading && oldItem.id == newItem.id ||
                oldItem is AccountActivityPagingState.ReachEnd && newItem is AccountActivityPagingState.ReachEnd && oldItem.id == newItem.id ||
                oldItem is AccountActivityPagingState.Rewards && newItem is AccountActivityPagingState.Rewards && oldItem.transaction.transactionNo == newItem.transaction.transactionNo ||
                oldItem is AccountActivityPagingState.Empty && newItem is AccountActivityPagingState.Empty && oldItem.id == newItem.id ||
                oldItem is AccountActivityPagingState.Error && newItem is AccountActivityPagingState.Error && oldItem.id == newItem.id ||
                oldItem is AccountActivityPagingState.PrepaidLedger && newItem is AccountActivityPagingState.PrepaidLedger && oldItem.prepaidLedger.id == newItem.prepaidLedger.id ||
                oldItem is AccountActivityPagingState.BillStatement && newItem is AccountActivityPagingState.BillStatement && oldItem.billingStatement.id == newItem.billingStatement.id ||
                oldItem is AccountActivityPagingState.BillPayment && newItem is AccountActivityPagingState.BillPayment && oldItem.billPayment.receiptId == newItem.billPayment.receiptId

    override fun areContentsTheSame(
        oldItem: AccountActivityPagingState,
        newItem: AccountActivityPagingState
    ): Boolean =
        oldItem == newItem // if areItemsTheSame returns true, it means that oldItem and newItem are also the same type.
}
