/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AddAccountFindModemNumberFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountFindModemNumberFragment :
    NoBottomNavViewBindingFragment<AddAccountFindModemNumberFragmentBinding>(bindViewBy = {
        AddAccountFindModemNumberFragmentBinding.inflate(it)
    }) {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val addAccountBroadbandNumberViewModel: AddAccountBroadbandNumberViewModel by hiltNavGraphViewModels(R.id.navigation_add_account)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            ivClose.setOnClickListener {
                addAccountBroadbandNumberViewModel.skipAddingAccount(
                    {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.DASHBOARD_KEY,
                            R.id.dashboardFragment
                        )
                    },
                    {}
                )
            }

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override val logTag = "FindModemUsernamePasswordFragment"
}
