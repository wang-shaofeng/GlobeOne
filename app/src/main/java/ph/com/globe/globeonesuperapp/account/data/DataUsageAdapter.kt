/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.data

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.domain.utils.isNoExpiry
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsConsumptionItemDataBinding
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsGroupMemberConsumptionItemDataBinding
import ph.com.globe.globeonesuperapp.group.GROUP_ROLE_MEMBER
import ph.com.globe.globeonesuperapp.utils.ui.KtItemHolder
import ph.com.globe.model.group.domain_models.UsageItem
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isPostpaidMobile
import ph.com.globe.model.util.convertKiloBytesToMegaOrGiga
import ph.com.globe.model.util.getDataUsageAmount
import ph.com.globe.model.util.getMegaOrGigaStringFromKiloBytes
import ph.com.globe.model.util.toFormattedConsumption

class DataUsageAdapter(
    private val enrolledAccount: EnrolledAccount,
    private val itemCallback: (UsageItem, String) -> Unit,
    private val learnMoreCallback: () -> Unit
) :
    ListAdapter<UsageItem, KtItemHolder>(
        object : DiffUtil.ItemCallback<UsageItem>() {

            override fun areItemsTheSame(oldItem: UsageItem, newItem: UsageItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: UsageItem,
                newItem: UsageItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    private var postpaidMobileRefreshDate = ""

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): KtItemHolder = when (viewType) {
        // if user is part of group data as a member other type of view will be shown
        GROUP_DATA_MEMBER_USAGE_VIEW -> GroupMemberUsageHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.account_details_group_member_consumption_item_data, parent, false)
        )
        // else, we show generic data consumption view
        else -> KtItemHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.account_details_consumption_item_data, parent, false)
        )
    }

    override fun onBindViewHolder(holder: KtItemHolder, position: Int) {
        val usageItem = getItem(position)
        when (holder) {
            is GroupMemberUsageHolder -> holder.bind(position)
            else -> {
                with(holder.containerView) {
                    with(usageItem) {
                        AccountDetailsConsumptionItemDataBinding.bind(holder.containerView).apply {
                            setOnClickListener {
                                itemCallback.invoke(usageItem, duvUsage.getUsageInfo())
                            }
                            tvSubscriptionName.text = title
                            tvGroupMemberRole.apply {
                                text = accountRole
                                isVisible = accountRole.isNotEmpty()
                            }

                            groupAddOnData.isVisible = addOnData

                            val formattedDate = if (enrolledAccount.isPostpaidMobile()) {
                                context.getString(R.string.refreshes_on, postpaidMobileRefreshDate)
                            } else {
                                if (expiration.isNoExpiry()) {
                                    context.getString(R.string.valid_for_no_expiry)
                                } else {
                                    context.getString(
                                        R.string.expires_on_with_formatted_date,
                                        expiration
                                    )
                                }
                            }

                            duvUsage.setContent(
                                usageItem.left,
                                total,
                                formattedDate,
                                apps,
                                isUnlimited,
                                learnMoreCallback
                            )
                        }
                    }
                }
            }
        }
    }

    inner class GroupMemberUsageHolder(viewItem: View) : KtItemHolder(viewItem) {
        fun bind(position: Int) {
            val usageItem = getItem(position)
            with(containerView) {
                AccountDetailsGroupMemberConsumptionItemDataBinding.bind(containerView).apply {
                    setOnClickListener {
                        itemCallback.invoke(usageItem, tvDataUsageUsage.text.toString())
                    }
                    tvSubscriptionName.text = usageItem.title
                    tvGroupMemberRole.apply {
                        text = usageItem.accountRole
                        isVisible = usageItem.accountRole.isNotEmpty()
                    }

                    groupAddOnData.isVisible = usageItem.addOnData

                    val formattedDate = if (enrolledAccount.isPostpaidMobile()) {
                        context.getString(R.string.refreshes_on, postpaidMobileRefreshDate)
                    } else {
                        if (usageItem.expiration.isNoExpiry()) {
                            context.getString(R.string.valid_for_no_expiry)
                        } else {
                            context.getString(
                                R.string.expires_on_with_formatted_date,
                                usageItem.expiration
                            )
                        }
                    }
                    tvDataUsageExpiration.text = if (usageItem.isUnlimited) {
                        resources.getString(R.string.enjoy_your_surfing)
                    } else formattedDate
                    val formattedTotalDisplay =
                        "${usageItem.total.convertKiloBytesToMegaOrGiga()}${usageItem.total.getMegaOrGigaStringFromKiloBytes()}"
                    tvDataUsageCap.text = SpannableString(
                        resources.getString(
                            R.string.a_gb_was_set_for_your_usage,
                            formattedTotalDisplay
                        )
                    ).apply {
                        setSpan(
                            StyleSpan(Typeface.BOLD),
                            indexOf(formattedTotalDisplay),
                            indexOf(formattedTotalDisplay) + formattedTotalDisplay.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    tvDataUsageUsage.text = if (usageItem.isUnlimited) {
                        resources.getString(R.string.unli)
                    } else {
                        val formattedString = getDataUsageAmount(usageItem.used, usageItem.total)
                            .toFormattedConsumption()

                        SpannableString(formattedString).apply {
                            setSpan(
                                ForegroundColorSpan(
                                    AppCompatResources.getColorStateList(
                                        context,
                                        R.color.neutral_B_0
                                    ).defaultColor
                                ),
                                formattedString.indexOf("/"),
                                formattedString.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                    tvDataUsageError.isVisible = usageItem.left == 0
                    ivUnlimited.isVisible = usageItem.isUnlimited
                }
            }
        }
    }

    fun setRefreshDate(date: String) {
        postpaidMobileRefreshDate = date
    }

    override fun getItemViewType(position: Int): Int =
        if (currentList[position].accountRole == GROUP_ROLE_MEMBER) {
            GROUP_DATA_MEMBER_USAGE_VIEW
        } else {
            GENERAL_DATA_USAGE_VIEW
        }

    companion object {
        private const val GENERAL_DATA_USAGE_VIEW = 0
        private const val GROUP_DATA_MEMBER_USAGE_VIEW = 1
    }
}
