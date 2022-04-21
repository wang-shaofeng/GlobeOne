/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.databinding.HelpFragmentBinding
import ph.com.globe.globeonesuperapp.utils.setWhiteStatusBar
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.BottomNavViewBindingFragment

@AndroidEntryPoint
class HelpFragment : BottomNavViewBindingFragment<HelpFragmentBinding>(bindViewBy = {
    HelpFragmentBinding.inflate(it)
}) {

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setWhiteStatusBar()

        with(viewBinding) {
            btnGetHelp.setOnClickListener {
                generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(HELP_URL))
                    startActivity(intent)
                }
            }
        }
    }

    override val logTag = "HelpFragment"
}

const val HELP_URL = "https://m.me/globeph?ref=NG1"
