/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.rewards_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.utils.ui.KtItemHolder

class EmptyStateAdapter : RecyclerView.Adapter<KtItemHolder>() {

    var visibility: Int = View.GONE
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtItemHolder =
        KtItemHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rewards_empty_state_item, parent, false)
        )

    override fun onBindViewHolder(holder: KtItemHolder, position: Int) = Unit

    override fun getItemCount(): Int = if (visibility == View.GONE) 0 else 1
}
