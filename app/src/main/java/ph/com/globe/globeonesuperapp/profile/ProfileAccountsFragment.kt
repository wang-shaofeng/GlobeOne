/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile

import ph.com.globe.globeonesuperapp.databinding.ProfileAccountsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

class ProfileAccountsFragment : NoBottomNavViewBindingFragment<ProfileAccountsFragmentBinding>(bindViewBy = {
    ProfileAccountsFragmentBinding.inflate(it)
}) {
    override val logTag = "ProfileAccountsFragment"
}
