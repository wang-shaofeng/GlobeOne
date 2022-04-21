package ph.com.globe.globeonesuperapp.utils.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.BalancePostpaidViewBinding
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.model.account.BillStatus
import ph.com.globe.model.account.PostpaidPaymentStatus
import ph.com.globe.model.util.AccountStatus
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toFormattedStringOrEmpty
import ph.com.globe.util.toFormattedStringOrNull

class BalancePostpaidView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding =
        BalancePostpaidViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.btnBillBreakdown.setOnClickListener {
            findNavController().safeNavigate(R.id.action_accountDetailsFragment_to_billDetailsFragment)
        }
        binding.ivAllSet.setOnClickListener {
            findNavController().safeNavigate(R.id.action_accountDetailsFragment_to_billDetailsFragment)
        }
        binding.btnPayNow.setOnClickListener {
            findNavController().safeNavigate(R.id.action_accountDetailsFragment_to_billingsChooseAmountFragment)
        }
    }

    fun setupWithStatuses(billStatus: BillStatus, accountStatus: AccountStatus) {
        with(binding) {
            when (billStatus) {
                is BillStatus.Success -> {
                    when (billStatus.paymentStatus) {
                        PostpaidPaymentStatus.AllSet -> {
                            ivReceiptDecoration.visibility = View.GONE
                            lavLoadingBackground.visibility = View.GONE
                            lavLoadingForeground.visibility = View.GONE
                            when (accountStatus) {
                                AccountStatus.Disconnected -> ivAllSet.setImageResource(R.drawable.allset_card_disconected)
                                else -> ivAllSet.setImageResource(R.drawable.allset_card)
                            }
                            if ((billStatus.billAmount?.toFloatOrNull() ?: 0f) == 0f) {
                                ivAllSet.setOnClickListener {
                                    findNavController().safeNavigate(R.id.action_accountDetailsFragment_to_billingsChooseAmountFragment)
                                }
                            }
                            binding.ivAllSet.visibility = View.VISIBLE
                        }
                        PostpaidPaymentStatus.BillDueSoon -> {
                            lavLoadingBackground.visibility = View.GONE
                            lavLoadingForeground.visibility = View.GONE
                            clBalanceContent.visibility = View.VISIBLE
                            ivBackground.visibility = View.VISIBLE
                            ivBackground.setImageResource(R.drawable.ic_bills_due_background)
                            tvTitle.text =
                                resources.getString(R.string.account_details_postpaid_your_bill_date_is_soon)
                            tvTotalAmount.text = (billStatus.billAmount?.toFloatOrNull()
                                ?: 0f).toPezosFormattedDisplayBalance()
                            billStatus.dueDate?.toFormattedStringOrEmpty(GlobeDateFormat.MonthAbbrCustomDate)
                                ?.let {
                                    tvDueDate.visibility = View.VISIBLE
                                    tvDueDate.text = resources.getString(
                                        R.string.account_details_template_bill_due_on, it
                                    )
                                }
                            bscBillStatus.setPaymentStatus(PostpaidPaymentStatus.BillDueSoon)
                        }
                        PostpaidPaymentStatus.BillOverdue -> {
                            lavLoadingBackground.visibility = View.GONE
                            lavLoadingForeground.visibility = View.GONE
                            clBalanceContent.visibility = View.VISIBLE
                            ivBackground.visibility = View.VISIBLE
                            tvTotalAmount.text =
                                (billStatus.billAmount?.toFloatOrNull()
                                    ?: 0f).toPezosFormattedDisplayBalance()
                            ivBackground.setImageResource(R.drawable.ic_bills_overdue)
                            tvTitle.text = when (accountStatus) {
                                AccountStatus.Disconnected -> resources.getString(R.string.account_details_postpaid_disconected)
                                AccountStatus.Active -> resources.getString(R.string.account_details_postpaid_not_yet_disconected)
                                else -> ""
                            }
                            billStatus.dueDate?.toFormattedStringOrNull(GlobeDateFormat.MonthAbbrCustomDate)
                                ?.let {
                                    tvDueDate.visibility = VISIBLE
                                    tvDueDate.text = resources.getString(
                                        R.string.account_details_template_bill_due_last,
                                        it
                                    )
                                }

                            bscBillStatus.setPaymentStatus(PostpaidPaymentStatus.BillOverdue)
                        }
                    }
                }

                is BillStatus.Loading -> {
                    ivReceiptDecoration.visibility = View.VISIBLE
                    groupLoading.visibility = View.VISIBLE
                    clBalanceContent.visibility = View.GONE
                    ivBackground.visibility = View.GONE
                    ivAllSet.visibility = View.GONE
                }

                is BillStatus.Error -> {
                    binding.root.visibility = View.GONE
                }
            }
        }
    }
}
