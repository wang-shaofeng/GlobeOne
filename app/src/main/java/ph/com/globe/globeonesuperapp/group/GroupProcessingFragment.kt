/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GroupProcessingFragmentBinding
import ph.com.globe.globeonesuperapp.group.GroupViewModel.GroupInfoResult.GroupInfoSuccess
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class GroupProcessingFragment : NoBottomNavViewBindingFragment<GroupProcessingFragmentBinding>({
    GroupProcessingFragmentBinding.inflate(it)
}) {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val groupViewModel: GroupViewModel by navGraphViewModels(R.id.group_subgraph) { defaultViewModelProviderFactory }

    private val groupProcessingFragmentArgs by navArgs<GroupProcessingFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(groupProcessingFragmentArgs) {
            groupViewModel.fetchAllInfoAsOwner(
                ownerAccountAlias = accountAlias,
                ownerMobileNumber = accountNumber,
                skelligWallet = skelligWallet,
                skelligCategory = skelligCategory
            )
        }

        groupViewModel.groupProcessingResult.observe(viewLifecycleOwner, {
            when (it) {
                is GroupInfoSuccess -> {
                    findNavController().safeNavigate(GroupProcessingFragmentDirections.actionGroupProcessingFragmentToGroupOverviewFragment())
                }
                else -> {
                    findNavController().navigateUp()
                }
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // This is left empty because you need to wait for the processing to complete
        }
    }

    override val logTag = "GroupProcessingFragment"
}

const val OWNER_NUMBER = "accountNumber"
const val OWNER_ALIAS = "accountAlias"
const val SKELLING_WALLET = "skelligWallet"
const val SKELLING_CATEGORY = "skelligCategory"
