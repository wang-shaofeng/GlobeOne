/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.databinding.SearchRecommendationItemLayoutBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding

class SearchRecommendationRecyclerViewAdapter(private val callback: (String) -> Unit) :
    ListAdapter<String, RecyclerViewHolderBinding<SearchRecommendationItemLayoutBinding>>(
        object : DiffUtil.ItemCallback<String>() {

            override fun areItemsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: String,
                newItem: String
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolderBinding(
            SearchRecommendationItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<SearchRecommendationItemLayoutBinding>,
        position: Int
    ) {
        val item = currentList[position]

        with(holder.viewBinding) {
            tvRecommendation.text = item

            root.setOnClickListener {
                callback.invoke(item)
            }
        }
    }
}
