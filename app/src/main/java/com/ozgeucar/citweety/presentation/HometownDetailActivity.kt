package com.ozgeucar.citweety.presentation

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ozgeucar.citweety.R
import com.ozgeucar.citweety.data.local.AppDatabase
import com.ozgeucar.citweety.data.CityMapData
import com.ozgeucar.citweety.data.CityRepository
import com.ozgeucar.citweety.data.PlaceEntity
import com.ozgeucar.citweety.domain.model.Place

// --- YENİ EKLENEN COMPOSE VE LOKASYON İMPORTLARI ---
import androidx.compose.ui.platform.ComposeView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import android.net.Uri
import android.content.Intent
import com.ozgeucar.citweety.domain.model.LocalFoodSpot
import com.ozgeucar.citweety.presentation.components.LocalDelicacySection

class HometownDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var activeCityData: CityMapData? = null
    private lateinit var cityName: String
    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hometown_detail)

        database = AppDatabase.getDatabase(this)

        // Intent adını değiştirmedik, mevcut yapını koruduk
        cityName = intent.getStringExtra("HOMETOWN_NAME") ?: "Zielona Góra"
        findViewById<TextView>(R.id.tvHometownDetailName).text = cityName

        val tvLabel = findViewById<TextView>(R.id.tvHometownDetailLabel)
        if (!cityName.equals("Zielona Góra", ignoreCase = true)) {
            tvLabel.text = "Planlanan Keşif ve Rota Detayları ✈️"
        } else {
            tvLabel.text = "Mevcut Durduğun Şehir Detayları 🎓"
        }

        activeCityData = CityRepository.getMapDataByCity(cityName)

        // Eğer şehir havuzda yoksa okyanusa gitmesin diye varsayılan konum atıyoruz
        if (activeCityData == null) {
            val (defaultLat, defaultLng) = when (cityName.lowercase().trim()) {
                "amsterdam" -> Pair(52.3676, 4.9041)
                "roma" -> Pair(41.8902, 12.4922)
                "floransa" -> Pair(43.7731, 11.2560)
                "prag" -> Pair(50.0865, 14.4114)
                "brüksel" -> Pair(50.8467, 4.3524)
                "paris" -> Pair(48.8566, 2.3522)
                else -> Pair(51.9355, 15.5062)
            }
            activeCityData = CityMapData(
                cityName = cityName,
                centerLatitude = defaultLat,
                centerLongitude = defaultLng,
                zoomLevel = 12f,
                places = mutableListOf(),
                allDiscoverablePlaces = emptyList()
            )
        }

        // 🗄️ Verileri Room'dan arka planda yükle
        loadPlacesFromRoom()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.hometownMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val rvPlaces = findViewById<RecyclerView>(R.id.rvHometownPlaces)
        rvPlaces.layoutManager = LinearLayoutManager(this)

        placeAdapter = PlaceAdapter(activeCityData!!.places) {
            refreshMapMarkers()

            // 🎯 SİHİRLİ DOKUNUŞ 1: Döngüsel veri tabanı güncellemesini arka plana (Thread) alıyoruz!
            Thread {
                activeCityData!!.places.forEach { place ->
                    database.placeDao().updatePlace(PlaceEntity.fromPlace(place, cityName))
                }
            }.start()
        }
        rvPlaces.adapter = placeAdapter

        findViewById<View>(R.id.btnAddPlace).setOnClickListener {
            showSearchablePlaceDialog()
        }

        // ====================================================================
        // 🍔 YEREL LEZZET DURAKLARI COMPOSE ENTEGRASYONU 🍔
        // ====================================================================
        val composeView = findViewById<ComposeView>(R.id.compose_view_local_food)
        composeView.setContent {
            // Şehre göre devasa lezzet listemizden veriyi çekiyoruz
            val initialFoodSpots = getFoodSpotsForCity(cityName)

            if (initialFoodSpots.isNotEmpty()) {
                // Anlık kalp tıklamalarını algılamak için listeyi State'e dönüştürüyoruz
                val foodSpotsState = remember { mutableStateListOf(*initialFoodSpots.toTypedArray()) }

                MaterialTheme {
                    LocalDelicacySection(
                        foodSpots = foodSpotsState,
                        onNavigateToMap = { address ->
                            // Haritada Gör butonuna basılınca Google Haritalar'ı açan Intent
                            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try {
                                startActivity(mapIntent)
                            } catch (e: Exception) {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}"))
                                startActivity(browserIntent)
                            }
                        },
                        onFavoriteClick = { tiklananYemek ->
                            val index = foodSpotsState.indexOf(tiklananYemek)
                            if (index != -1) {
                                // Kalbi doldur/boşalt
                                val yeniDurum = !tiklananYemek.isFavorite
                                foodSpotsState[index] = tiklananYemek.copy(isFavorite = yeniDurum)

                                if (yeniDurum) {
                                    Toast.makeText(this@HometownDetailActivity, "${tiklananYemek.foodName} haritaya ekleniyor... 📍", Toast.LENGTH_SHORT).show()

                                    // SİHİRLİ DOKUNUŞ: Metin adresini (String) GPS koordinatına (LatLng) çeviriyoruz
                                    Thread {
                                        try {
                                            val geocoder = android.location.Geocoder(this@HometownDetailActivity)
                                            val addressList = geocoder.getFromLocationName(tiklananYemek.address, 1)

                                            if (!addressList.isNullOrEmpty()) {
                                                val location = LatLng(addressList[0].latitude, addressList[0].longitude)

                                                runOnUiThread {
                                                    // Haritaya MAVİ renkli özel bir lezzet pini ekliyoruz
                                                    googleMap.addMarker(
                                                        MarkerOptions()
                                                            .position(location)
                                                            .title("😋 ${tiklananYemek.foodName}")
                                                            .snippet(tiklananYemek.placeName)
                                                            // Lezzet durakları kırmızı olan turistik yerlerden ayrılsın diye Mavi (AZURE) ikon yaptık
                                                            .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE))
                                                    )
                                                    // Kamerayı havalı bir animasyonla o yemeğe doğru yaklaştırıyoruz
                                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                                                }
                                            } else {
                                                runOnUiThread {
                                                    Toast.makeText(this@HometownDetailActivity, "Adresin tam konumu bulunamadı :(", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }.start()
                                } else {
                                    // Favori iptal edilirse, haritayı sıfırlayıp sadece kırmızı turistik yerleri geri getiriyoruz
                                    refreshMapMarkers()
                                }
                            }
                        }
                    )
                }
            }
        }
        // ====================================================================
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        refreshMapMarkers()
    }

    private fun refreshMapMarkers() {
        if (!::googleMap.isInitialized || activeCityData == null) return
        googleMap.clear()

        val cityCenter = LatLng(activeCityData!!.centerLatitude, activeCityData!!.centerLongitude)

        for (place in activeCityData!!.places) {
            val loc = LatLng(place.latitude, place.longitude)
            googleMap.addMarker(
                MarkerOptions().position(loc).title(place.name).snippet(place.description)
            )
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityCenter, activeCityData!!.zoomLevel))
    }

    // 🗄️ ROOM: Veri tabanı okumasını asenkron hale getiriyoruz
    private fun loadPlacesFromRoom() {
        if (activeCityData == null) return

        Thread {
            val savedEntities = database.placeDao().getPlacesByCity(cityName.lowercase().trim())
            if (savedEntities.isNotEmpty()) {
                runOnUiThread {
                    activeCityData!!.places.clear()
                    activeCityData!!.places.addAll(savedEntities.map { it.toPlace() })
                    if (::placeAdapter.isInitialized) {
                        placeAdapter.updateData(activeCityData!!.places)
                    }
                    refreshMapMarkers()
                }
            }
        }.start()
    }

    private fun showSearchablePlaceDialog() {
        if (activeCityData == null) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_place_search, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etPlaceSearch)
        val rvDialogPlaces = dialogView.findViewById<RecyclerView>(R.id.rvDialogPlaces)

        val filteredPlaces = mutableListOf<Place>().apply { addAll(activeCityData!!.allDiscoverablePlaces) }
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()

        val dialogAdapter = CompactPlaceAdapter(filteredPlaces) { selectedPlace ->
            if (!activeCityData!!.places.any { it.id == selectedPlace.id }) {
                activeCityData!!.places.add(selectedPlace)

                Thread {
                    database.placeDao().insertPlace(PlaceEntity.fromPlace(selectedPlace, cityName))
                }.start()

                placeAdapter.updateData(activeCityData!!.places)
                refreshMapMarkers()

                Toast.makeText(this, "${selectedPlace.name} rotana işlendi! 📍", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bu mekan zaten listende ekli canım!", Toast.LENGTH_SHORT).show()
            }
            alertDialog.dismiss()
        }

        rvDialogPlaces.layoutManager = LinearLayoutManager(this)
        rvDialogPlaces.adapter = dialogAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                filteredPlaces.clear()

                if (query.isEmpty()) {
                    filteredPlaces.addAll(activeCityData!!.allDiscoverablePlaces)
                } else {
                    val matches = activeCityData!!.allDiscoverablePlaces.filter {
                        it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
                    }
                    filteredPlaces.addAll(matches)
                }
                dialogAdapter.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        alertDialog.show()
    }

    private class CompactPlaceAdapter(
        private val places: List<Place>,
        private val onPlaceSelected: (Place) -> Unit
    ) : RecyclerView.Adapter<CompactPlaceAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvPlaceName)
            val tvDesc: TextView = view.findViewById(R.id.tvPlaceDesc)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val place = places[position]
            holder.tvName.text = place.name
            holder.tvDesc.text = "${place.category} - ${place.description}"
            holder.itemView.setOnClickListener { onPlaceSelected(place) }
        }

        override fun getItemCount(): Int = places.size
    }
}

