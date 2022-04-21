/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.itemdetails

import android.os.Bundle
import android.view.View
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.FreebiesListLayoutBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.util.FREEBIE_VOUCHER
import javax.inject.Inject

//TODO: maybe merge [FreebiesEntertainmentFragment] and [FreebiesHealthFragment] if they didn't have any different action in the future
@AndroidEntryPoint
class FreebiesHealthFragment @Inject constructor() :
    NoBottomNavViewBindingFragment<FreebiesListLayoutBinding>(
        bindViewBy = {
            FreebiesListLayoutBinding.inflate(it)
        }
    ), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider
    private val shopItemDetailsViewModel: ShopItemDetailsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private lateinit var freebiesSingleSelectRecyclerViewAdapter: FreebiesSingleSelectRecyclerViewAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            rvSingleSelectItems.itemAnimator = null
            freebiesSingleSelectRecyclerViewAdapter =
                FreebiesSingleSelectRecyclerViewAdapter { title, chargeParam, nonChargeParam, noneChargeId, apiProvisioningKeyword, freebieType ->
                    shopItemDetailsViewModel.selectFreebie(
                        title,
                        chargeParam,
                        nonChargeParam,
                        noneChargeId,
                        apiProvisioningKeyword,
                        freebieType
                    )
                }
            rvSingleSelectItems.adapter = freebiesSingleSelectRecyclerViewAdapter

            shopItemDetailsViewModel.singleSelectFreebiesLiveData.observe(
                viewLifecycleOwner,
                { freebies ->
                    freebiesSingleSelectRecyclerViewAdapter.submitList(freebies.filter { it.freebieSingleSelectItem.type == FREEBIE_VOUCHER })
                })
        }
    }

    //make viewpager height wrap content
    override fun onResume() {
        super.onResume()
        viewBinding.root.requestLayout()
    }

    override val logTag = "FreebiesHealthFragment"

    override val analyticsScreenName = "FreebiesHealthFragment"
}
