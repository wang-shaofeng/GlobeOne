/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.allrewards

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.coming_soon.showComingSoonDialog
import ph.com.globe.model.rewards.RewardsCatalogItem
import ph.com.globe.model.rewards.RewardsCategory

class RewardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val context = itemView.context
    private val ivType: ImageView = itemView.findViewById(R.id.iv_type_colored)
    private val tvType: TextView = itemView.findViewById(R.id.tv_type)
    private val tvName: TextView = itemView.findViewById(R.id.tv_name)
    private val tvPts: TextView = itemView.findViewById(R.id.tv_pts)

    fun bind(item: RewardsCatalogItem) {
        itemView.setOnClickListener {
            context.showComingSoonDialog()
        }

        tvPts.text =
            if (item.pointsCost != "0")
                context.resources.getQuantityString(
                    R.plurals.reward_points_short,
                    item.pointsCost.toInt(),
                    item.pointsCost.toInt()
                )
            else context.getString(R.string.free)

        tvName.text = item.name

        when (item.category) {
            RewardsCategory.OTHER -> {
                tvType.setText(R.string.other_category)
                ivType.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        if (item.pointsCost == "0") R.color.corporate_D_500
                        else R.color.corporate_A_400
                    ), PorterDuff.Mode.SRC_IN
                )
            }
            RewardsCategory.DONATION -> {
                tvType.setText(R.string.donation_category)
                ivType.setColorFilter(
                    ContextCompat.getColor(context, R.color.corporate_C_400),
                    PorterDuff.Mode.SRC_IN
                )
            }
            RewardsCategory.PROMO -> {
                tvType.setText(R.string.promo_category)
                ivType.setColorFilter(
                    ContextCompat.getColor(context, R.color.prepaid_E_500),
                    PorterDuff.Mode.SRC_IN
                )
            }
            RewardsCategory.RAFFLE -> {
                tvType.setText(R.string.raffle_category)
                ivType.setColorFilter(
                    ContextCompat.getColor(context, R.color.prepaid_D_600),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    companion object {
        fun inflate(layoutInflater: LayoutInflater, parent: ViewGroup): RewardHolder =
            RewardHolder(layoutInflater.inflate(R.layout.reward_item_new, parent, false))
    }
}
