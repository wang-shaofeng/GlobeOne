/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate.selection

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ChipImageLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.GocreateBoosterItemLayoutBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding

class BoosterSelectRecyclerViewAdapter(val callback: (GoCreateBoosterItem) -> Unit) :
    ListAdapter<GoCreateBoosterItem, RecyclerViewHolderBinding<out ViewBinding>>(

        object : DiffUtil.ItemCallback<GoCreateBoosterItem>() {

            override fun areItemsTheSame(
                oldItem: GoCreateBoosterItem,
                newItem: GoCreateBoosterItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: GoCreateBoosterItem,
                newItem: GoCreateBoosterItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<out ViewBinding> =
        RecyclerViewHolderBinding(
            GocreateBoosterItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        with(getItem(position)) {
            with(holder.viewBinding as GocreateBoosterItemLayoutBinding) {
                tvFreebieName.text = title

                root.isSelected = selected

                clearImages(this)
                for (icon in icons) {
                    val chip = ChipImageLayoutBinding.inflate(LayoutInflater.from(root.context))
                    GlobeGlide.with(chip.ivChipImage).load(icon).into(chip.ivChipImage)
                    cgFreebieImages.addView(chip.root)
                }

                vItemBottomLine.isVisible = position != currentList.size - 1

                root.setOnClickListener {
                    callback.invoke(getItem(position))
                }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<out ViewBinding>) {
        super.onViewRecycled(holder)

        clearImages(holder.viewBinding as GocreateBoosterItemLayoutBinding)
    }

    private fun clearImages(binding: GocreateBoosterItemLayoutBinding) = with(binding) {
        cgFreebieImages.children.forEach {
            val image = it.findViewById<ImageView>(R.id.iv_chip_image)
            GlobeGlide.with(image).clear(image)
        }
        cgFreebieImages.removeAllViews()
    }
}
