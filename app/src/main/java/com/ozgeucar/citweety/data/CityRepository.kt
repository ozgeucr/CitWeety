package com.ozgeucar.citweety.data

import com.ozgeucar.citweety.domain.model.Place

data class CityMapData(
    val cityName: String,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val zoomLevel: Float = 13f,
    val places: MutableList<Place>, // Kullanıcının listesine ekledikleri
    val allDiscoverablePlaces: List<Place> // O şehrin tüm aranabilir mekan havuzu!
)

object CityRepository {

    private val cityDatabase = mapOf(
        "zielona góra" to CityMapData(
            cityName = "Zielona Góra",
            centerLatitude = 51.9355,
            centerLongitude = 15.5062,
            zoomLevel = 14f,
            places = mutableListOf(),
            allDiscoverablePlaces = listOf(
                Place(id = "zg_1", name = "UZ Kampüsü (Campus A)", cityName = "Zielona Góra", category = "Eğitim", description = "Erasmus derslerinin kalbi burada atıyor 🎓", latitude = 51.9355, longitude = 15.5062, imageUrl = "", rating = 4.8f),
                Place(id = "zg_2", name = "Focus Mall", cityName = "Zielona Góra", category = "Alışveriş", description = "Şehrin en popüler sosyal ve alışveriş merkezi 🛍️", latitude = 51.9382, longitude = 15.5032, imageUrl = "", rating = 4.5f),
                Place(id = "zg_3", name = "Palmiarnia (Palma Evi)", cityName = "Zielona Góra", category = "Doğa", description = "Şehre tepeden bakan muazzam bir botanik park 🌴", latitude = 51.9395, longitude = 15.5112, imageUrl = "", rating = 4.7f)
            )
        ),
        "paris" to CityMapData(
            cityName = "Paris",
            centerLatitude = 48.8566,
            centerLongitude = 2.3522,
            zoomLevel = 12f,
            places = mutableListOf(),
            allDiscoverablePlaces = listOf(
                Place(id = "par_1", name = "Eyfel Kulesi", cityName = "Paris", category = "Simge", description = "Paris'in tüm asilliğini yukardan izleyin 🗼", latitude = 48.8584, longitude = 2.2945, imageUrl = "", rating = 4.9f),
                Place(id = "par_2", name = "Louvre Müzesi", cityName = "Paris", category = "Sanat", description = "Dünyanın en büyük sanat müzesi ve Mona Lisa 🖼️", latitude = 48.8606, longitude = 2.3376, imageUrl = "", rating = 4.8f),
                Place(id = "par_3", name = "Şanzelize Caddesi", cityName = "Paris", category = "Cadde", description = "Ünlü lüks mağazalar ve Zafer Takı yürüyüş yolu 🛍️", latitude = 48.8738, longitude = 2.2950, imageUrl = "", rating = 4.6f),
                Place(id = "par_4", name = "Versay Sarayı", cityName = "Paris", category = "Tarih", description = "Muazzam bahçeleriyle ünlü Fransız kraliyet sarayı 🏰", latitude = 48.8048, longitude = 2.1203, imageUrl = "", rating = 4.8f)
            )
        ),
        "berlin" to CityMapData(
            cityName = "Berlin",
            centerLatitude = 52.5200,
            centerLongitude = 13.4050,
            zoomLevel = 12f,
            places = mutableListOf(),
            allDiscoverablePlaces = listOf(
                Place(id = "ber_1", name = "Brandenburg Kapısı", cityName = "Berlin", category = "Tarih", description = "Berlin'in en ikonik tarihi sembolü 🏛️", latitude = 52.5163, longitude = 13.3777, imageUrl = "", rating = 4.9f),
                Place(id = "ber_2", name = "Alexanderplatz", cityName = "Berlin", category = "Meydan", description = "Televizyon kulesi ve büyük sosyal yaşam alanı 🗼", latitude = 52.5219, longitude = 13.4132, imageUrl = "", rating = 4.6f),
                Place(id = "ber_3", name = "Museum Island", cityName = "Berlin", category = "Kültür", description = "Sanat dolu muhteşem müzeler adası 🎨", latitude = 52.5169, longitude = 13.4010, imageUrl = "", rating = 4.8f)
            )
        )
    )

    fun getMapDataByCity(cityName: String): CityMapData? {
        return cityDatabase[cityName.lowercase().trim()]
    }

    fun getCityCenterAndZoom(cityName: String): Triple<Double, Double, Float> {
        val cityData = getMapDataByCity(cityName)
        return if (cityData != null) {
            Triple(cityData.centerLatitude, cityData.centerLongitude, cityData.zoomLevel)
        } else {
            Triple(48.8566, 2.3522, 12f)
        }
    }

    fun addPlaceToUserList(cityName: String, place: Place): Boolean {
        val cityData = getMapDataByCity(cityName)
        return if (cityData != null && !cityData.places.contains(place)) {
            cityData.places.add(place)
            true
        } else {
            false
        }
    }

    fun removePlaceFromUserList(cityName: String, place: Place): Boolean {
        val cityData = getMapDataByCity(cityName)
        return cityData?.places?.remove(place) ?: false
    }
}