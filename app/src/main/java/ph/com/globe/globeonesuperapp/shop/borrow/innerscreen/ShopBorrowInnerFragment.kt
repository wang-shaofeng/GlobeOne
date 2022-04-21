/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.borrow.innerscreen

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.BORROW_SCREEN
import ph.com.globe.analytics.events.CLICKABLE_TEXT
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopBorrowInnerFragmentBinding
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.BORROW_ID
import ph.com.globe.globeonesuperapp.shop.ShopViewModel
import ph.com.globe.globeonesuperapp.shop.promo.ShopOfferRecyclerViewAdapter
import ph.com.globe.globeonesuperapp.shop.promo.filter.ShopOffersSortFilterViewModel
import ph.com.globe.globeonesuperapp.shop.promo.filter.SortType
import ph.com.globe.globeonesuperapp.shop.promo.search.SEARCH_TYPE_SHOP
import ph.com.globe.globeonesuperapp.shop.select_other_account.LOGGED_IN_STATUS_KEY
import ph.com.globe.globeonesuperapp.shop.select_other_account.TITLE_KEY
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class ShopBorrowInnerFragment : NoBottomNavViewBindingFragment<ShopBorrowInnerFragmentBinding>(
    bindViewBy = {
        ShopBorrowInnerFragmentBinding.inflate(it)
    }
), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val shopViewModel: ShopViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val contactsViewModel: ContactsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val shopOffersSortFilterViewModel: ShopOffersSortFilterViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private val shopBorrowInnerFragmentArgs by navArgs<ShopBorrowInnerFragmentArgs>()

    private lateinit var shopOfferRecyclerViewAdapterOne: ShopOfferRecyclerViewAdapter
    private lateinit var shopOfferRecyclerViewAdapterTwo: ShopOfferRecyclerViewAdapter
    private lateinit var shopOfferRecyclerViewAdapterThree: ShopOfferRecyclerViewAdapter
    private lateinit var allOffersRecyclerViewAdapter: ShopOfferRecyclerViewAdapter
    private lateinit var unavailableOffersRecyclerViewAdapter: ShopOfferRecyclerViewAdapter

    private var lastTab = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:borrow screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {
            with(incOffers) {
                shopOfferRecyclerViewAdapterOne = createAdapter()
                rvOffersOne.adapter = shopOfferRecyclerViewAdapterOne

                shopOfferRecyclerViewAdapterTwo = createAdapter()
                rvOffersTwo.adapter = shopOfferRecyclerViewAdapterTwo

                shopOfferRecyclerViewAdapterThree = createAdapter()
                rvOffersThree.adapter = shopOfferRecyclerViewAdapterThree

                allOffersRecyclerViewAdapter = createAdapter()
                rvAllOffers.adapter = allOffersRecyclerViewAdapter

                unavailableOffersRecyclerViewAdapter = createAdapter()
                rvUnavailableOffers.adapter = unavailableOffersRecyclerViewAdapter

                etMobileNumber.setOnEditorActionListener { v, _, _ ->
                    if (v.text.isNotEmpty()) contactsViewModel.selectAndValidateNumber(v.text.toString())
                    closeKeyboard(v, requireContext())
                    true
                }

                clAllLists.visibility = View.VISIBLE
                tlBrands.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                    private fun refresh(tab: TabLayout.Tab?) {
                        lastTab = tab!!.position
                        shopOffersSortFilterViewModel.setBorrowCurrentTab(lastTab)
                    }

                    override fun onTabSelected(tab: TabLayout.Tab?) = refresh(tab)

                    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

                    override fun onTabReselected(tab: TabLayout.Tab?) = refresh(tab)
                })

                if (lastTab == -1) tlBrands.selectTab(tlBrands.getTabAt(shopBorrowInnerFragmentArgs.tabToScroll))
                else tlBrands.selectTab(tlBrands.getTabAt(lastTab))

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
                                    BORROW_SCREEN, CLICKABLE_TEXT, SortType.toAnalyticsTextValue(position)
                                )
                            )
                            shopOffersSortFilterViewModel.sortLoanable(SortType.toSortType(position))
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                    }

                wfShop.onBack {
                    findNavController().navigateUp()
                }

                tvSearchButton.setOnClickListener {
                    findNavController().safeNavigate(
                        ShopBorrowInnerFragmentDirections.actionShopBorrowInnerFragmentToShopSearchFragment(
                            SEARCH_TYPE_SHOP, BORROW_ID
                        )
                    )
                }

                with(shopOffersSortFilterViewModel) {
                    loanableFilteredOffersLists.observe(viewLifecycleOwner, { sections ->
                        if (sections != null && sections[0].name != null) {
                            val s = sections[0].sectionList
                            val sName = sections[0].name
                            shopOfferRecyclerViewAdapterOne.submitList(s)
                            tvSectionOneTitle.text = sName
                            tvSectionOneTitle.visibility = View.VISIBLE
                            rvOffersOne.visibility = View.VISIBLE
                        } else {
                            tvSectionOneTitle.visibility = View.GONE
                            rvOffersOne.visibility = View.GONE
                        }

                        if (sections != null && sections[1].name != null) {
                            val s = sections[1].sectionList
                            val sName = sections[1].name
                            shopOfferRecyclerViewAdapterTwo.submitList(s)
                            tvSectionTwoTitle.text = sName
                            tvSectionTwoTitle.visibility = View.VISIBLE
                            rvOffersTwo.visibility = View.VISIBLE
                        } else {
                            tvSectionTwoTitle.visibility = View.GONE
                            rvOffersTwo.visibility = View.GONE
                        }

                        if (sections != null && sections[2].name != null) {
                            val s = sections[2].sectionList
                            val sName = sections[2].name
                            shopOfferRecyclerViewAdapterThree.submitList(s)
                            tvSectionThreeTitle.text = sName
                            tvSectionThreeTitle.visibility = View.VISIBLE
                            rvOffersThree.visibility = View.VISIBLE
                        } else {
                            tvSectionThreeTitle.visibility = View.GONE
                            rvOffersThree.visibility = View.GONE
                        }


                        loanableAllOffersList.observe(viewLifecycleOwner, { allPromos ->
                            allOffersRecyclerViewAdapter.submitList(allPromos)
                            clAllLists.isVisible = allPromos.isNotEmpty()
                            clNoOffers.isVisible = sections?.get(0)?.name.isNullOrEmpty()
                                    && sections?.get(1)?.name.isNullOrEmpty()
                                    && sections?.get(2)?.name.isNullOrEmpty() && allPromos.isEmpty()
                        })
                    })

//                    unavailableLoansList.observe(viewLifecycleOwner, { unavailableLoans ->
//                        if (unavailableLoans.isEmpty()) {
//                            rvUnavailableOffers.visibility = View.GONE
//                            tvUnavailableOffersTitle.visibility = View.GONE
//                            tvUnavailableOffersDescription.visibility = View.GONE
//                        } else {
//                            rvUnavailableOffers.visibility = View.VISIBLE
//                            tvUnavailableOffersTitle.visibility = View.VISIBLE
//                            tvUnavailableOffersDescription.visibility = View.VISIBLE
//                        }
//                        unavailableOffersRecyclerViewAdapter.submitList(unavailableLoans)
//                    })

                    shopViewModel.loggedIn.observe(viewLifecycleOwner, { loggedIn ->
                        if (loggedIn) {
                            tilMobileNumber.setEndIconDrawable(R.drawable.ic_edit)
                            tilMobileNumber.setEndIconOnClickListener {
                                findNavController().safeNavigate(
                                    R.id.action_shopBorrowInnerFragment_to_selectOtherAccountFragment,
                                    bundleOf(
                                        TITLE_KEY to getString(R.string.shop_tab_borrow),
                                        LOGGED_IN_STATUS_KEY to loggedIn
                                    )
                                )
                            }
                            etMobileNumber.isFocusable = false
                        } else {
                            tilMobileNumber.setEndIconDrawable(R.drawable.ic_user)
                            tilMobileNumber.setEndIconOnClickListener {
                                findNavController().safeNavigate(ShopBorrowInnerFragmentDirections.actionShopBorrowInnerFragmentToContactsFragment())
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
                        })

                    etMobileNumber.addTextChangedListener { editable ->
                        requireContext().hideError(tilMobileNumber, etMobileNumber)
                        editable.formatCountryCodeIfExists()

                        tilMobileNumber.hint = contactsViewModel.getNumberOwnerOrPlaceholder(editable.getStringOrNull(), getString(R.string.mobile_number))
                    }
                    borrowActivePage.observe(viewLifecycleOwner) {
                        if (it == AppConstants.ALL_PROMOS_TAB) {
                            clSectionsLists.visibility = View.GONE
                            clAllLists.visibility = View.VISIBLE
                        } else {
                            clAllLists.visibility = View.GONE
                            clSectionsLists.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun createAdapter() = ShopOfferRecyclerViewAdapter { item ->
        logCustomEvent(
            analyticsEventsProvider.provideEvent(
                EventCategory.Engagement,
                BORROW_SCREEN, CLICKABLE_TEXT, item.name
            )
        )
        findNavController().safeNavigate(
            ShopBorrowInnerFragmentDirections.actionShopBorrowInnerFragmentToShopItemDetailsFragment(
                item
            )
        )
    }

    override val logTag = "ShopBorrowInnerFragment"

    override val analyticsScreenName = "shop.borrow"
}
