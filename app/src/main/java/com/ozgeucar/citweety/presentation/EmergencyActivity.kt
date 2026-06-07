package com.ozgeucar.citweety.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ozgeucar.citweety.R
import com.ozgeucar.citweety.domain.model.EmergencyContact
import java.util.Locale

class EmergencyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { EmergencyScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen() {
    val context = LocalContext.current
    var emergencyList by remember { mutableStateOf(getDefaultContacts()) }
    var locationTitle by remember { mutableStateOf(context.getString(R.string.emergency_default_title)) }
    val permissionDeniedMsg = stringResource(R.string.emergency_permission_denied)

    val locationPermissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocationAndContacts(context) { countryName, contacts ->
                locationTitle = context.getString(R.string.emergency_title_format, countryName)
                emergencyList = contacts
            }
        } else {
            Toast.makeText(context, permissionDeniedMsg, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndContacts(context) { countryName, contacts ->
                locationTitle = context.getString(R.string.emergency_title_format, countryName)
                emergencyList = contacts
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(locationTitle) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.errorContainer, titleContentColor = MaterialTheme.colorScheme.onErrorContainer)) }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(emergencyList) { contact ->
                EmergencyContactItem(contact = contact) { phoneNumber ->
                    context.startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phoneNumber") })
                }
            }
        }
    }
}

@Composable
fun EmergencyContactItem(contact: EmergencyContact, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = LocalIndication.current) { onClick(contact.phoneNumber) },
        shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = contact.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = contact.phoneNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Icon(imageVector = Icons.Default.Call, contentDescription = stringResource(R.string.emergency_call_desc), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun fetchLocationAndContacts(context: Context, onResult: (String, List<EmergencyContact>) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    try {
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location != null) processLocation(context, location, onResult)
        else locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, { loc -> processLocation(context, loc, onResult) }, null)
    } catch (e: SecurityException) { e.printStackTrace() }
}

private fun processLocation(context: Context, location: Location, onResult: (String, List<EmergencyContact>) -> Unit) {
    Thread {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val countryCode = addresses[0].countryCode ?: "PL"
                val countryName = addresses[0].countryName ?: ""
                val contacts = getEmergencyContactsForCountry(countryCode)
                Handler(Looper.getMainLooper()).post { onResult(countryName, contacts) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }.start()
}

fun getEmergencyContactsForCountry(countryCode: String): List<EmergencyContact> = getDefaultContacts()
fun getDefaultContacts(): List<EmergencyContact> = listOf(EmergencyContact("Avrupa Acil Çağrı Merkezi", "112"))