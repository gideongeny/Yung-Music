package com.gideongeng.music.utils

/**
 * Factory to provide the correct PlatformIntegrations for the current build flavor.
 */
object IntegrationsProvider {
    val integrations: PlatformIntegrations by lazy {
        // The specific class name will be same in both folders, 
        // but Gradle will only pick the one for the active flavor.
        // Wait, I named them differently in the filenames above.
        // Let's keep them as 'PlatformIntegrationsImpl' in both folders.
        PlatformIntegrationsImpl()
    }
}
