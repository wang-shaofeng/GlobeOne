package ph.com.globe.globeonesuperapp.shop.promo.itemdetails

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import kotlinx.parcelize.Parcelize
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.BoosterItemLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.ChipImageLayoutBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.utils.payment.stringToPesos
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.shop.domain_models.PROMO_API_SERVICE_PROVISION
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.model.shop.domain_models.Validity

class ShopPromoBoostersRecyclerViewAdapter(val callback: (String, String, Boolean) -> Unit) :
    ListAdapter<BoosterItem, RecyclerViewHolderBinding<out ViewBinding>>(
        object : DiffUtil.ItemCallback<BoosterItem>() {
            override fun areItemsTheSame(oldItem: BoosterItem, newItem: BoosterItem): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: BoosterItem, newItem: BoosterItem): Boolean =
                oldItem == newItem

        }) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<out ViewBinding> =
        RecyclerViewHolderBinding(
            BoosterItemLayoutBinding.inflate(
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
            with(holder.viewBinding as BoosterItemLayoutBinding) {
                tvBoosterName.text = boosterName
                tvBoosterDescription.text = boosterDescription
                tvBoosterPrice.text = boosterPrice.stringToPesos()

                appItems?.let {
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

                if (position == currentList.size - 1) vBoosterItemBottomLine.visibility = View.GONE
                else vBoosterItemBottomLine.visibility = View.VISIBLE
                holder.viewBinding.clBoosterItemLayout.apply {
                    isSelected = selected
                    isEnabled = enabled
                    ivBoosterCheckbox.isEnabled = enabled
                    setOnClickListener {
                        callback(serviceId, boosterPrice, !selected)
                    }
                }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<out ViewBinding>) {
        super.onViewRecycled(holder)

        clearImages(holder.viewBinding as BoosterItemLayoutBinding)
    }

    private fun clearImages(binding: BoosterItemLayoutBinding) = with(binding) {
        cgAppsImages.children.forEach {
            val image = it.findViewById<ImageView>(R.id.iv_chip_image)
            GlobeGlide.with(image).clear(image)
        }
        cgAppsImages.removeAllViews()
    }

}

@Parcelize
data class BoosterItem(
    val serviceId: String,
    val nonChargeServiceId: String,
    val chargeBoosterParam: String,
    val nonChargeBoosterParam: String,
    val productKeyword: String,
    val boosterName: String,
    val boosterDescription: String?,
    val boosterPrice: String,
    val boosterValidity: Validity?,
    val appItems: List<ApplicationItem>?,
    var selected: Boolean,
    var enabled: Boolean,
    val provisionByServiceId: Boolean,
) : Parcelable

@Parcelize
data class ApplicationItem(
    val appIcon: String,
    val appName: String
) : Parcelable

fun ShopItem.toBoosterItem(): BoosterItem {
    return BoosterItem(
        serviceId = chargePromoId,
        nonChargeServiceId = nonChargePromoId,
        chargeBoosterParam = chargeServiceParam, // TODO see about this too
        nonChargeBoosterParam = nonChargeServiceParam,
        productKeyword = apiProvisioningKeyword,
        boosterName = name,
        boosterDescription = applicationService?.description,
        boosterPrice = price,
        boosterValidity = validity,
        appItems = applicationService?.apps?.map { ApplicationItem(it.appIcon, it.appName) },
        selected = false,
        enabled = true,
        provisionByServiceId = apiSubscribe == PROMO_API_SERVICE_PROVISION
    )
}
