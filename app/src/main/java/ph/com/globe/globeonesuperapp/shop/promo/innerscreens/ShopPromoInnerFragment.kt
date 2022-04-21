/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.innerscreens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.CLICKABLE_ICON
import ph.com.globe.analytics.events.CLICKABLE_TEXT
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.PROMOS_SCREEN
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopPromoInnerFragmentBinding
import ph.com.globe.globeonesuperapp.shop.ShopViewModel
import ph.com.globe.globeonesuperapp.shop.promo.ShopOfferRecyclerViewAdapter
import ph.com.globe.globeonesuperapp.shop.promo.filter.ShopOffersSortFilterViewModel
import ph.com.globe.globeonesuperapp.shop.promo.filter.SortType
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.RAFFLE_DETAILS_URL
import ph.com.globe.globeonesuperapp.shop.promo.search.SEARCH_TYPE_SHOP
import ph.com.globe.globeonesuperapp.shop.select_other_account.LOGGED_IN_STATUS_KEY
import ph.com.globe.globeonesuperapp.shop.select_other_account.TITLE_KEY
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.AppConstants.ALL_PROMOS_TAB
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class ShopPromoInnerFragment : NoBottomNavViewBindingFragment<ShopPromoInnerFragmentBinding>(
    bindViewBy = {
        ShopPromoInnerFragmentBinding.inflate(it)
    }
), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val shopViewModel: ShopViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val contactsViewModel: ContactsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val shopOffersSortFilterViewModel: ShopOffersSortFilterViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private val shopPromoInnerFragmentArgs by navArgs<ShopPromoInnerFragmentArgs>()

    private lateinit var shopOfferRecyclerViewAdapterOne: ShopOfferRecyclerViewAdapter
    private lateinit var shopOfferRecyclerViewAdapterTwo: ShopOfferRecyclerViewAdapter
    private lateinit var shopOfferRecyclerViewAdapterThree: ShopOfferRecyclerViewAdapter
    private lateinit var allOffersRecyclerViewAdapter: ShopOfferRecyclerViewAdapter
    private lateinit var shopFilteredOfferRecyclerViewAdapter: ShopOfferRecyclerViewAdapter
    private lateinit var unavailableOffersRecyclerViewAdapter: ShopOfferRecyclerViewAdapter
    private lateinit var innerScreenBoosterRecyclerViewAdapter: InnerScreenBoosterRecyclerViewAdapter

    private var lastTab = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:promos screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {
            with(incPromos) {
                shopOfferRecyclerViewAdapterOne = createAdapter()
                rvOffersOne.adapter = shopOfferRecyclerViewAdapterOne

                shopOfferRecyclerViewAdapterTwo = createAdapter()
                rvOffersTwo.adapter = shopOfferRecyclerViewAdapterTwo

                shopOfferRecyclerViewAdapterThree = createAdapter()
                rvOffersThree.adapter = shopOfferRecyclerViewAdapterThree

                allOffersRecyclerViewAdapter = createAdapter()
                rvAllOffers.adapter = allOffersRecyclerViewAdapter

                unavailableOffersRecyclerViewAdapter = createAdapter()
                rvUnavailablePromos.adapter = unavailableOffersRecyclerViewAdapter

                shopFilteredOfferRecyclerViewAdapter = createAdapter()
                rvFiltered.adapter = shopFilteredOfferRecyclerViewAdapter

                innerScreenBoosterRecyclerViewAdapter =
                    InnerScreenBoosterRecyclerViewAdapter { item ->
                        findNavController().safeNavigate(
                            ShopPromoInnerFragmentDirections.actionShopPromoInnerFragmentToShopBoosterDetailsFragment(
                                item
                            )
                        )
                    }
                rvBoosters.adapter = innerScreenBoosterRecyclerViewAdapter

                etMobileNumber.setOnEditorActionListener { v, _, _ ->
                    if (v.text.isNotEmpty())
                        contactsViewModel.selectAndValidateNumber(v.text.toString())
                    closeKeyboard(v, requireContext())
                    true
                }

                ivRaffleBanner.setOnClickListener {
                    generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(RAFFLE_DETAILS_URL))
                        startActivity(intent)
                    }
                }

                tlBrands.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    private fun refresh(tab: TabLayout.Tab?) {
                        lastTab = tab!!.position
                        shopOffersSortFilterViewModel.setPromosCurrentTab(lastTab)
                        ivRaffleBanner.isVisible =
                            (lastTab == 1 || lastTab == 3) && shopViewModel.isRaffleSetABInProgress
                    }

                    override fun onTabSelected(tab: TabLayout.Tab?) = refresh(tab)
                    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                    override fun onTabReselected(tab: TabLayout.Tab?) = refresh(tab)
                })

                if (lastTab == -1) {
                    tlBrands.selectTab(tlBrands.getTabAt(shopPromoInnerFragmentArgs.tabToScroll))
                    lastTab = shopPromoInnerFragmentArgs.tabToScroll
                } else tlBrands.selectTab(tlBrands.getTabAt(lastTab))

                spSortDropdown.adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.sort_dropdown_item_layout,
                    resources.getStringArray(R.array.sort_array)
                )

                spSortDropdown.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            logCustomEvent(
                                analyticsEventsProvider.provideEvent(
                                    EventCategory.Engagement,
                                    PROMOS_SCREEN,
                                    CLICKABLE_TEXT,
                                    SortType.toAnalyticsTextValue(position)
                                )
                            )
                            shopOffersSortFilterViewModel.sortPromos(SortType.toSortType(position))
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                    }

                wfShop.onBack {
                    findNavController().navigateUp()
                }

                if (shopPromoInnerFragmentArgs.isFromAccountDetail) {
                    wfShop.setLabel(getString(R.string.account_details))
                    wfShop.setAllCaps(true)
                }

                ivFilter.setOnClickListener {
                    findNavController().safeNavigate(
                        ShopPromoInnerFragmentDirections.actionShopPromoInnerFragmentToShopPromoFilterFragment(
                            lastTab
                        )
                    )
                }

                tvSearchButton.setOnClickListener {
                    findNavController().safeNavigate(
                        ShopPromoInnerFragmentDirections.actionShopPromoInnerFragmentToShopSearchFragment(
                            SEARCH_TYPE_SHOP
                        )
                    )
                }

                tvViewAllBoosters.setOnClickListener {
                    shopOffersSortFilterViewModel.addBoosterFilter()
                }

                with(shopOffersSortFilterViewModel) {
                    promosFilteredOffersList.observe(viewLifecycleOwner, { shopModel ->
                        shopModel.sections.let { sections ->
                            if (sections != null && sections[0].name != null) {
                                val sectionList = sections[0].sectionList
                                val sectionName = sections[0].name
                                shopOfferRecyclerViewAdapterOne.submitList(sectionList)
                                tvSectionOneTitle.text = sectionName
                                tvSectionOneTitle.visibility = View.VISIBLE
                                rvOffersOne.visibility = View.VISIBLE
                            } else {
                                tvSectionOneTitle.visibility = View.GONE
                                rvOffersOne.visibility = View.GONE
                            }

                            if (sections != null && sections[1].name != null) {
                                val sectionList = sections[1].sectionList
                                val sectionName = sections[1].name
                                shopOfferRecyclerViewAdapterTwo.submitList(sectionList)
                                tvSectionTwoTitle.text = sectionName
                                tvSectionTwoTitle.visibility = View.VISIBLE
                                rvOffersTwo.visibility = View.VISIBLE
                            } else {
                                tvSectionTwoTitle.visibility = View.GONE
                                rvOffersTwo.visibility = View.GONE
                            }

                            if (sections != null && sections[2].name != null) {
                                val sectionList = sections[2].sectionList
                                val sectionName = sections[2].name
                                shopOfferRecyclerViewAdapterThree.submitList(sectionList)
                                tvSectionThreeTitle.text = sectionName
                                tvSectionThreeTitle.visibility = View.VISIBLE
                                rvOffersThree.visibility = View.VISIBLE
                            } else {
                                tvSectionThreeTitle.visibility = View.GONE
                                rvOffersThree.visibility = View.GONE
                            }

                            if (sections?.get(0)?.name.isNullOrEmpty()
                                && sections?.get(1)?.name.isNullOrEmpty()
                                && sections?.get(2)?.name.isNullOrEmpty()
                            ) {
                                clNoOffers.visibility = View.VISIBLE

                                if (numberOfFiltersApplied > 0) {
                                    tvNoOffersTitle.text =
                                        getString(R.string.no_promos_with_filters_title)
                                    tvNoOffersDescription.text =
                                        getString(R.string.no_promos_with_filters_description)
                                } else {
                                    tvNoOffersTitle.text = getString(R.string.no_promos_yet)
                                    tvNoOffersDescription.text =
                                        getString(R.string.check_this_page_daily_promos)
                                }

                            } else {
                                clNoOffers.visibility = View.GONE
                            }
                        }
                        shopModel.filteredList.let { filtered ->
                            if (filtered != null) {
                                shopFilteredOfferRecyclerViewAdapter.submitList(filtered)
                                rvFiltered.isVisible = filtered.isNotEmpty()
                                clNoOffers.isVisible = filtered.isEmpty()
                            } else {
                                rvFiltered.visibility = View.GONE
                            }
                        }
                    })

//                    unavailablePromosList.observe(viewLifecycleOwner, { unavailablePromos ->
//                        if (unavailablePromos.isEmpty()) {
//                            rvUnavailablePromos.visibility = View.GONE
//                            tvUnavailablePromosTitle.visibility = View.GONE
//                            tvUnavailablePromosDescription.visibility = View.GONE
//                        } else {
//                            rvUnavailablePromos.visibility = View.VISIBLE
//                            tvUnavailablePromosTitle.visibility = View.VISIBLE
//                            tvUnavailablePromosDescription.visibility = View.VISIBLE
//                        }
//                        unavailableOffersRecyclerViewAdapter.submitList(unavailablePromos)
//                    })

                    filteredPromosAllOffersList.observe(viewLifecycleOwner, { allPromos ->
                        allOffersRecyclerViewAdapter.submitList(allPromos)
                    })

                    numberOfFilters.observe(viewLifecycleOwner, { numberOfFilters ->
                        if (numberOfFilters > 0) {
                            ivFilter.setImageResource(R.drawable.ic_filter_selected)
                            tvFilterBadge.visibility = View.VISIBLE
                            tvFilterBadge.text = numberOfFilters.toString()
                        } else {
                            ivFilter.setImageResource(R.drawable.ic_filter_button)
                            tvFilterBadge.visibility = View.GONE
                        }
                    })

                    shopViewModel.loggedIn.observe(viewLifecycleOwner, { loggedIn ->
                        if (loggedIn) {
                            tilMobileNumber.setEndIconDrawable(R.drawable.ic_edit)
                            tilMobileNumber.setEndIconOnClickListener {
                                findNavController().safeNavigate(
                                    R.id.action_shopPromoInnerFragment_to_selectOtherAccountFragment,
                                    bundleOf(
                                        TITLE_KEY to getString(R.string.promos),
                                        LOGGED_IN_STATUS_KEY to loggedIn
                                    )
                                )
                            }
                            etMobileNumber.isFocusable = false
                        } else {
                            tilMobileNumber.setEndIconDrawable(R.drawable.ic_user)
                            tilMobileNumber.setEndIconOnClickListener {
                                findNavController().safeNavigate(ShopPromoInnerFragmentDirections.actionShopPromoInnerFragmentToContactsFragment())
                            }
                            etMobileNumber.isFocusableInTouchMode = true
                        }
                    })

                    contactsViewModel.selectedNumber.observe(viewLifecycleOwner) { number ->
                        etMobileNumber.setText(number)
                    }

                    contactsViewModel.lastCheckedNumberValidation.observe(
                        viewLifecycleOwner,
                        { validation ->
                            reflectValidationToErrorDisplaying(
                                validation,
                                etMobileNumber,
                                tilMobileNumber
                            )
                            shopOffersSortFilterViewModel.setBrand(validation.brand)
                            if (validation.isValid) getPromoSubscriptionHistory(validation.number)
                        })

                    etMobileNumber.addTextChangedListener { editable ->
                        requireContext().hideError(tilMobileNumber, etMobileNumber)
                        editable.formatCountryCodeIfExists()

                        tilMobileNumber.hint = contactsViewModel.getNumberOwnerOrPlaceholder(
                            editable.getStringOrNull(),
                            getString(R.string.mobile_number)
                        )
                    }

                    promoActivePage.observe(viewLifecycleOwner) {
                        when (it.currentTab) {
                            ALL_PROMOS_TAB -> {
                                clSectionsLists.visibility = View.GONE
                                clAllLists.isVisible = !it.hasFilters

                            }
                            else -> {
                                clAllLists.visibility = View.GONE
                                clSectionsLists.isVisible = !it.hasFilters

                            }
                        }
                    }

                    boostersForUser.observe(viewLifecycleOwner, { list ->
                        clBoosters.isVisible = list.isNotEmpty()
                        if (boosterFilterApplied) clNoOffers.isVisible = list.isEmpty()
                        innerScreenBoosterRecyclerViewAdapter.submitList(list)
                    })

                    hidePromos.observe(viewLifecycleOwner, {
                        it.handleEvent { hide ->
                            when (promoActivePage.value!!.currentTab) {
                                ALL_PROMOS_TAB -> {
                                    tvBoostersSectionTitle.isVisible = !hide
                                    tvViewAllBoosters.isVisible = !hide
                                    clAllLists.isVisible = !hide
                                }
                                else -> {
                                    clSectionsLists.isVisible = !hide
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    private fun createAdapter() = ShopOfferRecyclerViewAdapter { item ->
        logCustomEvent(
            analyticsEventsProvider.provideEvent(
                EventCategory.Engagement,
                PROMOS_SCREEN, CLICKABLE_ICON, item.name
            )
        )
        findNavController().safeNavigate(
            ShopPromoInnerFragmentDirections.actionShopPromoInnerFragmentToShopItemDetailsFragment(
                item
            )
        )
    }

    override val logTag = "ShopPromoInnerFragment"

    override val analyticsScreenName = "shop.promos"
}
