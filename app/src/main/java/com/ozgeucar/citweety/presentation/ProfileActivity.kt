package com.ozgeucar.citweety.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ozgeucar.citweety.R

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // 🧠 Firebase'den mevcut kullanıcının E-postasını çekiyoruz
        val currentUser = auth.currentUser
        if (currentUser != null) {
            tvEmail.text = currentUser.email ?: "Email bulunamadı"

            // Eğer giriş yaparken isim (DisplayName) kaydettirdiysen onu çeker, yoksa varsayılan metin kalır
            if (!currentUser.displayName.isNullOrEmpty()) {
                tvName.text = currentUser.displayName
            }
        }

        // 🚪 ÇIKIŞ YAP (LOGOUT) İŞLEMİ
        btnLogout.setOnClickListener {
            // 1. Firebase oturumunu tamamen kapat
            auth.signOut()
            Toast.makeText(this, "Başarıyla çıkış yapıldı! 👋", Toast.LENGTH_SHORT).show()

            // 2. Kullanıcıyı Login sayfasına gönder ve ARKADAKİ TÜM SAYFALARI (Geçmişi) SİL!
            // NOT: Eğer giriş sayfanın adı farklıysa (örneğin SignInActivity), aşağıdaki LoginActivity yazısını ona göre değiştir.
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}