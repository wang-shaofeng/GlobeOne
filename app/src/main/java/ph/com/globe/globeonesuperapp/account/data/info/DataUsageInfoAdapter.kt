package ph.com.globe.globeonesuperapp.account.data.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsDataUsageInfoItemBinding
import ph.com.globe.globeonesuperapp.utils.ui.KtItemHolder
import ph.com.globe.model.group.domain_models.DATA_TYPE_ONE_TIME_ACCESS
import ph.com.globe.model.group.domain_models.DATA_TYPE_RECURRING_ACCESS
import ph.com.globe.model.group.domain_models.UsagePromo
import ph.com.globe.model.util.*
import java.text.DecimalFormat

class DataUsageInfoAdapter(private val dataType: String) : ListAdapter<UsagePromo, KtItemHolder>(
    object : DiffUtil.ItemCallback<UsagePromo>() {
        override fun areItemsTheSame(oldItem: UsagePromo, newItem: UsagePromo): Boolean {
            return oldItem.promoName == newItem.promoName
        }

        override fun areContentsTheSame(
            oldItem: UsagePromo,
            newItem: UsagePromo
        ): Boolean {
            return oldItem.promoName == newItem.promoName
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = KtItemHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.account_details_data_usage_info_item, parent, false)
    )

    override fun onBindViewHolder(holder: KtItemHolder, position: Int) {
        val usagePromo = getItem(position)

        with(holder.containerView) {
            AccountDetailsDataUsageInfoItemBinding.bind(this).apply {
                tvUsageItemTitle.text = usagePromo.promoName
                tvExpirationDate.text = when (dataType) {
                    DATA_TYPE_ONE_TIME_ACCESS -> context.getString(
                        R.string.expires,
                        usagePromo.endDate
                    )
                    DATA_TYPE_RECURRING_ACCESS -> context.getString(
                        R.string.refreshes_on,
                        usagePromo.endDate
                    )
                    else -> ""
                }
                tvDataLeft.text = context.getString(
                    R.string.res_left,
                    getFormattedData(usagePromo.dataRemaining.toFloat())
                ).lowercase()
                tvDataTotal.text = getFormattedData(usagePromo.dataTotal.toFloat())
            }
        }
    }

    private fun getFormattedData(data: Float): String {
        val df = DecimalFormat("#.#")
        val amount = when {
            data >= KB_IN_GB -> data / KB_IN_GB
            data >= KB_IN_MB -> data / KB_IN_MB
            else -> data
        }
        val unit = when {
            data >= KB_IN_GB -> GB
            data >= KB_IN_MB -> MB
            else -> KB
        }
        return "${df.format(amount)} $unit"
    }
}
