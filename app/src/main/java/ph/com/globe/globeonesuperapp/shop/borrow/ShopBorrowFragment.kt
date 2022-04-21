/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.borrow

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopBorrowFragmentBinding
import ph.com.globe.globeonesuperapp.shop.ShopFragmentDirections
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.BORROW_ID
import ph.com.globe.globeonesuperapp.shop.ShopViewModel
import ph.com.globe.globeonesuperapp.shop.promo.ShopOfferRecyclerViewAdapter
import ph.com.globe.globeonesuperapp.shop.promo.filter.Section
import ph.com.globe.globeonesuperapp.shop.promo.filter.ShopOffersSortFilterViewModel
import ph.com.globe.globeonesuperapp.shop.select_other_account.LOGGED_IN_STATUS_KEY
import ph.com.globe.globeonesuperapp.shop.select_other_account.TITLE_KEY
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment
import ph.com.globe.model.shop.CatalogStatus
import javax.inject.Inject

@AndroidEntryPoint
class ShopBorrowFragment : NestedViewBindingFragment<ShopBorrowFragmentBinding>(bindViewBy = {
    ShopBorrowFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val shopViewModel: ShopViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val shopOffersSortFilterViewModel: ShopOffersSortFilterViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private val contactsViewModel: ContactsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private lateinit var shopOfferRecyclerViewAdapterOne: ShopOfferRecyclerViewAdapter
    private lateinit var shopOfferRecyclerViewAdapterTwo: ShopOfferRecyclerViewAdapter
    private lateinit var shopOfferRecyclerViewAdapterThree: ShopOfferRecyclerViewAdapter
    private lateinit var unavailableOffersRecyclerViewAdapter: ShopOfferRecyclerViewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            with(incAvailablePromos) {
                shopOfferRecyclerViewAdapterOne = createAdapter()
                rvOffersOne.adapter = shopOfferRecyclerViewAdapterOne

                shopOfferRecyclerViewAdapterTwo = createAdapter()
                rvOffersTwo.adapter = shopOfferRecyclerViewAdapterTwo

                shopOfferRecyclerViewAdapterThree = createAdapter()
                rvOffersThree.adapter = shopOfferRecyclerViewAdapterThree

                unavailableOffersRecyclerViewAdapter = createAdapter()
                rvUnavailableOffers.adapter = unavailableOffersRecyclerViewAdapter

                etMobileNumber.setOnEditorActionListener { v, _, _ ->
                    if (v.text.isNotEmpty()) contactsViewModel.selectAndValidateNumber(v.text.toString())
                    closeKeyboard(v, requireContext())
                    true
                }

                clGlobePrepaid.setOnClickListener {
                    findNavController().safeNavigate(
                        ShopFragmentDirections.actionShopFragmentToShopBorrowInnerFragment(1)
                    )
                }

                clTm.setOnClickListener {
                    findNavController().safeNavigate(
                        ShopFragmentDirections.actionShopFragmentToShopBorrowInnerFragment(2)
                    )
                }

                btnAllPromos.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            LOAN_CATALOG_SCREEN, BUTTON, VIEW_ALL_PROMOS
                        )
                    )
                    findNavController().safeNavigate(
                        ShopFragmentDirections.actionShopFragmentToShopBorrowInnerFragment(
                            AppConstants.ALL_PROMOS_TAB
                        )
                    )
                }

                val offersObserver = Observer<List<Section>> { sections ->
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

                    val isEmptyState = sections?.get(0)?.name.isNullOrEmpty()
                            && sections?.get(1)?.name.isNullOrEmpty()
                            && sections?.get(2)?.name.isNullOrEmpty()

                    groupOffers.isVisible = !isEmptyState
                    clNoOffers.isVisible = isEmptyState
                }

                shopViewModel.catalogStatus.observe(viewLifecycleOwner) { status ->

                    // Setup offers LiveData observer
                    with(shopOffersSortFilterViewModel.loanableOffersLists) {
                        when (status) {
                            CatalogStatus.Success -> observe(viewLifecycleOwner, offersObserver)
                            else -> {
                                groupOffers.visibility = View.GONE
                                removeObserver(offersObserver)
                            }
                        }
                    }

                    incLoading.root.isVisible = status is CatalogStatus.Loading
                    incError.root.isVisible = status is CatalogStatus.Error
                }

                // Manual refresh state handling based on vertical scroll
                with(shopViewModel) {
                    selectedTabId.observe(viewLifecycleOwner) { tabId ->
                        if (tabId == BORROW_ID)
                            setRefreshEnabled(svShopBorrow.scrollY == 0)
                    }

                    svShopBorrow.setOnScrollChangeListener(
                        NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                            setRefreshEnabled(scrollY == 0)
                        })
                }

                // Reload
                incError.btnReload.setOnClickListener {
                    shopViewModel.fetchOffers(forceRefresh = true)
                }

//                shopOffersSortFilterViewModel.unavailableOffersList.observe(
//                    viewLifecycleOwner,
//                    { unavailablePromos ->
//                        unavailableOffersRecyclerViewAdapter.submitList(unavailablePromos)
//                    })

                shopViewModel.loggedIn.observe(viewLifecycleOwner, { loggedIn ->
                    if (loggedIn) {
                        tilMobileNumber.setEndIconDrawable(R.drawable.ic_edit)
                        tilMobileNumber.setEndIconOnClickListener {
                            findNavController().safeNavigate(
                                R.id.action_shopFragment_to_selectOtherAccountFragment,
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
                            findNavController().safeNavigate(ShopFragmentDirections.actionShopFragmentToContactsFragment())
                        }
                        etMobileNumber.isFocusableInTouchMode = true
                    }
                })

                contactsViewModel.selectedNumber.observe(viewLifecycleOwner, {
                    etMobileNumber.setText(it)
                })

                contactsViewModel.lastCheckedNumberValidation.observe(
                    viewLifecycleOwner,
                    { validation ->
                        reflectValidationToErrorDisplaying(
                            validation,
                            etMobileNumber,
                            tilMobileNumber,
                            true
                        )
                        shopOffersSortFilterViewModel.setBrand(validation.brand)
                    })

                etMobileNumber.addTextChangedListener { editable ->
                    requireContext().hideError(tilMobileNumber, etMobileNumber)

                    editable.formatCountryCodeIfExists()

                    tilMobileNumber.hint = contactsViewModel.getNumberOwnerOrPlaceholder(editable.getStringOrNull(), getString(R.string.mobile_number))
                }
            }
        }
    }

    private fun createAdapter() = ShopOfferRecyclerViewAdapter { item ->
        logCustomEvent(
            analyticsEventsProvider.provideEvent(
                EventCategory.Engagement,
                LOAN_CATALOG_SCREEN, CLICKABLE_TEXT, item.name
            )
        )
        findNavController().safeNavigate(
            ShopFragmentDirections.actionShopFragmentToShopItemDetailsFragment(item)
        )
    }

    override val logTag = "ShopBorrowFragment"

    override val analyticsScreenName = "shop.borrow"
}
