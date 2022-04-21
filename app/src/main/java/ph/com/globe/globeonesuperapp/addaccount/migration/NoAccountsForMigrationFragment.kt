/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.migration

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.databinding.NoAccountsForMigrationFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class NoAccountsForMigrationFragment :
    NoBottomNavViewBindingFragment<NoAccountsForMigrationFragmentBinding>({
        NoAccountsForMigrationFragmentBinding.inflate(it)
    }) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.btnProceed.setOnClickListener {
            findNavController().safeNavigate(NoAccountsForMigrationFragmentDirections.actionNoAccountsForMigrationFragmentToAddAccountMoreAccountsFragment())
        }
    }

    override val logTag = "NoAccountsForMigrationFragment"
}
