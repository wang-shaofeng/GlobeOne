/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.data_as_currency

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.NoAccountsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

class NoAccountsFragment : NoBottomNavViewBindingFragment<NoAccountsFragmentBinding>(bindViewBy = {
    NoAccountsFragmentBinding.inflate(it)
}) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setLightStatusBar()

        with(viewBinding) {
            wfNoAccounts.onBack {
                findNavController().navigateUp()
            }

            btnAddAccount.setOnClickListener {
                findNavController().safeNavigate(R.id.action_noAccountsFragment_to_navigation_add_account)
            }
        }
    }

    override val logTag = "NoAccountsFragment"
}
