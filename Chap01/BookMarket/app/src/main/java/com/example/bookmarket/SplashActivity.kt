package com.example.bookmarket

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var logoAnimation: AnimationDrawable
    private lateinit var walkAnimation: AnimationDrawable
    private var remainingMillis = 4200L
    private var resumedAt = 0L
    private var navigating = false

    private val openStartScreen = Runnable {
        if (!isFinishing && !isDestroyed && !navigating) {
            navigating = true
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        remainingMillis = savedInstanceState
            ?.getLong("remainingMillis", 4200L) ?: 4200L

        val root = findViewById<View>(R.id.splashRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        logoAnimation = findViewById<ImageView>(R.id.splashLogo)
            .drawable as AnimationDrawable
        walkAnimation = findViewById<ImageView>(R.id.splashWalker)
            .drawable as AnimationDrawable
    }

    override fun onResume() {
        super.onResume()
        resumedAt = SystemClock.elapsedRealtime()
        logoAnimation.start()
        walkAnimation.start()
        handler.postDelayed(openStartScreen, remainingMillis)
    }

    override fun onPause() {
        handler.removeCallbacks(openStartScreen)
        remainingMillis = (remainingMillis -
            (SystemClock.elapsedRealtime() - resumedAt)).coerceAtLeast(0L)
        resumedAt = 0L
        logoAnimation.stop()
        walkAnimation.stop()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val elapsed = if (resumedAt > 0L) {
            SystemClock.elapsedRealtime() - resumedAt
        } else {
            0L
        }
        outState.putLong(
            "remainingMillis",
            (remainingMillis - elapsed).coerceAtLeast(0L)
        )
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        handler.removeCallbacks(openStartScreen)
        super.onDestroy()
    }
}
