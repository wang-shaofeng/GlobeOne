package ph.com.globe.globeonesuperapp.account.data.info

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AccountDetailsViewModel
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsDataUsageInfoFragmentBinding
import ph.com.globe.globeonesuperapp.utils.getRefreshDate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.group.domain_models.DATA_TYPE_RECURRING_ACCESS

class DataUsageInfoFragment :
    NoBottomNavViewBindingFragment<AccountDetailsDataUsageInfoFragmentBinding>(bindViewBy = {
        AccountDetailsDataUsageInfoFragmentBinding.inflate(it)
    }) {

    private val accountDetailsViewModel: AccountDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    private val args: DataUsageInfoFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setLightStatusBar()

        with(viewBinding) {
            with(args.usageItem) {

                wfAccountDetails.onBack {
                    findNavController().navigateUp()
                }

                tvUsageInfoTitle.text = title
                ivAddOn.isVisible = addOnData

                val usagesAdapter = DataUsageInfoAdapter(addOnDataType)

                rvUsageItems.adapter = usagesAdapter
                usagesAdapter.submitList(includedPromos)

                if (addOnDataType == DATA_TYPE_RECURRING_ACCESS) {
                    // Get refresh date from billing details
                    accountDetailsViewModel.billingDetails.observe(viewLifecycleOwner) { billingDetails ->
                        val refreshDate = billingDetails.getRefreshDate()
                        usagesAdapter.submitList(includedPromos.map { it.copy(endDate = refreshDate) })
                    }
                }
            }
        }
    }

    override val logTag = "DataUsageInfoFragment"
}
