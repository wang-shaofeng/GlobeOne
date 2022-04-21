/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.rewards_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.utils.ui.KtItemHolder

class TextAdapter(private val text: String) : RecyclerView.Adapter<KtItemHolder>() {

    private var visibility = View.GONE

    var isVisible: Boolean = false
        set(value) {
            field = value
            visibility = if (value) View.VISIBLE else View.GONE

            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtItemHolder =
        KtItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.text_item, parent, false))

    override fun onBindViewHolder(holder: KtItemHolder, position: Int) {
        (holder.itemView as TextView).apply {
            text = this@TextAdapter.text
            visibility = this@TextAdapter.visibility
        }
    }

    override fun getItemCount(): Int = 1
}
