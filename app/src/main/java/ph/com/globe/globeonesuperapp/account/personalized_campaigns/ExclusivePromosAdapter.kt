/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.personalized_campaigns

import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.analytics.logger.eLog
import ph.com.globe.data.db.shop.UNLIMITED_VALUE_INDICATOR
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ExclusivePromosItemBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.utils.payment.intToPesos
import ph.com.globe.globeonesuperapp.utils.payment.stringToPesos
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.account.AvailableCampaignPromosModel
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.model.shop.domain_models.TYPE_DISCOUNTED
import ph.com.globe.model.util.*
import java.text.DecimalFormat

class ExclusivePromosAdapter(private val callback: (ShopItem, AvailableCampaignPromosModel) -> Unit) :
    ListAdapter<Pair<ShopItem, AvailableCampaignPromosModel>, RecyclerViewHolderBinding<ExclusivePromosItemBinding>>(
        shopItemDiffUtil
    ),
    HasLogTag {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolderBinding(
            ExclusivePromosItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<ExclusivePromosItemBinding>,
        position: Int
    ) {
        with(holder.viewBinding) {
            getItem(position).let { (shopItem, model) ->
                val itemPrice = shopItem.price.toInt() - (shopItem.discount ?: "0").toInt()
                try {
                    vColor.setBackgroundColor(Color.parseColor(shopItem.displayColor))
                } catch (e: Exception) {
                    vColor.setBackgroundColor(Color.parseColor("#000000"))
                    eLog(Exception("displayColor has a bad format: ${shopItem.displayColor}"))
                }

                tvShopItemName.text = shopItem.name
                tvShopItemValidity.apply {
                    model.benefitsSkuDays?.let { benefitsSkuDays ->
                        if (visibility == View.GONE || visibility == View.INVISIBLE)
                            visibility = View.VISIBLE

                        text = resources.getQuantityString(
                            R.plurals.valid_for,
                            benefitsSkuDays.toInt(),
                            benefitsSkuDays
                        )
                    } ?: run {
                        if (visibility == View.VISIBLE)
                            visibility = View.INVISIBLE
                    }
                }

                tvShopItemPrice.text =
                    if (shopItem.price.toInt() == 0) {
                        root.resources.getString(R.string.free)
                    } else {
                        itemPrice.intToPesos()
                    }

                ivAppIcon.let {
                    it.isVisible = shopItem.includedApps.isNotEmpty()
                    if (shopItem.includedApps.isNotEmpty()) {
                        GlobeGlide.with(it).load(shopItem.includedApps[0].appIcon).into(it)
                    }
                }

                /**
                 * logic is same with ShopOfferRecyclerViewAdapter
                 */
                with(shopItem) {
                    if (maximumDataAllocation != 0L) {
                        // if maximumDataAllocation field exists the offer contains the data allocation value which we display
                        // whether that is mobile/home/app data
                        ivShopItemDataSizeIcon.visibility = View.VISIBLE
                        ivShopItemDataSizeIcon.setImageResource(R.drawable.ic_shop_item_data_size_icon)
                        tvShopItemDataSize.apply {
                            text = when (maximumDataAllocation) {
                                // if maximum possible data allocation is equal to any single service data amount (home, app, mobile)
                                // that means the offer contains only a single data offer and we should show the exact data amount offered
                                in listOf(homeDataSize, appDataSize, mobileDataSize).flatten() ->
                                    root.resources.displayDataAllocationFormat(maximumDataAllocation)

                                // else, the offer contains multiple services with multiple data amount offerings so we show "Up to"
                                // before the maximumDataAllocation value
                                else ->
                                    resources.getString(
                                        R.string.up_to,
                                        root.resources.displayDataAllocationFormat(
                                            maximumDataAllocation
                                        )
                                    )
                            }
                        }
                    } else {
                        boosterAllocation?.firstOrNull()?.let { boosterAllocationAmount ->
                            // if booster allocation field is existing the offer is of type booster
                            // and it's data allocation amount is displayed
                            ivShopItemDataSizeIcon.visibility = View.VISIBLE
                            ivShopItemDataSizeIcon.setImageResource(R.drawable.ic_shop_item_data_size_icon)
                            tvShopItemDataSize.apply {
                                text =
                                    root.resources.displayDataAllocationFormat(
                                        boosterAllocationAmount
                                    )
                            }
                        } ?: run {
                            // if the offer doesn't have data allocation of any kind we show calls amount value
                            // on the shop card
                            callSize.firstOrNull()?.let {
                                ivShopItemDataSizeIcon.visibility = View.VISIBLE
                                ivShopItemDataSizeIcon.setImageResource(R.drawable.ic_shop_item_call_size_icon)
                                tvShopItemDataSize.text =
                                    if (it == UNLIMITED_VALUE_INDICATOR) root.resources.getString(
                                        R.string.unli
                                    ) else root.resources.getString(
                                        R.string.call_size,
                                        it / 60
                                    )
                            } ?: run {
                                // if the offer doesn't have data allocation of any kind and no calls amount
                                // we show the amount of texts offered to the shop card
                                smsSize.firstOrNull()?.let {
                                    ivShopItemDataSizeIcon.visibility = View.VISIBLE
                                    ivShopItemDataSizeIcon.setImageResource(R.drawable.ic_shop_item_sms_size_icon)
                                    tvShopItemDataSize.text =
                                        if (it == UNLIMITED_VALUE_INDICATOR) root.resources.getString(
                                            R.string.unli
                                        ) else root.resources.getString(
                                            R.string.text_size,
                                            it
                                        )
                                } ?: run {
                                    ivShopItemDataSizeIcon.visibility = View.GONE
                                    tvShopItemDataSize.visibility = View.GONE
                                }
                            }
                        }
                    }
                }

                shopItem.smsSize.firstOrNull()?.let {
                    ivShopItemSmsSizeIcon.visibility = View.VISIBLE
                    tvShopItemSmsSize.apply {
                        visibility = View.VISIBLE
                        text =
                            if (it == -1L) resources.getString(R.string.unli) else resources.getString(
                                R.string.text_size,
                                it
                            )
                    }
                } ?: run {
                    ivShopItemSmsSizeIcon.visibility = View.GONE
                    tvShopItemSmsSize.visibility = View.GONE
                }

                shopItem.callSize.firstOrNull()?.let {
                    ivShopItemCallSizeIcon.visibility = View.VISIBLE
                    tvShopItemCallSize.apply {
                        visibility = View.VISIBLE
                        text =
                            if (it == -1L) resources.getString(R.string.unli) else resources.getString(
                                R.string.call_size,
                                it / 60
                            )
                    }
                } ?: run {
                    ivShopItemCallSizeIcon.visibility = View.GONE
                    tvShopItemCallSize.visibility = View.GONE
                }

                tvShopItemAttribute.apply {
                    isVisible = shopItem.types.isNotBlank()
                    text = shopItem.types
                }

                tvShopItemDiscount.apply {
                    if (shopItem.types.contains(TYPE_DISCOUNTED)) {
                        visibility = View.VISIBLE
                        text = shopItem.price.stringToPesos()
                        paintFlags = paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    } else visibility = View.GONE
                }

                cvPromos.setOnClickListener {
                    callback.invoke(shopItem, model)
                }
            }
        }
    }

    private fun Resources.displayDataAllocationFormat(dataAmountInBytes: Long): String =
        when {
            dataAmountInBytes == UNLIMITED_VALUE_INDICATOR -> getString(R.string.unli)
            dataAmountInBytes < KB -> "$dataAmountInBytes $B_STRING"
            dataAmountInBytes < MB -> "${DecimalFormat("#.#").format((dataAmountInBytes.toFloat() / KB))} $KB_STRING"
            dataAmountInBytes < GB -> "${DecimalFormat("#.#").format((dataAmountInBytes.toFloat() / MB))} $MB_STRING"
            else -> "${DecimalFormat("#.#").format((dataAmountInBytes.toFloat() / GB))} $GB_STRING"
        }

    override val logTag = "ExclusivePromosAdapter"
}

val shopItemDiffUtil =
    object : DiffUtil.ItemCallback<Pair<ShopItem, AvailableCampaignPromosModel>>() {

        override fun areItemsTheSame(
            oldItem: Pair<ShopItem, AvailableCampaignPromosModel>,
            newItem: Pair<ShopItem, AvailableCampaignPromosModel>
        ) = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: Pair<ShopItem, AvailableCampaignPromosModel>,
            newItem: Pair<ShopItem, AvailableCampaignPromosModel>
        ) = oldItem == newItem
    }
