/**
 * YungMusic AdMob Banner Component (FOSS Variant)
 * No-op (empty) implementation for the FOSS flavor.
 */

package com.gideongeng.music.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Empty banner placeholder for FOSS builds
 */
@Composable
fun AdMobBanner(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    // No-op for FOSS
    Spacer(modifier = modifier)
}
