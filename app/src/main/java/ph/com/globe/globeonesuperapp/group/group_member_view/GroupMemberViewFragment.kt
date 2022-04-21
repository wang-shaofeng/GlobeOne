/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group.group_member_view

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GroupMemberViewFragmentBinding
import ph.com.globe.globeonesuperapp.group.GroupViewModel.DeleteGroupMemberResult.DeleteGroupMemberSuccess
import ph.com.globe.globeonesuperapp.group.group_member_view.GroupMemberViewModel.RetrieveGroupMemberInfoResult.RetrieveGroupMemberInfoSuccess
import ph.com.globe.globeonesuperapp.group.member_overview.DATA_NO_LIMIT
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.formatPhoneNumber
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.shop.formattedForPhilippines
import javax.inject.Inject

@AndroidEntryPoint
class GroupMemberViewFragment : NoBottomNavViewBindingFragment<GroupMemberViewFragmentBinding>({
    GroupMemberViewFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val appDataViewModel: AppDataViewModel by activityViewModels()
    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val groupMemberViewFragmentArgs by navArgs<GroupMemberViewFragmentArgs>()
    private val groupMemberViewModel: GroupMemberViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupMemberViewModel.fetchData(
            groupOwnerMsisdn = groupMemberViewFragmentArgs.groupOwnerNumber,
            skelligWallet = groupMemberViewFragmentArgs.skelligWallet,
            skelligCategory = groupMemberViewFragmentArgs.skelligCategory,
            accountAlias = groupMemberViewFragmentArgs.accountAlias,
            memberMsisdn = groupMemberViewFragmentArgs.accountMobileNumber
        )

        with(viewBinding) {
            tvGroupMemberLearnMore.setOnClickListener {
                generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(LEARN_MORE_URL))
                    startActivity(intent)
                }
            }
            tvLeaveGroup.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ACCOUNT_DETAILS_SCREEN, CLICKABLE_TEXT, LEAVE_GROUP
                    )
                )
                groupMemberViewModel.showRemoveMemberDialog({
                    groupMemberViewModel.removeMember(
                        accountAlias = groupMemberViewFragmentArgs.accountAlias
                    )
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            ACCOUNT_DETAILS_SCREEN, CLICKABLE_TEXT, YES
                        )
                    )
                }, {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            ACCOUNT_DETAILS_SCREEN, CLICKABLE_TEXT, NO
                        )
                    )
                })
            }

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            groupMemberViewModel.groupMemberInfoResult.observe(viewLifecycleOwner, {
                if (it is RetrieveGroupMemberInfoSuccess) {
                    tvGroupName.text = it.groupName
                    tvMemberMobileNumber.text =
                        it.memberMobileNumber.formattedForPhilippines().formatPhoneNumber()

                    if (it.dataLimitFormatted == DATA_NO_LIMIT)
                        tvMemberUsage.text = it.dataUsedFormatted
                    else {
                        val text = resources.getString(
                            R.string.member_data_usage,
                            it.dataUsedFormatted,
                            it.dataLimitFormatted
                        )
                        val color = MaterialColors.getColor(tvMemberUsage, R.attr.neutralA3)
                        tvMemberUsage.text = SpannableString(text).apply {
                            setSpan(
                                ForegroundColorSpan(color),
                                text.indexOf('/'),
                                text.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                    tvMemberUsageCap.text = SpannableString(
                        getString(
                            R.string.a_gb_was_set_for_your_usage,
                            it.dataLimitFormatted
                        )
                    ).apply {
                        setSpan(
                            StyleSpan(Typeface.BOLD),
                            indexOf(it.dataLimitFormatted),
                            indexOf(it.dataLimitFormatted) + it.dataLimitFormatted.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    tvMemberUsageCap.visibility = View.VISIBLE
                    tvMemberUsageError.isVisible = it.dataLeft == 0
                }
            })

            groupMemberViewModel.deleteMemberResult.observe(viewLifecycleOwner, {
                it.handleEvent {
                    when (it) {
                        is DeleteGroupMemberSuccess -> {
                            appDataViewModel.refreshAccountDetailsData(groupMemberViewFragmentArgs.accountMobileNumber)
                            findNavController().safeNavigate(GroupMemberViewFragmentDirections.actionGroupMemberViewFragmentToLeaveGroupSuccessfulFragment())
                        }

                        else -> {
                            findNavController().safeNavigate(GroupMemberViewFragmentDirections.actionGroupMemberViewFragmentToLeaveGroupUnsuccessfulFragment())
                        }
                    }
                }
            })
        }
    }

    override val logTag = "GroupMemberViewFragment"

    override val analyticsScreenName = "group.member_view"
}

private const val LEARN_MORE_URL = "https://www.globe.com.ph/help/surf4all.html"

