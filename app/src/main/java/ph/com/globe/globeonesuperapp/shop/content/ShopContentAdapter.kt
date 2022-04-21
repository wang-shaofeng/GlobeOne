/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.content

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopContentItemBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.utils.payment.intToPesos
import ph.com.globe.globeonesuperapp.utils.payment.setValidityText
import ph.com.globe.globeonesuperapp.utils.payment.stringToPesos
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.model.shop.domain_models.TYPE_DISCOUNTED
import ph.com.globe.model.shop.domain_models.TYPE_LIMITED
import ph.com.globe.model.shop.domain_models.TYPE_NEW

class ShopContentAdapter(private val callback: (ShopItem) -> Unit) :
    ListAdapter<ShopItem, RecyclerViewHolderBinding<ShopContentItemBinding>>(
        object : DiffUtil.ItemCallback<ShopItem>() {
            override fun areItemsTheSame(
                oldItem: ShopItem,
                newItem: ShopItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ShopItem,
                newItem: ShopItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolderBinding(
            ShopContentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<ShopContentItemBinding>,
        position: Int
    ) {
        val promo = getItem(position)

        with(holder.viewBinding) {
            GlobeGlide.with(ivPromoIcon).load(promo.asset).into(ivPromoIcon)

            tvPromoName.text = promo.name

            val promoType = when {
                promo.types.contains(TYPE_NEW) -> {
                    root.resources.getString(R.string.type_new)
                }
                promo.types.contains(TYPE_LIMITED) -> {
                    root.resources.getString(R.string.type_limited)
                }
                promo.types.contains(TYPE_DISCOUNTED) -> {
                    root.resources.getString(R.string.type_discounted)
                }
                else -> ""
            }

            tvPromoType.apply {
                text = promoType
                isVisible = promoType.isNotEmpty()
            }

            if (promo.discount.isNullOrBlank()) {
                tvPromoPrice.text = promo.price.stringToPesos()
                tvPromoPriceOld.visibility = View.GONE
            } else {
                val price = promo.price.toInt()
                val discount = promo.discount?.toInt() ?: 0

                tvPromoPrice.text = (price - discount).intToPesos()
                tvPromoPriceOld.apply {
                    text = price.intToPesos()
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    visibility = View.VISIBLE
                }
            }

            tvValid.text = root.resources.getString(
                R.string.content_valid_for,
                root.resources.setValidityText(promo.validity)
            )

            root.setOnClickListener { callback.invoke(promo) }
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<ShopContentItemBinding>) {
        super.onViewRecycled(holder)

        with(holder.viewBinding) {
            GlobeGlide.with(ivPromoIcon).clear(ivPromoIcon)
        }
    }
}
