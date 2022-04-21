/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.AUTH_KEY
import ph.com.globe.globeonesuperapp.databinding.TokenExpiredFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.OverlayAndDialogFactories
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class TokenExpiredFragment : NoBottomNavViewBindingFragment<TokenExpiredFragmentBinding>({
    TokenExpiredFragmentBinding.inflate(it)
}) {

    @Inject
    lateinit var overlayAndDialogFactories: OverlayAndDialogFactories

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // block back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { }

        handler.handleDialog(overlayAndDialogFactories.createTokenExpiredDialog {
            crossBackstackNavigator.crossNavigateWithoutHistory(AUTH_KEY, R.id.selectSignMethodFragment)
        })
    }

    override val logTag: String = "Token Expired Fragment"
}
