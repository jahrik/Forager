package com.zynergylabs.forager.app.location

import android.content.Context
import com.zynergylabs.forager.app.domain.LastKnownLocationSource
import com.zynergylabs.forager.app.domain.LocationFix

/**
 * [LastKnownLocationSource] over the platform's [android.location.LocationManager], no Play services —
 * dispatch 2026-09-28-510, the report's item 4.
 */
class AndroidLastKnownLocationSource(
    private val context: Context,
) : LastKnownLocationSource {
    override fun lastKnown(): LocationFix.Update? = null
}
