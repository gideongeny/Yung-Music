/**
 * YungMusic AdMob Interstitial Manager (FOSS Variant)
 * No-op implementation for the FOSS flavor.
 */

package com.gideongeng.music.ui.component

import android.app.Activity
import android.content.Context

/**
 * No-op interstitial manager for FOSS builds
 */
class AdMobInterstitialManager(
    context: Context,
    adUnitId: String,
    onFirstAdLoaded: () -> Unit = {}
) {
    fun showAdIfAvailable(activity: Activity, ignoreFrequency: Boolean = false): Boolean = false
    fun isAdReady(): Boolean = false
}
