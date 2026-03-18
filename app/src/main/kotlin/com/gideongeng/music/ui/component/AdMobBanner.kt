/**
 * YungMusic AdMob Banner Component
 * Displays banner ads using Google AdMob
 */

package com.gideongeng.music.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * AdMob Banner Ad Component
 * 
 * @param adUnitId The AdMob ad unit ID (use test ID for testing)
 * @param modifier Modifier for the banner container
 */
@Composable
fun AdMobBanner(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
