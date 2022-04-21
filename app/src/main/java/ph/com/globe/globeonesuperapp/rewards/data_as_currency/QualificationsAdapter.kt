/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.data_as_currency

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ph.com.globe.domain.rewards.usecases.INSUFFICIENT_DATA
import ph.com.globe.domain.rewards.usecases.NO_DATA
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ConversionQualificationDropdownItemBinding
import ph.com.globe.globeonesuperapp.databinding.ConversionQualificationItemBinding
import ph.com.globe.globeonesuperapp.databinding.ConversionSelectItemBinding
import ph.com.globe.model.rewards.QualificationDetails
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.model.util.brand.*

class QualificationsAdapter(
    context: Context
) : ArrayAdapter<QualificationDetails>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context)
                .inflate(
                    if (position == 0) R.layout.conversion_select_item else R.layout.conversion_qualification_item,
                    parent,
                    false
                )
        if (position != 0) getItem(position)?.let { qualification ->
            ConversionQualificationItemBinding.bind(view).apply {
                tvNumber.text = qualification.number.formattedForPhilippines()
                tvAccountName.text = qualification.accountName
                tvPromoName.apply {
                    text = qualification.promoName.asFormattedPromoName()
                    isVisible = qualification.promoName.isNotEmpty()
                            && qualification.brand?.brandType != AccountBrandType.Prepaid
                }
                tvAmount.text = context.resources.getString(R.string.data_in_gb, qualification.max)
            }
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        if (position == 0) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.conversion_select_item, parent, false)

            ConversionSelectItemBinding.bind(view).apply {
                ivArrowDown.rotation = 270F
                root.setOnClickListener {
                    val root = parent.rootView
                    root.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
                    root.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
                }
            }
        } else {
            view = LayoutInflater.from(context)
                .inflate(R.layout.conversion_qualification_dropdown_item, parent, false)

            ConversionQualificationDropdownItemBinding.bind(view).apply {
                getItem(position)?.let { qualification ->
                    tvConvertDataInfo.isVisible = position == 1
                    tvNumber.apply {
                        text = qualification.number.formattedForPhilippines()
                        if (qualification.dataRemaining == 0)
                            setTextColor(
                                AppCompatResources.getColorStateList(
                                    context,
                                    R.color.neutral_A_4
                                )
                            )
                    }
                    tvAccountName.apply {
                        text = qualification.accountName
                        if (qualification.dataRemaining == 0)
                            setTextColor(
                                AppCompatResources.getColorStateList(
                                    context,
                                    R.color.neutral_A_4
                                )
                            )
                    }
                    ivAccountBrand.apply {
                        setImageResource(
                            if (qualification.enrolledAccount.segment == AccountSegment.Mobile) R.drawable.ic_mobile_icon
                            else R.drawable.ic_broadband_icon
                        )
                        if (qualification.dataRemaining == 0)
                            setColorFilter(
                                ContextCompat.getColor(
                                    context,
                                    R.color.neutral_A_4
                                )
                            )
                    }
                    tvPromoName.apply {
                        text = qualification.promoName.asFormattedPromoName()
                        isVisible = qualification.promoName.isNotEmpty()
                                && qualification.brand?.brandType != AccountBrandType.Prepaid
                    }
                    tvAmount.apply {
                        text = context.resources.getString(R.string.data_in_gb, qualification.max)
                        if (qualification.dataRemaining == 0)
                            setTextColor(
                                AppCompatResources.getColorStateList(
                                    context,
                                    R.color.neutral_A_4
                                )
                            )
                    }

                    when (qualification.error) {
                        NO_DATA -> {
                            tvError.apply {
                                visibility = View.VISIBLE
                                text = resources.getString(R.string.no_data)
                            }
                        }
                        INSUFFICIENT_DATA -> {
                            tvError.apply {
                                visibility = View.VISIBLE
                                text = resources.getString(R.string.insufficient_data)
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
        return view
    }

    override fun getItem(position: Int): QualificationDetails? {
        if (position == 0) {
            return null
        }
        return super.getItem(position - 1)
    }

    override fun getCount() = super.getCount() + 1

    override fun isEnabled(position: Int) = position != 0 && getItem(position)?.dataRemaining != 0

    fun updateContents(items: List<QualificationDetails>) {
        clear()
        addAll(items)
    }

    private fun String.asFormattedPromoName() =
        if (this.length > 20) this.substring(0, 20).plus("...") else this
}
