package com.ozgeucar.citweety.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.google.firebase.auth.FirebaseAuth
import com.ozgeucar.citweety.R

class ProfileActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val sharedPref = getSharedPreferences("CitWeety_Settings", Context.MODE_PRIVATE)
        val isDarkSaved = sharedPref.getBoolean("dark_mode", false)

        setContent {
            var isDarkMode by remember { mutableStateOf(isDarkSaved) }
            val colors = if (isDarkMode) darkColorScheme() else lightColorScheme()

            MaterialTheme(colorScheme = colors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen(
                        auth = auth,
                        isDarkMode = isDarkMode,
                        onThemeChange = { checked ->
                            // Sadece Compose temasını değiştiriyoruz, sistemi çökertmemek için global komutu kaldırdık.
                            isDarkMode = checked
                            sharedPref.edit().putBoolean("dark_mode", checked).apply()
                        },
                        onLanguageChange = { langCode ->
                            // Ana ekrana atma (restartApp) komutu tamamen silindi!
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
                        },
                        onLogoutClick = {
                            auth.signOut()
                            Toast.makeText(this@ProfileActivity, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ProfileActivity, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    auth: FirebaseAuth,
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val currentUser = auth.currentUser
    val email = currentUser?.email ?: stringResource(R.string.email_not_found)
    val name = currentUser?.displayName?.takeIf { it.isNotEmpty() } ?: "Erasmus Gezgini"

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.profile_settings_title), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Person, contentDescription = "Profil", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 🌙 GECE MODU ŞALTERİ
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, contentDescription = "Gece Modu", tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.dark_mode_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Switch(checked = isDarkMode, onCheckedChange = { onThemeChange(it) })
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🌍 DİL BUTONLARI
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Default.Language, contentDescription = "Dil", tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.app_language_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { onLanguageChange("en") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("🇬🇧 English") }
            Button(onClick = { onLanguageChange("tr") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("🇹🇷 Türkçe") }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Çıkış", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.logout_button), fontSize = 16.sp, color = Color.White)
        }
    }
}