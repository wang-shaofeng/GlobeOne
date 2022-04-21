package ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.topup

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesFragmentDirections
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesViewModel
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.PrepaidLedgerAdapter
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.PrepaidLedgerViewModel
import ph.com.globe.globeonesuperapp.databinding.ScreenSliderFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment

@AndroidEntryPoint
class TopupFragment : NestedViewBindingFragment<ScreenSliderFragmentBinding>({
    ScreenSliderFragmentBinding.inflate(it)
}) {

    private val prepaidLedgerViewModel: PrepaidLedgerViewModel by navGraphViewModels(R.id.account_activities_subgraph)

    private val topupViewModel: TopupViewModel by hiltNavGraphViewModels(R.id.account_activities_subgraph)

    private val accountActivityViewModel by hiltNavGraphViewModels<AccountActivitiesViewModel>(R.id.account_activities_subgraph)

    private lateinit var adapter: PrepaidLedgerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            accountActivityViewModel.enrolledAccount.observe(viewLifecycleOwner) {
                if (it != null) {
                    topupViewModel.setEnrolledAccount(it)
                }
            }

            adapter = PrepaidLedgerAdapter(
                onClick = {
                    findNavController().safeNavigate(
                        AccountActivitiesFragmentDirections.actionAccountActivityFragmentToAccountPrepaidLedgerDetailsFragment(
                            prepaidLedgerItem = it,
                            msisdn = accountActivityViewModel.enrolledAccount.value?.primaryMsisdn,
                            alias = accountActivityViewModel.enrolledAccount.value?.accountAlias
                        )
                    )
                },
                reachEnd = { topupViewModel.loadMore() },
                somethingWentWrongOnClick = { topupViewModel.reload() },
                backToTopOnCLick = {
                    viewBinding.rvPrepaidTransaction.layoutManager?.scrollToPosition(0)
                    accountActivityViewModel.scrollToTop()
                }
            )

            rvPrepaidTransaction.setHasFixedSize(true)
            rvPrepaidTransaction.adapter = adapter

            prepaidLedgerViewModel.dateFilter.observe(viewLifecycleOwner) {
                topupViewModel.setFilterDate(it)
            }

            topupViewModel.transactions.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
        }
    }

    override val logTag = "TopupFragment"
}
