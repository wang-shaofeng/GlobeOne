/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

fun loadAssetTextAsString(context: Context, name: String): String? {
    var input: BufferedReader? = null
    try {
        val buf = StringBuilder()
        val inputStream = context.assets.open("hpw_scripts/$name")
        input = BufferedReader(InputStreamReader(inputStream))
        var str: String?
        var isFirst = true
        while (input.readLine().also { str = it } != null) {
            if (isFirst) isFirst = false else buf.append('\n')
            buf.append(str)
        }
        return buf.toString()
    } catch (e: Exception) {
        return null
    } finally {
        if (input != null) {
            try {
                input.close()
            } catch (e: IOException) {

            }
        }
    }
}
