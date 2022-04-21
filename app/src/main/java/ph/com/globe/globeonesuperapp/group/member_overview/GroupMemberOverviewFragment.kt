/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group.member_overview

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GroupMemberOverviewFragmentBinding
import ph.com.globe.globeonesuperapp.group.GroupViewModel
import ph.com.globe.globeonesuperapp.group.GroupViewModel.DeleteGroupMemberResult.DeleteGroupMemberSuccess
import ph.com.globe.globeonesuperapp.group.member_overview.GroupDataLimitViewModel.SetMemberUsageResult.SetMemberUsageSuccess
import ph.com.globe.globeonesuperapp.group.member_overview.GroupDataLimitViewModel.UserInfoResult.GroupMemberInfo
import ph.com.globe.globeonesuperapp.group.member_overview.GroupDataLimitViewModel.UserInfoResult.GroupsApiFailure
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.formatPhoneNumber
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GroupMemberOverviewFragment :
    NoBottomNavViewBindingFragment<GroupMemberOverviewFragmentBinding>({
        GroupMemberOverviewFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val appDataViewModel: AppDataViewModel by activityViewModels()

    private val groupViewModel: GroupViewModel by navGraphViewModels(R.id.group_subgraph) { defaultViewModelProviderFactory }
    private val groupDataLimitViewModel: GroupDataLimitViewModel by viewModels()

    private val groupMemberOverviewFragmentArgs by navArgs<GroupMemberOverviewFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groupMember = groupMemberOverviewFragmentArgs.groupMember
        groupDataLimitViewModel.fetchUserInfo(
            memberMobileNumber = groupMember.memberNumber,
            memberAccountAlias = groupMember.memberAccountAlias,
            ownerAccountAlias = groupMember.ownerAccountAlias,
            walletId = groupMember.walletId,
            keyword = groupMember.keyword,
            ownerMobileNumber = groupMember.ownerMobileNumber,
            groupTotalAllocated = groupMember.totalAllocated,
        )

        with(viewBinding) {
            val groupDataLimitRecyclerViewAdapter = GroupDataLimitRecyclerViewAdapter {
                groupDataLimitViewModel.setNewLimit(it)
                tvSetDataLimitAnytime.visibility = View.VISIBLE
                exDataLimit.collapse()
                btnSave.isEnabled = true
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        GROUP_DATA_SCREEN, CLICKABLE_TEXT, it
                    )
                )
            }
            exDataLimit.secondLayout.findViewById<RecyclerView>(R.id.rv_data_limits).adapter =
                groupDataLimitRecyclerViewAdapter

            tvGroupName.text = groupMember.skelligCategory
            tvMemberRole.text = groupMember.memberRole
            tvMemberName.text = groupMember.memberAccountAlias
            tvMemberMobileNumber.text = groupMember.memberNumber.formatPhoneNumber()

            exDataLimit.setOnClickListener {
                if (exDataLimit.isExpanded) {
                    tvSetDataLimitAnytime.visibility = View.VISIBLE
                    exDataLimit.collapse()
                } else {
                    tvSetDataLimitAnytime.visibility = View.GONE
                    exDataLimit.expand()
                }
            }

            btnSave.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        GROUP_DATA_SCREEN, CLICKABLE_TEXT, SAVE
                    )
                )
                groupDataLimitViewModel.saveLimit()
            }

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            tvRemoveMember.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        GROUP_DATA_SCREEN, CLICKABLE_TEXT, REMOVE
                    )
                )
                groupViewModel.showRemoveMemberDialog({
                    groupViewModel.removeMember(groupMemberOverviewFragmentArgs.groupMember)
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            GROUP_DATA_SCREEN, CLICKABLE_TEXT, YES
                        )
                    )
                }, {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            GROUP_DATA_SCREEN, CLICKABLE_TEXT, NO
                        )
                    )
                })
            }

            groupDataLimitViewModel.dataLimitsLiveData.observe(viewLifecycleOwner, { list ->
                groupDataLimitRecyclerViewAdapter.submitList(list)
            })

            groupDataLimitViewModel.userInfoResult.observe(viewLifecycleOwner, { memberInfo ->
                when(memberInfo) {
                    is GroupMemberInfo -> {
                        val deviceDate = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
                        tvGroupStartDate.text =
                            resources.getString(R.string.group_start_date, deviceDate)
                        if (memberInfo.dataLimitFormatted == DATA_NO_LIMIT)
                            tvMemberUsage.text = memberInfo.dataUsedFormatted
                        else tvMemberUsage.text = resources.getString(
                            R.string.member_data_usage,
                            memberInfo.dataUsedFormatted,
                            memberInfo.dataLimitFormatted
                        )
                        exDataLimit.parentLayout.findViewById<TextView>(R.id.tv_current_limit).text =
                            memberInfo.dataLimitFormatted
                    }

                    is GroupsApiFailure -> {
                        findNavController().safeNavigate(
                            GroupMemberOverviewFragmentDirections.actionGroupMemberOverviewFragmentToGroupApiFailureFragment()
                        )
                    }
                }
            })

            groupDataLimitViewModel.setMemberUsageResult.observe(viewLifecycleOwner, {
                it.handleEvent { result ->
                    if (result is SetMemberUsageSuccess) {
                        appDataViewModel.refreshAccountDetailsData(groupMember.memberNumber)
                        findNavController().navigateUp()
                    }
                }
            })

            groupViewModel.deleteMemberResult.observe(viewLifecycleOwner, {
                it.handleEvent { result ->
                    if (result is DeleteGroupMemberSuccess) {
                        appDataViewModel.refreshAccountDetailsData(groupMember.memberNumber)
                        findNavController().navigateUp()
                    }
                }
            })
        }
    }

    override val logTag = "GroupMemberOverviewFragment"

    override val analyticsScreenName = "group.member_overview"
}
