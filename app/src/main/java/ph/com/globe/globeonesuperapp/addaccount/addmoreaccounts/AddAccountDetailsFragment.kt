/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel.SaveChangesResult.SaveChangesSuccess
import ph.com.globe.globeonesuperapp.databinding.AddAccountDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class AddAccountDetailsFragment : NoBottomNavViewBindingFragment<AddAccountDetailsFragmentBinding>(
    bindViewBy = {
        AddAccountDetailsFragmentBinding.inflate(it)
    }
) {

    private val addAccountMoreAccountsViewModel: AddAccountMoreAccountsViewModel by navGraphViewModels(
        R.id.navigation_add_account
    ) { defaultViewModelProviderFactory }

    private val addAccountDetailsFragmentArgs by navArgs<AddAccountDetailsFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        val msisdn = addAccountDetailsFragmentArgs.account.enrollAccount.msisdn ?: ""

        with(viewBinding) {
            etAccountNumber.setText(
                msisdn.toDisplayUINumberFormat()
            )
            etAccountNumber.isEnabled = false
            etAccountName.setText(addAccountDetailsFragmentArgs.account.enrollAccount.accountAlias)
            tvBrand.text = addAccountDetailsFragmentArgs.account.enrollAccount.brandType.toString()

            etAccountName.addTextChangedListener { editable ->
                btnSaveChanges.isEnabled = true
                requireContext().hideError(tilAccountName, etAccountName)
            }

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }

            btnSaveChanges.setOnClickListener {
                addAccountMoreAccountsViewModel.saveChanges(
                    addAccountDetailsFragmentArgs.account,
                    etAccountName.text.toString()
                )
            }

            btnRemoveAccount.setOnClickListener {
                addAccountMoreAccountsViewModel.createDeleteAccountDialog(
                    {
                        addAccountMoreAccountsViewModel.deleteAccount(
                            addAccountDetailsFragmentArgs.account,
                            addAccountDetailsFragmentArgs.currentTab
                        )
                        findNavController().navigateUp()
                    },
                    {}
                )
            }

            addAccountMoreAccountsViewModel.saveChangesResult.observe(viewLifecycleOwner, {
                it.handleEvent { result ->
                    if (result is SaveChangesSuccess) {
                        findNavController().navigateUp()
                    } else {
                        btnSaveChanges.isEnabled = false
                        requireContext().showError(
                            tilAccountName,
                            etAccountName,
                            getString(R.string.duplicated_name_error)
                        )
                    }
                }
            })
        }
    }

    override val logTag = "AddAccountDetailsFragment"
}
