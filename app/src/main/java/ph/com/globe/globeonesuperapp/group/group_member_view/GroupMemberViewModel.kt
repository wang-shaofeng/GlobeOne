/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group.group_member_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.group.GroupDomainManager
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.errors.group.DeleteGroupMemberError
import ph.com.globe.globeonesuperapp.group.GroupViewModel.DeleteGroupMemberResult
import ph.com.globe.globeonesuperapp.group.group_member_view.GroupMemberViewModel.RetrieveGroupMemberInfoResult.RetrieveGroupMemberInfoSuccess
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.group.DeleteGroupMemberParams
import ph.com.globe.model.group.GetGroupListParams
import ph.com.globe.model.group.RetrieveMemberUsageParams
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.model.util.convertKiloBytesToFormattedAmount
import ph.com.globe.model.util.convertKiloBytesToMegaOrGiga
import ph.com.globe.model.util.getMegaOrGigaStringFromKiloBytesAndZero
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import javax.inject.Inject

@HiltViewModel
class GroupMemberViewModel @Inject constructor(
    private val groupDomainManager: GroupDomainManager,
    private val overlayAndDialogFactories: OverlayAndDialogFactories
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _groupMemberInfoResult = MutableLiveData<RetrieveGroupMemberInfoResult?>()
    val groupMemberInfoResult: LiveData<RetrieveGroupMemberInfoResult?> = _groupMemberInfoResult

    private val _deleteMemberResult = MutableLiveData<OneTimeEvent<DeleteGroupMemberResult>>()
    val deleteMemberResult: LiveData<OneTimeEvent<DeleteGroupMemberResult>> = _deleteMemberResult

    private var groupInfo: RetrieveGroupMemberInfoSuccess? = null

    fun fetchData(
        groupOwnerMsisdn: String,
        skelligWallet: String,
        skelligCategory: String,
        memberMsisdn: String,
        accountAlias: String
    ) = viewModelScope.launchWithLoadingOverlay(handler) {

        val getGroupListResponse = groupDomainManager.getGroupList(
            GetGroupListParams(
                accountAlias = accountAlias,
                isGroupOwner = groupOwnerMsisdn.contains(memberMsisdn.convertToClassicNumberFormat())
            )
        ).successOrErrorAction {
            dLog("Failed to get group list.")
            _groupMemberInfoResult.value = RetrieveGroupMemberInfoResult.GroupsApiFailure
            return@launchWithLoadingOverlay
        }

        skelligWallet.let { walletId ->
            groupDomainManager.retrieveMemberUsage(
                RetrieveMemberUsageParams(
                    isGroupOwner = false,
                    memberAccountAlias = accountAlias,
                    keyword = walletId,
                    memberMobileNumber = memberMsisdn,
                    ownerMobileNumber = groupOwnerMsisdn
                )
            ).fold(
                { retrieveMemberUsageResponse ->
                    with(retrieveMemberUsageResponse.result) {
                        dLog("Fetched member info")
                        getGroupListResponse.result.groups.find { it.ownerMobileNumber.formattedForPhilippines() == groupOwnerMsisdn.formattedForPhilippines() }
                            ?.let {
                                groupInfo = RetrieveGroupMemberInfoSuccess(
                                    groupName = skelligCategory,
                                    groupId = it.groupId,
                                    memberMobileNumber = memberMsisdn,
                                    dataUsedFormatted = "${
                                        volumeUsed.toInt().convertKiloBytesToFormattedAmount()
                                    } ${volumeUsed.getMegaOrGigaStringFromKiloBytesAndZero()}",
                                    dataUsed = (volumeUsed.toIntOrNull()
                                        ?: 0).convertKiloBytesToMegaOrGiga().toInt(),
                                    dataLimit = (totalAllocated.toIntOrNull()
                                        ?: 0).convertKiloBytesToMegaOrGiga().toInt(),
                                    dataLimitFormatted = "${
                                        totalAllocated.toInt().convertKiloBytesToFormattedAmount()
                                    } ${totalAllocated.getMegaOrGigaStringFromKiloBytesAndZero()}",
                                    dataLeft = (volumeRemaining.toIntOrNull()
                                        ?: 0).convertKiloBytesToMegaOrGiga().toInt(),
                                    expiredDateFormatted = endDate.convertDateToGroupDataFormat()
                                )
                                _groupMemberInfoResult.value = groupInfo
                                return@launchWithLoadingOverlay
                            }
                    }
                },
                {
                    dLog("Failed to fetch member usage.")
                    _groupMemberInfoResult.value = RetrieveGroupMemberInfoResult.GroupsApiFailure
                    return@launchWithLoadingOverlay
                }
            )
        }

        dLog("Failed to fetch member info.")
        _groupMemberInfoResult.value = RetrieveGroupMemberInfoResult.GroupsApiFailure
    }

    fun showRemoveMemberDialog(yesCallback: () -> Unit, noCallback: () -> Unit) {
        handler.handleDialog(
            overlayAndDialogFactories.createLeaveGroupDialog(yesCallback, noCallback)
        )
    }

    fun removeMember(accountAlias: String) = viewModelScope.launchWithLoadingOverlay(handler) {
        groupDomainManager.deleteGroupMember(
            DeleteGroupMemberParams(
                groupId = groupInfo?.groupId ?: "",
                accountAlias = accountAlias,
                isGroupOwner = false
            )
        ).fold(
            {
                dLog("Left group.")
                _deleteMemberResult.value =
                    OneTimeEvent(DeleteGroupMemberResult.DeleteGroupMemberSuccess)
            }, {
                dLog("Failed to leave group.")
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

    sealed class RetrieveGroupMemberInfoResult {
        data class RetrieveGroupMemberInfoSuccess(
            val groupName: String,
            val groupId: String,
            val memberMobileNumber: String,
            val dataUsedFormatted: String,
            val dataUsed: Int,
            val dataLimitFormatted: String,
            val dataLimit: Int,
            val dataLeft: Int,
            val expiredDateFormatted: String
        ) : RetrieveGroupMemberInfoResult()

        object GroupsApiFailure : RetrieveGroupMemberInfoResult()
    }

    override val logTag = "GroupMemberViewModel"
}
