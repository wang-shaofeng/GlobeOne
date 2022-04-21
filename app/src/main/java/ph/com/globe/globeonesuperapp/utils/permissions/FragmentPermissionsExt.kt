/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.permissions

import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ph.com.globe.globeonesuperapp.R

/**
 * Extension function with a purpose to simplify logic based on having permission to use certain resources.
 * MUST BE CALLED IN FRAGMENT'S onCreate METHOD!
 * @param [permissionType] Permission type we base our logic on
 * @param [rationaleTitle] Rationale dialog title
 * @param [rationaleMessage] Rationale dialog message
 * @param [whenGranted] Lambda to be invoked in case user grants the permission
 * @param [whenDenied] Lambda to be invoked in case user denies the permission
 * */
fun Fragment.requestPermission(
    permissionType: String,
    @StringRes rationaleTitle: Int? = null,
    @StringRes rationaleMessage: Int? = null,
    whenGranted: () -> Unit,
    whenDenied: () -> Unit,
) {
    val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                whenGranted.invoke()
            } else {
                whenDenied.invoke()
            }
        }
    when {
        ContextCompat.checkSelfPermission(
            requireContext(),
            permissionType
        ) == PermissionChecker.PERMISSION_GRANTED -> {
            whenGranted.invoke()
        }
        shouldShowRequestPermissionRationale(permissionType) -> {
            if (rationaleTitle != null && rationaleMessage != null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(rationaleTitle)
                    .setMessage(rationaleMessage)
                    .setPositiveButton(R.string.rationale_button_allow) { _, _ ->
                        requestPermissions.launch(permissionType)
                    }
                    .setNegativeButton(R.string.rationale_button_deny) { _, _ ->
                        whenDenied.invoke()
                    }
                    .show()
            }
        }
        else -> {
            requestPermissions.launch(permissionType)
        }
    }
}

/**
 * Extension function with a purpose to register for the activity result when requesting [Manifest.permission.WRITE_EXTERNAL_STORAGE] permissions.
 * MUST BE CALLED IN FRAGMENT'S onCreate METHOD!
 * @param [whenGranted] Lambda to be invoked in case user grants the permission
 *
 * @return [ActivityResultLauncher<String>] if the app registered the [ActivityResultLauncher]
 * @return @null if the app already has [Manifest.permission.WRITE_EXTERNAL_STORAGE] permissions
 * */
fun Fragment.registerActivityResultForStoragePermission(
    whenGranted: () -> Unit = {}
): ActivityResultLauncher<String>? {
    return if (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PermissionChecker.PERMISSION_GRANTED
    )
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                whenGranted.invoke()
            }
        }
    else
        null
}

/**
 * Extension function that launches [requestStorageActivityLauncher] requesting the permissions once again if the user allows it.
 * Should be called in View.onClick() method with [requestStorageActivityLauncher] instance returned from [Fragment.registerActivityResultForStoragePermission]
 *
 * @param [requestStorageActivityLauncher] ActivityResultLauncher returned from [Fragment.registerActivityResultForStoragePermission]
 *
 * */
fun Fragment.requestStoragePermissionsIfNeededAndPerformSuccessAction(
    requestStorageActivityLauncher: ActivityResultLauncher<String>
) {
    when {
        shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.rationale_title))
                .setMessage(getString(R.string.storage_rationale_message))
                .setPositiveButton(R.string.rationale_button_allow) { _, _ ->
                    requestStorageActivityLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                .setNegativeButton(R.string.rationale_button_deny) { _, _ ->
                }
                .show()
        }
        else -> {
            requestStorageActivityLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}
