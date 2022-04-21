/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.os.Build
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import ph.com.globe.globeonesuperapp.R

fun Fragment.setStatusBarColor(colorId: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        // for API 21-23 statusBar color will be the value
        // of 'colorPrimaryDark' attr of Material theme
        return
    }

    val window = requireActivity().window

    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    val color = ResourcesCompat.getColor(resources, colorId, requireActivity().theme)
    window.statusBarColor = color

    // make icons dark if the background is lighter than chosen boundary color and vice-versa
    val boundaryColor =
        ResourcesCompat.getColor(resources, R.color.dashboard_morning, requireActivity().theme)

    setStatusBarIconsColor(color.lightness() > boundaryColor.lightness())
}

/**
 * @fun [lightness] calculates luma of the rgb color
 * in Int form based on the CCIR 601 standard formula
 */
private fun Int.lightness(): Float {
    // R, G and B input range = 0 - 255
    // L output range = 0 - 1.0
    val r = this shr 16 and 0xFF
    val g = this shr 8 and 0xFF
    val b = this and 0xFF

    val valR = (r / 255f)
    val valG = (g / 255f)
    val valB = (b / 255f)

    return 0.299f * valR + 0.587f * valG + 0.114f * valB
}

/**
 * adapts statusBar icons color to statusBar background so its icons
 * can be easily noticed.
 * @param light: specifies whether statusBar background is light, in order
 * to make its icons dark and vice-versa
 */
fun Fragment.setStatusBarIconsColor(light: Boolean) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        // for API 21-23 statusBar icons color will be white
        // by default as per Android guidelines
        return
    }

    // requires API >= 23

    val window = requireActivity().window

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        // deprecated in API 30
        var flags = window.decorView.systemUiVisibility
        flags = if (light) {
            flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        window.decorView.systemUiVisibility = flags
    } else {
        // requires API 30
        if (light) {
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                APPEARANCE_LIGHT_STATUS_BARS,
                APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                0,
                APPEARANCE_LIGHT_STATUS_BARS
            )
        }
    }
}

fun Fragment.setWhiteStatusBar() {
    setStatusBarColor(R.color.absolute_white)
}

fun Fragment.setLightStatusBar() {
    setStatusBarColor(R.color.accent_light)
}

fun Fragment.setDarkStatusBar() {
    setStatusBarColor(R.color.corporate_A_800)
}

fun Fragment.setMorningStatusBar() {
    setStatusBarColor(R.color.dashboard_morning)
}

fun Fragment.setDaytimeStatusBar() {
    setStatusBarColor(R.color.dashboard_day)
}

fun Fragment.setSunsetStatusBar() {
    setStatusBarColor(R.color.dashboard_sunset)
}

fun Fragment.setNightStatusBar() {
    setStatusBarColor(R.color.dashboard_night)
}
