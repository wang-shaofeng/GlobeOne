/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group.group_overview

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GroupOverviewFragmentBinding
import ph.com.globe.globeonesuperapp.group.GroupViewModel
import ph.com.globe.globeonesuperapp.group.GroupViewModel.GroupInfoResult.GroupInfoSuccess
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class GroupOverviewFragment : NoBottomNavViewBindingFragment<GroupOverviewFragmentBinding>({
    GroupOverviewFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val groupViewModel: GroupViewModel by navGraphViewModels(R.id.group_subgraph) { defaultViewModelProviderFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:manage group data screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {
            val groupMembersRecyclerViewAdapter = GroupMembersRecyclerViewAdapter { member ->
                findNavController().safeNavigate(
                    GroupOverviewFragmentDirections.actionGroupOverviewFragmentToGroupMemberOverviewFragment(
                        member
                    )
                )
            }
            rvGroupMembers.adapter = groupMembersRecyclerViewAdapter

            groupViewModel.groupMembers.observe(viewLifecycleOwner, { list ->
                groupMembersRecyclerViewAdapter.submitList(list)

                tvNumberOfMembers.text = "${list.size}"

                if (list.size > 1) {
                    tvNoGroupMembers.visibility = View.GONE
                    btnDone.visibility = View.VISIBLE
                } else {
                    tvNoGroupMembers.visibility = View.VISIBLE
                    btnDone.visibility = View.GONE
                }

                if (list.size == 2) groupViewModel.startTimer()
            })

            groupViewModel.groupProcessingResult.observe(viewLifecycleOwner, {
                when (it) {
                    is GroupInfoSuccess -> {
                        tvGroupName.text = it.skelligCategory
                        duvGroupInfo.setContent(
                            it.volumeRemaining,
                            it.totalAllocated,
                            getString(R.string.expires_on_with_formatted_date, it.endDate),
                            null
                        )
                        tvNumberOfMembersLimit.text =
                            resources.getString(
                                R.string.members_number_limit,
                                it.memberLimit.toString()
                            )
                    }
                }
            })

            groupViewModel.showBubbleLiveData.observe(viewLifecycleOwner, {
                it.handleEvent {
                    groupMembersRecyclerViewAdapter.showBubble()
                }
            })

            groupViewModel.hideBubble.observe(viewLifecycleOwner, {
                it.handleEvent {
                    groupMembersRecyclerViewAdapter.hideBubble()
                }
            })

            groupViewModel.hideAddMemberButton.observe(viewLifecycleOwner, {
                it.handleEvent { hide ->
                    if (hide) btnAddMember.visibility = View.GONE
                    else btnAddMember.visibility = View.VISIBLE
                }
            })

            btnAddMember.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        GROUP_DATA_SCREEN, BUTTON, ADD_ACCOUNT
                    )
                )
                findNavController().safeNavigate(GroupOverviewFragmentDirections.actionGroupOverviewFragmentToAddGroupMemberFragment())
            }

            btnDone.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        GROUP_DATA_SCREEN, BUTTON, DONE
                    )
                )
                crossBackstackNavigator.crossNavigateWithoutHistory(
                    BaseActivity.DASHBOARD_KEY,
                    R.id.dashboardFragment
                )
            }

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override val logTag = "GroupOverviewFragment"

    override val analyticsScreenName = "group.overview"
}
