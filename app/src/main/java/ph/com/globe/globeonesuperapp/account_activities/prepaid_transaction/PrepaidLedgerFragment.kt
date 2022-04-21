package ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.PrepaidLedgerViewModel.*
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.data.DataFragment
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.promo.PromoFragment
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.sms.SMSFragment
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.topup.TopupFragment
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.voice.VoiceFragment
import ph.com.globe.globeonesuperapp.databinding.PrepaidLedgerFragmentBinding
import ph.com.globe.globeonesuperapp.utils.date.DateFilter
import ph.com.globe.globeonesuperapp.utils.date.MINUS_TWO_DAY
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment

@AndroidEntryPoint
class PrepaidLedgerFragment : NestedViewBindingFragment<PrepaidLedgerFragmentBinding>({
    PrepaidLedgerFragmentBinding.inflate(it)
}) {

    private val prepaidLedgerViewModel by hiltNavGraphViewModels<PrepaidLedgerViewModel>(R.id.account_activities_subgraph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            val adapter = ScreenSliderAdapter(this@PrepaidLedgerFragment)
            vpPrepaidLedger.adapter = adapter

            TabLayoutMediator(tlAccount, vpPrepaidLedger) { tab, position ->
                tab.text = when (position) {
                    0 -> PREPAID_LEDGER_LOAD_TAB
                    1 -> PREPAID_LEDGER_DATA_TAB
                    2 -> PREPAID_LEDGER_PROMOS_TAB
                    3 -> PREPAID_LEDGER_CALLS_TAB
                    else -> PREPAID_LEDGER_TEXT_TAB
                }
            }.attach()

            sSortDropdown.adapter = ArrayAdapter(
                requireContext(),
                R.layout.sort_dropdown_item_layout,
                resources.getStringArray(R.array.transactions_date_filter)
            )

            sSortDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    prepaidLedgerViewModel.setFilterDate(
                        when (position) {
                            0 -> DateFilter.Last2Days(buffer = MINUS_TWO_DAY)
                            1 -> DateFilter.Last3Days(buffer = MINUS_TWO_DAY)
                            2 -> DateFilter.Last7Days(buffer = MINUS_TWO_DAY)
                            else -> DateFilter.Last30Days(buffer = MINUS_TWO_DAY)
                        }
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

            prepaidLedgerViewModel.dateFilter.observe(viewLifecycleOwner, {
                tvDateFilter.text = when (it) {
                    is DateFilter.Last2Days -> getString(R.string.date_filter_2_days)
                    is DateFilter.Last3Days -> getString(R.string.date_filter_3_days)
                    is DateFilter.Last7Days -> getString(R.string.date_filter_7_days)
                    else -> getString(R.string.date_filter_30_days)
                }
            })

            cvDateFilter.setOnClickListener { sSortDropdown.performClick() }
        }
    }

    private inner class ScreenSliderAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> TopupFragment()
            1 -> DataFragment()
            2 -> PromoFragment()
            3 -> VoiceFragment()
            else -> SMSFragment()
        }
    }

    override val logTag = "PrepaidLedgerFragment"
}