// 🍔 TÜM ŞEHİRLER İÇİN DEVASA LEZZET REHBERİ 🍔
fun getFoodSpotsForCity(cityName: String): List<LocalFoodSpot> {
    return when (cityName.trim().lowercase()) {

        "prag" -> listOf(
            LocalFoodSpot("1", "Trdelník (Baca Tatlısı)", "Good Food Coffee & Bakery", "Kömür ateşinde pişen meşhur tarçınlı rulo tatlı", listOf("Hamur", "Tarçın", "Şeker"), "$", "Akşamüstü", "Karlova 160/8, Prag", 4.6),
            LocalFoodSpot("2", "Svíčková (Et Yemeği)", "Kantýna", "Geleneksel Çek sığır eti ve özel krema sosu", listOf("Sığır eti", "Krema", "Kızılcık"), "$$$", "Akşam Yemeği", "Politických vězňů 1511/5, Prag", 4.7),
            LocalFoodSpot("3", "Gulaş & Ekmek", "Lokál Dlouhááá", "Ev yapımı Çek ekmeğiyle servis edilen yoğun gulaş", listOf("Sığır eti", "Bira sosu", "Soğan"), "$$", "Öğle Yemeği", "Dlouhá 33, Prag", 4.6)
        )

        "viyana" -> listOf(
            LocalFoodSpot("4", "Wiener Schnitzel", "Figlmüller", "Tabaktan taşan dünyaca ünlü devasa şinitzel", listOf("Dana eti", "Galeta unu"), "$$$", "Öğle 13:00", "Bäckerstraße 6, Viyana", 4.7),
            LocalFoodSpot("5", "Sachertorte", "Café Sacher", "Viyana'nın orijinal çikolatalı ve kayısılı pastası", listOf("Çikolata", "Kayısı reçeli"), "$$$", "Kahve Saati", "Philharmoniker Str. 4, Viyana", 4.5),
            LocalFoodSpot("6", "Apfelstrudel", "Café Central", "Tarihi ve şık ambiyansta fırından yeni çıkmış elmalı turta", listOf("Elma", "Tarçın", "Hamur"), "$$", "Sabah 10:00", "Herrengasse 14, Viyana", 4.6)
        )

        "budapeşte" -> listOf(
            LocalFoodSpot("7", "Kürtőskalács", "Molnár's Kürtőskalács", "Taze pişmiş dumanı tüten Macar bacası tatlısı", listOf("Hamur", "Şeker", "Ceviz"), "$", "Akşamüstü", "Váci u. 31, Budapeşte", 4.7),
            LocalFoodSpot("8", "Lángos", "Retró Lángos", "Sarımsaklı, ekşi kremalı ve peynirli kızarmış hamur", listOf("Hamur", "Kaşar Peyniri", "Ekşi Krema"), "$", "Öğle Molası", "Bajcsy-Zsilinszky út 25, Budapeşte", 4.8),
            LocalFoodSpot("9", "Gulaş Çorbası", "Gettó Gulyás", "Geleneksel tarzda hazırlanan yoğun Macar çorbası", listOf("Kuşbaşı Et", "Patates", "Paprika"), "$$", "Akşam Yemeği", "Wesselényi u. 18, Budapeşte", 4.6)
        )

        "brüksel" -> listOf(
            LocalFoodSpot("10", "Belçika Waffle'ı", "Maison Dandoy", "1829'dan beri hizmet veren tarihi çıtır waffle fırını", listOf("Hamur", "İnci Şeker", "Çikolata"), "$$", "Sabah 09:00", "Rue Charles Buls 14, Brüksel", 4.6),
            LocalFoodSpot("11", "Moules-Frites", "Chez Léon", "Özel tencerede midye ve efsane Belçika patatesi", listOf("Midye", "Tereyağı", "Beyaz Sos"), "$$$", "Akşam Yemeği", "Rue des Bouchers 18, Brüksel", 4.4),
            LocalFoodSpot("12", "Külah Patates", "Fritland", "Sokak lezzetinin zirvesi, özel soslu Belçika patates kızartması", listOf("Patates", "Andalouse Sos"), "$", "Gece Atıştırmalığı", "Rue Henri Maus 49, Brüksel", 4.5)
        )

        "varşova" -> listOf(
            LocalFoodSpot("13", "Pierogi", "Zapiecek", "Geleneksel kıyafetlerle servis edilen Polonya mantısı", listOf("Et", "Patates", "Mantar"), "$$", "Öğle Yemeği", "Świętojańska 13, Varşova", 4.6),
            LocalFoodSpot("14", "Zapiekanka", "Zapiekanki Warszawskie", "Polonya usulü çıtır baget üstü sokak pizzası", listOf("Baget Ekmeği", "Mantar", "Eritme Peynir"), "$", "Gece Acıkması", "Widok 19, Varşova", 4.5),
            LocalFoodSpot("15", "Pączki (Donut)", "Cukiernia Pawłowicz", "İçi bol gül reçeli dolgulu taze Polonya donutu", listOf("Mayalı Hamur", "Gül Reçeli", "Pudra Şekeri"), "$", "Sabah 08:00", "Chmielna 13, Varşova", 4.8)
        )

        "wroclaw" -> listOf(
            LocalFoodSpot("16", "Fırın Pierogi", "Pierogarnia Stary Młyn", "Fırınlanmış devasa pierogiler ve ekşi krema", listOf("Peynir", "Ispanak", "Et"), "$$", "Akşam Yemeği", "Rynek 26, Wroclaw", 4.7),
            LocalFoodSpot("17", "Placki Ziemniaczane", "Kurna Chata", "Tavada kızarmış çıtır patates krepleri", listOf("Rende Patates", "Soğan", "Et Sosu"), "$$", "Öğle Molası", "Odrzańska 17, Wroclaw", 4.6),
            LocalFoodSpot("18", "Lokal Dondurma", "Lody Roma", "Şehrin en köklü ve doğal dondurmacısı", listOf("Doğal Süt", "Mevsim Meyveleri", "Karamel"), "$", "Akşamüstü", "Ludwika Rydygiera 51, Wroclaw", 4.8)
        )

        "barselona" -> listOf(
            LocalFoodSpot("19", "Tapas", "Quimet & Quimet", "Ayakta atıştırılan efsanevi ve ikonik tapas barı", listOf("Deniz ürünleri", "Keçi Peyniri", "Zeytin"), "$$", "Öğle 12:00", "Carrer del Poeta Cabanyes 25, Barselona", 4.8),
            LocalFoodSpot("20", "Churros", "Xurreria Trebol", "Sıcak çikolataya batırılan çıtır İspanyol churros'u", listOf("Kızarmış Hamur", "Şeker", "Sıcak Çikolata"), "$", "Sabah 09:00", "Corsega 341, Barselona", 4.7),
            LocalFoodSpot("21", "Paella", "La Barraca", "Deniz manzaralı otantik deniz ürünleri paellası", listOf("Pirinç", "Karides", "Kalamar", "Safran"), "$$$", "Akşam Yemeği", "Passeig Marítim 1, Barselona", 4.5)
        )

        "paris" -> listOf(
            LocalFoodSpot("22", "Kruvasan & Kahve", "Boulangerie Utopie", "Ödüllü fırından taze ve çıtır çıtır kruvasan", listOf("Un", "Bol Tereyağı"), "$", "Sabah 07:00", "20 Rue Jean-Pierre Timbaud, Paris", 4.8),
            LocalFoodSpot("23", "Macaron", "Pierre Hermé", "Dünyaca ünlü, ağızda dağılan renkli makaronlar", listOf("Badem unu", "Ganaj", "Meyve Püresi"), "$$$", "Öğleden Sonra", "72 Rue Bonaparte, Paris", 4.9),
            LocalFoodSpot("24", "Sokak Krebi (Crêpe)", "La Droguerie", "Marais bölgesinin en iyi bol malzemeli sokak krebi", listOf("İnce Hamur", "Nutella", "Muz"), "$", "Akşamüstü", "56 Rue des Rosiers, Paris", 4.7)
        )

        "amsterdam" -> listOf(
            LocalFoodSpot("25", "Stroopwafel", "Rudi's Original", "Pazarda gözünüzün önünde yapılan sıcak karamelli waffle", listOf("İnce Hamur", "Karamel Şurubu"), "$", "Öğle Gezmesi", "Albert Cuyp Market, Amsterdam", 4.9),
            LocalFoodSpot("26", "Vlaamse Frites", "Vleminckx", "Külah içinde servis edilen meşhur Hollanda patatesi", listOf("Taze Patates", "Mayonez", "Yer Fıstığı Sosu"), "$", "Akşamüstü", "Voetboogstraat 33, Amsterdam", 4.7),
            LocalFoodSpot("27", "Bitterballen", "Café de Klos", "Bira yanında tüketilen çıtır Hollanda et topları", listOf("Et Ragout", "Galeta Unu", "Hardal"), "$$", "Akşam", "Kerkstraat 41, Amsterdam", 4.6)
        )

        "berlin" -> listOf(
            LocalFoodSpot("28", "Currywurst", "Curry 36", "Berlin'in sembolü meşhur körili sosis ve patates", listOf("Bratwurst", "Köri Sosu", "Ketçap"), "$", "Gece Atıştırması", "Mehringdamm 36, Berlin", 4.6),
            LocalFoodSpot("29", "Gemüse Kebap", "Mustafa's", "Önünde hep kuyruk olan Berlin usulü sebzeli döner", listOf("Tavuk Döner", "Kızarmış Sebze", "Özel Sos"), "$", "Öğle 13:00", "Mehringdamm 32, Berlin", 4.5),
            LocalFoodSpot("30", "Pretzel (Simit)", "Zeit für Brot", "Fırından taze çıkmış yumuşak ve dev Alman simidi", listOf("Mayalı Hamur", "İri Tuz"), "$$", "Sabah 08:00", "Alte Schönhauser Str. 4, Berlin", 4.7)
        )

        "roma" -> listOf(
            LocalFoodSpot("31", "Pizza al Taglio", "Bonci Pizzarium", "Makasla kesilen, kalın hamurlu gurme dilim pizza", listOf("Özel Hamur", "Burrata", "Taze Fesleğen"), "$$", "Öğle 12:30", "Via della Meloria 43, Roma", 4.7),
            LocalFoodSpot("32", "Gelato (Dondurma)", "Giolitti", "1900'lerden kalma tarihi Roma dondurmacısı", listOf("Doğal İnek Sütü", "Mevsim Meyvesi", "Antep Fıstığı"), "$", "Sıcak Öğleden Sonra", "Via degli Uffici del Vicario 40, Roma", 4.6),
            LocalFoodSpot("33", "Tiramisu", "Pompi", "Şehrin en ünlü klasik ve çilekli tiramisucusu", listOf("Mascarpone Peyniri", "Espresso", "Kedi Dili"), "$$", "Akşam", "Via Albalonga 7, Roma", 4.8)
        )

        else -> emptyList()
    }
}