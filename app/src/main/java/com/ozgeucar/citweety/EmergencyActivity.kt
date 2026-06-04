package com.ozgeucar.citweety

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class EmergencyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        val btnPolice = findViewById<Button>(R.id.btnCallPolice)
        val btnAmbulance = findViewById<Button>(R.id.btnCallAmbulance)
        val btnFire = findViewById<Button>(R.id.btnCallFire)

        // 112 Avrupa genelinde ortak acil durum numarasıdır
        btnPolice.setOnClickListener { makeCall("112") }
        btnAmbulance.setOnClickListener { makeCall("112") }
        btnFire.setOnClickListener { makeCall("112") }
    }

    private fun makeCall(number: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }
}