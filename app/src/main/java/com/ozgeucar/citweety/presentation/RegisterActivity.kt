package com.ozgeucar.citweety.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.ozgeucar.citweety.MainActivity
import com.ozgeucar.citweety.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 🛡️ Emülatör ağ sorunları için test modunu açık tutuyoruz (Spam hatasını engeller)
        auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)

        val etFullName = findViewById<EditText>(R.id.etRegisterFullName)
        val etUsername = findViewById<EditText>(R.id.etRegisterUsername)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etAge = findViewById<EditText>(R.id.etRegisterAge)
        val etHometown = findViewById<EditText>(R.id.etRegisterHometown)
        val spinnerGender = findViewById<Spinner>(R.id.spinnerGender)
        val btnRegisterSubmit = findViewById<Button>(R.id.btnRegisterSubmit)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        val genderOptions = arrayOf("Cinsiyet Seçiniz", "Kadın", "Erkek", "Belirtmek İstemiyorum")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        spinnerGender.adapter = adapter

        btnRegisterSubmit.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val ageStr = etAge.text.toString().trim()
            val hometown = etHometown.text.toString().trim()
            val gender = spinnerGender.selectedItem.toString()

            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || ageStr.isEmpty() || hometown.isEmpty() || gender == "Cinsiyet Seçiniz") {
                Toast.makeText(this, "Lütfen tüm alanları eksiksiz doldurun canım!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalıdır!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnRegisterSubmit.isEnabled = false
            val originalText = btnRegisterSubmit.text
            btnRegisterSubmit.text = "Kontrol ediliyor... ⏳"

            // 🕵️‍♀️ 1. ADIM: KULLANICI ADI BENZERSİZLİK KONTROLÜ
            firestore.collection("users").whereEqualTo("username", username).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Eğer bu kullanıcı adını içeren bir belge varsa:
                        Toast.makeText(this, "Bu kullanıcı adı zaten alınmış, lütfen başka bir tane dene! 🕵️‍♀️", Toast.LENGTH_LONG).show()
                        btnRegisterSubmit.isEnabled = true
                        btnRegisterSubmit.text = originalText
                    } else {
                        // Kullanıcı adı müsait! Şimdi normal kayda devam edebiliriz.
                        btnRegisterSubmit.text = "Hesap Oluşturuluyor... ⏳"

                        // 2. ADIM: FİREBASE AUTH İLE KAYIT
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener { authResult ->
                                val user = authResult.user
                                val uid = user?.uid

                                // İsmi doğrudan profile gömüyoruz
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build()

                                user?.updateProfile(profileUpdates)?.addOnCompleteListener {

                                    // 3. ADIM: TÜM DETAYLARI FIRESTORE'A KAYDETME
                                    val userMap = hashMapOf(
                                        "fullName" to fullName,
                                        "username" to username,
                                        "email" to email,
                                        "age" to ageStr.toInt(),
                                        "gender" to gender,
                                        "hometown" to hometown
                                    )

                                    if (uid != null) {
                                        firestore.collection("users").document(uid).set(userMap)
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "CitWety'ye hoş geldin $fullName! 🎉", Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this, MainActivity::class.java))
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "Veritabanı hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                                btnRegisterSubmit.isEnabled = true
                                                btnRegisterSubmit.text = originalText
                                            }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                btnRegisterSubmit.isEnabled = true
                                btnRegisterSubmit.text = originalText

                                if (e is FirebaseAuthUserCollisionException) {
                                    Toast.makeText(this, "Bu e-posta zaten kayıtlı canım! Direkt 'Giriş Yap' kısmına geçebilirsin. 🥰", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(this, "Kayıt hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    btnRegisterSubmit.isEnabled = true
                    btnRegisterSubmit.text = originalText
                    Toast.makeText(this, "Bağlantı hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }

        tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}