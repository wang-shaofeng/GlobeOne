package ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.promo

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
class PromoFragment : NestedViewBindingFragment<ScreenSliderFragmentBinding>({
    ScreenSliderFragmentBinding.inflate(it)
}) {

    private val prepaidLedgerViewModel: PrepaidLedgerViewModel by navGraphViewModels(R.id.account_activities_subgraph)

    private val promoViewModel: PromoViewModel by hiltNavGraphViewModels(R.id.account_activities_subgraph)

    private val accountActivityViewModel by hiltNavGraphViewModels<AccountActivitiesViewModel>(R.id.account_activities_subgraph)

    private lateinit var adapter: PrepaidLedgerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            accountActivityViewModel.enrolledAccount.observe(viewLifecycleOwner) {
                if (it != null) {
                    promoViewModel.setEnrolledAccount(it)
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
                reachEnd = { promoViewModel.loadMore() },
                somethingWentWrongOnClick = { promoViewModel.reload() },
                backToTopOnCLick = {
                    viewBinding.rvPrepaidTransaction.layoutManager?.scrollToPosition(0)
                    accountActivityViewModel.scrollToTop()
                }
            )

            prepaidLedgerViewModel.dateFilter.observe(viewLifecycleOwner) {
                promoViewModel.setFilterDate(it)
            }

            rvPrepaidTransaction.setHasFixedSize(true)
            rvPrepaidTransaction.adapter = adapter

            promoViewModel.transactions.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
        }
    }

    override val logTag = "PromoFragment"
}
