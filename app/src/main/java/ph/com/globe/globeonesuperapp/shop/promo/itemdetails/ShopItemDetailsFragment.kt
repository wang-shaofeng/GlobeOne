/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.itemdetails

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.analytics.logger.eLog
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.*
import ph.com.globe.globeonesuperapp.databinding.ShopItemDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.shop.ShopViewModel
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel.GetLoanInfoResult.*
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel.RaffleBannerResult.ShowRaffleBanner
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel.RaffleBannerResult.ShowRaffleBanner.BannerType.ELIGIBLE
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel.SubscribeResult.*
import ph.com.globe.globeonesuperapp.shop.select_other_account.LOGGED_IN_STATUS_KEY
import ph.com.globe.globeonesuperapp.shop.select_other_account.TITLE_KEY
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.payment.*
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.shop.domain_models.CONTENT_PROMO_METHOD_DCB
import ph.com.globe.model.shop.domain_models.PROMO_API_SERVICE_PROVISION
import ph.com.globe.model.util.BORROW
import ph.com.globe.model.util.FREEBIE_VOUCHER
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ShopItemDetailsFragment @Inject constructor() :
    NoBottomNavViewBindingFragment<ShopItemDetailsFragmentBinding>(
        bindViewBy = {
            ShopItemDetailsFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val shopViewModel: ShopViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val contactsViewModel: ContactsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val shopItemDetailsViewModel: ShopItemDetailsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val shopItemDetailsFragmentArgs by navArgs<ShopItemDetailsFragmentArgs>()

    private lateinit var shopItemInclusionsRecyclerViewAdapter: ShopItemServiceRecyclerViewAdapter
    private lateinit var shopPromoBoosterRecyclerViewAdapter: ShopPromoBoostersRecyclerViewAdapter

    private val userLoggedIn
        get() = shopViewModel.isLoggedIn()

    private var shouldProceed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:subscription screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        val shopItem = shopItemDetailsFragmentArgs.shopItem

        shopItemDetailsViewModel.showHideRaffleBanner(shopItem.sections.any { it.name == "Raffle" })

        with(viewBinding) {
            if (userLoggedIn) {
                tilMobileNumber.setEndIconDrawable(R.drawable.ic_edit)
                tilMobileNumber.setEndIconOnClickListener {
                    findNavController().safeNavigate(ShopItemDetailsFragmentDirections.actionShopItemDetailsFragmentToSelectOtherAccountFragment())
                }
                etMobileNumber.isFocusable = false
            } else {
                tilMobileNumber.setEndIconDrawable(R.drawable.ic_user)
                tilMobileNumber.setEndIconOnClickListener {
                    findNavController().safeNavigate(ShopItemDetailsFragmentDirections.actionShopItemDetailsFragmentToContactsFragment())
                }
                etMobileNumber.isFocusableInTouchMode = true
            }
            with(shopItem) {
                try {
                    clPromoHeader.setBackgroundColor(Color.parseColor(displayColor))
                } catch (e: Exception) {
                    clPromoHeader.setBackgroundColor(Color.parseColor("#000000"))
                    eLog(Exception("displayColor has a bad format: $displayColor"))
                }

                if (loanable) {
                    wfPromos.setLabel(getString(R.string.shop_tab_borrow).uppercase())
                    tvBuyingFor.text = getString(R.string.you_are_borrowing_for)
                    btnSubscribe.text = getString(R.string.shop_tab_borrow)
                    tvServiceFee.visibility = View.VISIBLE
                    tvServiceFee.text = getString(R.string.loan_service_fee, fee)
                } else if (isContent) {
                    wfPromos.setLabel(getString(R.string.shop_tab_content).uppercase())
                    clBoosters.visibility = View.GONE
                    vHorizontalLine1.visibility = View.GONE

                    if (!asset.isNullOrBlank()) {
                        ivItemIcon.visibility = View.VISIBLE
                        GlobeGlide.with(ivItemIcon).load(asset).into(ivItemIcon)
                    } else {
                        ivItemIcon.visibility = View.GONE
                    }

                    if (method == CONTENT_PROMO_METHOD_DCB) {
                        tvBuyingFor.visibility = View.GONE
                        tilMobileNumber.visibility = View.GONE
                    }
                }

                tvItemName.text = name

                val promoPrice = (price.toIntOrNull() ?: 0)
                tvItemPrice.text = (promoPrice - (discount?.toIntOrNull() ?: 0)).intToPesos()
                tvItemPriceOld.apply {
                    text = promoPrice.intToPesos()
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    isVisible = !discount.isNullOrBlank()
                }

                tvValidity.text = resources.setValidityTextEx(validity)

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
                    shopItemDetailsViewModel.hasFreebiesWithSingleSelect(it)
                    tvFreebieText.text = it.description
                    //check if freebie services contain freebie voucher
                    val showVoucher = freebie?.items?.any { fb -> fb.type == FREEBIE_VOUCHER }
                    vpFreebies.adapter =
                        FreebiesPagerAdapter(this@ShopItemDetailsFragment, showVoucher ?: false)

                    TabLayoutMediator(tlFreebies, vpFreebies) { tab, position ->
                        tab.text = when (position) {

                            FreebiesPagerAdapter.TAB_FREEBIE_ENTERTAINMENT -> getString(R.string.entertainment)

                            FreebiesPagerAdapter.TAB_FREEBIE_HEALTH_AND_SAVING -> getString(R.string.health_and_saving)

                            else -> throw IllegalStateException()
                        }
                    }.attach()
                }

                boosters?.takeIf { it.isNotEmpty() }?.let { list ->
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
                            if (boostersPriceSum == 0) tvBoostersPriceSum.visibility = View.GONE
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

                shopItemDetailsViewModel.enableButton.observe(viewLifecycleOwner, {
                    it.handleEvent { enable ->
                        btnSubscribe.isEnabled = enable
                    }
                })

                etMobileNumber.setOnEditorActionListener { v, _, _ ->
                    if (v.text.isNotEmpty())
                        contactsViewModel.selectAndValidateNumber(v.text.toString())
                    closeKeyboard(v, requireContext())
                    true
                }

                etMobileNumber.addTextChangedListener { editable ->
                    editable.formatCountryCodeIfExists()
                    tilMobileNumber.hint = contactsViewModel.getNumberOwnerOrPlaceholder(editable.getStringOrNull(), getString(R.string.mobile_number))
                    shopItemDetailsViewModel.mobileNumberEditTextChange(editable.toString())
                    requireContext().hideError(tilMobileNumber, etMobileNumber)
                }

                btnSubscribe.setOnClickListener {
                    if (isContent && method == CONTENT_PROMO_METHOD_DCB) {
                        generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                            val intent =
                                Intent(Intent.ACTION_VIEW, Uri.parse(partnerRedirectionLink))
                            startActivity(intent)
                        }
                    } else if (
                        contactsViewModel.lastCheckedNumberValidation.value?.isValid == true &&
                        contactsViewModel.lastCheckedNumberValidation.value?.number == etMobileNumber.text.toString()
                    ) {
                        val analyticsUiSection =
                            if (shopItem.isContent) SUBSCRIBE_SCREEN else CONVERSION_SCREEN
                        val analyticsClickText = if (shopItem.loanable) BORROW else SUBSCRIBE
                        logCustomEvent(
                            analyticsEventsProvider.provideEvent(
                                EventCategory.Conversion,
                                analyticsUiSection, BUTTON, analyticsClickText,
                                productName = shopItem.takeIf { !it.loanable }?.name
                            )
                        )
                        shopItemDetailsViewModel.subscribe(
                            shopItem = shopItem,
                            loggedIn = userLoggedIn,
                            isEnrolledAccount = contactsViewModel.isEnrolledAccountNumber(
                                etMobileNumber.text.toString()
                            )
                        )
                    } else {
                        contactsViewModel.selectAndValidateNumber(etMobileNumber.text.toString())
                        shouldProceed = true
                    }
                }

                clRaffleBanner.setOnClickListener {
                    generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(RAFFLE_DETAILS_URL))
                        startActivity(intent)
                    }
                }

                wfPromos.onBack {
                    shopItemDetailsViewModel.resetSelection()
                    findNavController().navigateUp()
                }

                if(shopItemDetailsFragmentArgs.isFromAccountDetail){
                    wfPromos.setLabel(getString(R.string.account_details))
                    wfPromos.setAllCaps(true)
                }

                requireActivity().onBackPressedDispatcher.addCallback(
                    viewLifecycleOwner,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            shopItemDetailsViewModel.resetSelection()
                            findNavController().navigateUp()
                        }
                    })

                shopItemDetailsViewModel.showHideRaffleBanner.observe(viewLifecycleOwner, {
                    it.handleEvent { result ->
                        if (result is ShowRaffleBanner) {
                            clRaffleBanner.visibility = View.VISIBLE
                            tvRaffleBannerTitle.text =
                                if (result.bannerType == ELIGIBLE) getString(R.string.raffle_promo_eligable_for_raffle) else getString(
                                    R.string.raffle_promo_buy_hpw_device
                                )
                            clRaffleBanner.setOnClickListener {
                                generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                                    val intent =
                                        Intent(Intent.ACTION_VIEW, Uri.parse(RAFFLE_DETAILS_URL))
                                    startActivity(intent)
                                }
                            }
                        } else {
                            clRaffleBanner.visibility = View.GONE
                        }
                    }
                })

                shopViewModel.loggedIn.observe(viewLifecycleOwner, { loggedIn ->
                    if (loggedIn) {
                        tilMobileNumber.setEndIconDrawable(R.drawable.ic_edit)
                        tilMobileNumber.setEndIconOnClickListener {
                            findNavController().safeNavigate(
                                R.id.action_shopItemDetailsFragment_to_selectOtherAccountFragment,
                                bundleOf(
                                    TITLE_KEY to if (loanable) getString(R.string.shop_tab_borrow) else getString(
                                        R.string.promos
                                    ),
                                    LOGGED_IN_STATUS_KEY to loggedIn
                                )
                            )
                        }
                        etMobileNumber.isFocusable = false
                    } else {
                        tilMobileNumber.setEndIconDrawable(R.drawable.ic_user)
                        tilMobileNumber.setEndIconOnClickListener {
                            findNavController().safeNavigate(ShopItemDetailsFragmentDirections.actionShopItemDetailsFragmentToContactsFragment())
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
                        if (isContent && method == CONTENT_PROMO_METHOD_DCB) {
                            btnSubscribe.isEnabled = true
                            return@observe
                        }

                        btnSubscribe.isEnabled = false
                        reflectValidationToErrorDisplaying(
                            validation,
                            etMobileNumber,
                            tilMobileNumber
                        )
                        if (validation.brand != null)
                            shopItemDetailsViewModel.checkBrand(validation, shopItem)
                        if (validation.isValid && shouldProceed) {
                            shopItemDetailsViewModel.subscribe(
                                shopItem = shopItem,
                                loggedIn = userLoggedIn,
                                isEnrolledAccount = contactsViewModel.isEnrolledAccountNumber(
                                    etMobileNumber.text.toString()
                                )
                            )
                            shouldProceed = false
                        }
                    })

                shopItemDetailsViewModel.subscribeResult.observe(viewLifecycleOwner, {
                    it.handleEvent { subscribeResult ->
                        when (subscribeResult) {
                            is SubscribePromoSuccess -> {
                                val checkout = checkoutToPaymentParams(
                                    paymentType = BUY_PROMO,
                                    transactionType = NON_BILL,
                                    amount = (price.toDoubleOrNull()
                                        ?: 0.0) - (discount?.toDoubleOrNull()
                                        ?: 0.0),
                                    mobileNumber = etMobileNumber.text.toString(),
                                    promoChargeId = chargePromoId,
                                    promoNonChargeId = if (subscribeResult.nonChargeId.isBlank()) nonChargePromoId else subscribeResult.nonChargeId,
                                    chargePromoParam = if (subscribeResult.chargeParam.isBlank()) chargeServiceParam else subscribeResult.chargeParam,
                                    nonChargePromoParam = if (subscribeResult.nonChargeParam.isBlank()) nonChargeServiceParam else subscribeResult.nonChargeParam,
                                    isFreebieVoucher = subscribeResult.freebieType == FREEBIE_VOUCHER,
                                    freebieName = subscribeResult.freebieName,
                                    apiProvisioningKeyword = if (subscribeResult.apiProvisioningKeyword.isBlank()) apiProvisioningKeyword else subscribeResult.apiProvisioningKeyword,
                                    isGroupDataPromo = sections.any { it.name == "Group Data" },
                                    shareKeyword = shareKeyword,
                                    shareFee = shareFee?.toDoubleOrNull() ?: 0.0,
                                    paymentName = name,
                                    validity = validity,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    discount = discount?.toDoubleOrNull() ?: 0.0,
                                    brandType = contactsViewModel.lastCheckedNumberValidation.value?.brandType,
                                    brand = contactsViewModel.lastCheckedNumberValidation.value?.brand,
                                    isEnrolledAccount = contactsViewModel
                                        .isEnrolledAccountNumber(etMobileNumber.text.toString()),
                                    shareable = shareable,
                                    selectedBoosters = subscribeResult.boostersApplied,
                                    skelligWallet = skelligWallet,
                                    skelligCategory = skelligCategory,
                                    provisionByServiceId = apiSubscribe == PROMO_API_SERVICE_PROVISION,
                                    displayColor = displayColor
                                )
                                shopItemDetailsViewModel.resetSelection()
                                findNavController().safeNavigate(
                                    ShopItemDetailsFragmentDirections.actionShopItemDetailsFragmentToNavigationPayment(
                                        checkout
                                    )
                                )
                            }

                            is SubscribeContentSuccess -> {
                                val paymentType = if (isVoucher) BUY_VOUCHER else BUY_CONTENT
                                val mobileNumber = etMobileNumber.text.toString()
                                val amount = if (!discount.isNullOrBlank()) {
                                    val promoDiscount = discount?.toIntOrNull() ?: 0
                                    ((price.toIntOrNull() ?: 0) - promoDiscount)
                                } else {
                                    price.toIntOrNull() ?: 0
                                }
                                val checkout = checkoutToPaymentParams(
                                    paymentType = paymentType,
                                    mobileNumber = mobileNumber,
                                    promoChargeId = chargePromoId,
                                    chargePromoParam = chargeServiceParam,
                                    apiProvisioningKeyword = apiProvisioningKeyword,
                                    amount = amount.toDouble(),
                                    paymentName = name,
                                    validity = validity,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    discount = discount?.toDoubleOrNull() ?: 0.0,
                                    skelligWallet = skelligWallet,
                                    skelligCategory = skelligCategory,
                                    provisionByServiceId = apiSubscribe == PROMO_API_SERVICE_PROVISION,
                                    isEnrolledAccount = contactsViewModel
                                        .isEnrolledAccountNumber(mobileNumber),
                                    isVoucher = isVoucher,
                                    partnerName = partnerName,
                                    partnerRedirectionLink = partnerRedirectionLink,
                                    brandType = contactsViewModel.lastCheckedNumberValidation.value?.brandType,
                                    brand = contactsViewModel.lastCheckedNumberValidation.value?.brand,
                                    denomCategory = denomCategory,
                                    productDescription = getString(
                                        R.string.content_product_description,
                                        name,
                                        validity
                                    ),
                                    displayColor = displayColor,
                                    monitoredInApp = monitoredInApp
                                )
                                shopItemDetailsViewModel.resetSelection()
                                findNavController().safeNavigate(
                                    ShopItemDetailsFragmentDirections.actionShopItemDetailsFragmentToNavigationPayment(
                                        checkout
                                    )
                                )
                            }

                            is SubscribePossible -> {
                                btnSubscribe.isEnabled = true
                            }

                            else -> {
                                requireContext().showError(
                                    tilMobileNumber,
                                    etMobileNumber,
                                    resources.getString(R.string.cant_subscribe_error)
                                )
                                btnSubscribe.isEnabled = false
                            }
                        }
                    }
                })

                shopItemDetailsViewModel.phoneNumberLoanValidityResult.observe(viewLifecycleOwner, {
                    it.handleEvent { result ->
                        when (result) {
                            is OtpSentSuccessfully -> {
                                findNavController().safeNavigate(
                                    ShopItemDetailsFragmentDirections.actionShopItemDetailsFragmentToShopBorrowSubmitOtpFragment(
                                        result.phoneNumber,
                                        result.brandType,
                                        result.referenceId
                                    )
                                )
                            }

                            is LoanLoggedIn -> {
                                findNavController().safeNavigate(
                                    ShopItemDetailsFragmentDirections.actionShopItemDetailsFragmentToShopBorrowProcessingFragment(
                                        null
                                    )
                                )
                            }

                            is NoBrand -> {
                                eLog(Exception("Account doesn't have brand"))
                                shopItemDetailsViewModel.showNoBrandError {
                                    findNavController().popBackStack(
                                        R.id.shopFragment,
                                        false
                                    )
                                }
                                btnSubscribe.isEnabled = false
                            }
                        }
                    }
                })
            }
        }
    }

    override fun onDestroyView() {
        shopItemDetailsViewModel.clearBoosters()
        shopItemDetailsViewModel.filterBoosters(emptyList())
        super.onDestroyView()
    }

    override val logTag = "ShopItemDetailsFragment"

    override val analyticsScreenName = "shop.item_details"
}

const val RAFFLE_DETAILS_URL = "https://www.globe.com.ph/new-globeone-promos.html"
