/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel
import ph.com.globe.globeonesuperapp.databinding.AddAccountNumberFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountNumberFragment : NoBottomNavViewBindingFragment<AddAccountNumberFragmentBinding>(
    bindViewBy = {
        AddAccountNumberFragmentBinding.inflate(it)
    }
) {

    private val addAccountNumberFragmentArgs by navArgs<AddAccountNumberFragmentArgs>()

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val addAccountMoreAccountsViewModel: AddAccountMoreAccountsViewModel by navGraphViewModels(
        R.id.navigation_add_account
    ) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDarkStatusBar()
        with(viewBinding) {
            vpAddAccount.adapter =
                AddAccountNumberPagerAdapter(this@AddAccountNumberFragment)

            TabLayoutMediator(tlAddAccount, vpAddAccount) { tab, position ->
                tab.text = when (position) {

                    0 -> resources.getString(R.string.broadband)

                    1 -> resources.getString(R.string.mobile)

                    else -> throw IllegalStateException()
                }
            }.attach()

            tlAddAccount.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                private fun showHideStep(tab: TabLayout.Tab?) {
                    vStepLine4.isVisible =
                        tab!!.position == 1 && !addAccountNumberFragmentArgs.addManually
                }

                override fun onTabSelected(tab: TabLayout.Tab?) = showHideStep(tab)
                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                override fun onTabReselected(tab: TabLayout.Tab?) = showHideStep(tab)
            })

            ivClose.setOnClickListener {
                crossBackstackNavigator.crossNavigateWithoutHistory(
                    BaseActivity.DASHBOARD_KEY,
                    R.id.dashboardFragment
                )
            }

            // Adding a callback on back pressed to replace the standard up navigation with popBackStack
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (requireArguments().getBoolean(FROM_DASHBOARD)) {
                            crossBackstackNavigator.crossNavigateWithoutHistory(
                                BaseActivity.DASHBOARD_KEY,
                                R.id.dashboardFragment
                            )
                        } else {
                            findNavController().navigateUp()
                        }
                    }
                }
            )
        }
    }

    override val logTag = "AddAccountNumberFragment"
}

const val FROM_DASHBOARD = "FromFragment"
