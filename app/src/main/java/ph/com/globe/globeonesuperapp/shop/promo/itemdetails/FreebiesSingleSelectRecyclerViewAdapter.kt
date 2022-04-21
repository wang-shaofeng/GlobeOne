/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.itemdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ChipImageLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.FreebieSingleSelectItemLayoutBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel.FreebieSingleSelectItemUiModel
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.util.B_STRING
import ph.com.globe.model.util.FREEBIE_VOUCHER

class FreebiesSingleSelectRecyclerViewAdapter(val callback: (String, String, String, String, String, String) -> Unit) :
    ListAdapter<FreebieSingleSelectItemUiModel, RecyclerViewHolderBinding<out ViewBinding>>(

        object : DiffUtil.ItemCallback<FreebieSingleSelectItemUiModel>() {

            override fun areItemsTheSame(
                oldItem: FreebieSingleSelectItemUiModel,
                newItem: FreebieSingleSelectItemUiModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: FreebieSingleSelectItemUiModel,
                newItem: FreebieSingleSelectItemUiModel
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
            FreebieSingleSelectItemLayoutBinding.inflate(
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
            with(holder.viewBinding as FreebieSingleSelectItemLayoutBinding) {
                vItemTopLine.isVisible = position == 0

                tvFreebieName.text = freebieSingleSelectItem.title

                root.isSelected = selected

                tvFreebieQuotaAllocation.isInvisible =
                    freebieSingleSelectItem.type == FREEBIE_VOUCHER
                            || freebieSingleSelectItem.sizeUnit == B_STRING && freebieSingleSelectItem.size == 1L

                tvFreebieQuotaAllocation.text =
                    root.resources.getQuantityString(
                        R.plurals.freebie_single_select_item_validity,
                        freebieSingleSelectItem.duration,
                        freebieSingleSelectItem.size.toInt(),
                        freebieSingleSelectItem.sizeUnit,
                        if (freebieSingleSelectItem.duration > 1) freebieSingleSelectItem.duration.toString() else ""
                    )

                clearImages(this)
                for (icon in freebieSingleSelectItem.icons) {
                    val chip = ChipImageLayoutBinding.inflate(LayoutInflater.from(root.context))
                    GlobeGlide.with(chip.ivChipImage).load(icon).into(chip.ivChipImage)
                    cgFreebieImages.addView(chip.root)
                }

                if (position == currentList.size - 1) vItemBottomLine.visibility = View.GONE
                else vItemBottomLine.visibility = View.VISIBLE

                root.setOnClickListener {
                    callback.invoke(
                        freebieSingleSelectItem.title,
                        freebieSingleSelectItem.serviceChargeParam,
                        freebieSingleSelectItem.serviceNonChargeParam,
                        freebieSingleSelectItem.serviceNoneChargeId,
                        freebieSingleSelectItem.apiProvisioningKeyword,
                        freebieSingleSelectItem.type
                    )
                }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<out ViewBinding>) {
        super.onViewRecycled(holder)

        clearImages(holder.viewBinding as FreebieSingleSelectItemLayoutBinding)
    }

    private fun clearImages(binding: FreebieSingleSelectItemLayoutBinding) = with(binding) {
        cgFreebieImages.children.forEach {
            val image = it.findViewById<ImageView>(R.id.iv_chip_image)
            GlobeGlide.with(image).clear(image)
        }
        cgFreebieImages.removeAllViews()
    }
}
