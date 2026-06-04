package com.ozgeucar.citweety.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ozgeucar.citweety.domain.model.EmergencyContact
import java.util.Locale

class EmergencyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                EmergencyScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen() {
    val context = LocalContext.current

    // Compose State Yapıları: Konum bulundukça liste ve başlık anında güncellenecek
    var emergencyList by remember { mutableStateOf(getDefaultContacts()) }
    var locationTitle by remember { mutableStateOf("Acil Durum Rehberi") }

    // Modern Android'de İzin İsteme Yöntemi (Launcher)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocationAndContacts(context) { countryName, contacts ->
                locationTitle = "$countryName Acil Durum Rehberi 📍"
                emergencyList = contacts
            }
        } else {
            Toast.makeText(context, "Konum izni reddedildi. Varsayılan numaralar gösteriliyor.", Toast.LENGTH_SHORT).show()
        }
    }

    // Ekran açılır açılmaz bir kere çalışacak tetikleyici (Side-Effect)
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // İzin zaten verilmişse direkt konumu çek
            fetchLocationAndContacts(context) { countryName, contacts ->
                locationTitle = "$countryName Acil Durum Rehberi 📍"
                emergencyList = contacts
            }
        } else {
            // İzin yoksa kullanıcıdan izin iste
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(locationTitle) }, // Dinamik başlığımız
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(emergencyList) { contact ->
                EmergencyContactItem(contact = contact) { phoneNumber ->
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun EmergencyContactItem(contact: EmergencyContact, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { onClick(contact.phoneNumber) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Ara",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ====================================================================
// 🌍 KONUM VE VERİ YÖNETİMİ YARDIMCI FONKSİYONLARI 🌍
// ====================================================================

private fun fetchLocationAndContacts(context: Context, onResult: (String, List<EmergencyContact>) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    try {
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        if (location != null) {
            processLocation(context, location, onResult)
        } else {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, { loc ->
                processLocation(context, loc, onResult)
            }, null)
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

private fun processLocation(context: Context, location: Location, onResult: (String, List<EmergencyContact>) -> Unit) {
    // Geocoder ağır bir işlem olduğu için ana ekranı dondurmasın diye arka plana (Thread) alıyoruz
    Thread {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val countryCode = addresses[0].countryCode ?: "PL"
                val countryName = addresses[0].countryName ?: ""
                val contacts = getEmergencyContactsForCountry(countryCode)

                // Verileri bulduktan sonra sonuçları tekrar Compose (UI) ekranına yolluyoruz
                Handler(Looper.getMainLooper()).post {
                    onResult(countryName, contacts)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}

// Ülke koduna göre numaraları döndüren dinamik havuz
fun getEmergencyContactsForCountry(countryCode: String): List<EmergencyContact> {
    return when (countryCode.uppercase().trim()) {
        "PL" -> getDefaultContacts()
        "IT" -> listOf(
            EmergencyContact("Avrupa Acil Çağrı Merkezi", "112"),
            EmergencyContact("Polis (Carabinieri)", "112"),
            EmergencyContact("Ambulans (Emergenza Sanitaria)", "118"),
            EmergencyContact("Roma Umberto I Hastanesi", "+390649971"),
            EmergencyContact("T.C. Roma Büyükelçiliği", "+3906445941")
        )
        "DE" -> listOf(
            EmergencyContact("Avrupa Acil Çağrı Merkezi", "112"),
            EmergencyContact("Polis (Polizei)", "110"),
            EmergencyContact("İtfaiye ve Ambulans", "112"),
            EmergencyContact("Berlin Charité Hastanesi", "+493045050"),
            EmergencyContact("T.C. Berlin Büyükelçiliği", "+4930275850")
        )
        "FR" -> listOf(
            EmergencyContact("Avrupa Acil Çağrı Merkezi", "112"),
            EmergencyContact("Polis (Police Secours)", "17"),
            EmergencyContact("Ambulans (SAMU)", "15"),
            EmergencyContact("Paris Hôtel-Dieu Hastanesi", "+33142348234"),
            EmergencyContact("T.C. Paris Büyükelçiliği", "+33153927111")
        )
        "NL" -> listOf(
            EmergencyContact("Avrupa Acil Çağrı Merkezi", "112"),
            EmergencyContact("Polis (Politie)", "09008844"),
            EmergencyContact("Amsterdam UMC Hastanesi", "+31205669111"),
            EmergencyContact("T.C. Lahey Büyükelçiliği", "+31703023100")
        )
        "CZ" -> listOf(
            EmergencyContact("Avrupa Acil Çağrı Merkezi", "112"),
            EmergencyContact("Polis (Policie)", "158"),
            EmergencyContact("Ambulans (Záchranná služba)", "155"),
            EmergencyContact("Prag Motol Üniversite Hastanesi", "+420224431111"),
            EmergencyContact("T.C. Prag Büyükelçiliği", "+420224311401")
        )
        else -> getDefaultContacts() // Tanımlı olmayan bir Avrupa ülkesinde ise varsayılan listeyi verir
    }
}

// Konum bulunamazsa veya izin verilmezse gösterilecek varsayılan merkez numaraları
fun getDefaultContacts(): List<EmergencyContact> {
    return listOf(
        EmergencyContact("Avrupa Acil Çağrı Merkezi", "112"),
        EmergencyContact("Polis (Policja)", "997"),
        EmergencyContact("Ambulans (Pogotowie)", "999"),
        EmergencyContact("Zielona Góra Üniversite Hastanesi", "+48683296200"),
        EmergencyContact("T.C. Varşova Büyükelçiliği", "+48228546110")
    )
}