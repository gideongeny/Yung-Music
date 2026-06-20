package com.gideongeng.music.utils

import android.content.Context

/**
 * Interface for platform-specific integrations (Ads, Analytics, etc.)
 */
interface PlatformIntegrations {
    fun initialize(context: Context)
    fun reportException(throwable: Throwable)
}
