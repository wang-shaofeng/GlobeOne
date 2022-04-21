/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.select_other_account

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.SelectOtherAccountFragmentBinding
import ph.com.globe.globeonesuperapp.payment.CURRENT_NAV_GRAPH
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.shop.util.NumberValidation
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.profile.domain_models.isPrepaid
import ph.com.globe.model.shop.formattedForPhilippines
import java.lang.IllegalStateException
import javax.inject.Inject

@AndroidEntryPoint
class SelectOtherAccountFragment :
    NoBottomNavViewBindingFragment<SelectOtherAccountFragmentBinding>(
        bindViewBy = {
            SelectOtherAccountFragmentBinding.inflate(it)
        }
    ) {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val belongsToNavigationId: Int
        get() = arguments?.getString(CURRENT_NAV_GRAPH)?.toInt() ?: R.id.shop_subgraph

    private lateinit var contactsViewModel: ContactsViewModel

    private val selectOtherAccountViewModel: SelectOtherAccountViewModel by viewModels()

    private lateinit var selectOtherAccountRecyclerViewAdapter: SelectOtherAccountRecyclerViewAdapter

    private var otherSelectedNumber = ""

    private val buttonClickListener =
        Observer<NumberValidation> { validation ->
            if (validation?.number == otherSelectedNumber && validation.isValid)
                findNavController().navigateUp()
        }

    private val accountItems = mutableListOf<AccountItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDarkStatusBar()

        contactsViewModel =
            navGraphViewModels<ContactsViewModel>(belongsToNavigationId) { defaultViewModelProviderFactory }.value

        with(viewBinding) {
            if (arguments?.getBoolean(LOGGED_IN_STATUS_KEY) == false) {
                tvChooseAccountTitle.visibility = View.GONE
                vAccountsSeparator.visibility = View.GONE
            }
            if (arguments?.getString(TITLE_KEY) == getString(R.string.charge_to_load)) {
                tvChooseAccountDescription.visibility = View.VISIBLE
            }
            tvHeaderTitle.text = arguments?.getString(TITLE_KEY)
            selectOtherAccountRecyclerViewAdapter =
                SelectOtherAccountRecyclerViewAdapter(
                    enablingViewsCallback = { enableButton ->
                        btnSelectAccount.isEnabled = enableButton
                        tilMobileNumber.isSelected = false
                    }, selectAccountCallback = { account ->
                        selectOtherAccountViewModel.selectedAccountNumber =
                            account.phoneNumber.formattedForPhilippines()
                        etMobileNumber.setText(account.phoneNumber)
                        closeKeyboard(etMobileNumber, requireContext())
                        tilMobileNumber.error = null
                    }, (arguments?.getString(
                        TITLE_KEY
                    ) == getString(R.string.charge_to_load))
                )

            rvAccounts.adapter = selectOtherAccountRecyclerViewAdapter

            contactsViewModel.enrolledAccounts.observe(viewLifecycleOwner) { enrolledAccounts ->
                val isLoadTab =
                    (arguments?.getString(TITLE_KEY)
                        .equals(getString(R.string.shop_tab_load), true)) || (arguments?.getString(
                        TITLE_KEY
                    ) == getString(R.string.charge_to_load))

                val filterEnrolledAccounts =
                    if (isLoadTab) {
                        val split = enrolledAccounts.partition { it.isPrepaid() }
                        val sorted = split.first + split.second
                        if (arguments?.getString(TITLE_KEY) == getString(R.string.charge_to_load)) {
                            val primaryMsisdn = arguments?.getString(
                                BUYING_LOAD_ON_CHARGE_TO_LOAD_NUMBER_KEY
                            )

                            if (primaryMsisdn == null) {
                                sorted
                            } else {
                                sorted.filterNot {
                                    it.primaryMsisdn.convertToClassicNumberFormat() == primaryMsisdn.convertToClassicNumberFormat()
                                }
                            }
                        } else {
                            sorted
                        }
                    } else
                        enrolledAccounts.filter { it.isPrepaid() }

                accountItems.clear()
                accountItems.addAll(filterEnrolledAccounts.map {
                    AccountItem(
                        it.accountAlias,
                        it.primaryMsisdn,
                        it.brandType,
                        contactsViewModel.selectedNumber.value == it.mobileNumber,
                        isClickable = if (isLoadTab) {
                            it.isPrepaid()
                        } else {
                            true
                        }
                    )
                })

                if (arguments?.getString(TITLE_KEY) == getString(R.string.charge_to_load))
                    contactsViewModel.inquireBalance()

                selectOtherAccountRecyclerViewAdapter.submitList(accountItems)
            }

            contactsViewModel.balanceUpdated.observe(viewLifecycleOwner) {
                val index =
                    accountItems.indexOfFirst { accountItem -> accountItem.phoneNumber == it.first }
                accountItems.getOrNull(index)?.balance = it?.second
                val balance = arguments?.getFloat(BUYING_LOAD_ON_CHARGE_AMOUNT_KEY) ?: 0f
                if (it.second != null && it.second ?: 0f < balance + HARDCODED_SHARELOAD_FEE) {
                    accountItems.getOrNull(index)?.isClickable = false
                }
                if (rvAccounts.isVisible) {
                    selectOtherAccountRecyclerViewAdapter.notifyItemChanged(index)
                }
            }

            etMobileNumber.setText(contactsViewModel.selectedNumber.value)

            contactsViewModel.selectedNumber.observe(viewLifecycleOwner, {
                chooseManualInput()
                etMobileNumber.setText(it)
                requireContext().hideError(tilMobileNumber, etMobileNumber)
            })

            with(etMobileNumber) {
                addTextChangedListener { editable ->
                    requireContext().hideError(tilMobileNumber, etMobileNumber)
                    btnSelectAccount.isEnabled = !editable.isNullOrBlank()
                    editable.formatCountryCodeIfExists()
                    tilMobileNumber.hint = contactsViewModel.getNumberOwnerOrPlaceholder(
                        editable.getStringOrNull(),
                        getString(R.string.mobile_number)
                    )
                }

                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus)
                        chooseManualInput()
                    btnSelectAccount.isEnabled = !text.isNullOrBlank()
                }

                setOnEditorActionListener { v, _, _ ->
                    if (v.text.isNotEmpty())
                        contactsViewModel.selectAndValidateNumber(v.text.toString())
                    closeKeyboard(v, requireContext())
                    true
                }
            }

            contactsViewModel.lastCheckedNumberValidation.observe(
                viewLifecycleOwner,
                { validation ->
                    val isPrepaid = validation.brand?.isPrepaid() == true
                    btnSelectAccount.isEnabled = isPrepaid

                    reflectValidationToErrorDisplaying(
                        validation,
                        etMobileNumber,
                        tilMobileNumber,
                        true
                    )
                })

            viewBinding.tilMobileNumber.setEndIconOnClickListener {
                findNavController().safeNavigate(
                    when (belongsToNavigationId) {
                        R.id.shop_subgraph -> R.id.action_selectOtherAccountFragment_to_contacts_fragment
                        R.id.payment_subgraph -> R.id.action_selectOtherAccountFragmentPayment_to_contactsFragment
                        else -> throw IllegalStateException("Transition from current subgraph not supported")
                    },
                    bundleOf(
                        CURRENT_NAV_GRAPH to belongsToNavigationId.toString()
                    )
                )
            }

            ivClose.setOnClickListener {
                findNavController().popBackStack()
            }

            btnSelectAccount.setOnClickListener {
                otherSelectedNumber = selectOtherAccountViewModel.selectedAccountNumber
                    ?: etMobileNumber.text.toString()

                if (sameNumberOnChargeToLoadBuyingLoad(otherSelectedNumber)) {
                    requireContext().showError(
                        tilMobileNumber,
                        etMobileNumber,
                        getString(R.string.you_cannot_charge_the_load)
                    )
                } else {
                    contactsViewModel.selectAndValidateNumber(otherSelectedNumber)

                    contactsViewModel.lastCheckedNumberValidation.observe(
                        viewLifecycleOwner,
                        buttonClickListener
                    )
                }
            }
        }
    }

    private fun SelectOtherAccountFragmentBinding.chooseManualInput() {
        selectOtherAccountRecyclerViewAdapter.unSelectItems()
        selectOtherAccountViewModel.selectedAccountNumber = null
        tilMobileNumber.isSelected = true
    }

    private fun sameNumberOnChargeToLoadBuyingLoad(selectedNumber: String): Boolean =
        arguments?.getString(BUYING_LOAD_ON_CHARGE_TO_LOAD_NUMBER_KEY)
            ?.convertToClassicNumberFormat() == selectedNumber.convertToClassicNumberFormat()

    override val logTag = "SelectOtherAccountFragment"
}

const val TITLE_KEY = "Title"

const val LOGGED_IN_STATUS_KEY = "LoggedInStatus"

const val BUYING_LOAD_ON_CHARGE_TO_LOAD_NUMBER_KEY = "BuyingLoadOnChargeToLoad"

const val BUYING_LOAD_ON_CHARGE_AMOUNT_KEY = "BuyingLoadOnChargeAmount"

private const val HARDCODED_SHARELOAD_FEE = 1.0
