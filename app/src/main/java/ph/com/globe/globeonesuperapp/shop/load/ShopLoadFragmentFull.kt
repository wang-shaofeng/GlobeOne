/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.load

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.BUTTON
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.LOAD_SCREEN
import ph.com.globe.analytics.events.SUBSCRIBE
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopLoadFragmentFullBinding
import ph.com.globe.globeonesuperapp.shop.ShopFragmentDirections
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter
import ph.com.globe.globeonesuperapp.shop.ShopViewModel
import ph.com.globe.globeonesuperapp.shop.load.ShopLoadViewModel.Wallet
import ph.com.globe.globeonesuperapp.shop.select_other_account.LOGGED_IN_STATUS_KEY
import ph.com.globe.globeonesuperapp.shop.select_other_account.TITLE_KEY
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.balance.calculateDiscountPrice
import ph.com.globe.globeonesuperapp.utils.balance.toFormattedDisplayPrice
import ph.com.globe.globeonesuperapp.utils.closeKeyboard
import ph.com.globe.globeonesuperapp.utils.convertToClassicNumberFormat
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.payment.*
import ph.com.globe.globeonesuperapp.utils.reflectValidationToErrorDisplaying
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.globeonesuperapp.utils.getStringOrNull
import ph.com.globe.globeonesuperapp.utils.ui.formatOptionValue
import javax.inject.Inject

