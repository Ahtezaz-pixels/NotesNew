package com.ahtezaz.notesapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ahtezaz.notesapp.databinding.ActivityMainBinding
import com.google.android.gms.ads.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "TAG"
    lateinit var binding: ActivityMainBinding
    lateinit var interstitialAd: InterstitialAd
    lateinit var adRequest: AdRequest
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: Call`")
        // initializing our mobile ads.
        loadSplashScreen()

        MobileAds.initialize(this)

        // on below line we are
        // initializing our ad request.
        adRequest = AdRequest.Builder().build()

        // on below line we are
        // initializing our interstitial ad.
        interstitialAd = InterstitialAd(this)

        // on below line we are setting ad
        // unit id for our interstitial ad.
        interstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"

        interstitialAd.loadAd(adRequest)



        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                startActivity(Intent(this@MainActivity, NotesListActivity::class.java))
                finish()
            }

            override fun onAdFailedToLoad(p0: LoadAdError?) {
                super.onAdFailedToLoad(p0)
                Toast.makeText(this@MainActivity , "Ad loading Failed",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@MainActivity, NotesListActivity::class.java))
                finish()
            }

            override fun onAdLoaded() {
                // on below line we are calling display
                // ad function to display interstitial ad.
                displayInterstitialAd(interstitialAd)
            }
        }


    }


    private fun loadSplashScreen() = CoroutineScope(Dispatchers.Main).launch {
        Log.d(TAG, "loadSplashScreen: In Notes")
        delay(4000)

    }

    private fun displayInterstitialAd(interstitialAd: InterstitialAd) {
        // on below line we are
        // checking if the ad is loaded
        if (interstitialAd.isLoaded) {
            // if the ad is loaded we are displaying
            // interstitial ad by calling show method.
            interstitialAd.show()
        }
    }

}