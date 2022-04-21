package ph.com.globe.globeonesuperapp.account.or

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.OrStatementFragmentBinding
import ph.com.globe.globeonesuperapp.utils.permissions.registerActivityResultForStoragePermission
import ph.com.globe.globeonesuperapp.utils.permissions.requestStoragePermissionsIfNeededAndPerformSuccessAction
import ph.com.globe.globeonesuperapp.utils.saveBillToCache
import ph.com.globe.globeonesuperapp.utils.saveBillToDownloads
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.showSnackbar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import java.io.File

@AndroidEntryPoint
class ORStatementFragment :
    NoBottomNavViewBindingFragment<OrStatementFragmentBinding>(
        bindViewBy = { OrStatementFragmentBinding.inflate(it) }
    ) {

    private val orStatementViewModel: ORStatementViewModel by viewModels()

    var requestStorageActivityLauncher: ActivityResultLauncher<String>? = null

    private lateinit var currentORStatementStatus: ORStatementViewModel.ORStatementStatus.Success

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStorageActivityLauncher =
            registerActivityResultForStoragePermission {
                if (this::currentORStatementStatus.isInitialized) {
                    handleDownload(currentORStatementStatus)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setDarkStatusBar()

        with(viewBinding) {

            with(orStatementViewModel) {

                downloadBillPdf(
                    failureCallback = { findNavController().navigateUp() }
                )

                ivClose.setOnClickListener {
                    findNavController().navigateUp()
                }

                orStatementStatus.observe(viewLifecycleOwner, {

                    it.handleEvent { orStatementStatus ->

                        if (orStatementStatus is ORStatementViewModel.ORStatementStatus.Success) {
                            currentORStatementStatus = orStatementStatus

                            saveBillToCache(orStatementStatus.pdfByteArray)

                            pdfView.fromFile(File(requireContext().cacheDir, "cached_bill.pdf"))
                                .show()

                            btnDownload.isEnabled = true

                            btnDownload.setOnClickListener {

                                if (requestStorageActivityLauncher != null) {
                                    requestStoragePermissionsIfNeededAndPerformSuccessAction(
                                        requestStorageActivityLauncher!!
                                    )
                                } else {
                                    handleDownload(orStatementStatus)
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private fun handleDownload(orStatementStatus: ORStatementViewModel.ORStatementStatus.Success) {
        saveBillToDownloads(
            orStatementStatus.pdfByteArray,
            orStatementStatus.pdfName
        )

        GlobeSnackbarLayoutBinding.inflate(
            LayoutInflater.from(
                requireContext()
            )
        ).apply {
            tvGlobeSnackbarTitle.setText(R.string.account_details_bill_downloaded_pdf_message)
            tvGlobeSnackbarDescription.setText(R.string.account_details_bill_downloaded_pdf_description)
            showSnackbar(this)
        }
    }

    override val logTag = "ORStatementFragment"
}
