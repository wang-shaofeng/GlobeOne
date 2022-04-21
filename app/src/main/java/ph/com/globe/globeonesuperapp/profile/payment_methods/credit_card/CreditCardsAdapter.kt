/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods.credit_card

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.CreditCardItemBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.payment.CreditCardModel
import java.util.*

class CreditCardsAdapter(
    val swipeLeft: Boolean = false,
    val deleteCallback: (CreditCardItem) -> Unit = {}
) :
    ListAdapter<CreditCardItem, RecyclerViewHolderBinding<CreditCardItemBinding>>(
        object : DiffUtil.ItemCallback<CreditCardItem>() {

            override fun areItemsTheSame(
                oldItem: CreditCardItem,
                newItem: CreditCardItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: CreditCardItem,
                newItem: CreditCardItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolderBinding(
            CreditCardItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<CreditCardItemBinding>,
        position: Int
    ) {
        with(holder.viewBinding) {
            with(getItem(position)) {
                tvCardNumber.text =
                    root.resources.getString(R.string.card_holder_string, cardSummary)
                tvCardExpirationDate.apply {
                    text = expirationString
                    if (expirationString == root.resources.getString(R.string.expired))
                        setTextColor(
                            AppCompatResources.getColorStateList(root.context, R.color.error_red)
                        )
                }
                if (swipeLeft)
                    slSwipeableItem.post {
                        slSwipeableItem.openRight(
                            animated = true
                        )
                    }
                flDeleteView.setOnClickListener {
                    deleteCallback(this)
                }
            }
        }
    }
}

data class CreditCardItem(
    val cardSummary: String,
    val expirationString: String
)

fun CreditCardModel.toCreditCardItem(context: Context): CreditCardItem =
    CreditCardItem(
        this.cardSummary,
        this.getExpirationString(context)
    )

fun CreditCardModel.getExpirationString(context: Context): String =
    if (GregorianCalendar() < GregorianCalendar(expiryYear.toInt(), expiryMonth.toInt(), 1))
        context.getString(R.string.expires_on, expiryMonth, expiryYear.substring(1, 3))
    else
        context.getString(R.string.expired)
