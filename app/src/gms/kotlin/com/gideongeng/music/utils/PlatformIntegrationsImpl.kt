package com.gideongeng.music.utils

import android.content.Context
import com.google.android.gms.ads.MobileAds
import timber.log.Timber

/**
 * GMS implementation of platform integrations
 */
class PlatformIntegrationsImpl : PlatformIntegrations {
    override fun initialize(context: Context) {
        Timber.d("Initializing GMS integrations (Ads, Analytics)")
        MobileAds.initialize(context) {}
        // Firebase is initialized automatically by the google-services plugin
    }

    override fun reportException(throwable: Throwable) {
        // Here we could report to Crashlytics if needed
        Timber.e(throwable, "Reporting exception to Firebase")
    }
}
