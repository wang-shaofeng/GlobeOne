/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.pos.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ph.com.globe.analytics.logger.eLog
import ph.com.globe.globeonesuperapp.databinding.CameraFragmentBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.ViewBindingFragment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraFragment :
    ViewBindingFragment<CameraFragmentBinding>({ CameraFragmentBinding.inflate(it) }, null) {

    private var errorCallback: ErrorCallback? = null
    private var pictureCallback: PictureCallback? = null

    private var previewUseCase: Preview? = null
    private var imageAnalyzerUseCase: ImageAnalysis? = null
    private var camera: Camera? = null

    private var permissionsDialogRunning: Boolean = false
    private var rationaleDialogRunning: Boolean = false
    private var shouldShowRationaleDialog: Boolean = false

    private lateinit var analyzerExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            permissionsDialogRunning = it.getBoolean(STATE_PERMISSION_DIALOG_RUNNING, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        analyzerExecutor = Executors.newSingleThreadExecutor()
        shouldShowRationaleDialog = !shouldShowRequestPermissionRationale()
    }

    override fun onDestroyView() {
        analyzerExecutor.shutdown()
        ProcessCameraProvider.getInstance(requireContext()).get().unbindAll()
        super.onDestroyView()
    }

    override fun onDetach() {
        errorCallback = null
        pictureCallback = null
        super.onDetach()
    }

    override fun onPause() {
        imageAnalyzerUseCase?.clearAnalyzer()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_PERMISSION_DIALOG_RUNNING, permissionsDialogRunning)
    }

    override fun onResume() {
        super.onResume()
        when {
            !checkCameraExists() -> errorCallback?.onCameraNotExists()
            permissionsDialogRunning || rationaleDialogRunning -> Unit
            !checkPermission() -> requestNecessaryPermissions()
            else -> viewBinding.pvQrcode.post { updateCameraUi() }
        }

        rationaleDialogRunning = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val isClickedDoNotAskAgain = !shouldShowRequestPermissionRationale()

        when {
            checkPermission() -> updateCameraUi()
            !shouldShowRationaleDialog && isClickedDoNotAskAgain -> errorCallback?.onUserDeniedPermissions()
            shouldShowRationaleDialog && isClickedDoNotAskAgain -> {
                errorCallback?.onUserShouldShowRationaleDialog()
                shouldShowRationaleDialog = isClickedDoNotAskAgain
                rationaleDialogRunning = true
            }
            else -> errorCallback?.onUserDeniedPermissions()
        }

        permissionsDialogRunning = false
    }

    private fun checkPermission(): Boolean {
        var granted = true
        for (permission in CAMERA_PERMISSION_ARRAY) {
            granted = granted && (ActivityCompat.checkSelfPermission(requireContext(), permission)
                    == PackageManager.PERMISSION_GRANTED)
        }
        return granted
    }

    private fun requestNecessaryPermissions() {
        permissionsDialogRunning = true
        requestPermissions(
            CAMERA_PERMISSION_ARRAY,
            CAMERA_PERMISSION_CODE
        )
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        var shouldShow = true
        for (permission in CAMERA_PERMISSION_ARRAY) {
            shouldShow = shouldShow && shouldShowRequestPermissionRationale(permission)
        }
        return shouldShow
    }

    private fun updateCameraUi() {
        if (view != null) {
            val metrics = DisplayMetrics().also { viewBinding.pvQrcode.display.getRealMetrics(it) }

            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

            val rotation = viewBinding.pvQrcode.display.rotation

            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraProviderFuture.addListener(Runnable {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                previewUseCase = Preview.Builder()
                    .setTargetAspectRatio(screenAspectRatio)
                    .setTargetRotation(rotation)
                    .build()

                imageAnalyzerUseCase = ImageAnalysis.Builder()
                    .setTargetAspectRatio(screenAspectRatio)
                    .setTargetRotation(rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(analyzerExecutor, imageAnalyzer) }

                cameraProvider.unbindAll()

                try {
                    camera =
                        cameraProvider.bindToLifecycle(
                            viewLifecycleOwner,
                            cameraSelector,
                            previewUseCase,
                            imageAnalyzerUseCase
                        )

                    viewBinding.pvQrcode.preferredImplementationMode =
                        PreviewView.ImplementationMode.TEXTURE_VIEW
                    previewUseCase?.setSurfaceProvider(
                        viewBinding.pvQrcode.createSurfaceProvider(
                            camera?.cameraInfo
                        )
                    )
                } catch (exc: Exception) {
                    eLog(exc)
                }

            }, ContextCompat.getMainExecutor(requireContext()))
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun checkCameraExists(): Boolean {
        val packageManager = requireActivity().packageManager
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    /**
     * [ImageAnalyzer] is class which is used to convert image from YUV_420_888 format to bitmap,
     * and call callback with it.
     */
    private val imageAnalyzer = ImageAnalyzer { image -> pictureCallback?.analyzePicture(image) }

    fun setPictureCallback(pictureCallback: PictureCallback) {
        this.pictureCallback = pictureCallback
    }

    fun setErrorCallback(errorCallback: ErrorCallback) {
        this.errorCallback = errorCallback
    }

    companion object {
        val CAMERA_PERMISSION_ARRAY = arrayOf(
            Manifest.permission.CAMERA
        )

        const val CAMERA_PERMISSION_CODE = 2

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        const val STATE_PERMISSION_DIALOG_RUNNING = "permission_dialog_running"

    }

    interface ErrorCallback {
        fun onCameraNotExists()
        fun onUserDeniedPermissions()
        fun onUserShouldShowRationaleDialog()
    }

    interface PictureCallback {
        fun analyzePicture(bitmap: Bitmap)
    }

    override val logTag: String = "CameraFragment"
}
