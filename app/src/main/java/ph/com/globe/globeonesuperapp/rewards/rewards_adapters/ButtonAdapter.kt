/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.rewards_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.utils.ui.KtItemHolder

class ButtonAdapter(private val callback: () -> Unit) : RecyclerView.Adapter<KtItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtItemHolder =
        KtItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.button_item, parent, false)
        )

    override fun onBindViewHolder(holder: KtItemHolder, position: Int) {
        holder.itemView.setOnClickListener { callback() }
    }

    override fun getItemCount(): Int = 1
}
