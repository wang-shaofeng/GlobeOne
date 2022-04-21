package ph.com.globe.globeonesuperapp.addaccount.broadband.simserial

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.broadband.AddAccountBroadbandNumberViewModel
import ph.com.globe.globeonesuperapp.databinding.AddAccountFindSimSerialFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountFindSimSerialFragment :
    NoBottomNavViewBindingFragment<AddAccountFindSimSerialFragmentBinding>(
        bindViewBy = {
            AddAccountFindSimSerialFragmentBinding.inflate(it)
        }) {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val addAccountBroadbandNumberViewModel: AddAccountBroadbandNumberViewModel by hiltNavGraphViewModels(R.id.navigation_add_account)

    private val addAccountFindSimSerialFragmentArgs by navArgs<AddAccountFindSimSerialFragmentArgs>()

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

            tvAddAccountOtp.setOnClickListener {
                findNavController().safeNavigate(
                    AddAccountFindSimSerialFragmentDirections.actionAddAccountFindSimSerialFragmentToAddAccountBroadbandNumberWithOtpFragment(
                        hpwNumber = addAccountFindSimSerialFragmentArgs.hpwNumber
                    )
                )
            }
        }
    }

    override val logTag = "AddAccountFindSimSerialFragment"
}
