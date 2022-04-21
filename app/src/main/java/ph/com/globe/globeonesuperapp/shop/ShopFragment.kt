/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopFragmentBinding
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.BORROW_ID
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.CONTENT_ID
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.LOAD_ID
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.PROMO_ID
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.AuthenticatedBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.utils.view_binding.BottomNavVisibility
import ph.com.globe.model.shop.formattedForPhilippines

@AndroidEntryPoint
class ShopFragment :
    AuthenticatedBottomNavViewBindingFragment<ShopFragmentBinding>(bindViewBy = {
        ShopFragmentBinding.inflate(
            it
        )
    }) {

    private val shopFragmentArgs by navArgs<ShopFragmentArgs>()

    private val shopViewModel: ShopViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private val contactsViewModel: ContactsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private var currentSelectedTab: Int = -1

    private var bottomNavVisibility = BottomNavVisibility.AUTHENTICATED_BOTTOM_NAV

    private val onPageChangeCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                shopViewModel.onPageSelected(position)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) = Unit

            override fun onPageScrollStateChanged(state: Int) = Unit
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (shopFragmentArgs.isToShopItemDetails && shopFragmentArgs.shopItem != null) {
            findNavController().safeNavigate(
                ShopFragmentDirections.actionGlobalShopItemDetailsFragment(
                    shopFragmentArgs.shopItem!!,
                    true
                )
            )
        }
        if (shopFragmentArgs.isToShopPromoInner) {
            findNavController().safeNavigate(
                ShopFragmentDirections.actionGlobalShopPromoInnerFragment(isFromAccountDetail = true)
            )
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setLightStatusBar()

        if (currentSelectedTab == -1) currentSelectedTab = shopFragmentArgs.tabToSelect

        shopFragmentArgs.mobileNumber?.let { forwardedNumber ->
            contactsViewModel.selectAndValidateNumber(forwardedNumber.formattedForPhilippines())
            shopViewModel.selectLoadFragment(ShopPagerAdapter.LOAD_FULL_ID)
        }

        with(viewBinding) {
            with(shopViewModel) {

                wfShop.onBack {
                    findNavController().navigateUp()
                }

                vpShop.apply {
                    adapter = ShopPagerAdapter(this@ShopFragment)
                    registerOnPageChangeCallback(onPageChangeCallback)
                }

                selectedLoadFragment.observe(viewLifecycleOwner, { fragmentId ->
                    (vpShop.adapter as ShopPagerAdapter).selectLoadFragment(fragmentId)
                })

                TabLayoutMediator(tlShop, vpShop) { tab, position ->
                    tab.text = when (position) {

                        PROMO_ID -> resources.getString(R.string.shop_tab_promos)

                        LOAD_ID -> resources.getString(R.string.shop_tab_load)

                        BORROW_ID -> resources.getString(R.string.shop_tab_borrow)

                        CONTENT_ID -> resources.getString(R.string.shop_tab_content)

                        else -> throw IllegalStateException()
                    }
                }.attach()

                loggedIn.observe(viewLifecycleOwner, {
                    when {
                        it.not() -> {
                            wfShop.visibility = View.VISIBLE
                            wfShop.setLabel(getString(R.string.wayfinder_get_started))
                            wfShop.setAllCaps(true)
                        }
                        /**
                         * show the title from the previous page
                         */
                        shopFragmentArgs.toolbarTab != null -> {
                            wfShop.visibility = View.VISIBLE
                            wfShop.setLabel(shopFragmentArgs.toolbarTab!!)
                            wfShop.setAllCaps(true)
                            bottomNavVisibility = BottomNavVisibility.NO_BOTTOM_NAV
                        }
                        else -> {
                            wfShop.visibility = View.GONE
                        }
                    }
                })

                tlShop.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        currentSelectedTab = tab?.position ?: 0
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                    override fun onTabReselected(tab: TabLayout.Tab?) {
                        currentSelectedTab = tab?.position ?: 0
                    }
                })

                if (currentSelectedTab == -1) wfShop.visibility = View.GONE
                else vpShop.setCurrentItem(currentSelectedTab, false)

                // Manual refreshing
                refreshEnabled.observe(viewLifecycleOwner) { isEnabled ->
                    srlShop.isEnabled = isEnabled
                }

                srlShop.setRefreshListener {
                    fetchOffers(forceRefresh = true)
                }

                isRefreshingData.observe(viewLifecycleOwner) { refreshing ->
                    srlShop.setRefreshing(refreshing)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        generalEventsViewModel.setBottomNavVisibility(bottomNavVisibility)
    }

    override val logTag = "ShopFragment"
}
