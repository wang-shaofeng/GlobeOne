/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.analytics.logger.eLog
import ph.com.globe.data.db.shop.UNLIMITED_VALUE_INDICATOR
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ChipImageLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.ShopItemLayoutBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.utils.payment.intToPesos
import ph.com.globe.globeonesuperapp.utils.payment.setValidityText
import ph.com.globe.globeonesuperapp.utils.payment.setValidityTextEx
import ph.com.globe.globeonesuperapp.utils.payment.stringToPesos
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.model.shop.domain_models.TYPE_DISCOUNTED
import ph.com.globe.model.util.*
import ph.com.globe.util.nonEmptyOrNull
import java.text.DecimalFormat

class ShopOfferRecyclerViewAdapter(private val callback: (ShopItem) -> Unit) :
    ListAdapter<ShopItem, RecyclerViewHolderBinding<ShopItemLayoutBinding>>(shopItemDiffUtil),
    HasLogTag {

    private var handleNoExpiry:Boolean = true

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<ShopItemLayoutBinding> =
        RecyclerViewHolderBinding(
            ShopItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<ShopItemLayoutBinding>,
        position: Int
    ) {
        val item = getItem(position)
        with(holder.viewBinding) {
            with(item) {
                val itemPrice = price.toInt() - (discount ?: "0").toInt()
                try {
                    vVerticalLine.setBackgroundColor(Color.parseColor(displayColor))
                } catch (e: Exception) {
                    vVerticalLine.setBackgroundColor(Color.parseColor("#000000"))
                    eLog(Exception("displayColor has a bad format: $displayColor"))
                }
                tvShopItemName.text = name

                if (handleNoExpiry) {
                    tvShopItemValidity.text = root.resources.setValidityTextEx(validity)
                } else {
                    tvShopItemValidity.isVisible = validity != null
                    tvShopItemValidity.text = root.resources.getString(
                        R.string.valid_for,
                        root.resources.setValidityText(validity)
                    )
                }

                tvShopItemPrice.text = itemPrice.intToPesos()

                if (maximumDataAllocation != 0L) {
                    // if maximumDataAllocation field exists the offer contains the data allocation value which we display
                    // whether that is mobile/home/app data
                    ivShopItemAllocationIcon.setImageResource(R.drawable.ic_shop_item_data_size_icon)
                    tvShopItemAllocationSize.apply {
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
                                    root.resources.displayDataAllocationFormat(maximumDataAllocation)
                                )
                        }
                    }
                } else {
                    boosterAllocation?.firstOrNull()?.let { boosterAllocationAmount ->
                        // if booster allocation field is existing the offer is of type booster
                        // and it's data allocation amount is displayed
                        ivShopItemAllocationIcon.setImageResource(R.drawable.ic_shop_item_data_size_icon)
                        applicationService?.apps?.firstOrNull()?.appName?.nonEmptyOrNull()
                            .let { appName ->
                                // if the offer's service contains the app name we display it here
                                // else, it stays hidden
                                tvDataFor.isVisible = appName != null
                                tvDataFor.text = root.resources.getString(
                                    R.string.for_app,
                                    appName
                                )
                            }
                        tvShopItemAllocationSize.apply {
                            text =
                                root.resources.displayDataAllocationFormat(boosterAllocationAmount)
                        }
                    } ?: run {
                        // if the offer doesn't have data allocation of any kind we show calls amount value
                        // on the shop card
                        callSize.firstOrNull()?.let {
                            ivShopItemAllocationIcon.setImageResource(R.drawable.ic_shop_item_call_size_icon)
                            tvShopItemAllocationSize.text =
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
                                ivShopItemAllocationIcon.setImageResource(R.drawable.ic_shop_item_sms_size_icon)
                                tvShopItemAllocationSize.text =
                                    if (it == UNLIMITED_VALUE_INDICATOR) root.resources.getString(
                                        R.string.unli
                                    ) else root.resources.getString(
                                        R.string.text_size,
                                        it
                                    )
                            } ?: run {
                                ivShopItemAllocationIcon.visibility = View.GONE
                                tvShopItemAllocationSize.visibility = View.GONE
                            }
                        }

                    }
                }

                clearImages(holder.viewBinding)
                applicationService?.apps?.let {
                    for (app in it) {
                        val chip = ChipImageLayoutBinding.inflate(LayoutInflater.from(root.context))
                        GlobeGlide.with(chip.ivChipImage).load(app.appIcon).into(chip.ivChipImage)
                        cgAppImages.addView(chip.root)
                    }
                }

                tvShopItemAttribute.apply {
                    isVisible = types.isNotBlank()
                    text = types
                }

                tvShopItemDiscount.apply {
                    if (types.contains(TYPE_DISCOUNTED)) {
                        visibility = View.VISIBLE
                        text = price.stringToPesos()
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else visibility = View.INVISIBLE
                }

                cvShopItemLayout.setOnClickListener {
                    callback.invoke(this)
                }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<ShopItemLayoutBinding>) {
        super.onViewRecycled(holder)

        clearImages(holder.viewBinding)
    }

    private fun clearImages(binding: ShopItemLayoutBinding) = with(binding) {
        cgAppImages.children.forEach {
            val image = it.findViewById<ImageView>(R.id.iv_chip_image)
            GlobeGlide.with(image).clear(image)
        }
        cgAppImages.removeAllViews()
    }

    private fun Resources.displayDataAllocationFormat(dataAmountInBytes: Long): String =
        when {
            dataAmountInBytes == UNLIMITED_VALUE_INDICATOR -> getString(R.string.unli)
            dataAmountInBytes < KB -> "$dataAmountInBytes $B_STRING"
            dataAmountInBytes < MB -> "${DecimalFormat("#.#").format((dataAmountInBytes.toFloat() / KB))} $KB_STRING"
            dataAmountInBytes < GB -> "${DecimalFormat("#.#").format((dataAmountInBytes.toFloat() / MB))} $MB_STRING"
            else -> "${DecimalFormat("#.#").format((dataAmountInBytes.toFloat() / GB))} $GB_STRING"
        }

    fun setHandleNoExpiry(handleNoExpiry:Boolean){
        this.handleNoExpiry = handleNoExpiry
    }

    override val logTag = "ShopOfferRecyclerViewAdapter"
}

val shopItemDiffUtil = object : DiffUtil.ItemCallback<ShopItem>() {

    override fun areItemsTheSame(oldItem: ShopItem, newItem: ShopItem) = oldItem == newItem

    override fun areContentsTheSame(oldItem: ShopItem, newItem: ShopItem) = oldItem == newItem
}
