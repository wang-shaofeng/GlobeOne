/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.maintenance

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.model.maintenance.MaintenanceUIModel

class MaintenanceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val tvMaintenanceTitle: TextView
    private val tvMaintenanceDescription: TextView
    private val ivMaintenance: ImageView

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MaintenanceView,
            defStyleAttr,
            defStyleRes
        ).apply {
            val layoutResId = getResourceId(
                R.styleable.MaintenanceView_layout,
                R.layout.maintenance_view_layout
            )
            val rootView =
                LayoutInflater.from(context).inflate(layoutResId, this@MaintenanceView, true)
            tvMaintenanceTitle = rootView.findViewById(R.id.tv_maintenance_title)
            tvMaintenanceDescription = rootView.findViewById(R.id.tv_maintenance_description)
            ivMaintenance = rootView.findViewById(R.id.iv_maintenance)
        }
    }

    fun setMaintenance(maintenanceUIModel: MaintenanceUIModel) {
        with(maintenanceUIModel) {
            isVisible = hasMaintenance
            if (hasMaintenance) {
                tvMaintenanceTitle.text = title
                tvMaintenanceDescription.text = content
                GlobeGlide.with(ivMaintenance).load(imageUrl).into(ivMaintenance)
            }
        }
    }
}
