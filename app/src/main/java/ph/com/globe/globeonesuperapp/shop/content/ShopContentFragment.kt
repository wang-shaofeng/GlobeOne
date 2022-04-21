/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.content

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.CLICKABLE_TEXT
import ph.com.globe.analytics.events.CONTENT_SCREEN
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopContentFragmentBinding
import ph.com.globe.globeonesuperapp.shop.ShopFragmentDirections
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.CONTENT_ID
import ph.com.globe.globeonesuperapp.shop.ShopViewModel
import ph.com.globe.globeonesuperapp.shop.content.filter.ShopContentFilterViewModel
import ph.com.globe.globeonesuperapp.shop.promo.filter.SortType
import ph.com.globe.globeonesuperapp.shop.select_other_account.LOGGED_IN_STATUS_KEY
import ph.com.globe.globeonesuperapp.shop.select_other_account.TITLE_KEY
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment
import ph.com.globe.model.shop.CatalogStatus
import ph.com.globe.model.shop.domain_models.ShopItem
import javax.inject.Inject

@AndroidEntryPoint
class ShopContentFragment : NestedViewBindingFragment<ShopContentFragmentBinding>(bindViewBy = {
    ShopContentFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val shopViewModel: ShopViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val contactsViewModel: ContactsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val filterViewModel: ShopContentFilterViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private val contentPromoAdapter = ShopContentAdapter { item ->
        logCustomEvent(
            analyticsEventsProvider.provideEvent(
                EventCategory.Engagement,
                CONTENT_SCREEN, CLICKABLE_TEXT, item.name
            )
        )
        findNavController().safeNavigate(
            ShopFragmentDirections.actionShopFragmentToShopItemDetailsFragment(item)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        with(viewBinding) {

            shopViewModel.loggedIn.observe(viewLifecycleOwner, { loggedIn ->
                if (loggedIn) {
                    tilMobileNumber.setEndIconDrawable(R.drawable.ic_edit)
                    tilMobileNumber.setEndIconOnClickListener {
                        findNavController().safeNavigate(
                            R.id.action_shopFragment_to_selectOtherAccountFragment,
                            bundleOf(
                                TITLE_KEY to getString(R.string.shop_tab_content),
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

            val offersObserver = Observer<List<ShopItem>> { contentPromos ->
                contentPromoAdapter.submitList(contentPromos)

                // Handle empty state
                llNoPromosFound.isVisible = contentPromos.isEmpty()
            }

            shopViewModel.catalogStatus.observe(viewLifecycleOwner) { status ->

                // Setup offers LiveData observer
                with(filterViewModel.filteredContentPromoOffersList) {
                    when (status) {
                        CatalogStatus.Success -> observe(viewLifecycleOwner, offersObserver)
                        else -> {
                            contentPromoAdapter.submitList(emptyList())
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
                    if (tabId == CONTENT_ID)
                        setRefreshEnabled(svShopContent.scrollY == 0)
                }

                svShopContent.setOnScrollChangeListener(
                    NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                        setRefreshEnabled(scrollY == 0)
                    })
            }

            // Reload
            incError.btnReload.setOnClickListener {
                shopViewModel.fetchOffers(forceRefresh = true)
            }

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
                    filterViewModel.setNumberBrand(validation.brand)
                })

            etMobileNumber.addTextChangedListener { editable ->
                requireContext().hideError(tilMobileNumber, etMobileNumber)
                editable.formatCountryCodeIfExists()

                tilMobileNumber.hint = contactsViewModel.getNumberOwnerOrPlaceholder(editable.getStringOrNull(), getString(R.string.mobile_number))
            }

            etMobileNumber.setOnEditorActionListener { v, _, _ ->
                if (v.text.isNotEmpty()) contactsViewModel.selectAndValidateNumber(v.text.toString())
                closeKeyboard(v, requireContext())
                true
            }

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
                                CONTENT_SCREEN,
                                CLICKABLE_TEXT,
                                SortType.toAnalyticsTextValue(position)
                            )
                        )
                        filterViewModel.sortContentPromos(SortType.toSortType(position))
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }

            ivFilter.setOnClickListener {
                findNavController().safeNavigate(ShopFragmentDirections.actionShopFragmentToShopContentFilterFragment())
            }

            rvContentPromos.adapter = contentPromoAdapter

            filterViewModel.numberOfFilters.observe(viewLifecycleOwner, { numberOfFilters ->
                if (numberOfFilters > 0) {
                    ivFilter.setImageResource(R.drawable.ic_filter_selected)
                    tvFilterBadge.visibility = View.VISIBLE
                    tvFilterBadge.text = numberOfFilters.toString()
                } else {
                    ivFilter.setImageResource(R.drawable.ic_filter_button)
                    tvFilterBadge.visibility = View.GONE
                }
            })
        }
    }

    override val logTag = "ShopContentFragment"

    override val analyticsScreenName = "shop.content"
}
