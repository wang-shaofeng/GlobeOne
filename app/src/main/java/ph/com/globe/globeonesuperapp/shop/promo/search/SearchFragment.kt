/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.SearchFragmentBinding
import ph.com.globe.globeonesuperapp.databinding.SearchHistoryItemLayoutBinding
import ph.com.globe.globeonesuperapp.rewards.rewards_adapters.RewardsAdapter
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.BORROW_ID
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.PROMO_ID
import ph.com.globe.globeonesuperapp.shop.promo.ShopOfferRecyclerViewAdapter
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.closeKeyboard
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.rewards.RewardsCatalogItem
import ph.com.globe.model.shop.domain_models.ShopItem
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : NoBottomNavViewBindingFragment<SearchFragmentBinding>(
    bindViewBy = {
        SearchFragmentBinding.inflate(it)
    }
), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private lateinit var contactsViewModel: ContactsViewModel
    private val searchViewModel: SearchViewModel by viewModels()

    private lateinit var shopOfferRecyclerViewAdapter: ShopOfferRecyclerViewAdapter
    private lateinit var unavailableOffersRecyclerViewAdapter: ShopOfferRecyclerViewAdapter

    private lateinit var availableRewardsAdapter: RewardsAdapter
    private lateinit var unavailableRewardsAdapter: RewardsAdapter

    private lateinit var searchRecommendationRecyclerViewAdapter: SearchRecommendationRecyclerViewAdapter

    private val shopSearchFragmentArgs by navArgs<SearchFragmentArgs>()

    private val navgraphId: Int
        get() = when (shopSearchFragmentArgs.type) {
            SEARCH_TYPE_SHOP -> R.id.shop_subgraph
            SEARCH_TYPE_REWARDS -> R.id.rewards_subgraph
            else -> R.id.shop_subgraph
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactsViewModel =
            navGraphViewModels<ContactsViewModel>(navgraphId) { defaultViewModelProviderFactory }.value

        with(viewBinding) {
            if (shopSearchFragmentArgs.type == SEARCH_TYPE_SHOP) {
                if (shopSearchFragmentArgs.tabSelected == BORROW_ID) {
                    searchViewModel.initForLoanable(true)
                    tvBuyingForTitle.text = getString(R.string.you_are_borrowing_for)
                    tvSearchFragmentTitle.text =
                        getString(R.string.shop_tab_borrow).toUpperCase(Locale.ROOT)
                } else {
                    searchViewModel.initForLoanable(false)
                }

                tvEmptyStateTitle.text = getString(R.string.no_promos_title)
                tvEmptyStateDescription.text = getString(R.string.no_promos_description)

            } else if (shopSearchFragmentArgs.type == SEARCH_TYPE_REWARDS) {
                tvBuyingForTitle.isVisible = false
                tvSearchFragmentTitle.text =
                    getString(R.string.rewards_catalog).toUpperCase(Locale.ROOT)
                searchViewModel.initForRewards()

                tvEmptyStateTitle.text = getString(R.string.empty_state_no_rewards_title)
                tvEmptyStateDescription.text =
                    getString(R.string.empty_state_no_rewards_description)
            }

            with(searchViewModel) {
                if (shopSearchFragmentArgs.type == SEARCH_TYPE_SHOP) {
                    shopOfferRecyclerViewAdapter = createAdapter()
                    rvSearchedPromos.adapter = shopOfferRecyclerViewAdapter

                    unavailableOffersRecyclerViewAdapter = createAdapter()
                    rvUnavailableOffers.adapter = unavailableOffersRecyclerViewAdapter

                } else if (shopSearchFragmentArgs.type == SEARCH_TYPE_REWARDS) {
                    availableRewardsAdapter = RewardsAdapter {
                        // TODO: SearchFragmentDirection doesn't contain this action.
                        findNavController().safeNavigate(
                            R.id.action_searchFragment2_to_rewardDetailsFragment,
                            bundleOf("rewardItem" to it)
                        )
                    }
                    rvSearchedPromos.adapter = availableRewardsAdapter

                    unavailableRewardsAdapter = RewardsAdapter {
                        // TODO: SearchFragmentDirection doesn't contain this action.
                        findNavController().safeNavigate(
                            R.id.action_searchFragment2_to_rewardDetailsFragment,
                            bundleOf("rewardItem" to it)
                        )
                    }
                    rvUnavailableOffers.adapter = unavailableRewardsAdapter
                }

                searchRecommendationRecyclerViewAdapter =
                    SearchRecommendationRecyclerViewAdapter { recommendation ->
                        searchFromHistoryAndRecommendation(recommendation)
                    }
                rvSearchRecommendation.adapter = searchRecommendationRecyclerViewAdapter

                ivBack.setOnClickListener {
                    requireActivity().onBackPressed()
                }

                etPromosSearch.doAfterTextChanged {
                    if (it?.length ?: 0 > 2) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            rvSearchRecommendation.visibility = View.VISIBLE
                            recommendSearch(it.toString())
                        }, SEARCH_DELAY)
                    } else rvSearchRecommendation.visibility = View.GONE
                }

                tvClearSearches.setOnClickListener {
                    clearSearchHistory()
                    clRecentSearch.visibility = View.GONE
                }

                etPromosSearch.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        clSearchHistoryAndRecommendation.visibility = View.VISIBLE
                        clNoPromos.visibility = View.GONE
                        svSearchResult.visibility = View.GONE
                    }
                }

                etPromosSearch.setOnEditorActionListener { v, _, _ ->
                    if (v.text.isNotEmpty()) {
                        logCustomEvent(
                            analyticsEventsProvider.provideEvent(
                                EventCategory.Core,
                                getAnalyticsUiSection(),
                                labelKeyword = KEYWORD_TYPE,
                                searchKeyword = v.text.toString()
                            )
                        )
                        searchByName(v.text.toString())
                    } else {
                        tvEmptyStateTitle.visibility = View.GONE
                        tvEmptyStateDescription.visibility = View.GONE
                        clNoPromos.visibility = View.VISIBLE
                    }
                    clSearchHistoryAndRecommendation.visibility = View.GONE
                    closeKeyboard(v, requireContext())
                    true
                }

                searchHistory.observe(viewLifecycleOwner, { searches ->
                    cgRecentSearches.removeAllViews()
                    if (searches.isEmpty()) clRecentSearch.visibility = View.GONE
                    else {
                        clRecentSearch.visibility = View.VISIBLE
                        for (search in searches) {
                            val chip = SearchHistoryItemLayoutBinding.inflate(layoutInflater)
                            chip.tvText.text = search
                            chip.clSearchHistoryItemLayout.setOnClickListener {
                                searchFromHistoryAndRecommendation(search)
                            }
                            cgRecentSearches.addView(chip.root)
                        }
                    }
                })

                searchedOffers.observe(viewLifecycleOwner, { list ->
                    clNoPromos.isVisible = list.isEmpty()

                    tvEmptyStateTitle.isVisible =
                        list.isEmpty() && etPromosSearch.text.toString().isNotEmpty()
                    tvEmptyStateDescription.isVisible =
                        list.isEmpty() && etPromosSearch.text.toString().isNotEmpty()

                    svSearchResult.isVisible = list.isNotEmpty()

                    if (list.isNotEmpty()) {
                        tvNumberResult.text = SpannableString(
                            resources.getQuantityString(
                                R.plurals.displaying_x_results,
                                list.size,
                                list.size
                            )
                        ).apply {
                            setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.neutral_A_0
                                    )
                                ),
                                resources.getString(R.string.displaying).length,
                                length - resources.getString(R.string.rewards).length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }

                        when (shopSearchFragmentArgs.type) {
                            SEARCH_TYPE_SHOP -> shopOfferRecyclerViewAdapter.submitList(list as MutableList<ShopItem>?)
                            SEARCH_TYPE_REWARDS -> availableRewardsAdapter.submitList(list as MutableList<RewardsCatalogItem>)
                            else -> Unit
                        }

                        unavailableOffersList.observe(viewLifecycleOwner, { unavailableList ->
                            when (shopSearchFragmentArgs.type) {
                                SEARCH_TYPE_SHOP -> unavailableOffersRecyclerViewAdapter.submitList(
                                    unavailableList as MutableList<ShopItem>?
                                )
                                SEARCH_TYPE_REWARDS -> unavailableRewardsAdapter.submitList(
                                    unavailableList as MutableList<RewardsCatalogItem>?
                                )
                                else -> Unit
                            }
                        })
                    }
                })

                searchRecommendation.observe(viewLifecycleOwner, { recommendations ->
                    searchRecommendationRecyclerViewAdapter.submitList(recommendations)
                })

                contactsViewModel.selectedNumber.observe(viewLifecycleOwner, {
                    if (!it.isNullOrEmpty()) {
                        tvAccountPhoneNumber.visibility = View.VISIBLE
                        tvBuyingForTitle.visibility = View.VISIBLE
                        tvAccountPhoneNumber.text = it
                        val accountName = contactsViewModel.getNumberOwnerOrPlaceholder(
                            it,
                            getString(R.string.mobile_number)
                        )
                        if (accountName != getString(R.string.mobile_number)) {
                            tvAccountName.visibility = View.VISIBLE
                            tvAccountName.text = accountName
                        }
                    }
                })
            }
        }
    }

    private fun createAdapter() = ShopOfferRecyclerViewAdapter { item ->
        findNavController().safeNavigate(
            SearchFragmentDirections.actionShopSearchFragmentToShopItemDetailsFragment(
                item
            )
        )
    }

    private fun searchFromHistoryAndRecommendation(search: String) =
        with(viewBinding) {
            clNoPromos.visibility = View.VISIBLE
            clSearchHistoryAndRecommendation.visibility = View.GONE
            searchViewModel.searchByName(search)
            closeKeyboard(requireView(), requireContext())
        }

    private fun getAnalyticsUiSection(): String {
        return when (shopSearchFragmentArgs.type) {
            SEARCH_TYPE_SHOP -> {
                when (shopSearchFragmentArgs.tabSelected) {
                    BORROW_ID -> BORROW_SCREEN
                    PROMO_ID -> PROMOS_SCREEN
                    else -> ""
                }
            }
            SEARCH_TYPE_REWARDS -> REWARDS_SCREEN
            else -> ""
        }
    }

    override val logTag = "ShopSearchFragment"

    override val analyticsScreenName = "shop.search"
}

private const val SEARCH_DELAY = 500L
const val SEARCH_TYPE_SHOP = "shop"
const val SEARCH_TYPE_REWARDS = "rewards"