@AndroidEntryPoint
class ShopLoadFragmentFull : NestedViewBindingFragment<ShopLoadFragmentFullBinding>(bindViewBy = {
    ShopLoadFragmentFullBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val contactsViewModel: ContactsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private val shopViewModel: ShopViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private val shopLoadViewModel: ShopLoadViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    // First pair element represents available options, second element represents allowed limits
    private val gpLoadParams =
        Pair(listOf(20, 30, 50, 100, 200, 300, 500, 600, 900), Pair(20, 2000))

    private val tmLoadParams =
        Pair(listOf(20, 30, 50, 100, 200, 300, 500, 600, 900), Pair(5, 2000))

    private val hpwLoadParams =
        Pair(listOf(200, 300, 500, 600, 750, 900, 1000, 1200, 1500), Pair(20, 2000))

    private val retailLoadParams =
        Pair(listOf(200, 300, 500, 1000, 2000, 3000, 5000), Pair(50, 5000))

    private var checkedType = CheckedType.PERSONAL

    private var discountPrice: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:load screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewBinding) {

            with(shopLoadViewModel) {
                ivRetailer.setOnClickListener {
                    selectWallet(Wallet.Retailer)
                }

                ivPersonal.setOnClickListener {
                    selectWallet(Wallet.Personal)
                }

                walletType.observe(viewLifecycleOwner, {
                    when (it) {
                        is Wallet.Personal -> {
                            checkedType = CheckedType.PERSONAL
                            bingoView.checkedType = CheckedType.PERSONAL
                            ivGetOff.isVisible = true
                            contactsViewModel.lastCheckedNumberValidation.value?.apply {
                                updateLoadParams(
                                    when (brand) {
                                        AccountBrand.GhpPrepaid -> gpLoadParams
                                        AccountBrand.Tm -> tmLoadParams
                                        AccountBrand.Hpw -> hpwLoadParams
                                        else -> gpLoadParams
                                    }
                                )
                            }

                            ivRetailer.setImageResource(R.drawable.ic_retailer_inactive)
                            ivPersonal.setImageResource(R.drawable.ic_personal_active)
                        }
                        is Wallet.Retailer -> {
                            bingoView.checkedType = CheckedType.RETAILER
                            checkedType = CheckedType.RETAILER
                            ivGetOff.isVisible = false
                            contactsViewModel.lastCheckedNumberValidation.value?.apply {
                                updateLoadParams(retailLoadParams)
                            }
                            ivRetailer.setImageResource(R.drawable.ic_retailer_active)
                            ivPersonal.setImageResource(R.drawable.ic_personal_inactive)
                        }
                    }
                })
            }

            shopViewModel.loggedIn.observe(viewLifecycleOwner, { loggedIn ->
                if (loggedIn) {
                    tilMobileNumber.endIconDrawable =
                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_edit)
                    tilMobileNumber.setEndIconOnClickListener {
                        findNavController().safeNavigate(
                            R.id.action_shopFragment_to_selectOtherAccountFragment,
                            bundleOf(
                                TITLE_KEY to getString(R.string.load_capital),
                                LOGGED_IN_STATUS_KEY to loggedIn
                            )
                        )
                    }
                } else {
                    tilMobileNumber.endIconDrawable =
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_user
                        )
                    tilMobileNumber.setEndIconOnClickListener {
                        shopViewModel.selectLoadFragment(ShopPagerAdapter.LOAD_INITIAL_ID)
                    }
                }
            })

            contactsViewModel.selectedNumber.observe(viewLifecycleOwner, { selectedNumber ->
                etMobileNumber.setText(selectedNumber)
                shopLoadViewModel.selectWallet(Wallet.Personal)
            })

            etMobileNumber.addTextChangedListener {
                tilMobileNumber.hint = contactsViewModel.getNumberOwnerOrPlaceholder(
                    it.getStringOrNull(),
                    getString(R.string.mobile_number)
                )
            }

            contactsViewModel.lastCheckedNumberValidation.observe(
                viewLifecycleOwner,
                { validation ->
                    reflectValidationToErrorDisplaying(validation, etMobileNumber, tilMobileNumber)
                })

            etLoadAmount.doOnTextChanged { text, _, _, _ ->
                updateSubscribeEnabled()
                with(text.toString()) {
                    if (isNotEmpty()) {
                        if (isFormattedAmount(this)) {
                            if (this.pesosToDouble() != shopLoadViewModel.amount.toDouble()) {
                                bingoView.unselectAll()
                            }
                            shopLoadViewModel.amount = this.pesosToInt()
                            checkAmountValid(shopLoadViewModel.amountLimits)
                            etLoadAmount.setSelection(etLoadAmount.text.toString().length)
                        } else {
                            val formattedAmount = formatAmount(this)
                            etLoadAmount.setText(formattedAmount)
                        }
                    }
                }
            }

            bingoView.setOnAmountSelectedListener { amount ->
                shopLoadViewModel.amount = amount
                etLoadAmount.setText(amount.formatOptionValue())
                closeKeyboard(etLoadAmount, requireContext())
            }

            contactsViewModel.lastCheckedNumberValidation.observe(
                viewLifecycleOwner,
                { validation ->
                    val isPrepaid = validation.brand?.isPrepaid() == true

                    reflectValidationToErrorDisplaying(
                        validation,
                        etMobileNumber,
                        tilMobileNumber,
                        true
                    )

                    if (isPrepaid.not()) {
                        return@observe
                    }

                    updateLoadParams(
                        when (validation.brand) {
                            AccountBrand.GhpPrepaid -> gpLoadParams
                            AccountBrand.Tm -> tmLoadParams
                            AccountBrand.Hpw -> hpwLoadParams
                            else -> gpLoadParams
                        }
                    )
                })

            contactsViewModel.isRetailer.observe(viewLifecycleOwner, { isRetailer ->
                clWalletType.isVisible = isRetailer
            })

            btnSubscribe.setOnClickListener {
                if (checkAmountValid(shopLoadViewModel.amountLimits)) {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            LOAD_SCREEN, BUTTON, SUBSCRIBE,
                            productName = etLoadAmount.text.toString()
                        )
                    )

                    val isRetailer = (contactsViewModel.isRetailer.value ?: false)
                            && shopLoadViewModel.walletType.value is Wallet.Retailer

                    findNavController().safeNavigate(
                        ShopFragmentDirections.actionShopFragmentToNavigationPayment(
                            checkoutToPaymentParams(
                                paymentType = BUY_LOAD,
                                transactionType = NON_BILL,
                                mobileNumber = etMobileNumber.text.toString()
                                    .convertToClassicNumberFormat(),
                                amount = etLoadAmount.text.toString().pesosToDouble(),
                                paymentName = etLoadAmount.text.toString(),
                                price = etLoadAmount.text.toString().pesosToDouble(),
                                skelligWallet = null,
                                skelligCategory = null,
                                provisionByServiceId = false,
                                isRetailer = isRetailer,
                                currentAmount = if (isRetailer) null else discountPrice
                            )
                        )
                    )
                }
            }
        }
    }

    private fun isFormattedAmount(amountString: String): Boolean {
        return (amountString.isNotEmpty()
                && amountString.startsWith(PRICE_PREFIX).not()
                && amountString.startsWith('0').not()
                && isDotFormatted(amountString)
                )
    }

    private fun isDotFormatted(amount: String): Boolean {
        return when {
            amount.length < 4 -> true
            amount.length == 4 -> false
            else -> amount.contains(",")
        }
    }

    private fun formatAmount(amountString: String): String {
        return if (amountString.isNotEmpty()) {
            var tempString = amountString.substringAfter(PRICE_PREFIX)
            while (tempString.startsWith('0')) {
                tempString = tempString.substringAfter('0')
            }
            val resultString = buildString {
                tempString.forEach {
                    if (it in '0'..'9') {
                        append(it)
                    }
                }
            }

            resultString.toIntOrNull()?.formatOptionValue() ?: ""
        } else ""
    }

    private fun checkAmountValid(limits: Pair<Int, Int>): Boolean {
        with(viewBinding.etLoadAmount.text.toString()) {
            return if (isNotEmpty()) {
                val amount = pesosToDouble()
                val amountInLimits = amount >= limits.first && amount <= limits.second
                viewBinding.tilLoadAmount.error =
                    if (!amountInLimits)
                        if (amount < limits.first) {
                            getString(
                                R.string.error_amount_out_of_limits,
                                limits.first,
                                limits.second.formatOptionValue()
                            )
                        } else {
                            getString(
                                R.string.error_amount_exceeded_limits,
                                limits.second.formatOptionValue()
                            )
                        } else null

                viewBinding.tilLoadAmount.errorIconDrawable = null

                if (amountInLimits && checkedType == CheckedType.PERSONAL) {
                    discountPrice = calculateDiscountPrice(amount, DISCOUNT)

                    viewBinding.tvPayOnly.text =
                        getString(
                            R.string.pay_only_when_check_out,
                            discountPrice.toFormattedDisplayPrice()
                        )

                    viewBinding.tvPayOnly.isVisible = true
                } else {
                    viewBinding.tvPayOnly.isVisible = false
                }

                if (viewBinding.bingoView.optionValues.contains(amount.toInt())) {
                    viewBinding.bingoView.select(amount.toInt())
                } else {
                    viewBinding.bingoView.unselectAll()
                }

                return amountInLimits
            } else false
        }
    }

    private fun updateSubscribeEnabled() {
        viewBinding.btnSubscribe.isEnabled =
            contactsViewModel.lastCheckedNumberValidation.value?.isValid == true && checkAmountValid(
                shopLoadViewModel.amountLimits
            )
    }

    private fun updateLoadParams(params: Pair<List<Int>, Pair<Int, Int>>) {
        viewBinding.etLoadAmount.setText("")
        viewBinding.tvPayOnly.isVisible = false
        discountPrice = null
        viewBinding.bingoView.optionValues = params.first
        shopLoadViewModel.amountLimits = params.second
        checkAmountValid(shopLoadViewModel.amountLimits)

        viewBinding.tilLoadAmount.hint =
            getString(
                R.string.amount_limits_hint,
                shopLoadViewModel.amountLimits.first.formatOptionValue(),
                shopLoadViewModel.amountLimits.second.formatOptionValue()
            )
    }

    override val logTag = "ShopLoadFragmentFull"

    override val analyticsScreenName = "shop.load"

    companion object {
        const val DISCOUNT = 0.96

        enum class CheckedType {
            PERSONAL, RETAILER
        }
    }
}
