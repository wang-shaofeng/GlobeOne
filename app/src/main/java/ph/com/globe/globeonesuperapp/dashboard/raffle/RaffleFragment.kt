/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.dashboard.raffle

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.RaffleFragmentBinding
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class RaffleFragment : NoBottomNavViewBindingFragment<RaffleFragmentBinding>(bindViewBy = {
    RaffleFragmentBinding.inflate(it)
}) {

    val raffleViewModel: RaffleViewModel by hiltNavGraphViewModels(R.id.dashboard_subgraph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        with(viewBinding) {
            val raffleAdapter = RaffleAdapter()
            rvRaffles.adapter = raffleAdapter

            raffleViewModel.tickets.observe(viewLifecycleOwner, {
                raffleAdapter.submitList(it)
            })

            raffleViewModel.ticketCount.observe(viewLifecycleOwner, {
                tvRaffleCount.text = it.toString()
            })

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override val logTag = "RaffleFragment"
}
