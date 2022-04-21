/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.pos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.WhichEnrolledAccountWithPointsItemBinding
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.balance.toFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.util.brand.toUserFriendlyBrandName
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateWithTimeZoneOrNull
import ph.com.globe.util.toFormattedStringOrEmpty

class EnrolledAccountsWithPointsAdapter(private val callback: (EnrolledAccountWithBrandAndPointsUiModel) -> Unit) :
    ListAdapter<EnrolledAccountWithBrandAndPointsUiModel, RecyclerViewHolderBinding<WhichEnrolledAccountWithPointsItemBinding>>(
        EnrolledAccountWithBrandAndPointsDiffUtil
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<WhichEnrolledAccountWithPointsItemBinding> =
        RecyclerViewHolderBinding(
            WhichEnrolledAccountWithPointsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<WhichEnrolledAccountWithPointsItemBinding>,
        position: Int
    ) {
        with(holder.viewBinding) {
            with(getItem(position)) {
                tvAccountName.text = enrolledAccountWithPoints.enrolledAccount.accountAlias
                tvAccountNumber.text =
                    enrolledAccountWithPoints.enrolledAccount.primaryMsisdn.toDisplayUINumberFormat()
                tvBrand.isVisible = enrolledAccountWithPoints.brand != null
                tvBrand.text = enrolledAccountWithPoints.brand?.toUserFriendlyBrandName(
                    enrolledAccountWithPoints.enrolledAccount.segment
                )

                when {
                    isError || loadingPoints -> {
                        tvAccountName.setTextColor(
                            AppCompatResources.getColorStateList(
                                tvAccountName.context,
                                R.color.neutral_B_0
                            )
                        )
                        cbAccount.isSelected = false
                        cbAccount.isEnabled = false
                        mcvAccount.setOnClickListener(null)
                        mcvAccount.isEnabled = false
                        mcvAccount.setCardBackgroundColor(
                            AppCompatResources.getColorStateList(
                                mcvAccount.context,
                                R.color.neutral_A_5
                            ).defaultColor
                        )
                        mcvAccount.strokeWidth =
                            mcvAccount.resources.getDimension(R.dimen.stroke_width_small).toInt()
                        mcvAccount.setStrokeColor(
                            AppCompatResources.getColorStateList(
                                mcvAccount.context,
                                R.color.neutral_A_4
                            )
                        )

                        gPoints.isVisible = false
                    }
                    !loadingPoints && (enrolledAccountWithPoints.expiringAmount ?: "0") == "0" -> {
                        tvAccountName.setTextColor(
                            AppCompatResources.getColorStateList(
                                tvAccountName.context,
                                R.color.neutral_B_0
                            )
                        )
                        cbAccount.isSelected = false
                        cbAccount.isEnabled = false
                        mcvAccount.setOnClickListener(null)
                        mcvAccount.isEnabled = false
                        mcvAccount.strokeWidth =
                            mcvAccount.resources.getDimension(R.dimen.stroke_width_small).toInt()
                        mcvAccount.setStrokeColor(
                            AppCompatResources.getColorStateList(
                                mcvAccount.context,
                                R.color.neutral_A_4
                            )
                        )
                        mcvAccount.setCardBackgroundColor(
                            AppCompatResources.getColorStateList(
                                mcvAccount.context,
                                R.color.neutral_A_5
                            ).defaultColor
                        )

                        gPoints.isVisible = true

                        tvUserPts.text = tvUserPts.resources.getString(
                            R.string.pts_placeholder,
                            "0"
                        )

                        tvExpDate.text = ""
                    }
                    else -> {
                        gPoints.isVisible = true

                        tvExpDate.text = gPoints.context.getString(
                            R.string.rewards_expires_on,
                            enrolledAccountWithPoints.expiringAmount,
                            enrolledAccountWithPoints.expirationDate?.toDateWithTimeZoneOrNull()?.toFormattedStringOrEmpty(GlobeDateFormat.RewardPointsExpiry)
                        )

                        tvUserPts.text = tvUserPts.context.getString(
                            R.string.pts_placeholder,
                            enrolledAccountWithPoints.points.toFormattedDisplayBalance()
                        )

                        tvAccountName.setTextColor(
                            AppCompatResources.getColorStateList(
                                tvAccountName.context,
                                R.color.accent_dark
                            )
                        )
                        cbAccount.isEnabled = true
                        mcvAccount.setCardBackgroundColor(
                            AppCompatResources.getColorStateList(
                                mcvAccount.context,
                                R.color.absolute_white
                            ).defaultColor
                        )
                        cbAccount.isSelected = isSelected
                        mcvAccount.isEnabled = true
                        mcvAccount.setOnClickListener { callback(this) }
                        if (!isSelected) {
                            mcvAccount.strokeWidth =
                                mcvAccount.resources.getDimension(R.dimen.stroke_width_small)
                                    .toInt()
                            mcvAccount.setStrokeColor(
                                AppCompatResources.getColorStateList(
                                    mcvAccount.context,
                                    R.color.neutral_A_5
                                )
                            )
                        } else {
                            mcvAccount.strokeWidth =
                                mcvAccount.resources.getDimension(R.dimen.stroke_width_standard)
                                    .toInt()
                            mcvAccount.setStrokeColor(
                                AppCompatResources.getColorStateList(
                                    mcvAccount.context,
                                    R.color.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<WhichEnrolledAccountWithPointsItemBinding>) {
        super.onViewRecycled(holder)

        holder.viewBinding.mcvAccount.setOnCheckedChangeListener(null)
    }
}

private object EnrolledAccountWithBrandAndPointsDiffUtil :
    DiffUtil.ItemCallback<EnrolledAccountWithBrandAndPointsUiModel>() {
    override fun areItemsTheSame(
        oldItem: EnrolledAccountWithBrandAndPointsUiModel,
        newItem: EnrolledAccountWithBrandAndPointsUiModel
    ): Boolean =
        oldItem.enrolledAccountWithPoints.enrolledAccount == newItem.enrolledAccountWithPoints.enrolledAccount

    override fun areContentsTheSame(
        oldItem: EnrolledAccountWithBrandAndPointsUiModel,
        newItem: EnrolledAccountWithBrandAndPointsUiModel
    ): Boolean = oldItem == newItem
}
