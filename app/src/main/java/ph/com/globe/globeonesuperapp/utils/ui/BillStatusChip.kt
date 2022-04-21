package ph.com.globe.globeonesuperapp.utils.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.BillStatusChipBinding
import ph.com.globe.model.account.PostpaidPaymentStatus

class BillStatusChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding =
        BillStatusChipBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BillStatusChip,
            defStyleAttr, defStyleRes
        ).apply {
            try {
                if (getBoolean(R.styleable.BillStatusChip_biggerPadding, false)) {
                    val horizontalPadding: Int =
                        resources.getDimension(R.dimen.padding_ssmedium).toInt()
                    val verticalPadding: Int =
                        resources.getDimension(R.dimen.padding_standard).toInt()
                    binding.tvStatus.setPadding(
                        horizontalPadding,
                        verticalPadding,
                        horizontalPadding,
                        verticalPadding
                    )
                }
            } finally {
                recycle()
            }
        }
    }

    fun setPaymentStatus(status: PostpaidPaymentStatus) {
        when (status) {
            PostpaidPaymentStatus.BillDueSoon -> {
                binding.cvChip.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.caution,
                        null
                    )
                )
                binding.tvStatus.text =
                    resources.getString(R.string.account_details_bill_status_due_soon)
            }
            PostpaidPaymentStatus.BillOverdue -> {
                binding.cvChip.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.error_red,
                        null
                    )
                )
                binding.tvStatus.text =
                    resources.getString(R.string.account_details_bill_status_overdue)
            }
        }
    }
}
