/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopPromoFragmentBinding
import ph.com.globe.globeonesuperapp.shop.ShopFragmentDirections
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.PROMO_ID
import ph.com.globe.globeonesuperapp.shop.ShopViewModel
import ph.com.globe.globeonesuperapp.shop.promo.filter.Section
import ph.com.globe.globeonesuperapp.shop.promo.filter.ShopOffersSortFilterViewModel
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.RAFFLE_DETAILS_URL
import ph.com.globe.globeonesuperapp.shop.select_other_account.LOGGED_IN_STATUS_KEY
import ph.com.globe.globeonesuperapp.shop.select_other_account.TITLE_KEY
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.AppConstants.ALL_PROMOS_TAB
import ph.com.globe.globeonesuperapp.utils.AppConstants.GLOBE_PREPAID_TAB
import ph.com.globe.globeonesuperapp.utils.AppConstants.HOME_WIFI_PREPAID_TAB
import ph.com.globe.globeonesuperapp.utils.AppConstants.TM_TAB
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment
import ph.com.globe.model.shop.CatalogStatus
import javax.inject.Inject

@AndroidEntryPoint
class ShopPromoFragment : NestedViewBindingFragment<ShopPromoFragmentBinding>(bindViewBy = {
    ShopPromoFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val shopViewModel: ShopViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val shopOffersSortFilterViewModel: ShopOffersSortFilterViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private val contactsViewModel: ContactsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private lateinit var shopOfferRecyclerViewAdapterOne: ShopOfferRecyclerViewAdapter
    private lateinit var shopOfferRecyclerViewAdapterTwo: ShopOfferRecyclerViewAdapter
    private lateinit var shopOfferRecyclerViewAdapterThree: ShopOfferRecyclerViewAdapter
    private lateinit var unavailableOffersRecyclerViewAdapter: ShopOfferRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:promos screen"))
    }

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
                rvUnavailablePromos.adapter = unavailableOffersRecyclerViewAdapter

                etMobileNumber.setOnEditorActionListener { v, _, _ ->
                    if (v.text.isNotEmpty())
                        contactsViewModel.selectAndValidateNumber(v.text.toString())
                    closeKeyboard(v, requireContext())
                    true
                }

                clGlobePrepaid.setOnClickListener {
                    findNavController().safeNavigate(
                        ShopFragmentDirections.actionShopFragmentToShopPromoInnerFragment(
                            GLOBE_PREPAID_TAB
                        )
                    )
                }

                clTm.setOnClickListener {
                    findNavController().safeNavigate(
                        ShopFragmentDirections.actionShopFragmentToShopPromoInnerFragment(
                            TM_TAB
                        )
                    )
                }

                clHomePrepaidWifi.setOnClickListener {
                    findNavController().safeNavigate(
                        ShopFragmentDirections.actionShopFragmentToShopPromoInnerFragment(
                            HOME_WIFI_PREPAID_TAB
                        )
                    )
                }

                btnAllPromos.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            PROMOS_SCREEN, BUTTON, VIEW_ALL_PROMOS
                        )
                    )
                    findNavController().safeNavigate(
                        ShopFragmentDirections.actionShopFragmentToShopPromoInnerFragment(
                            ALL_PROMOS_TAB
                        )
                    )
                }

                shopViewModel.raffleSetABInProgress.observe(viewLifecycleOwner, {
                    ivRaffleBanner.isVisible = it
                })

                val offersObserver = Observer<List<Section>> { sections ->
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

                    val isEmptyState = sections?.get(0)?.name.isNullOrEmpty()
                            && sections?.get(1)?.name.isNullOrEmpty()
                            && sections?.get(2)?.name.isNullOrEmpty()

                    groupOffers.isVisible = !isEmptyState
                    clNoOffers.isVisible = isEmptyState
                }

                shopViewModel.catalogStatus.observe(viewLifecycleOwner) { status ->

                    // Setup offers LiveData observer
                    with(shopOffersSortFilterViewModel.promosOffersLists) {
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
                        if (tabId == PROMO_ID)
                            setRefreshEnabled(svShopPromo.scrollY == 0)
                    }

                    svShopPromo.setOnScrollChangeListener(
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
                                    TITLE_KEY to getString(R.string.promos),
                                    LOGGED_IN_STATUS_KEY to loggedIn
                                )
                            )
                        }
                        etMobileNumber.isFocusable = false

                        // Display GoCreate banner for logged in user
                        incGoCreateBanner.root.apply {
                            visibility = View.VISIBLE
                            setOnClickListener {
                                findNavController().safeNavigate(
                                    ShopFragmentDirections.actionShopFragmentToGoCreateSubgraph(
                                        entryPoint = getString(R.string.wayfinder_promos),
                                        contactsViewModel.selectedNumber.value
                                    )
                                )
                            }
                        }
                    } else {
                        tilMobileNumber.setEndIconDrawable(R.drawable.ic_user)
                        tilMobileNumber.setEndIconOnClickListener {
                            findNavController().safeNavigate(ShopFragmentDirections.actionShopFragmentToContactsFragment())
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

                ivRaffleBanner.setOnClickListener {
                    generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(RAFFLE_DETAILS_URL))
                        startActivity(intent)
                    }
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
            ShopFragmentDirections.actionShopFragmentToShopItemDetailsFragment(item)
        )
    }

    override val logTag = "ShopPromoFragment"

    override val analyticsScreenName = "shop.promos"
}
