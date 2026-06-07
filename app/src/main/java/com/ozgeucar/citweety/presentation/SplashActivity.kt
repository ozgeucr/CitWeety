package com.ozgeucar.citweety.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.ozgeucar.citweety.MainActivity
import com.ozgeucar.citweety.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ sistem splash screen'ini yükle
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 1.5 saniye bekle ve yönlendir
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, 1500)
    }

    private fun checkUserStatus() {
        val auth = FirebaseAuth.getInstance()
        val destination = if (auth.currentUser != null) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }

        startActivity(Intent(this, destination))
        finish()
        
        // Geçiş animasyonu (opsiyonel, daha yumuşak bir geçiş için)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}