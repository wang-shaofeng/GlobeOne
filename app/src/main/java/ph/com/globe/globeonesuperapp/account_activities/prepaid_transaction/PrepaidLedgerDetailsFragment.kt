package ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.PrepaidLedgerDetailsViewModel.ChannelNameResult.LoadBoughtChannelName
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.PrepaidLedgerDetailsViewModel.ChannelNameResult.PromoReceivedOthersChannelName
import ph.com.globe.globeonesuperapp.databinding.AccountPrepaidLedgerDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.oneTimeEventObserve
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.*
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.Load.LoadType.*
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.Promo.PromoType.*
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.Text.TextType.*
import ph.com.globe.model.prepaid.TIME_BASED
import ph.com.globe.model.prepaid.VOLUME_BASED
import ph.com.globe.model.util.megaBytesToDataUnitsFormatted
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.convertToTimeFormat
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toFormattedStringOrEmpty
import kotlin.math.roundToInt

@AndroidEntryPoint
class PrepaidLedgerDetailsFragment :
    NoBottomNavViewBindingFragment<AccountPrepaidLedgerDetailsFragmentBinding>({
        AccountPrepaidLedgerDetailsFragmentBinding.inflate(it)
    }) {

    private val prepaidLedgerDetailViewModel by hiltNavGraphViewModels<PrepaidLedgerDetailsViewModel>(
        R.id.account_activities_subgraph
    )

    private val args by navArgs<PrepaidLedgerDetailsFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            wfTransactionDetails.onBack { findNavController().navigateUp() }

            val msisdn = args.msisdn
            val alias = args.alias
            with(args.prepaidLedgerItem) {
                clTransactionDate.visibility = View.VISIBLE
                tvDateName.text =
                    eventStartDate.toDateOrNull().toFormattedStringOrEmpty(GlobeDateFormat.Voucher)
                val transCnt =
                    if (transactionCount == null) 1 else transactionCount?.toDouble()?.roundToInt()
                val amount = chargeAmount.plus(serviceCharge)

                tvChargeAmount.setAmountText(amount, type)
                tvChargeToLoad.isVisible = amount != 0.0

                when (type) {
                    is Call -> {
                        ivToType.visibility = View.VISIBLE
                        clTransactionTo.visibility = View.VISIBLE
                        tvToName.text = targetMobileNumber

                        clTransactionBy.visibility = View.VISIBLE
                        tvByName.text = alias
                        tvByNumber.text = msisdn

                        clTransactionDesc1.visibility = View.VISIBLE
                        tvDescTitle1.text = getString(R.string.prepaid_ledger_call_type)
                        tvDescName1.text = transactionTypeMapping

                        when ((type as Call).type) {
                            Call.CallType.CALL_MADE -> {
                                ivTransactionType.setImageResource(R.drawable.ic_call_made)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.call_made,
                                    transCnt ?: 0
                                )

                                tvToTitle.text = getString(R.string.prepaid_ledger_number_called)
                                ivToType.setImageResource(R.drawable.ic_transaction_made)
                                tvByTitle.text = getString(R.string.prepaid_ledger_call_made_by)
                            }
                            Call.CallType.CALL_RECEIVED -> {
                                clCallTextReceivedNote.visibility = View.VISIBLE
                                tvCallTextReceivedNote.text =
                                    getString(R.string.call_text_received_note, CALL)

                                ivTransactionType.setImageResource(R.drawable.ic_call_received)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.call_received,
                                    transCnt ?: 0
                                )

                                tvToTitle.text = getString(R.string.prepaid_ledger_caller)
                                ivToType.setImageResource(R.drawable.ic_transaction_received)
                                tvByTitle.text = getString(R.string.prepaid_ledger_received_by)
                                tvToName.text = sourceMobileNumber
                            }
                        }
                    }
                    is Text -> {
                        ivToType.visibility = View.VISIBLE
                        clTransactionTo.visibility = View.VISIBLE
                        tvToName.text = targetMobileNumber

                        clTransactionBy.visibility = View.VISIBLE
                        tvByName.text = alias
                        tvByNumber.text = msisdn

                        clTransactionDesc1.visibility = View.VISIBLE
                        tvDescTitle1.text = getString(R.string.prepaid_ledger_text_type)
                        tvDescName1.text = transactionTypeMapping
                        clTransactionDate.visibility = View.VISIBLE

                        when ((type as Text).type) {
                            TEXT_SENT -> {
                                ivTransactionType.setImageResource(R.drawable.ic_text_sent)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.text_sent,
                                    transCnt ?: 0
                                )

                                tvToTitle.text = getString(R.string.prepaid_ledger_number_texted)
                                ivToType.setImageResource(R.drawable.ic_transaction_made)
                                tvByTitle.text = getString(R.string.sent_by)
                            }
                            TEXT_RECEIVED -> {
                                clCallTextReceivedNote.visibility = View.VISIBLE
                                tvCallTextReceivedNote.text =
                                    getString(R.string.call_text_received_note, TEXT)

                                ivTransactionType.setImageResource(R.drawable.ic_text_received)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.text_received,
                                    transCnt ?: 0
                                )

                                tvToTitle.text = getString(R.string.prepaid_ledger_received_from)
                                ivToType.setImageResource(R.drawable.ic_transaction_received)
                                tvByTitle.text = getString(R.string.prepaid_ledger_received_by)
                                tvToName.text = sourceMobileNumber
                            }
                            TEXT_REFUNDED -> {
                                ivTransactionType.setImageResource(R.drawable.ic_text_refunded)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.text_refunded,
                                    transCnt ?: 0
                                )

                                clTransactionTo.visibility = View.GONE
                                clTransactionBy.visibility = View.GONE
                            }
                        }
                    }
                    is Load -> {

                        clTransactionTo.visibility = View.GONE
                        clTransactionBy.visibility = View.VISIBLE

                        when ((type as Load).type) {
                            LOAD_BOUGHT -> {
                                ivTransactionType.setImageResource(R.drawable.ic_load_bought)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.load_bought,
                                    transCnt ?: 0
                                )

                                tvByTitle.text = getString(R.string.prepaid_ledger_bought_by)
                                tvByName.text = alias
                                tvByNumber.text = msisdn

                                clTransactionDesc1.visibility = View.VISIBLE
                                tvDescTitle1.text = getString(R.string.prepaid_ledger_bought_from)
                                prepaidLedgerDetailViewModel.getChannelName(channel, type)
                            }
                            LOAD_RECEIVED -> {
                                ivTransactionType.setImageResource(R.drawable.ic_load_received)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.load_received,
                                    transCnt ?: 0
                                )

                                tvByTitle.text = getString(R.string.prepaid_ledger_received_by)
                                tvByName.text = alias
                                tvByNumber.text = msisdn

                                clTransactionDesc1.visibility = View.VISIBLE
                                tvDescTitle1.text = getString(R.string.sent_by)
                                tvDescName1.setStartDrawable(R.drawable.ic_transaction_received)
                                tvDescName1.text = sourceMobileNumber
                            }
                            LOAD_SHARED -> {
                                ivTransactionType.setImageResource(R.drawable.ic_load_shared)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.load_shared,
                                    transCnt ?: 0
                                )

                                tvByTitle.text = getString(R.string.prepaid_ledger_shared_by)
                                tvByName.text = alias
                                tvByNumber.text = msisdn

                                clTransactionDesc1.visibility = View.VISIBLE
                                tvDescTitle1.text = getString(R.string.prepaid_ledger_shared_to)
                                tvDescName1.setStartDrawable(R.drawable.ic_transaction_made)
                                tvDescName1.text = targetMobileNumber
                            }
                            LOAD_LOANED -> {
                                ivTransactionType.setImageResource(R.drawable.ic_load_loaned)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.load_loaned,
                                    transCnt ?: 0
                                )

                                tvByTitle.text = getString(R.string.prepaid_ledger_loaned_by)
                                tvByName.text = alias
                                tvByNumber.text = msisdn

                                clTransactionDesc1.visibility = View.GONE
                            }
                            LOAN_PAID -> {
                                ivTransactionType.setImageResource(R.drawable.ic_loan_paid)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.loan_paid,
                                    transCnt ?: 0
                                )

                                tvByTitle.text = getString(R.string.prepaid_ledger_paid_by)
                                tvByName.text = alias
                                tvByNumber.text = msisdn

                                clTransactionDesc1.visibility = View.GONE
                            }
                            LOAD_EXPIRED -> {
                                ivTransactionType.setImageResource(R.drawable.ic_load_expired)
                                tvTransactionType.setTransactionTypeText(R.string.load_expired)

                                tvByTitle.text = getString(R.string.prepaid_ledger_deducted_from)
                                tvByName.text = alias
                                tvByNumber.text = msisdn

                                clTransactionDesc1.visibility = View.GONE
                            }
                        }
                    }
                    is Promo -> {
                        when ((type as Promo).type) {
                            PROMO_BOUGHT -> {
                                ivTransactionType.setImageResource(R.drawable.ic_promo_bought)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.promo_bought,
                                    transCnt ?: 0
                                )

                                clTransactionTo.visibility = View.GONE
                                clTransactionBy.visibility = View.VISIBLE
                                tvByTitle.text = getString(R.string.prepaid_ledger_bought_by)
                                tvByName.text = alias
                                tvByNumber.text = msisdn

                                clTransactionDesc1.visibility = View.GONE
                            }
                            PROMO_LOANED -> {
                                ivTransactionType.setImageResource(R.drawable.ic_promo_loaned)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.promo_loaned,
                                    transCnt ?: 0
                                )

                                tvChargeAmount.isVisible = false
                                tvChargeToLoad.isVisible = false

                                clTransactionTo.visibility = View.GONE
                                clTransactionBy.visibility = View.VISIBLE
                                tvByTitle.text = getString(R.string.prepaid_ledger_loaned_by)
                                tvByName.text = alias
                                tvByNumber.text = msisdn

                                clTransactionDesc1.visibility = View.GONE
                                cvLoanNote.visibility = View.VISIBLE
                            }
                            PROMO_RECEIVED -> {
                                ivTransactionType.setImageResource(R.drawable.ic_promo_received)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.promo_received,
                                    transCnt ?: 0
                                )

                                tvChargeAmount.isVisible = false
                                tvChargeToLoad.isVisible = false

                                clTransactionTo.visibility = View.GONE
                                clTransactionBy.visibility = View.GONE

                                clTransactionDesc1.visibility = View.VISIBLE
                                tvDescTitle1.text = getString(R.string.prepaid_ledger_shared_by)
                                tvDescName1.setStartDrawable(R.drawable.ic_transaction_received)
                                tvDescName1.text = sourceMobileNumber
                            }
                            PROMO_SHARED -> {
                                ivTransactionType.setImageResource(R.drawable.ic_promo_shared)
                                tvTransactionType.setTransactionTypeText(
                                    R.string.promo_shared,
                                    transCnt ?: 0
                                )

                                clTransactionTo.visibility = View.VISIBLE
                                tvToTitle.text = getString(R.string.prepaid_ledger_shared_to)
                                ivToType.visibility = View.VISIBLE
                                ivToType.setImageResource(R.drawable.ic_transaction_made)
                                tvToName.text = targetMobileNumber

                                clTransactionBy.visibility = View.VISIBLE
                                tvByTitle.text = getString(R.string.prepaid_ledger_shared_by)
                                tvByName.text = alias
                                tvByNumber.text = msisdn

                                clTransactionDesc1.visibility = View.GONE
                            }
                            PROMO_RECEIVED_OTHERS -> {
                                ivTransactionType.setImageResource(R.drawable.ic_promo_received)
                                tvTransactionType.setTransactionTypeText(R.string.promo_received)

                                tvChargeAmount.isVisible = false
                                tvChargeToLoad.isVisible = false

                                clTransactionTo.visibility = View.GONE
                                clTransactionBy.visibility = View.GONE

                                clTransactionDesc1.visibility = View.GONE

                                clTransactionDesc2.visibility = View.VISIBLE
                                tvDescTitle2.text = getString(R.string.prepaid_ledger_channel)
                                prepaidLedgerDetailViewModel.getChannelName(channel, type)
                            }
                        }
                    }
                    Data -> {
                        ivTransactionType.setImageResource(R.drawable.ic_data_used)
                        tvTransactionType.setTransactionTypeText(R.string.data_used_prepaid)

                        clTransactionTo.visibility = View.GONE
                        clTransactionBy.visibility = View.GONE

                        when (unitOfMeasurementCode) {
                            TIME_BASED -> {
                                clTransactionDesc1.visibility = View.VISIBLE
                                tvDescTitle1.text =
                                    getString(R.string.prepaid_ledger_total_data_used)
                                tvDescName1.text = dataVolumeCount?.megaBytesToDataUnitsFormatted()

                                clTransactionDesc2.visibility = View.VISIBLE
                                tvDescTitle2.text =
                                    getString(R.string.prepaid_ledger_usage_duration)
                                tvDescName2.text = durationCount?.convertToTimeFormat()
                            }
                            VOLUME_BASED -> {
                                clTransactionDesc1.visibility = View.GONE
                                clTransactionDesc2.visibility = View.VISIBLE
                                tvDescTitle2.text =
                                    getString(R.string.prepaid_ledger_total_data_used)
                                tvDescName2.text = dataVolumeCount?.megaBytesToDataUnitsFormatted()
                            }
                            else -> {
                                clTransactionDesc1.visibility = View.GONE
                                clTransactionDesc2.visibility = View.GONE
                            }
                        }
                    }
                    None -> {
                        clTransactionDetails.visibility = View.GONE
                        clTransactionTo.visibility = View.GONE
                        clTransactionBy.visibility = View.GONE
                        clTransactionDesc1.visibility = View.GONE
                        clTransactionDesc2.visibility = View.GONE
                    }
                }

                prepaidLedgerDetailViewModel.channelNameResult.oneTimeEventObserve(
                    viewLifecycleOwner,
                    { result ->
                        when (result) {
                            is LoadBoughtChannelName -> {
                                tvDescName1.text = result.channelName
                            }
                            is PromoReceivedOthersChannelName -> {
                                tvDescName2.text = result.channelName
                            }
                        }
                    })
            }
        }
    }

    override val logTag = "PrepaidLedgerDetailsFragment"
}

