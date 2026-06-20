package com.gideongeng.music.utils

import android.content.Context
import timber.log.Timber

/**
 * FOSS implementation of platform integrations (No-op)
 */
class PlatformIntegrationsImpl : PlatformIntegrations {
    override fun initialize(context: Context) {
        Timber.d("FOSS build: Skipping GMS integrations")
    }

    override fun reportException(throwable: Throwable) {
        Timber.e(throwable, "FOSS build exception (no remote reporting)")
    }
}
