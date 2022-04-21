/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.personalized_campaigns

import android.graphics.Color.parseColor
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logger.CompositeUxLogger.eLog
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AccountDetailsViewModel
import ph.com.globe.globeonesuperapp.databinding.ExclusivePromoDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.payment.*
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.shop.domain_models.PROMO_API_SERVICE_PROVISION
import javax.inject.Inject

@AndroidEntryPoint
class ExclusivePromoDetailsFragment :
    NoBottomNavViewBindingFragment<ExclusivePromoDetailsFragmentBinding>({
        ExclusivePromoDetailsFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val accountDetailsViewModel: AccountDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    private val shopItemDetailsViewModel by viewModels<ShopItemDetailsViewModel>()

    private val args by navArgs<ExclusivePromoDetailsFragmentArgs>()

    private lateinit var shopItemInclusionsRecyclerViewAdapter: ShopItemServiceRecyclerViewAdapter
    private lateinit var freebiesSingleSelectRecyclerViewAdapter: FreebiesSingleSelectRecyclerViewAdapter
    private lateinit var shopPromoBoosterRecyclerViewAdapter: ShopPromoBoostersRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:subscription screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shopItem = args.shopItem

        with(viewBinding) {
            if (args.offerType == OfferType.FREEBIE) {
                tvBuyingFor.text = getString(R.string.you_are_claiming_for)
            }

            with(accountDetailsViewModel) {
                accountAlias.observe(viewLifecycleOwner) {
                    tvAccountName.text = it
                }
                selectedEnrolledAccountLiveData.observe(viewLifecycleOwner) {
                    tvAccountPhoneNumber.text = it.primaryMsisdn.toDisplayUINumberFormat()
                }
            }

            with(shopItem) {
                try {
                    clPromoHeader.setBackgroundColor(parseColor(displayColor))
                } catch (e: Exception) {
                    clPromoHeader.setBackgroundColor(parseColor("#000000"))
                    eLog(Exception("displayColor has a bad format: $displayColor"))
                }

                if (shopItem.includedApps.isNotEmpty()) {
                    clIncludedApps.isVisible = true

                    shopItem.includedApps[0].let {
                        tvAppName.text = it.appName
                        GlobeGlide.with(ivAppIcon).load(it.appIcon).into(ivAppIcon)
                    }
                }

                if (loanable) {
                    clLoanDescription.visibility = View.VISIBLE
                    tvBuyingFor.text =
                        getString(R.string.you_are_borrowing_for)
                    btnSubscribe.text =
                        getString(R.string.shop_tab_borrow)
                } else if (isContent) {
                    clBoosters.visibility = View.GONE
                    vHorizontalLine1.visibility = View.GONE

                    if (!asset.isNullOrBlank()) {
                        ivItemIcon.visibility = View.VISIBLE
                        GlobeGlide.with(ivItemIcon).load(asset).into(ivItemIcon)
                    } else {
                        ivItemIcon.visibility = View.GONE
                    }
                }
                tvItemName.text = name

                val promoPrice = (price.toIntOrNull() ?: 0)
                if (promoPrice == 0) {
                    tvItemPrice.text = getString(R.string.free)
                } else {
                    tvItemPrice.text = (promoPrice - (discount?.toIntOrNull() ?: 0)).intToPesos()
                    tvItemPriceOld.apply {
                        text = promoPrice.intToPesos()
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        isVisible = !discount.isNullOrBlank()
                    }
                }

                tvValidity.apply {
                    args.availableCampaignPromosModel.benefitsSkuDays?.let { benefitsSkuDays ->
                        if (visibility == View.GONE || visibility == View.INVISIBLE)
                            visibility = View.VISIBLE

                        text = resources.getQuantityString(
                            R.plurals.valid_for,
                            benefitsSkuDays.toInt(),
                            benefitsSkuDays
                        )
                    } ?: run {
                        if (visibility == View.VISIBLE)
                            visibility = View.INVISIBLE
                    }
                }

                tvItemDescription.text = description

                shopItemInclusionsRecyclerViewAdapter = ShopItemServiceRecyclerViewAdapter()
                rvInclusionsItems.adapter = shopItemInclusionsRecyclerViewAdapter
                val inclusionsList = mutableListOf<PromoServiceItem>()
                mobileDataSize.forEachIndexed { index, _ ->
                    if (mobileDataDescription.size > index)
                        inclusionsList.add(
                            PromoServiceItem(
                                R.drawable.ic_promo_details_data_icon,
                                mobileDataDescription[index]
                            )
                        )
                }

                homeDataSize.forEachIndexed { index, _ ->
                    if (homeDataDescription.size > index)
                        inclusionsList.add(
                            PromoServiceItem(
                                R.drawable.ic_promo_details_data_icon,
                                homeDataDescription[index]
                            )
                        )
                }

                appDataSize.forEachIndexed { index, _ ->
                    if (appDataDescription.size > index)
                        inclusionsList.add(
                            PromoServiceItem(
                                R.drawable.ic_promo_details_data_icon,
                                appDataDescription[index]
                            )
                        )
                }

                smsSize.forEachIndexed { index, _ ->
                    if (smsDescription.size > index)
                        inclusionsList.add(
                            PromoServiceItem(
                                R.drawable.ic_promo_details_text_icon,
                                smsDescription[index]
                            )
                        )
                }

                callSize.forEachIndexed { index, _ ->
                    if (callDescription.size > index)
                        inclusionsList.add(
                            PromoServiceItem(
                                R.drawable.ic_promo_details_voice_icon,
                                callDescription[index]
                            )
                        )
                }

                tvInclusions.isVisible = inclusionsList.isNotEmpty()
                rvInclusionsItems.isVisible = inclusionsList.isNotEmpty()
                if (inclusionsList.isNotEmpty()) {
                    shopItemInclusionsRecyclerViewAdapter.submitList(inclusionsList)
                }

                freebie?.let {
                    clFreebies.visibility = View.VISIBLE
                    rvSingleSelectItems.itemAnimator = null
                    shopItemDetailsViewModel.hasFreebiesWithSingleSelect(it)
                    tvFreebieText.text = it.description

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
                            freebiesSingleSelectRecyclerViewAdapter.submitList(freebies)
                        })
                }

                boosters?.let { list ->
                    shopItemDetailsViewModel.clearBoosters()
                    clBoosters.visibility = View.VISIBLE

                    shopItemDetailsViewModel.filterBoosters(list)

                    shopItemDetailsViewModel.boostersLiveData.observe(
                        viewLifecycleOwner,
                        { boosters ->
                            shopPromoBoosterRecyclerViewAdapter.submitList(null)
                            shopPromoBoosterRecyclerViewAdapter.submitList(boosters)
                        })

                    shopItemDetailsViewModel.boostersPriceSumLiveData.observe(viewLifecycleOwner, {
                        it.handleEvent { boostersPriceSum ->
                            if (boostersPriceSum == 0) tvBoostersPriceSum.visibility =
                                View.GONE
                            else {
                                tvBoostersPriceSum.text = "+${boostersPriceSum.intToPesos()}"
                                tvBoostersPriceSum.visibility = View.VISIBLE
                            }
                        }
                    })

                    shopPromoBoosterRecyclerViewAdapter =
                        ShopPromoBoostersRecyclerViewAdapter { serviceId, price, selected ->
                            shopItemDetailsViewModel.toggleBooster(serviceId, price, selected)
                        }
                    rvBoosters.itemAnimator = null
                    rvBoosters.adapter = shopPromoBoosterRecyclerViewAdapter
                }

                btnSubscribe.setOnClickListener {
                    when (args.offerType) {
                        OfferType.FREEBIE -> findNavController().safeNavigate(
                            ExclusivePromoDetailsFragmentDirections.actionExclusivePromoDetailsFragmentToPersonalizedCampaignsLoadingFragment(
                                args.availableCampaignPromosModel
                            )
                        )
                        OfferType.EXCLUSIVE_PROMOS -> shopItemDetailsViewModel.subscribe(
                            shopItem = shopItem,
                            loggedIn = true,
                            checkMobileNumberBrand = false
                        )
                    }

                }

                ivBack.setOnClickListener {
                    findNavController().navigateUp()
                }

                shopItemDetailsViewModel.subscribeResult.observe(viewLifecycleOwner, {
                    it.handleEvent { subscribeResult ->
                        when (subscribeResult) {
                            is ShopItemDetailsViewModel.SubscribeResult.SubscribePromoSuccess -> {
                                val checkout = checkoutToPaymentParams(
                                    paymentType = BUY_PROMO,
                                    transactionType = NON_BILL,
                                    mobileNumber = args.availableCampaignPromosModel.mobileNumber,
                                    promoChargeId = args.availableCampaignPromosModel.maId,
                                    chargePromoParam = args.availableCampaignPromosModel.customerParameter1,
                                    apiProvisioningKeyword = args.availableCampaignPromosModel.channel,
                                    isGroupDataPromo = sections.any { it?.name ?: "" == "Group Data" },
                                    shareKeyword = shareKeyword,
                                    shareFee = shareFee?.toDoubleOrNull() ?: 0.0,
                                    paymentName = name,
                                    validity = validity,
                                    price = price.toDouble(),
                                    amount = price.toDouble(),
                                    discount = discount?.toDouble() ?: 0.0,
                                    shareable = shareable,
                                    selectedBoosters = subscribeResult.boostersApplied,
                                    skelligWallet = skelligWallet,
                                    skelligCategory = skelligCategory,
                                    provisionByServiceId = apiSubscribe == PROMO_API_SERVICE_PROVISION,
                                    isExclusivePromo = true,
                                    availMode = args.availableCampaignPromosModel.availMode
                                )
                                findNavController().safeNavigate(
                                    ExclusivePromoDetailsFragmentDirections.actionExclusivePromoDetailsFragmentToPaymentSubgraph(
                                        checkout
                                    )
                                )
                            }
                        }
                    }
                })
            }
        }
    }

    override val logTag: String = "ExclusivePromoDetailsFragment"

    override val analyticsScreenName = "shop.item_details"
}
