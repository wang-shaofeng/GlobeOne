package ph.com.globe.globeonesuperapp.account_activities.rewards

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesFragmentDirections
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesViewModel
import ph.com.globe.globeonesuperapp.databinding.AccountRewardsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.date.DateFilter
import ph.com.globe.globeonesuperapp.utils.date.MINUS_ONE_DAY
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment

@AndroidEntryPoint
class AccountRewardsFragment : NestedViewBindingFragment<AccountRewardsFragmentBinding>({
    AccountRewardsFragmentBinding.inflate(it)
}) {

    private val accountRewardsViewModel by hiltNavGraphViewModels<AccountRewardsViewModel>(R.id.account_activities_subgraph)

    private val accountActivityViewModel by hiltNavGraphViewModels<AccountActivitiesViewModel>(R.id.account_activities_subgraph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountActivityViewModel.enrolledAccount.observe(viewLifecycleOwner) {
            accountRewardsViewModel.setEnrolledAccount(it)
        }

        with(viewBinding) {

            val adapter = AccountRewardsAdapter(
                onClick = {
                    findNavController().safeNavigate(
                        AccountActivitiesFragmentDirections.actionAccountActivityFragmentToAccountRewardDetailsFragment(
                            it
                        )
                    )
                },
                reachEnd = { accountRewardsViewModel.loadMore() },
                somethingWentWrongOnClick = { accountRewardsViewModel.reload() },
                backToTopOnCLick = {
                    viewBinding.rvRewards.layoutManager?.scrollToPosition(0)
                    accountActivityViewModel.scrollToTop()
                }
            )

            sSortDropdown.adapter = ArrayAdapter(
                requireContext(),
                R.layout.sort_dropdown_item_layout,
                resources.getStringArray(R.array.rewards_date_filter)
            )

            sSortDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    accountRewardsViewModel.setDateFilter(
                        when (position) {
                            0 -> DateFilter.Yesterday(buffer = MINUS_ONE_DAY)
                            1 -> DateFilter.Last3Days(buffer = MINUS_ONE_DAY)
                            2 -> DateFilter.Last7Days(buffer = MINUS_ONE_DAY)
                            else -> DateFilter.Last30Days(buffer = MINUS_ONE_DAY)
                        }
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

            accountRewardsViewModel.dateFilter.observe(viewLifecycleOwner, {
                tvDateFilter.text = when (it) {
                    is DateFilter.Yesterday -> getString(R.string.date_filter_yesterday)
                    is DateFilter.Last3Days -> getString(R.string.date_filter_3_days)
                    is DateFilter.Last7Days -> getString(R.string.date_filter_7_days)
                    else -> getString(R.string.date_filter_30_days)
                }
            })

            cvDateFilter.setOnClickListener { sSortDropdown.performClick() }

            rvRewards.setHasFixedSize(true)
            rvRewards.adapter = adapter

            accountRewardsViewModel.transactions.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
        }
    }

    override val logTag = "AccountRewardsFragment"
}
