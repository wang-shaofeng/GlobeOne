/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

/**
 * This class is a generic replacement for empty custom view holders used in recycler views
 */
open class KtItemHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
    LayoutContainer