private fun TextView.setStartDrawable(@DrawableRes icon: Int) {
    setCompoundDrawablesWithIntrinsicBounds(
        icon,
        0,
        0,
        0
    )
}

private fun TextView.setTransactionTypeText(resId: Int, transactionCount: Int = 0) = when {
    transactionCount <= 1 -> text = this.resources.getString(resId, "")
    else -> text = this.resources.getString(resId, " (${transactionCount})")
}

private fun TextView.setAmountText(amount: Double, type: PrepaidLedgerTransactionItem.PrepaidType) {

    isVisible = amount != 0.0
    text = when (type) {
        is Call -> {
            resources.getString(R.string.prepaid_ledger_amount_deduct, amount)
        }
        is Data -> {
            resources.getString(R.string.prepaid_ledger_amount_deduct, amount)
        }
        is Load -> {
            when (type.type) {
                LOAD_BOUGHT, LOAD_RECEIVED, LOAD_LOANED -> {
                    resources.getString(R.string.prepaid_ledger_amount_add, amount)
                }
                LOAD_SHARED, LOAN_PAID, LOAD_EXPIRED -> {
                    resources.getString(R.string.prepaid_ledger_amount_deduct, amount)
                }
            }
        }
        is Promo -> {
            when (type.type) {
                PROMO_BOUGHT, PROMO_LOANED, PROMO_SHARED, PROMO_RECEIVED_OTHERS -> {
                    resources.getString(R.string.prepaid_ledger_amount_deduct, amount)
                }
                PROMO_RECEIVED -> {
                    resources.getString(R.string.prepaid_ledger_amount_neutral, amount)
                }
            }
        }
        is Text -> {
            when (type.type) {
                TEXT_RECEIVED, TEXT_SENT -> {
                    resources.getString(R.string.prepaid_ledger_amount_deduct, amount)
                }
                TEXT_REFUNDED -> {
                    resources.getString(R.string.prepaid_ledger_amount_add, amount)
                }
            }
        }
        None -> {
            resources.getString(R.string.prepaid_ledger_amount_neutral, amount)
        }
    }
}

const val TEXT = "text"

const val CALL = "call"
