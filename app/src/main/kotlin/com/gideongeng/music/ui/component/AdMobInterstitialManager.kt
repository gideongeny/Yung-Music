/**
 * YungMusic AdMob Interstitial Manager
 * Manages interstitial ad loading and display
 */

package com.gideongeng.music.ui.component

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import timber.log.Timber

/**
 * Manages interstitial ads with frequency capping
 */
class AdMobInterstitialManager(
    private val context: Context,
    private val adUnitId: String
) {
    private var interstitialAd: InterstitialAd? = null
    private var lastAdShownTime: Long = 0
    private val minTimeBetweenAds = 5 * 60 * 1000L // 5 minutes in milliseconds

    init {
        loadAd()
    }

    /**
     * Load an interstitial ad
     */
    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Timber.d("Interstitial ad failed to load: ${adError.message}")
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Timber.d("Interstitial ad loaded successfully")
                    interstitialAd = ad
                    
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Timber.d("Interstitial ad dismissed")
                            interstitialAd = null
                            loadAd() // Load next ad
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Timber.d("Interstitial ad failed to show: ${adError.message}")
                            interstitialAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            Timber.d("Interstitial ad showed")
                        }
                    }
                }
            }
        )
    }

    /**
     * Show the interstitial ad if available and enough time has passed
     * 
     * @param activity The activity to show the ad in
     * @return true if ad was shown, false otherwise
     */
    fun showAdIfAvailable(activity: Activity): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Check if enough time has passed since last ad
        if (currentTime - lastAdShownTime < minTimeBetweenAds) {
            Timber.d("Not enough time passed since last ad")
            return false
        }

        return if (interstitialAd != null) {
            interstitialAd?.show(activity)
            lastAdShownTime = currentTime
            true
        } else {
            Timber.d("Interstitial ad not ready yet")
            loadAd() // Try to load if not available
            false
        }
    }

    /**
     * Check if ad is ready to show
     */
    fun isAdReady(): Boolean {
        val currentTime = System.currentTimeMillis()
        return interstitialAd != null && (currentTime - lastAdShownTime >= minTimeBetweenAds)
    }
}
