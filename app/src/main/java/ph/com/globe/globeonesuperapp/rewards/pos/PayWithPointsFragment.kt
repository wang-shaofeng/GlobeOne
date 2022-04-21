/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.pos

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.BUTTON
import ph.com.globe.analytics.events.ENTER_CODE_MANUALLY
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.PAYMENT_SCREEN
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.errors.rewards.GetMerchantDetailsError
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PayWithPointsFragmentBinding
import ph.com.globe.globeonesuperapp.rewards.pos.camera.CameraFragment
import ph.com.globe.globeonesuperapp.rewards.pos.camera.crop
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.oneTimeEventObserve
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject

@AndroidEntryPoint
class PayWithPointsFragment : NoBottomNavViewBindingFragment<PayWithPointsFragmentBinding>({
    PayWithPointsFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val posViewModel by navGraphViewModels<POSViewModel>(R.id.pos_subgraph) { defaultViewModelProviderFactory }

    private var scanner: BarcodeScanner? = null

    /**
     * Used to crop image obtained from [RectangleView]
     */
    private var cropWidthRatio: Float = 0f
    private var cropHeightRatio: Float = 0f
    private var decorationAspectRatio: Float = 0f

    private val args by navArgs<PayWithPointsFragmentArgs>()

    private var shoRationale = false

    private var shouldScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        posViewModel.chosenAccount(args.enrolledAccountWithPoints)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        posViewModel.startCounting()

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .add(R.id.fl_camera, CameraFragment())
                .commit()
        }

        childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            if (fragment is CameraFragment) {
                fragment.setErrorCallback(cameraErrorCallback)
                fragment.setPictureCallback(cameraPictureCallback)
            }
        }

        viewBinding.ivBack.setOnClickListener { findNavController().navigateUp() }

        viewBinding.recvDecoration.post {
            if (getView() != null)
                viewBinding.recvDecoration.let { rvDecoration ->
                    cropHeightRatio = rvDecoration.heightRatio
                    cropWidthRatio = rvDecoration.widthRatio
                    decorationAspectRatio = rvDecoration.aspectRatio
                }
        }

        posViewModel.merchantStatus.oneTimeEventObserve(viewLifecycleOwner) {
            it.onSuccess {
                if (posViewModel.chosenAccount == null) {
                    findNavController().safeNavigate(R.id.action_payWithPointsFragment_to_whichEnrolledAccountFragment)
                } else {
                    findNavController().safeNavigate(R.id.action_payWithPointsFragment_to_payPointsFragment)
                }
            }.onFailure {
                when (it) {
                    is GetMerchantDetailsError.General -> {
                        findNavController().safeNavigate(R.id.action_payWithPointsFragment_to_POSRedeemPointsUnsuccessfulFragment)
                    }
                    is GetMerchantDetailsError.TenSeconds -> {
                        findNavController().safeNavigate(
                            PayWithPointsFragmentDirections.actionPayWithPointsFragmentToPOSRedeemPointsUnsuccessfulFragment(
                                State.QR_CODE_FAILURE_10_SECONDS
                            )
                        )
                    }
                    else -> {
                        findNavController().safeNavigate(
                            PayWithPointsFragmentDirections.actionPayWithPointsFragmentToPOSRedeemPointsUnsuccessfulFragment(
                                State.QR_CODE_NOT_MATCH
                            )
                        )
                    }
                }
            }
        }

        viewBinding.btnSettings.setOnClickListener { openAppSettings() }
        viewBinding.btnEnterCodeManually.setOnClickListener {
            logCustomEvent(
                analyticsEventsProvider.provideEvent(
                    EventCategory.Engagement,
                    PAYMENT_SCREEN, BUTTON, ENTER_CODE_MANUALLY
                )
            )
            findNavController().safeNavigate(R.id.action_payWithPointsFragment_to_enterMerchantCodeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        posViewModel.stopCounting()
    }

    override fun onResume() {
        with(viewBinding) {
            if (!shoRationale) {
                gCamera.isVisible = true
                btnSettings.isVisible = false
                tvNoPermissionDescription.isVisible = false
            } else {
                gCamera.isVisible = false
                btnSettings.isVisible = true
                tvNoPermissionDescription.isVisible = true
                shoRationale = false
            }
        }

        shouldScanning = true

        super.onResume()
        createMLDetector()
    }

    private fun createMLDetector() {
        val options =
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()

        scanner = BarcodeScanning.getClient(options)
    }

    fun analyzePictureIfDetectorExists(bitmap: Bitmap) {
        if (decorationAspectRatio > 0 && cropHeightRatio > 0 && cropHeightRatio > 0 && shouldScanning) {
            val croppedBitmap = bitmap.crop(decorationAspectRatio, cropWidthRatio, cropHeightRatio)
            bitmap.recycle()

            runBarcodeScanner(croppedBitmap)
        }
    }

    private fun runBarcodeScanner(bitmap: Bitmap) {
        scanner?.run {
            val image = InputImage.fromBitmap(bitmap, 0)
            process(image)
                .addOnSuccessListener {
                    for (firebaseBarcode in it) {
                        firebaseBarcode.displayValue?.let {
                            val uuid = it.removePrefix("branch:")
                            shouldScanning = false

                            posViewModel.getMerchantUsingUUID(uuid)
                        }
                    }
                }
        }
    }

    private val cameraErrorCallback = object : CameraFragment.ErrorCallback {
        override fun onCameraNotExists() {
            if (!isDetached) {
                findNavController().navigateUp()
            }
        }

        override fun onUserDeniedPermissions() {
            if (!isDetached) {
                findNavController().navigateUp()
            }
        }

        override fun onUserShouldShowRationaleDialog() {
            if (!isDetached) {
                shoRationale = true
                view?.let {
                    with(viewBinding) {
                        gCamera.isVisible = false
                        btnSettings.isVisible = true
                        tvNoPermissionDescription.isVisible = true
                    }
                }
            }
        }
    }

    private val cameraPictureCallback = object : CameraFragment.PictureCallback {
        @Synchronized
        override fun analyzePicture(bitmap: Bitmap) {
            if (!isDetached) {
                analyzePictureIfDetectorExists(bitmap)
            }
        }
    }

    private fun openAppSettings() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${requireActivity().packageName}")
        ).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { startActivity(it) }
    }

    override val logTag: String = "PayWithPointsFragment"

    override val analyticsScreenName = "pos.pay_with_points"
}
