/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities.payments

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesFragmentDirections
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesViewModel
import ph.com.globe.globeonesuperapp.databinding.AccountBillPaymentsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.date.DateFilter
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment

class BillPaymentsFragment :
    NestedViewBindingFragment<AccountBillPaymentsFragmentBinding>({
        AccountBillPaymentsFragmentBinding.inflate(it)
    }) {

    private val billPaymentsViewModel by hiltNavGraphViewModels<BillPaymentsViewModel>(
        R.id.account_activities_subgraph
    )

    private val accountActivityViewModel by hiltNavGraphViewModels<AccountActivitiesViewModel>(R.id.account_activities_subgraph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountActivityViewModel.enrolledAccount.observe(viewLifecycleOwner) {
            billPaymentsViewModel.setEnrolledAccount(it)
        }

        with(viewBinding) {
            val adapter = BillPaymentAdapter(
                onClick = {
                    findNavController().safeNavigate(
                        AccountActivitiesFragmentDirections.actionAccountActivityFragmentToPaymentDetailsAccount(
                            it,
                            billPaymentsViewModel.token
                        )
                    )
                },
                somethingWentWrongOnClick = {
                    billPaymentsViewModel.load()
                },
                backToTopOnCLick = {
                    viewBinding.rvPayments.layoutManager?.scrollToPosition(0)
                    accountActivityViewModel.scrollToTop()
                }
            )

            sSortDropdown.adapter = ArrayAdapter(
                requireContext(),
                R.layout.sort_dropdown_item_layout,
                resources.getStringArray(R.array.date_filter_months)
            )

            sSortDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    billPaymentsViewModel.setDateFilter(
                        when (position) {
                            0 -> DateFilter.Last3Months
                            1 -> DateFilter.Last6Months
                            2 -> DateFilter.Last12Months
                            else -> DateFilter.Last24Months
                        }
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

            billPaymentsViewModel.dateFilter.observe(viewLifecycleOwner, {
                tvDateFilter.text = when (it) {
                    DateFilter.Last3Months -> getString(R.string.date_filter_last_3_months)
                    DateFilter.Last6Months -> getString(R.string.date_filter_last_6_months)
                    DateFilter.Last12Months -> getString(R.string.date_filter_last_12_months)
                    else -> getString(R.string.date_filter_last_24_months)
                }
            })

            cvDateFilter.setOnClickListener { sSortDropdown.performClick() }

            rvPayments.setHasFixedSize(true)
            rvPayments.adapter = adapter

            billPaymentsViewModel.data.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
        }
    }

    override val logTag = "AccountBillPaymentsFragment"
}
