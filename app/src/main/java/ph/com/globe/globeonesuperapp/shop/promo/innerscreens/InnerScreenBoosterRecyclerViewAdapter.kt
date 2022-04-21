/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.innerscreens

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ChipImageLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.ShopBoosterItemLayoutBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.shop.promo.filter.BoosterDetailsItem
import ph.com.globe.globeonesuperapp.utils.payment.stringToPesos
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding

class InnerScreenBoosterRecyclerViewAdapter(val callback: (BoosterDetailsItem) -> Unit) :
    ListAdapter<BoosterDetailsItem, RecyclerViewHolderBinding<ShopBoosterItemLayoutBinding>>(
        object : DiffUtil.ItemCallback<BoosterDetailsItem>() {
            override fun areItemsTheSame(oldItem: BoosterDetailsItem, newItem: BoosterDetailsItem): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: BoosterDetailsItem, newItem: BoosterDetailsItem): Boolean =
                oldItem == newItem

        }) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<ShopBoosterItemLayoutBinding> =
        RecyclerViewHolderBinding(
            ShopBoosterItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<ShopBoosterItemLayoutBinding>,
        position: Int
    ) {
        val item = getItem(position)
        with(holder.viewBinding) {
            tvBoosterName.text = item.name
            tvBoosterDescription.text = item.applicationService?.description
            tvBoosterPrice.text = item.price.stringToPesos()

            item.applicationService?.apps?.let {
                clearImages(this)
                cgAppsImages.setChipSpacing(
                    root.resources.getDimension(R.dimen.margin_xxxsmall).toInt()
                )
                for (app in it) {
                    val chip = ChipImageLayoutBinding.inflate(LayoutInflater.from(root.context))
                    GlobeGlide.with(chip.ivChipImage).load(app.appIcon).into(chip.ivChipImage)
                    cgAppsImages.addView(chip.root)
                }
            }

            clShopBoosterItemLayout.apply {
                setOnClickListener {
                    callback.invoke(item)
                }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<ShopBoosterItemLayoutBinding>) {
        super.onViewRecycled(holder)

        clearImages(holder.viewBinding)
    }

    private fun clearImages(binding: ShopBoosterItemLayoutBinding) = with(binding) {
        cgAppsImages.children.forEach {
            val image = it.findViewById<ImageView>(R.id.iv_chip_image)
            GlobeGlide.with(image).clear(image)
        }
        cgAppsImages.removeAllViews()
    }
}
