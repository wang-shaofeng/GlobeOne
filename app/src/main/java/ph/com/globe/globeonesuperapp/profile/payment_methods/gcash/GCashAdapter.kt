/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods.gcash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ProfileGCashItemBinding
import ph.com.globe.globeonesuperapp.utils.convertToPrefixNumberFormat
import ph.com.globe.globeonesuperapp.utils.payment.toPesosWithDecimal
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.profile.domain_models.EnrolledAccount

class GCashAdapter(
    val lifecycleOwner: LifecycleOwner,
    val swipeLeft: Boolean,
    val deleteCallback: (GCashItem) -> Unit = {},
    val registerForGCashBalance: (GCashItem) -> LiveData<Double>
) :
    ListAdapter<GCashItem, RecyclerViewHolderBinding<ProfileGCashItemBinding>>(
        object : DiffUtil.ItemCallback<GCashItem>() {

            override fun areItemsTheSame(
                oldItem: GCashItem,
                newItem: GCashItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: GCashItem,
                newItem: GCashItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolderBinding(
            ProfileGCashItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<ProfileGCashItemBinding>,
        position: Int
    ) {
        with(holder.viewBinding) {
            with(getItem(position)) {
                tvPhoneNumber.text = mobileNumber.convertToPrefixNumberFormat()
                tvAccountName.text = accountName
                if (swipeLeft)
                    slSwipeableItem.post {
                        slSwipeableItem.openRight(
                            animated = true
                        )
                    }
                flDeleteView.setOnClickListener {
                    deleteCallback(this)
                }
                registerForGCashBalance(this).observe(lifecycleOwner) {
                    tvGCashBalance.apply {
                        text = it.toPesosWithDecimal()
                        if (it == 0.toDouble())
                            setTextColor(resources.getColor(R.color.state_caution_orange))
                    }
                }
            }
        }
    }
}

data class GCashItem(
    val mobileNumber: String,
    val accountName: String,
    val gcashBalance: String = ""
)

fun EnrolledAccount.toGCashItem(): GCashItem =
    GCashItem(
        this.primaryMsisdn,
        this.accountAlias
    )
