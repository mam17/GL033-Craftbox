package com.example.myapplication.ui.alertfull

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.example.myapplication.R
import com.example.myapplication.ui.splash.SplashActivity

class AlertFullScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_full_screen)
        supportActionBar?.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        findViewById<AppCompatButton>(R.id.btnTryAgain)?.setOnClickListener {
            FirebaseAnalytics.getInstance(this).logEvent("alert_full_screen_try_again_click", null)
            nextSplash()
        }
        findViewById<View>(R.id.vOpenSplash)?.setOnClickListener {
            FirebaseAnalytics.getInstance(this)
                .logEvent("alert_full_screen_open_splash_click", null)
            nextSplash()
        }
    }

    private fun nextSplash() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}