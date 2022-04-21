/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.group.GroupDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.errors.group.AddGroupMemberError
import ph.com.globe.errors.group.DeleteGroupMemberError
import ph.com.globe.errors.group.GetGroupListError
import ph.com.globe.errors.group.RetrieveGroupUsageError
import ph.com.globe.globeonesuperapp.group.GroupViewModel.GroupInfoResult.GroupInfoSuccess
import ph.com.globe.globeonesuperapp.group.GroupViewModel.GroupInfoResult.WalletItem
import ph.com.globe.globeonesuperapp.group.add_member.AddEnrolledAccountItem
import ph.com.globe.globeonesuperapp.group.group_overview.GroupMemberItem
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.group.*
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.fold
import ph.com.globe.util.toDateWithTimeZoneOrNull
import ph.com.globe.util.toFormattedStringOrEmpty
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val profileDomainManager: ProfileDomainManager,
    private val groupDomainManager: GroupDomainManager,
    private val requestBubbleTimer: RequestBubbleTimer,
    private val overlayAndDialogFactories: OverlayAndDialogFactories
) : BaseViewModel(), RequestBubbleTimerReceiver {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _groupProcessingResult = MutableLiveData<GroupInfoResult>()
    val groupProcessingResult: LiveData<GroupInfoResult> = _groupProcessingResult

    private val _groupMembers = MutableLiveData<List<GroupMemberItem>>()
    val groupMembers: LiveData<List<GroupMemberItem>> = _groupMembers

    private val _enrolledAccounts = MutableLiveData<List<AddEnrolledAccountItem>>()
    val enrolledAccounts: LiveData<List<AddEnrolledAccountItem>> = _enrolledAccounts

    private val _addGroupMemberResult = MutableLiveData<OneTimeEvent<AddMemberResult>>()
    val addGroupMemberResult: LiveData<OneTimeEvent<AddMemberResult>> = _addGroupMemberResult

    private val _deleteMemberResult = MutableLiveData<OneTimeEvent<DeleteGroupMemberResult>>()
    val deleteMemberResult: LiveData<OneTimeEvent<DeleteGroupMemberResult>> = _deleteMemberResult

    private val _hideBubble = MutableLiveData<OneTimeEvent<Unit>>()
    val hideBubble: LiveData<OneTimeEvent<Unit>> = _hideBubble

    private val _hideAddMemberButton = MutableLiveData<OneTimeEvent<Boolean>>()
    val hideAddMemberButton: LiveData<OneTimeEvent<Boolean>> = _hideAddMemberButton

    private val _showBubbleLiveData = MutableLiveData<OneTimeEvent<Unit>>()
    val showBubbleLiveData: LiveData<OneTimeEvent<Unit>> = _showBubbleLiveData

    private var showBubble = true

    private lateinit var group: MutableList<GroupMemberItem>
    private lateinit var accounts: List<AddEnrolledAccountItem>

    private var numberOfNotEnrolledMembers = 0

    private lateinit var groupInfo: GroupInfoSuccess

    lateinit var ownerAccountAlias: String

    fun fetchAllInfoAsOwner(
        ownerAccountAlias: String,
        ownerMobileNumber: String,
        skelligWallet: String,
        skelligCategory: String
    ) = viewModelScope.launch {
        this@GroupViewModel.ownerAccountAlias = ownerAccountAlias
        getEnrolledAccounts()
        groupDomainManager.retrieveGroupUsage(
            RetrieveGroupUsageParams(ownerAccountAlias, skelligWallet)
        ).fold(
            { retrieveGroupUsageResponse ->
                dLog("Fetched group usage.")
                groupDomainManager.getGroupList(
                    GetGroupListParams(
                        accountAlias = ownerAccountAlias,
                        isGroupOwner = true
                    )
                ).fold(
                    { getGroupListResponse ->
                        dLog("Fetched group list.")
                        val groupItem =
                            getGroupListResponse.result.groups.find { it.ownerMobileNumber.formattedForPhilippines() == ownerMobileNumber.formattedForPhilippines() }
                        groupInfo = retrieveGroupUsageResponse.result.toGroupInfoSuccess(
                            groupItem,
                            skelligCategory
                        )
                        _groupProcessingResult.value = groupInfo

                        group = retrieveGroupUsageResponse.result.members
                            ?.map {
                                it.toGroupMemberItem(groupItem?.ownerMobileNumber ?: "")
                            }?.toMutableList()
                            ?: mutableListOf(
                                GroupMemberItem(
                                    memberAccountAlias = ownerAccountAlias,
                                    memberRole = GROUP_ROLE_OWNER,
                                    memberNumber = ownerMobileNumber,
                                    walletId = groupInfo.walletId,
                                    keyword = groupInfo.wallets.firstOrNull()?.keyword ?: "",
                                    skelligCategory = groupInfo.skelligCategory,
                                    totalAllocated = groupInfo.totalAllocated,
                                    ownerMobileNumber = groupInfo.ownerMobileNumber,
                                    ownerAccountAlias = ownerAccountAlias,
                                )
                            )
                        _groupMembers.value = group
                        updateAccounts()
                        checkGroupLimit(group.size)
                    },
                    {
                        dLog("Failed to fetch group list.")
                        if (it is GetGroupListError.General) {
                            handler.handleGeneralError(it.error)
                        }
                        _groupProcessingResult.value = GroupInfoResult.GenericError
                    }
                )
            },
            {
                dLog("Failed to fetch group usage.")
                when (it) {
                    is RetrieveGroupUsageError.MobileNumberNotFound -> {
                        _groupProcessingResult.value = GroupInfoResult.MobileNumberNotFound
                    }

                    is RetrieveGroupUsageError.WalletNotFound -> {
                        _groupProcessingResult.value = GroupInfoResult.WalletNotFound
                    }

                    is RetrieveGroupUsageError.GroupNotExist -> {
                        _groupProcessingResult.value = GroupInfoResult.GroupNotExist
                    }

                    is RetrieveGroupUsageError.SubscriberNotBelongToAnyPool -> {
                        _groupProcessingResult.value =
                            GroupInfoResult.SubscriberNotBelongToAnyPool
                    }

                    else -> {
                        if (it is RetrieveGroupUsageError.General)
                            handler.handleGeneralError(it.error)
                        _groupProcessingResult.value = GroupInfoResult.GenericError
                    }
                }
            }
        )
    }

    suspend fun getEnrolledAccounts() {
        val result = profileDomainManager.getEnrolledAccounts().first()
        result.fold({ list ->
            dLog("Fetched enrolled accounts.")
            accounts = list.map { enrolledAccount ->
                AddEnrolledAccountItem(
                    enrolledAccount.accountAlias,
                    enrolledAccount.primaryMsisdn,
                    selected = false,
                    addedToGroup = false
                )
            }
            _enrolledAccounts.value = accounts
        }, {
            dLog("Failed to fetch enrolled accounts.")
            accounts = emptyList()
            _enrolledAccounts.value = accounts
        })
    }

    fun selectAccount(mobileNumber: String) {
        unselectAccounts()
        accounts =
            accounts.map { if (it.msisdn == mobileNumber) it.copy(selected = true) else it }
        _enrolledAccounts.value = accounts
    }

    fun unselectAccounts() {
        accounts = accounts.map { it.copy(selected = false) }
        _enrolledAccounts.value = accounts
    }

    fun addGroupMember(memberMSISDN: String, ownerAccountAlias: String) = viewModelScope.launchWithLoadingOverlay(handler) {
        groupDomainManager.addGroupMember(AddGroupMemberParams(groupInfo.groupId, memberMSISDN, ownerAccountAlias))
            .fold(
                {
                    dLog("Added group member.")
                    for (member in group) {
                        if (member.memberNumber == memberMSISDN) {
                            _addGroupMemberResult.value =
                                OneTimeEvent(AddMemberResult.SubscriberAlreadyMember)
                            return@launchWithLoadingOverlay
                        }
                    }

                    var foundAccount = false
                    for (account in accounts) {
                        if (account.msisdn == memberMSISDN) {
                            group.add(
                                GroupMemberItem(
                                    memberAccountAlias = account.name,
                                    memberRole = GROUP_ROLE_MEMBER,
                                    memberNumber = account.msisdn,
                                    walletId = groupInfo.walletId,
                                    keyword = groupInfo.wallets.firstOrNull()?.keyword ?: "",
                                    skelligCategory = groupInfo.skelligCategory,
                                    totalAllocated = groupInfo.totalAllocated,
                                    ownerMobileNumber = groupInfo.ownerMobileNumber,
                                    ownerAccountAlias = ownerAccountAlias
                                )
                            )
                            foundAccount = true
                        }
                    }

                    if (foundAccount) updateAccounts()
                    else {
                        numberOfNotEnrolledMembers++
                        group.add(
                            GroupMemberItem(
                                "Member $numberOfNotEnrolledMembers",
                                GROUP_ROLE_MEMBER,
                                memberMSISDN,
                                groupInfo.walletId,
                                groupInfo.wallets.firstOrNull()?.keyword ?: "",
                                groupInfo.skelligCategory,
                                groupInfo.totalAllocated,
                                groupInfo.ownerMobileNumber,
                                ownerAccountAlias
                            )
                        )
                    }

                    checkGroupLimit(group.size)

                    _groupMembers.value = group
                    _enrolledAccounts.value = accounts
                    _addGroupMemberResult.value = OneTimeEvent(AddMemberResult.AddMemberSuccess)
                },
                {
                    dLog("Failed to add group member.")
                    when (it) {
                        is AddGroupMemberError.SubscriberAlreadyMember -> {
                            _addGroupMemberResult.value =
                                OneTimeEvent(AddMemberResult.SubscriberAlreadyMember)
                        }

                        is AddGroupMemberError.OwnerCantBeAdded -> {
                            _addGroupMemberResult.value =
                                OneTimeEvent(AddMemberResult.OwnerCantBeAdded)
                        }

                        is AddGroupMemberError.MemberLimitReached -> {
                            _addGroupMemberResult.value =
                                OneTimeEvent(AddMemberResult.MemberLimitReached)
                        }

                        is AddGroupMemberError.PoolNotActive -> {
                            _addGroupMemberResult.value =
                                OneTimeEvent(AddMemberResult.PoolNotActive)
                        }

                        is AddGroupMemberError.PoolNotExist -> {
                            _addGroupMemberResult.value =
                                OneTimeEvent(AddMemberResult.PoolNotExist)
                        }

                        is AddGroupMemberError.SubscriberBrandNotFound -> {
                            _addGroupMemberResult.value =
                                OneTimeEvent(AddMemberResult.SubscriberBrandNotFound)
                        }

                        is AddGroupMemberError.General -> handler.handleGeneralError(it.error)

                        else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                    }
                }
            )
    }

    private fun checkGroupLimit(groupSize: Int) {
        _hideAddMemberButton.value = OneTimeEvent(groupSize == groupInfo.memberLimit)
    }

    private fun updateAccounts() {
        for (member in group) {
            accounts = accounts.map {
                if (member.memberAccountAlias == it.name)
                    it.copy(
                        selected = false,
                        addedToGroup = member.memberNumber.formattedForPhilippines() == it.msisdn.formattedForPhilippines()
                    )
                else it
            }
        }
        _enrolledAccounts.value = accounts
    }

    fun startTimer() {
        if (showBubble) {
            requestBubbleTimer.startCountDown(this)
            showBubble = false
            _showBubbleLiveData.value = OneTimeEvent(Unit)
        }
    }

    fun showRemoveMemberDialog(yesCallback: () -> Unit, noCallback: () -> Unit) {
        handler.handleDialog(
            overlayAndDialogFactories.createRemoveGroupMemberDialog(yesCallback, noCallback)
        )
    }

    fun removeMember(memberItem: GroupMemberItem) =
        viewModelScope.launchWithLoadingOverlay(handler) {
            groupDomainManager.deleteGroupMember(
                DeleteGroupMemberParams(
                    isGroupOwner = true,
                    groupId = groupInfo.groupId,
                    memberMobileNumber = memberItem.memberNumber,
                    accountAlias = memberItem.ownerAccountAlias
                )
            ).fold(
                {
                    group.remove(memberItem)
                    _groupMembers.value = group
                    updateAccounts()
                    _deleteMemberResult.value =
                        OneTimeEvent(DeleteGroupMemberResult.DeleteGroupMemberSuccess)
                }, {
                    when (it) {
                        is DeleteGroupMemberError.GroupNotExist -> {
                            _deleteMemberResult.value =
                                OneTimeEvent(DeleteGroupMemberResult.GroupNotExist)
                        }

                        is DeleteGroupMemberError.GroupMemberNotExist -> {
                            _deleteMemberResult.value =
                                OneTimeEvent(DeleteGroupMemberResult.GroupMemberNotExist)
                        }

                        is DeleteGroupMemberError.General -> handler.handleGeneralError(it.error)

                        else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                    }
                }
            )
        }

    override fun countDownFinished() {
        _hideBubble.value = OneTimeEvent(Unit)
    }

    private fun RetrieveGroupUsageResult.toGroupInfoSuccess(
        groupItem: GroupItemJson?,
        skelligCategory: String,
    ) = GroupInfoSuccess(
        walletId = walletId ?: "",
        skelligCategory = skelligCategory,
        groupId = groupItem?.groupId ?: "",
        groupOwner = groupItem?.groupOwner ?: "",
        ownerMobileNumber = owner ?: "",
        startDate = startDate?.convertDateToGroupDataFormat() ?: "",
        endDate = endDate?.toDateWithTimeZoneOrNull().toFormattedStringOrEmpty(
            GlobeDateFormat.GroupDataFormat
        ),
        status = status ?: "",
        volumeRemaining = (volumeRemaining ?: "").toIntOrNull() ?: 0,
        totalAllocated = (totalAllocated ?: "").toIntOrNull() ?: 0,
        volumeUsed = (volumeUsed ?: "").toIntOrNull() ?: 0,
        memberLimit = (groupItem?.memberLimit?.toIntOrNull() ?: 0) + 1,
        unit = unit ?: "",
        wallets = groupItem?.wallets?.map { it.toWalletItem() } ?: listOf()
    )

    private fun WalletItemJson.toWalletItem() =
        WalletItem(
            id = id,
            ownerMobileNumber = ownerMobileNumber,
            name = name,
            keyword = keyword,
            affiliateDate = affiliateDate.convertDateToGroupDataFormat(),
            withdrawalDate = withdrawalDate.convertDateToGroupDataFormat(),
            status = status
        )

    private fun GroupMember.toGroupMemberItem(ownerMobileNumber: String) =
        GroupMemberItem(
            accounts.find { it.msisdn.formattedForPhilippines() == this.mobileNumber.formattedForPhilippines() }?.name
                ?: "Member ${++numberOfNotEnrolledMembers}",
            this.mobileNumber.getMemberRole(ownerMobileNumber),
            this.mobileNumber.formattedForPhilippines(),
            groupInfo.walletId,
            groupInfo.wallets.firstOrNull()?.keyword ?: "",
            groupInfo.skelligCategory,
            groupInfo.totalAllocated,
            ownerMobileNumber,
            ownerAccountAlias
        )

    sealed class DeleteGroupMemberResult {

        object DeleteGroupMemberSuccess : DeleteGroupMemberResult()

        object GroupNotExist : DeleteGroupMemberResult()

        object GroupMemberNotExist : DeleteGroupMemberResult()
    }

    sealed class GroupInfoResult {
        data class GroupInfoSuccess(
            val walletId: String,
            val skelligCategory: String,
            val groupId: String,
            val groupOwner: String,
            val ownerMobileNumber: String,
            val startDate: String,
            val endDate: String,
            val status: String,
            val volumeRemaining: Int,
            val totalAllocated: Int,
            val volumeUsed: Int,
            val memberLimit: Int,
            val unit: String,
            val wallets: List<WalletItem>
        ) : GroupInfoResult()

        data class WalletItem(
            val id: String,
            val ownerMobileNumber: String,
            val name: String,
            val keyword: String,
            val affiliateDate: String,
            val withdrawalDate: String,
            val status: String
        )

        object MobileNumberNotFound : GroupInfoResult()

        object SubscriberNotBelongToAnyPool : GroupInfoResult()

        object GroupNotExist : GroupInfoResult()

        object WalletNotFound : GroupInfoResult()

        object GenericError : GroupInfoResult()
    }

    sealed class AddMemberResult {

        object AddMemberSuccess : AddMemberResult()

        object SubscriberAlreadyMember : AddMemberResult()

        object OwnerCantBeAdded : AddMemberResult()

        object MemberLimitReached : AddMemberResult()

        object SubscriberBrandNotFound : AddMemberResult()

        object PoolNotExist : AddMemberResult()

        object PoolNotActive : AddMemberResult()
    }

    override val logTag = "GroupViewModel"
}

const val GROUP_ROLE_MEMBER = "member"
const val GROUP_ROLE_OWNER = "owner"

fun String.getMemberRole(ownerMobileNumber: String) =
    if (ownerMobileNumber.formattedForPhilippines() == this.formattedForPhilippines()) GROUP_ROLE_OWNER else GROUP_ROLE_MEMBER
