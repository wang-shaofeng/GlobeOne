/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AddAccountFinishFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountFinishFragment : NoBottomNavViewBindingFragment<AddAccountFinishFragmentBinding>({
    AddAccountFinishFragmentBinding.inflate(it)
}) {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val addAccountMoreAccountsViewModel: AddAccountMoreAccountsViewModel by navGraphViewModels(
        R.id.navigation_add_account
    ) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: here status bar should be transparent according to figma
        setDarkStatusBar()

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) { }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(3000)
            withContext(Dispatchers.Main) {
                if (addAccountMoreAccountsViewModel.hasPromo)
                    findNavController().safeNavigate(R.id.action_addAccountFinishFragment_to_surpriseIsComingFragment)
                else
                    crossBackstackNavigator.crossNavigateWithoutHistory(
                        BaseActivity.DASHBOARD_KEY,
                        R.id.dashboardFragment
                    )
            }
        }
    }

    override val logTag = "AddAccountFinishFragment"
}
