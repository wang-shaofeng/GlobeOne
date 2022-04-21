/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.load

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ShopLoadFragmentInitialBinding
import ph.com.globe.globeonesuperapp.shop.ShopFragmentDirections
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter
import ph.com.globe.globeonesuperapp.shop.ShopViewModel
import ph.com.globe.globeonesuperapp.shop.select_other_account.LOGGED_IN_STATUS_KEY
import ph.com.globe.globeonesuperapp.shop.select_other_account.TITLE_KEY
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.shop.util.NumberValidation
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment

@AndroidEntryPoint
class ShopLoadFragmentInitial :
    NestedViewBindingFragment<ShopLoadFragmentInitialBinding>(bindViewBy = {
        ShopLoadFragmentInitialBinding.inflate(it)
    }) {
    private val contactsViewModel: ContactsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private val shopViewModel: ShopViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private lateinit var buttonClickListener: Observer<NumberValidation>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {

            shopViewModel.loggedIn.observe(viewLifecycleOwner, { loggedIn ->
                if (loggedIn) {
                    tilMobileNumber.setEndIconDrawable(R.drawable.ic_edit)
                    tilMobileNumber.setEndIconOnClickListener {
                        findNavController().safeNavigate(
                            R.id.action_shopFragment_to_selectOtherAccountFragment,
                            bundleOf(
                                TITLE_KEY to getString(R.string.shop_tab_load),
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

            contactsViewModel.selectedNumber.observe(viewLifecycleOwner) { number ->
                etMobileNumber.setText(number)
            }

            contactsViewModel.lastCheckedNumberValidation.observe(
                viewLifecycleOwner,
                { validation ->

                    val isPrepaid = validation.brand?.isPrepaid() == true
                    btnNext.isEnabled = isPrepaid

                    reflectValidationToErrorDisplaying(
                        validation,
                        etMobileNumber,
                        tilMobileNumber,
                        true
                    )
                })

            etMobileNumber.addTextChangedListener { editable ->

                btnNext.isEnabled = etMobileNumber.text.toString().isNotEmpty()

                requireContext().hideError(
                    viewBinding.tilMobileNumber,
                    viewBinding.etMobileNumber
                )

                editable.formatCountryCodeIfExists()

                tilMobileNumber.hint = contactsViewModel.getNumberOwnerOrPlaceholder(editable.getStringOrNull(), getString(R.string.mobile_number))
            }

            etMobileNumber.setOnEditorActionListener { v, _, _ ->
                if (v.text.isNotEmpty())
                    contactsViewModel.selectAndValidateNumber(v.text.toString())
                closeKeyboard(v, requireContext())
                true
            }

            buttonClickListener = object : Observer<NumberValidation> {
                override fun onChanged(validation: NumberValidation?) {
                    if (validation?.number == etMobileNumber.text.toString()) {
                        if (validation.isValid)
                            shopViewModel.selectLoadFragment(ShopPagerAdapter.LOAD_FULL_ID)
                        else
                            contactsViewModel.selectAndValidateNumber(etMobileNumber.text.toString())
                    }
                    contactsViewModel.lastCheckedNumberValidation.removeObserver(this)
                }
            }

            btnNext.setOnClickListener {
                with(contactsViewModel) {
                    selectAndValidateNumber(etMobileNumber.text.toString())
                    lastCheckedNumberValidation.observe(viewLifecycleOwner, buttonClickListener)
                }
            }
        }
    }

    override fun onDestroyView() {
        contactsViewModel.lastCheckedNumberValidation.removeObserver(buttonClickListener)
        super.onDestroyView()
    }

    override val logTag = "ShopLoadFragmentInitial"
}
