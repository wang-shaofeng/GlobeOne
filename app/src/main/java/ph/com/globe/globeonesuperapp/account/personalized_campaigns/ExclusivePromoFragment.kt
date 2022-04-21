/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.personalized_campaigns

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ConcatAdapter
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AccountDetailsViewModel
import ph.com.globe.globeonesuperapp.databinding.ExclusivePromoFragmentBinding
import ph.com.globe.globeonesuperapp.rewards.rewards_adapters.TextAdapter
import ph.com.globe.globeonesuperapp.utils.formatPhoneNumber
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.shop.formattedForPhilippines

@AndroidEntryPoint
class ExclusivePromoFragment : NoBottomNavViewBindingFragment<ExclusivePromoFragmentBinding>({
    ExclusivePromoFragmentBinding.inflate(it)
}) {

    private val accountDetailsViewModel: AccountDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    private val args by navArgs<ExclusivePromoFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountDetailsViewModel.fetchOffersData(args.availableCampaignPromosModels.map { it.toModel() }
            .toList())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val textAdapter = TextAdapter(
            if (args.offerType == OfferType.EXCLUSIVE_PROMOS) getString(R.string.exclusive_sulit_promos)
            else getString(R.string.choose_your_free_app_data)
        )

        val promosAdapter = ExclusivePromosAdapter { shopItem, model ->
            findNavController().safeNavigate(
                ExclusivePromoFragmentDirections.actionExclusivePromoFragmentToExclusivePromoDetailsFragment(
                    model, shopItem, args.offerType
                )
            )
        }

        with(viewBinding) {

            if (args.offerType == OfferType.FREEBIE) {
                tvTitle.text = getString(R.string.choose_a_freebie)
                tvBuyingFor.text = getString(R.string.you_are_claiming_for)
            }

            ivBack.setOnClickListener { findNavController().navigateUp() }

            rvPromos.adapter = ConcatAdapter(textAdapter, promosAdapter)

            with(accountDetailsViewModel) {
                selectedEnrolledAccountLiveData.observe(viewLifecycleOwner) {
                    tvAccountPhoneNumber.text = it.primaryMsisdn.toDisplayUINumberFormat()
                }

                accountAlias.observe(viewLifecycleOwner) {
                    tvAccountName.text = it
                }

                offers.observe(viewLifecycleOwner) {
                    promosAdapter.submitList(it)
                    textAdapter.isVisible = it.isNotEmpty()
                }
            }
        }
    }

    override val logTag: String = "ExclusivePromoFragment"
}

enum class OfferType {
    EXCLUSIVE_PROMOS, FREEBIE
}
