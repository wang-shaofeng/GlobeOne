/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.boosterdetails

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.logger.eLog
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopBoosterDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.PromoServiceItem
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemServiceRecyclerViewAdapter
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.formatPhoneNumber
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.payment.*
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.shop.domain_models.PROMO_API_SERVICE_PROVISION
import ph.com.globe.model.shop.formattedForPhilippines
import ph.com.globe.model.shop.justPhoneNumber

@AndroidEntryPoint
class ShopBoosterDetailsFragment :
    NoBottomNavViewBindingFragment<ShopBoosterDetailsFragmentBinding>(
        bindViewBy = {
            ShopBoosterDetailsFragmentBinding.inflate(it)
        }
    ) {

    private lateinit var boosterApplicationsRecyclerViewAdapter: BoosterApplicationsRecyclerViewAdapter
    private lateinit var shopItemInclusionsRecyclerViewAdapter: ShopItemServiceRecyclerViewAdapter

    private val contactsViewModel: ContactsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private val shopBoosterDetailsFragmentArgs by navArgs<ShopBoosterDetailsFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val booster = shopBoosterDetailsFragmentArgs.booster

        with(viewBinding) {
            tvBoosterName.text = booster.name
            tvBoosterPrice.text = booster.price.stringToPesos()
            tvBoosterValidity.text = resources.setValidityText(booster.validity)
            tvBoosterDescription.text = booster.dataDescription

            try {
                clBoosterHeader.setBackgroundColor(Color.parseColor(booster.displayColor))
            } catch (e: Exception) {
                clBoosterHeader.setBackgroundColor(Color.parseColor("#000000"))
                eLog(Exception("displayColor has a bad format: ${booster.displayColor}"))
            }

            tvBoosterFor.text = resources.getString(R.string.booster_for, booster.forPromo)
            tvBoosterAvailableFor.text =
                resources.getString(R.string.booster_available_for, booster.forPromo)

            val inclusionsList = mutableListOf<PromoServiceItem>()
            booster.data?.forEach {
                inclusionsList.add(
                    PromoServiceItem(
                        R.drawable.ic_promo_details_data_icon,
                        booster.dataDescription
                    )
                )
            }
            shopItemInclusionsRecyclerViewAdapter = ShopItemServiceRecyclerViewAdapter()
            rvInclusionsItems.adapter = shopItemInclusionsRecyclerViewAdapter
            shopItemInclusionsRecyclerViewAdapter.submitList(inclusionsList)

            booster.applicationService?.apps?.let {
                boosterApplicationsRecyclerViewAdapter = BoosterApplicationsRecyclerViewAdapter()
                rvApps.adapter = boosterApplicationsRecyclerViewAdapter
                boosterApplicationsRecyclerViewAdapter.submitList(it)
            }

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnSubscribe.setOnClickListener {
                val checkout = checkoutToPaymentParams(
                    paymentType = BUY_PROMO,
                    transactionType = NON_BILL,
                    mobileNumber = tvMobileNumber.text.toString().justPhoneNumber(),
                    promoChargeId = booster.chargePromoId,
                    promoNonChargeId = booster.nonChargePromoId,
                    chargePromoParam = booster.chargeServiceParam,
                    nonChargePromoParam = booster.nonChargeServiceParam,
                    shareKeyword = booster.shareKeyword,
                    shareFee = booster.shareFee?.toDoubleOrNull() ?: 0.0,
                    paymentName = booster.name,
                    validity = booster.validity,
                    price = booster.price.toDoubleOrNull() ?: 0.0,
                    amount = booster.price.toDoubleOrNull() ?: 0.0,
                    discount = booster.discount?.toDoubleOrNull() ?: 0.0,
                    shareable = booster.shareable,
                    selectedBoosters = null,
                    skelligWallet = booster.skelligWallet,
                    skelligCategory = booster.skelligCategory,
                    provisionByServiceId = booster.apiSubscribe == PROMO_API_SERVICE_PROVISION
                )
                findNavController().safeNavigate(
                    ShopBoosterDetailsFragmentDirections.actionShopBoosterDetailsFragmentToPaymentSubgraph(
                        checkout
                    )
                )
            }

            contactsViewModel.selectedNumber.observe(viewLifecycleOwner, {
                val numberOwner = contactsViewModel.getNumberOwnerOrPlaceholder(it, "")
                if (numberOwner.isNotBlank()) tvAccountName.text = numberOwner
                else tvAccountName.visibility = View.GONE
                tvMobileNumber.text = it.formattedForPhilippines().formatPhoneNumber()
            })
        }
    }

    override val logTag = "ShopBoosterDetailsFragment"
}
