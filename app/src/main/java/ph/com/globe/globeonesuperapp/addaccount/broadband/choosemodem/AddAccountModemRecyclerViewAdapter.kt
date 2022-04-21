/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.choosemodem

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import kotlinx.parcelize.Parcelize
import ph.com.globe.globeonesuperapp.databinding.ModemItemLayoutBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import java.io.Serializable

class AddAccountModemRecyclerViewAdapter(val callback: (ModemItem) -> Unit) :
    ListAdapter<ModemItem, RecyclerViewHolderBinding<ModemItemLayoutBinding>>(
        object : DiffUtil.ItemCallback<ModemItem>() {
            override fun areItemsTheSame(oldItem: ModemItem, newItem: ModemItem): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: ModemItem, newItem: ModemItem): Boolean =
                oldItem == newItem

        }) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<ModemItemLayoutBinding> =
        RecyclerViewHolderBinding(
            ModemItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<ModemItemLayoutBinding>,
        position: Int
    ) {
        val modemItem = getItem(position)

        with(holder.viewBinding) {
            ivModemImage.setImageResource(modemItem.image)

            clModemItemLayout.isSelected = modemItem.selected
            clModemItemLayout.setOnClickListener {
                callback.invoke(modemItem)
            }
        }
    }
}

data class ModemItem(
    val name: String,
    val image: Int,
    val address: String,
    var selected: Boolean,
    val credential: Credential = Credential("", "")
) : Serializable {
    data class Credential(
        var username: String,
        var password: String
    ): Serializable
}
