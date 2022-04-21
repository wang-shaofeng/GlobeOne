/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group.member_overview

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.group.GroupDomainManager
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.errors.group.SetMemberUsageLimitError
import ph.com.globe.globeonesuperapp.group.member_overview.GroupDataLimitViewModel.UserInfoResult.GroupMemberInfo
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayOrDialog
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.group.RetrieveMemberUsageParams
import ph.com.globe.model.group.SetMemberUsageLimitParams
import ph.com.globe.model.util.GB_STRING
import ph.com.globe.model.util.convertGigaToKiloBytes
import ph.com.globe.model.util.convertKiloBytesToFormattedAmount
import ph.com.globe.model.util.getMegaOrGigaStringFromKiloBytesAndZero
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class GroupDataLimitViewModel @Inject constructor(
    private val groupDomainManager: GroupDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private var dataLimits = listOf(
        DataLimitItem("1", true),
        DataLimitItem("3", true),
        DataLimitItem("5", true),
        DataLimitItem("10", true),
        DataLimitItem(DATA_NO_LIMIT, true)
    )

    private lateinit var memberInfo: GroupMemberInfo

    private val _dataLimitsLiveData = MutableLiveData(dataLimits)
    val dataLimitsLiveData: LiveData<List<DataLimitItem>> = _dataLimitsLiveData

    private val _userInfoResult = MutableLiveData<UserInfoResult>()
    val userInfoResult: LiveData<UserInfoResult> = _userInfoResult

    private val _setMemberUsageResult = MutableLiveData<OneTimeEvent<SetMemberUsageResult>>()
    val setMemberUsageResult: LiveData<OneTimeEvent<SetMemberUsageResult>> = _setMemberUsageResult

    fun fetchUserInfo(
        memberMobileNumber: String,
        memberAccountAlias: String,
        ownerAccountAlias: String,
        walletId: String,
        keyword: String,
        ownerMobileNumber: String,
        groupTotalAllocated: Int
    ) = viewModelScope.launchWithLoadingOverlay(handler) {
        groupDomainManager.retrieveMemberUsage(
            RetrieveMemberUsageParams(
                // we are checking if the current user is an owner
                isGroupOwner = ownerMobileNumber.contains(memberMobileNumber),
                memberAccountAlias = memberAccountAlias,
                keyword = walletId,
                memberMobileNumber = memberMobileNumber,
                ownerMobileNumber = ownerMobileNumber
            )
        ).fold(
            { response ->
                dLog("Fetched member usage.")
                with(response.result) {
                    memberInfo = GroupMemberInfo(
                        dataUsedFormatted = "${
                            volumeUsed.toInt().convertKiloBytesToFormattedAmount()
                        } ${volumeUsed.getMegaOrGigaStringFromKiloBytesAndZero()}",
                        dataUsed = volumeUsed.toIntOrNull() ?: 0,
                        dataLimitFormatted = if (groupTotalAllocated == totalAllocated.toInt()) DATA_NO_LIMIT else "${
                            totalAllocated.toInt().convertKiloBytesToFormattedAmount()
                        } ${totalAllocated.getMegaOrGigaStringFromKiloBytesAndZero()}",
                        dataLimit = totalAllocated,
                        groupTotalAllocated = groupTotalAllocated,
                        expiredDateFormatted = endDate.convertDateToGroupDataFormat(),
                        keyword = keyword,
                        ownerMobileNumber = ownerMobileNumber,
                        mobileNumber = memberMobileNumber,
                        memberAccountAlias = memberAccountAlias,
                        ownerAccountAlias = ownerAccountAlias
                    )
                    _userInfoResult.value = memberInfo

                    dataLimits =
                        dataLimits.map {
                            it.copy(
                                enabled =
                                if (it.amount.isDigitsOnly()) it.amount.convertGigaToKiloBytes() > volumeUsed.toInt() && it.amount.convertGigaToKiloBytes() <= memberInfo.groupTotalAllocated
                                else true
                            )
                        }
                    _dataLimitsLiveData.value = dataLimits
                }
            },
            {
                dLog("Failed to fetch member usage.")
                _userInfoResult.value = UserInfoResult.GroupsApiFailure
            }
        )
    }

    fun setNewLimit(amount: String) {
        if (amount != DATA_NO_LIMIT) {
            memberInfo.dataLimitFormatted = "$amount $GB_STRING"
            memberInfo.dataLimit = amount.convertGigaToKiloBytes().toString()
        } else {
            memberInfo.dataLimitFormatted = amount
            memberInfo.dataLimit = memberInfo.groupTotalAllocated.toString()
        }
        _userInfoResult.value = memberInfo
    }

    fun saveLimit() = viewModelScope.launchWithLoadingOverlay(handler) {
        groupDomainManager.setMemberUsageLimit(
            SetMemberUsageLimitParams(
                memberMobileNumber = memberInfo.mobileNumber,
                accountAlias = memberInfo.ownerAccountAlias,
                keyword = memberInfo.keyword,
                groupName = null,
                usageLimit = memberInfo.dataLimit
            )
        ).fold(
            {
                dLog("Saved member limit.")
                _setMemberUsageResult.value =
                    OneTimeEvent(SetMemberUsageResult.SetMemberUsageSuccess)
            }, {
                dLog("Failed to save member limit.")
                when (it) {
                    is SetMemberUsageLimitError.GroupMemberNotExist -> {
                        _setMemberUsageResult.value =
                            OneTimeEvent(SetMemberUsageResult.GroupMemberNotExist)
                    }

                    is SetMemberUsageLimitError.GroupNotExist -> {
                        _setMemberUsageResult.value =
                            OneTimeEvent(SetMemberUsageResult.GroupNotExist)
                    }

                    is SetMemberUsageLimitError.WalletNotFound -> {
                        _setMemberUsageResult.value =
                            OneTimeEvent(SetMemberUsageResult.WalletNotFound)
                    }

                    is SetMemberUsageLimitError.SubscriberNotFound -> {
                        _setMemberUsageResult.value =
                            OneTimeEvent(SetMemberUsageResult.SubscriberNotFound)
                    }

                    is SetMemberUsageLimitError.ExceededTotalUsageLimit -> {
                        _setMemberUsageResult.value =
                            OneTimeEvent(SetMemberUsageResult.ExceededTotalUsageLimit)
                    }

                    is SetMemberUsageLimitError.General -> handler.handleGeneralError(it.error)

                    else -> handler.handleDialog(OverlayOrDialog.Dialog.UnknownError)
                }
            }
        )
    }

    sealed class UserInfoResult {
        data class GroupMemberInfo(
            val dataUsedFormatted: String,
            val dataUsed: Int,
            var dataLimitFormatted: String,
            var dataLimit: String,
            val groupTotalAllocated: Int,
            val expiredDateFormatted: String,
            val keyword: String,
            val ownerMobileNumber: String,
            val mobileNumber: String,
            val memberAccountAlias: String,
            val ownerAccountAlias: String
        ) : UserInfoResult()

        object GroupsApiFailure : UserInfoResult()
    }

    sealed class SetMemberUsageResult {

        object SetMemberUsageSuccess : SetMemberUsageResult()

        object GroupMemberNotExist : SetMemberUsageResult()

        object GroupNotExist : SetMemberUsageResult()

        object WalletNotFound : SetMemberUsageResult()

        object SubscriberNotFound : SetMemberUsageResult()

        object ExceededTotalUsageLimit : SetMemberUsageResult()
    }

    override val logTag = "GroupLimitViewModel"
}

const val DATA_NO_LIMIT = "No limit"
