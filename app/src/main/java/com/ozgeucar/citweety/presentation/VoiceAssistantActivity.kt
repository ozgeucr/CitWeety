package com.ozgeucar.citweety.presentation

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ozgeucar.citweety.R
import java.util.Locale

data class CountryItem(val name: String, val locale: Locale, val phrases: List<PhraseItem>)
data class PhraseItem(val localText: String, val translation: String, val icon: ImageVector)

class VoiceAssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                VoiceAssistantScreen { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf<CountryItem?>(null) }
    var favorites by remember { mutableStateOf(setOf<String>()) }
    var phraseFavorites by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var phraseSearchQuery by remember { mutableStateOf("") }

    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status -> if (status == TextToSpeech.SUCCESS) isTtsReady = true }
        tts = textToSpeech
        onDispose { textToSpeech.stop(); textToSpeech.shutdown() }
    }

    val countries = remember {
        listOf(
            CountryItem("POLONYA", Locale("pl", "PL"), listOf(
                PhraseItem("Dzień dobry", "Günaydın / Merhaba", Icons.Default.WavingHand),
                PhraseItem("Proszę o rachunek", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Gdzie jest stacja?", "İstasyon nerede?", Icons.Default.DirectionsTransit),
                PhraseItem("Dziękuję", "Teşekkür ederim", Icons.Default.RecordVoiceOver),
                PhraseItem("Ile to kosztuje?", "Bu ne kadar?", Icons.Default.Payment),
                PhraseItem("Przepraszam", "Afedersiniz / Özür dilerim", Icons.Default.RecordVoiceOver),
                PhraseItem("Która godzina?", "Saat kaç?", Icons.Default.AccessTime),
                PhraseItem("Gdzie jest toaleta?", "Tuvalet nerede?", Icons.Default.Wc),
                PhraseItem("Dobry wieczór", "İyi akşamlar", Icons.Default.NightsStay),
                PhraseItem("Do widzenia", "Hoşçakalın", Icons.Default.WavingHand),
                PhraseItem("Pomóż mi", "Yardım edin", Icons.AutoMirrored.Filled.Help),
                PhraseItem("Chciałbym kupić bilet", "Bir bilet almak istiyorum", Icons.Default.ConfirmationNumber),
                PhraseItem("Jakie jest hasło do Wi-Fi?", "Wi-Fi şifresi nedir?", Icons.Default.Wifi),
                PhraseItem("Czy może pan/pani zrobić nam zdjęcie?", "Fotoğrafımızı çekebilir misiniz?", Icons.Default.CameraAlt),
                PhraseItem("Czy mówi pan/pani po angielsku?", "İngilizce biliyor musunuz?", Icons.Default.Language),
                PhraseItem("Czy może pan/pani mówić wolniej?", "Lütfen daha yavaş konuşur musunuz?", Icons.Default.RecordVoiceOver),
                PhraseItem("Gdzie dokładnie jesteśmy na mapie?", "Haritada şu an tam olarak neredeyiz?", Icons.Default.Map),
                PhraseItem("Jak mogę dojść pod ten adres?", "Bu adrese nasıl gidebilirim?", Icons.Default.LocationOn),
                PhraseItem("Jaki jest następny przystanek?", "Bir sonraki durak neresi?", Icons.Default.DirectionsBus),
                PhraseItem("Gdzie jest postój taksówek?", "Taksi durağı nerede?", Icons.Default.LocalTaxi),
                PhraseItem("Czy może pan/pani zamówić dla mnie taksówkę?", "Bana bir taksi çağırabilir misiniz?", Icons.Default.LocalTaxi),
                PhraseItem("Czy można płacić kartą?", "Kredi kartı geçiyor mu?", Icons.Default.CreditCard),
                PhraseItem("Czy akceptują państwo tylko gotówkę?", "Sadece nakit mi kabul ediyorsunuz?", Icons.Default.Payments),
                PhraseItem("Nie mam drobnych.", "Üzerimde bozuk para yok.", Icons.Default.Savings),
                PhraseItem("Czy mogę prosić o paragon?", "Fiş alabilir miyim?", Icons.Default.Receipt),
                PhraseItem("Czy wszystko jest wliczone w cenę?", "Ücretin içine her şey dahil mi?", Icons.Default.AllInclusive),
                PhraseItem("Czy mogę zobaczyć menu?", "Menüyü görebilir miyim?", Icons.Default.RestaurantMenu),
                PhraseItem("Czy to jest świeże?", "Bu ürün taze mi?", Icons.Default.Eco),
                PhraseItem("Jakie są składniki?", "Bunun içinde hangi malzemeler var?", Icons.Default.Fastfood),
                PhraseItem("Mam alergię.", "Alerjim var.", Icons.Default.Warning),
                PhraseItem("Czy mogę prosić o zapakowanie reszty jedzenia?", "Kalan yemeği paket yapabilir misiniz?", Icons.Default.TakeoutDining),
                PhraseItem("Czy woda z kranu jest zdatna do picia?", "Musluk suyu içilebiliyor mu?", Icons.Default.WaterDrop),
                PhraseItem("O której godzinie otwieracie i zamykacie?", "Saat kaçta açılıyor ve kapanıyor?", Icons.Default.Schedule),
                PhraseItem("Gdzie jest najbliższy szpital?", "En yakın hastane nerede?", Icons.Default.LocalHospital),
                PhraseItem("Wezwij karetkę!", "Ambulans çağırın!", Icons.Default.MedicalServices),
                PhraseItem("Zgubiłem portfel/paszport.", "Cüzdanımı/pasaportumu kaybettim.", Icons.Default.ReportProblem),
                PhraseItem("Zadzwoń na policję.", "Polisi arayın.", Icons.Default.Security),
                PhraseItem("Czy w pobliżu jest apteka?", "Yakınlarda eczane var mı?", Icons.Default.MedicalServices),
                PhraseItem("Źle się czuję.", "Kendimi kötü hissediyorum.", Icons.Default.Warning),
                PhraseItem("Potrzebuję lekarza.", "Doktora ihtiyacım var.", Icons.Default.LocalHospital),
                PhraseItem("Gdzie jest centrum miasta?", "Şehir merkezi nerede?", Icons.Default.Explore),
                PhraseItem("Co pan/pani poleca?", "Ne tavsiye edersiniz?", Icons.Default.Info),
                PhraseItem("Czy jest jakaś zniżka?", "İndirim var mı?", Icons.Default.Percent),
                PhraseItem("Czy mogę to przymierzyć?", "Bunu deneyebilir miyim?", Icons.Default.FrontHand),
                PhraseItem("Wezmę to.", "Bunu alıyorum.", Icons.Default.Payments),
                PhraseItem("Tylko się rozglądam.", "Sadece bakıyorum.", Icons.Default.Search),
                PhraseItem("Czy ma pan/pani to w innym rozmiarze?", "Bunun başka bedeni var mı?", Icons.Default.Info),
                PhraseItem("Gdzie jest wejście/wyjście?", "Giriş/çıkış nerede?", Icons.AutoMirrored.Filled.ExitToApp),
                PhraseItem("Czy mogę zarezerwować pokój?", "Oda rezerve edebilir miyim?", Icons.Default.Bed),
                PhraseItem("Czy śniadanie jest wliczone?", "Kahvaltı dahil mi?", Icons.Default.FreeBreakfast)
            )),
            CountryItem("İSPANYA", Locale("es", "ES"), listOf(
                PhraseItem("Hola", "Merhaba", Icons.Default.WavingHand),
                PhraseItem("La cuenta, por favor", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("¿Dónde está la estación?", "İstasyon nerede?", Icons.Default.DirectionsTransit),
                PhraseItem("Gracias", "Teşekkür ederim", Icons.Default.RecordVoiceOver),
                PhraseItem("¿Cuánto cuesta?", "Bu ne kadar?", Icons.Default.Payment),
                PhraseItem("Perdón", "Affedersiniz", Icons.Default.RecordVoiceOver),
                PhraseItem("¿Qué hora es?", "Saat kaç?", Icons.Default.AccessTime),
                PhraseItem("¿Dónde está el baño?", "Tuvalet nerede?", Icons.Default.Wc),
                PhraseItem("Buenas noches", "İyi akşamlar", Icons.Default.NightsStay),
                PhraseItem("Adiós", "Hoşçakalın", Icons.Default.WavingHand),
                PhraseItem("Ayúdame", "Yardım edin", Icons.AutoMirrored.Filled.Help),
                PhraseItem("Me gustaría comprar un billete", "Bir bilet almak istiyorum", Icons.Default.ConfirmationNumber),
                PhraseItem("¿Cuál es la contraseña del Wi-Fi?", "Wi-Fi şifresi nedir?", Icons.Default.Wifi),
                PhraseItem("¿Podría tomarnos una foto?", "Fotoğrafımızı çekebilir misiniz?", Icons.Default.CameraAlt),
                PhraseItem("¿Habla inglés?", "İngilizce biliyor musunuz?", Icons.Default.Language),
                PhraseItem("¿Podría hablar más despacio, por favor?", "Lütfen daha yavaş konuşur musunuz?", Icons.Default.RecordVoiceOver),
                PhraseItem("¿Dónde estamos exactamente en el mapa?", "Haritada şu an tam olarak neredeyiz?", Icons.Default.Map),
                PhraseItem("¿Cómo puedo llegar a esta dirección?", "Bu adrese nasıl gidebilirim?", Icons.Default.LocationOn),
                PhraseItem("¿Cuál es la siguiente parada?", "Bir sonraki durak neresi?", Icons.Default.DirectionsBus),
                PhraseItem("¿Dónde hay una parada de taxis?", "Taksi durağı nerede?", Icons.Default.LocalTaxi),
                PhraseItem("¿Podría llamarme un taxi?", "Bana bir taksi çağırabilir misiniz?", Icons.Default.LocalTaxi),
                PhraseItem("¿Aceptan tarjeta de crédito?", "Kredi kartı geçiyor mu?", Icons.Default.CreditCard),
                PhraseItem("¿Solo aceptan efectivo?", "Sadece nakit mi kabul ediyorsunuz?", Icons.Default.Payments),
                PhraseItem("No tengo cambio.", "Üzerimde bozuk para yok.", Icons.Default.Savings),
                PhraseItem("¿Me da el recibo, por favor?", "Fiş alabilir miyim?", Icons.Default.Receipt),
                PhraseItem("¿Está todo incluido en el precio?", "Ücretin içine her şey dahil mi?", Icons.Default.AllInclusive),
                PhraseItem("¿Puedo ver el menú?", "Menüyü görebilir miyim?", Icons.Default.RestaurantMenu),
                PhraseItem("¿Es fresco este producto?", "Bu ürün taze mi?", Icons.Default.Eco),
                PhraseItem("¿Qué ingredientes tiene esto?", "Bunun içinde hangi malzemeler var?", Icons.Default.Fastfood),
                PhraseItem("Tengo alergia.", "Alerjim var.", Icons.Default.Warning),
                PhraseItem("¿Puede ponerme las sobras para llevar?", "Kalan yemeği paket yapabilir misiniz?", Icons.Default.TakeoutDining),
                PhraseItem("¿Se puede beber el agua del grifo?", "Musluk suyu içilebiliyor mu?", Icons.Default.WaterDrop),
                PhraseItem("¿A qué hora abren y cierran?", "Saat kaçta açılıyor ve kapanıyor?", Icons.Default.Schedule),
                PhraseItem("¿Dónde está el hospital más cercano?", "En yakın hastane nerede?", Icons.Default.LocalHospital),
                PhraseItem("¡Llame a una ambulance!", "Ambulans çağırın!", Icons.Default.MedicalServices),
                PhraseItem("He perdido mi cartera/pasaporte.", "Cüzdanımı/pasaportumu kaybettim.", Icons.Default.ReportProblem),
                PhraseItem("Llame a la policía.", "Polisi arayın.", Icons.Default.Security),
                PhraseItem("¿Hay una farmacia cerca?", "Yakınlarda eczane var mı?", Icons.Default.MedicalServices),
                PhraseItem("Me siento mal.", "Kendimi kötü hissediyorum.", Icons.Default.Warning),
                PhraseItem("Necesito un médico.", "Doktora ihtiyacım var.", Icons.Default.LocalHospital),
                PhraseItem("¿Dónde está el centro de la ciudad?", "Şehir merkezi nerede?", Icons.Default.Explore),
                PhraseItem("¿Qué me recomienda?", "Ne tavsiye edersiniz?", Icons.Default.Info),
                PhraseItem("¿Hay algún descuento?", "İndirim var mı?", Icons.Default.Percent),
                PhraseItem("¿Puedo probármelo?", "Bunu deneyebilir miyim?", Icons.Default.FrontHand),
                PhraseItem("Me lo llevo.", "Bunu alıyorum.", Icons.Default.Payments),
                PhraseItem("Solo estoy mirando.", "Sadece bakıyorum.", Icons.Default.Search),
                PhraseItem("¿Tiene esto en otra talla?", "Bunun başka bedeni var mı?", Icons.Default.Info),
                PhraseItem("¿Dónde está la entrada/salida?", "Giriş/çıkış nerede?", Icons.AutoMirrored.Filled.ExitToApp),
                PhraseItem("¿Puedo reservar una habitación?", "Oda rezerve edebilir miyim?", Icons.Default.Bed),
                PhraseItem("¿Está incluido el desayuno?", "Kahvaltı dahil mi?", Icons.Default.FreeBreakfast)
            )),
            CountryItem("FRANSA", Locale.FRANCE, listOf(
                PhraseItem("Bonjour", "Merhaba / İyi günler", Icons.Default.WavingHand),
                PhraseItem("L'addition, s'il vous plaît", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Où est la gare ?", "İstasyon nerede?", Icons.Default.DirectionsTransit),
                PhraseItem("Merci", "Teşekkür ederim", Icons.Default.RecordVoiceOver),
                PhraseItem("C'est combien ?", "Bu ne kadar?", Icons.Default.Payment),
                PhraseItem("Pardon", "Affedersiniz", Icons.Default.RecordVoiceOver),
                PhraseItem("Quelle heure est-il ?", "Saat kaç?", Icons.Default.AccessTime),
                PhraseItem("Où sont les toilettes ?", "Tuvalet nerede?", Icons.Default.Wc),
                PhraseItem("Bonsoir", "İyi akşamlar", Icons.Default.NightsStay),
                PhraseItem("Au revoir", "Hoşçakalın", Icons.Default.WavingHand),
                PhraseItem("Aidez-moi", "Yardım edin", Icons.AutoMirrored.Filled.Help),
                PhraseItem("Je voudrais acheter un billet", "Bir bilet almak istiyorum", Icons.Default.ConfirmationNumber),
                PhraseItem("Quel est le mot de passe du Wi-Fi ?", "Wi-Fi şifresi nedir?", Icons.Default.Wifi),
                PhraseItem("Pourriez-vous nous prendre en photo ?", "Fotoğrafımızı çekebilir misiniz?", Icons.Default.CameraAlt),
                PhraseItem("Parlez-vous anglais ?", "İngilizce biliyor musunuz?", Icons.Default.Language),
                PhraseItem("Pourriez-vous parler plus lentement, s'il vous plaît ?", "Lütfen daha yavaş konuşur musunuz?", Icons.Default.RecordVoiceOver),
                PhraseItem("Où sommes-nous exactement sur la carte ?", "Haritada şu an tam olarak neredeyiz?", Icons.Default.Map),
                PhraseItem("Comment puis-je aller à cette adresse ?", "Bu adrese nasıl gidebilirim?", Icons.Default.LocationOn),
                PhraseItem("Quel est le prochain arrêt ?", "Bir sonraki durak neresi?", Icons.Default.DirectionsBus),
                PhraseItem("Où est la station de taxis ?", "Taksi durağı nerede?", Icons.Default.LocalTaxi),
                PhraseItem("Pourriez-vous m'appeler un taxi ?", "Bana bir taksi çağırabilir misiniz?", Icons.Default.LocalTaxi),
                PhraseItem("Est-ce que vous acceptez la carte de crédit ?", "Kredi kartı geçiyor mu?", Icons.Default.CreditCard),
                PhraseItem("Acceptez-vous uniquement les espèces ?", "Sadece nakit mi kabul ediyorsunuz?", Icons.Default.Payments),
                PhraseItem("Je n'ai pas de monnaie.", "Üzerimde bozuk para yok.", Icons.Default.Savings),
                PhraseItem("Puis-je avoir un reçu ?", "Fiş alabilir miyim?", Icons.Default.Receipt),
                PhraseItem("Est-ce que tout est inclus dans le prix ?", "Ücretin içine her şey dahil mi?", Icons.Default.AllInclusive),
                PhraseItem("Puis-je voir le menu ?", "Menüyü görebilir miyim?", Icons.Default.RestaurantMenu),
                PhraseItem("Est-ce que ce produit est frais ?", "Bu ürün taze mi?", Icons.Default.Eco),
                PhraseItem("Quels sont les ingrédients ?", "Bunun içinde hangi malzemeler var?", Icons.Default.Fastfood),
                PhraseItem("J'ai une allergie.", "Alerjim var.", Icons.Default.Warning),
                PhraseItem("Pouvez-vous emballer le reste de la nourriture ?", "Kalan yemeği paket yapabilir misiniz?", Icons.Default.TakeoutDining),
                PhraseItem("L'eau du robinet est-elle potable ?", "Musluk suyu içilebiliyor mu?", Icons.Default.WaterDrop),
                PhraseItem("À quelle heure ouvrez-vous et fermez-vous ?", "Saat kaçta açılıyor ve kapanıyor?", Icons.Default.Schedule),
                PhraseItem("Où est l'hôpital le plus proche ?", "En yakın hastane nerede?", Icons.Default.LocalHospital),
                PhraseItem("Appelez une ambulance !", "Ambulans çağırın!", Icons.Default.MedicalServices),
                PhraseItem("J'ai perdu mon portefeuille/passeport.", "Cüzdanımı/pasaportumu kaybettim.", Icons.Default.ReportProblem),
                PhraseItem("Appelez la police.", "Polisi arayın.", Icons.Default.Security),
                PhraseItem("Y a-t-il une pharmacie à proximité ?", "Yakınlarda eczane var mı?", Icons.Default.MedicalServices),
                PhraseItem("Je me sens mal.", "Kendimi kötü hissediyorum.", Icons.Default.Warning),
                PhraseItem("J'ai besoin d'un médecin.", "Doktora ihtiyacım var.", Icons.Default.LocalHospital),
                PhraseItem("Où est le centre-ville ?", "Şehir merkezi nerede?", Icons.Default.Explore),
                PhraseItem("Que recommandez-vous ?", "Ne tavsiye edersiniz?", Icons.Default.Info),
                PhraseItem("Y a-t-il une remise ?", "İndirim var mı?", Icons.Default.Percent),
                PhraseItem("Puis-je l'essayer ?", "Bunu deneyebilir miyim?", Icons.Default.FrontHand),
                PhraseItem("Je le prends.", "Bunu alıyorum.", Icons.Default.Payments),
                PhraseItem("Je regarde seulement.", "Sadece bakıyorum.", Icons.Default.Search),
                PhraseItem("Avez-vous cela dans une autre taille ?", "Bunun başka bedeni var mı?", Icons.Default.Info),
                PhraseItem("Où est l'entrée/la sortie ?", "Giriş/çıkış nerede?", Icons.AutoMirrored.Filled.ExitToApp),
                PhraseItem("Puis-je réserver une chambre ?", "Oda rezerve edebilir miyim?", Icons.Default.Bed),
                PhraseItem("Le petit-déjeuner est-il inclus ?", "Kahvaltı dahil mi?", Icons.Default.FreeBreakfast)
            )),
            CountryItem("HOLLANDA", Locale("nl", "NL"), listOf(
                PhraseItem("Hoi / Hallo", "Merhaba", Icons.Default.WavingHand),
                PhraseItem("De rekening, alstublieft", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Waar is het station?", "İstasyon nerede?", Icons.Default.DirectionsTransit),
                PhraseItem("Dank u wel", "Teşekkür ederim", Icons.Default.RecordVoiceOver),
                PhraseItem("Hoeveel kost dit?", "Bu ne kadar?", Icons.Default.Payment),
                PhraseItem("Sorry", "Özür dilerim / Affedersiniz", Icons.Default.RecordVoiceOver),
                PhraseItem("Hoe laat is het?", "Saat kaç?", Icons.Default.AccessTime),
                PhraseItem("Waar is het toilet?", "Tuvalet nerede?", Icons.Default.Wc),
                PhraseItem("Goedenavond", "İyi akşamlar", Icons.Default.NightsStay),
                PhraseItem("Tot ziens", "Hoşçakalın", Icons.Default.WavingHand),
                PhraseItem("Help mij", "Yardım edin", Icons.AutoMirrored.Filled.Help),
                PhraseItem("Ik wil graag een kaartje kopen", "Bir bilet almak istiyorum", Icons.Default.ConfirmationNumber),
                PhraseItem("Wat is het wifi-wachtwoord?", "Wi-Fi şifresi nedir?", Icons.Default.Wifi),
                PhraseItem("Kunt u een foto van ons maken?", "Fotoğrafımızı çekebilir misiniz?", Icons.Default.CameraAlt),
                PhraseItem("Spreekt u Engels?", "İngilizce biliyor musunuz?", Icons.Default.Language),
                PhraseItem("Kunt u iets langzamer spreken, alstublieft?", "Lütfen daha yavaş konuşur musunuz?", Icons.Default.RecordVoiceOver),
                PhraseItem("Waar zijn we precies op de kaart?", "Haritada şu an tam olarak neredeyiz?", Icons.Default.Map),
                PhraseItem("Hoe kom ik op dit adres?", "Bu adrese nasıl gidebilirim?", Icons.Default.LocationOn),
                PhraseItem("Wat is de volgende halte?", "Bir sonraki durak neresi?", Icons.Default.DirectionsBus),
                PhraseItem("Waar is de taxistandplaats?", "Taksi durağı nerede?", Icons.Default.LocalTaxi),
                PhraseItem("Kunt u een taxi voor me bellen?", "Bana bir taksi çağırabilir misiniz?", Icons.Default.LocalTaxi),
                PhraseItem("Kan ik met een creditcard betalen?", "Kredi kartı geçiyor mu?", Icons.Default.CreditCard),
                PhraseItem("Accepteert u alleen contant geld?", "Sadece nakit mi kabul ediyorsunuz?", Icons.Default.Payments),
                PhraseItem("Ik heb geen kleingeld bij me.", "Üzerimde bozuk para yok.", Icons.Default.Savings),
                PhraseItem("Mag ik een bonnetje?", "Fiş alabilir miyim?", Icons.Default.Receipt),
                PhraseItem("Is alles bij de prijs inbegrepen?", "Ücretin içine her şey dahil mi?", Icons.Default.AllInclusive),
                PhraseItem("Mag ik het menu zien?", "Menüyü görebilir miyim?", Icons.Default.RestaurantMenu),
                PhraseItem("Is dit product vers?", "Bu ürün taze mi?", Icons.Default.Eco),
                PhraseItem("Welke ingrediënten zitten hierin?", "Bunun içinde hangi malzemeler var?", Icons.Default.Fastfood),
                PhraseItem("Ik heb een allergie.", "Alerjim var.", Icons.Default.Warning),
                PhraseItem("Kunt u het resterende eten inpakken?", "Kalan yemeği paket yapabilir misiniz?", Icons.Default.TakeoutDining),
                PhraseItem("Is het kraanwater drinkbaar?", "Musluk suyu içilebiliyor mu?", Icons.Default.WaterDrop),
                PhraseItem("Hoe laat gaat het open en dicht?", "Saat kaçta açılıyor ve kapanıyor?", Icons.Default.Schedule),
                PhraseItem("Waar is het dichtstbijzijnde ziekenhuis?", "En yakın hastane nerede?", Icons.Default.LocalHospital),
                PhraseItem("Bel een ambulance!", "Ambulans çağırın!", Icons.Default.MedicalServices),
                PhraseItem("Ik ben mijn portemonnee/paspoort kwijt.", "Cüzdanımı/pasaportumu kaybettim.", Icons.Default.ReportProblem),
                PhraseItem("Bel de politie.", "Polisi arayın.", Icons.Default.Security),
                PhraseItem("Is er een apotheek in de buurt?", "Yakınlarda eczane var mı?", Icons.Default.MedicalServices),
                PhraseItem("Ik voel me niet goed.", "Kendimi kötü hissediyorum.", Icons.Default.Warning),
                PhraseItem("Ik heb een dokter nodig.", "Doktora ihtiyacım var.", Icons.Default.LocalHospital),
                PhraseItem("Waar is het stadscentrum?", "Şehir merkezi nerede?", Icons.Default.Explore),
                PhraseItem("Wat raadt u aan?", "Ne tavsiye edersiniz?", Icons.Default.Info),
                PhraseItem("Is er korting?", "İndirim var mı?", Icons.Default.Percent),
                PhraseItem("Kan ik dit passen?", "Bunu deneyebilir miyim?", Icons.Default.FrontHand),
                PhraseItem("Ik neem dit.", "Bunu alıyorum.", Icons.Default.Payments),
                PhraseItem("Ik kijk alleen even rond.", "Sadece bakıyorum.", Icons.Default.Search),
                PhraseItem("Heeft u dit in een andere maat?", "Bunun başka bedeni var mı?", Icons.Default.Info),
                PhraseItem("Waar is de ingang/uitgang?", "Giriş/çıkış nerede?", Icons.AutoMirrored.Filled.ExitToApp),
                PhraseItem("Kan ik een kamer reserveren?", "Oda rezerve edebilir miyim?", Icons.Default.Bed),
                PhraseItem("Is het ontbijt inbegrepen?", "Kahvaltı dahil mi?", Icons.Default.FreeBreakfast)
            )),
            CountryItem("ALMANYA", Locale.GERMANY, listOf(
                PhraseItem("Hallo / Guten Tag", "Merhaba / İyi günler", Icons.Default.WavingHand),
                PhraseItem("Die Rechnung, bitte", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Wo ist der Bahnhof?", "İstasyon nerede?", Icons.Default.DirectionsTransit),
                PhraseItem("Danke", "Teşekkür ederim", Icons.Default.RecordVoiceOver),
                PhraseItem("Was kostet das?", "Bu ne kadar?", Icons.Default.Payment),
                PhraseItem("Entschuldigung", "Affedersiniz", Icons.Default.RecordVoiceOver),
                PhraseItem("Wie spät ist es?", "Saat kaç?", Icons.Default.AccessTime),
                PhraseItem("Wo ist die Toilette?", "Tuvalet nerede?", Icons.Default.Wc),
                PhraseItem("Guten Abend", "İyi akşamlar", Icons.Default.NightsStay),
                PhraseItem("Auf Wiedersehen", "Hoşçakalın", Icons.Default.WavingHand),
                PhraseItem("Helfen Sie mir", "Yardım edin", Icons.AutoMirrored.Filled.Help),
                PhraseItem("Ich möchte eine Fahrkarte kaufen", "Bir bilet almak istiyorum", Icons.Default.ConfirmationNumber),
                PhraseItem("Wie lautet das Wi-Fi-Passwort?", "Wi-Fi şifresi nedir?", Icons.Default.Wifi),
                PhraseItem("Könnten Sie ein Foto von uns machen?", "Fotoğrafımızı çekebilir misiniz?", Icons.Default.CameraAlt),
                PhraseItem("Sprechen Sie Englisch?", "İngilizce biliyor musunuz?", Icons.Default.Language),
                PhraseItem("Könnten Sie bitte langsamer sprechen?", "Lütfen daha yavaş konuşur musunuz?", Icons.Default.RecordVoiceOver),
                PhraseItem("Wo genau sind wir auf der Karte?", "Haritada şu an tam olarak neredeyiz?", Icons.Default.Map),
                PhraseItem("Wie komme ich zu dieser Adresse?", "Bu adrese nasıl gidebilirim?", Icons.Default.LocationOn),
                PhraseItem("Was ist die nächste Haltestelle?", "Bir sonraki durak neresi?", Icons.Default.DirectionsBus),
                PhraseItem("Wo ist der Taxistand?", "Taksi durağı nerede?", Icons.Default.LocalTaxi),
                PhraseItem("Könnten Sie mir ein Taxi rufen?", "Bana bir taksi çağırabilir misiniz?", Icons.Default.LocalTaxi),
                PhraseItem("Akzeptieren Sie Kreditkarten?", "Kredi kartı geçiyor mu?", Icons.Default.CreditCard),
                PhraseItem("Nehmen Sie nur Bargeld?", "Sadece nakit mi kabul ediyorsunuz?", Icons.Default.Payments),
                PhraseItem("Ich habe kein Kleingeld dabei.", "Üzerimde bozuk para yok.", Icons.Default.Savings),
                PhraseItem("Kann ich eine Quittung haben?", "Fiş alabilir miyim?", Icons.Default.Receipt),
                PhraseItem("Ist alles im Preis inbegriffen?", "Ücretin içine her şey dahil mi?", Icons.Default.AllInclusive),
                PhraseItem("Kann ich die Speisekarte sehen?", "Menüyü görebilir miyim?", Icons.Default.RestaurantMenu),
                PhraseItem("Ist dieses Produkt frisch?", "Bu ürün taze mi?", Icons.Default.Eco),
                PhraseItem("Welche Zutaten sind darin enthalten?", "Bunun içinde hangi malzemeler var?", Icons.Default.Fastfood),
                PhraseItem("Ich habe eine Allergie.", "Alerjim var.", Icons.Default.Warning),
                PhraseItem("Könnten Sie das restliche Essen einpacken?", "Kalan yemeği paket yapabilir misiniz?", Icons.Default.TakeoutDining),
                PhraseItem("Kann man das Leitungswasser trinken?", "Musluk suyu içilebiliyor mu?", Icons.Default.WaterDrop),
                PhraseItem("Wann wird geöffnet und geschlossen?", "Saat kaçta açılıyor ve kapanıyor?", Icons.Default.Schedule),
                PhraseItem("Wo ist das nächste Krankenhaus?", "En yakın hastane nerede?", Icons.Default.LocalHospital),
                PhraseItem("Rufen Sie einen Krankenwagen!", "Ambulans çağırın!", Icons.Default.MedicalServices),
                PhraseItem("Ich habe meinen Geldbeutel/Reisepass verloren.", "Cüzdanımı/pasaportumu kaybettim.", Icons.Default.ReportProblem),
                PhraseItem("Rufen Sie die Polizei.", "Polisi arayın.", Icons.Default.Security),
                PhraseItem("Gibt es eine Apotheke in der Nähe?", "Yakınlarda eczane var mı?", Icons.Default.MedicalServices),
                PhraseItem("Ich fühle mich schlecht.", "Kendimi kötü hissediyorum.", Icons.Default.Warning),
                PhraseItem("Ich brauche einen Arzt.", "Doktora ihtiyacım var.", Icons.Default.LocalHospital),
                PhraseItem("Wo ist das Stadtzentrum?", "Şehir merkezi nerede?", Icons.Default.Explore),
                PhraseItem("Was empfehlen Sie?", "Ne tavsiye edersiniz?", Icons.Default.Info),
                PhraseItem("Gibt es einen Rabatt?", "İndirim var mı?", Icons.Default.Percent),
                PhraseItem("Kann ich das anprobieren?", "Bunu deneyebilir miyim?", Icons.Default.FrontHand),
                PhraseItem("Ich nehme das.", "Bunu alıyorum.", Icons.Default.Payments),
                PhraseItem("Ich schaue mich nur um.", "Sadece bakıyorum.", Icons.Default.Search),
                PhraseItem("Haben Sie das in einer anderen Größe?", "Bunun başka bedeni var mı?", Icons.Default.Info),
                PhraseItem("Wo ist der Eingang/Ausgang?", "Giriş/çıkış nerede?", Icons.AutoMirrored.Filled.ExitToApp),
                PhraseItem("Kann ich ein Zimmer reservieren?", "Oda rezerve edebilir miyim?", Icons.Default.Bed),
                PhraseItem("Ist das Frühstück inbegriffen?", "Kahvaltı dahil mi?", Icons.Default.FreeBreakfast)
            )),
            CountryItem("İTALYA", Locale.ITALY, listOf(
                PhraseItem("Ciao / Buongiorno", "Merhaba / İyi günler", Icons.Default.WavingHand),
                PhraseItem("Il conto, per favore", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Dov'è la stazione?", "İstasyon nerede?", Icons.Default.DirectionsTransit),
                PhraseItem("Grazie", "Teşekkür ederim", Icons.Default.RecordVoiceOver),
                PhraseItem("Quanto costa?", "Bu ne kadar?", Icons.Default.Payment),
                PhraseItem("Scusi", "Affedersiniz", Icons.Default.RecordVoiceOver),
                PhraseItem("Che ore sono?", "Saat kaç?", Icons.Default.AccessTime),
                PhraseItem("Dov'è il bagno?", "Tuvalet nerede?", Icons.Default.Wc),
                PhraseItem("Buonasera", "İyi akşamlar", Icons.Default.NightsStay),
                PhraseItem("Arrivederci", "Hoşçakalın", Icons.Default.WavingHand),
                PhraseItem("Aiutami", "Yardım edin", Icons.AutoMirrored.Filled.Help),
                PhraseItem("Vorrei comprare un biglietto", "Bir bilet almak istiyorum", Icons.Default.ConfirmationNumber),
                PhraseItem("Qual è la password del Wi-Fi?", "Wi-Fi şifresi nedir?", Icons.Default.Wifi),
                PhraseItem("Potrebbe farci una foto?", "Fotoğrafımızı çekebilir misiniz?", Icons.Default.CameraAlt),
                PhraseItem("Parla inglese?", "İngilizce biliyor musunuz?", Icons.Default.Language),
                PhraseItem("Può parlare più lentamente, per favore?", "Lütfen daha yavaş konuşur musunuz?", Icons.Default.RecordVoiceOver),
                PhraseItem("Dove siamo esattamente sulla mappa?", "Haritada şu an tam olarak neredeyiz?", Icons.Default.Map),
                PhraseItem("Come posso andare a questo indirizzo?", "Bu adrese nasıl gidebilirim?", Icons.Default.LocationOn),
                PhraseItem("Qual è la prossima fermata?", "Bir sonraki durak neresi?", Icons.Default.DirectionsBus),
                PhraseItem("Dov'è il posteggio dei taxi?", "Taksi durağı nerede?", Icons.Default.LocalTaxi),
                PhraseItem("Può chiamarmi un taxi?", "Bana bir taksi çağırabilir misiniz?", Icons.Default.LocalTaxi),
                PhraseItem("Accettate carte di credito?", "Kredi kartı geçiyor mu?", Icons.Default.CreditCard),
                PhraseItem("Accettate solo contanti?", "Sadece nakit mi kabul ediyorsunuz?", Icons.Default.Payments),
                PhraseItem("Non ho monete.", "Üzerimde bozuk para yok.", Icons.Default.Savings),
                PhraseItem("Posso avere la ricevuta?", "Fiş alabilir miyim?", Icons.Default.Receipt),
                PhraseItem("È tutto incluso nel prezzo?", "Ücretin içine her şey dahil mi?", Icons.Default.AllInclusive),
                PhraseItem("Posso vedere il menu?", "Menüyü görebilir miyim?", Icons.Default.RestaurantMenu),
                PhraseItem("Questo prodotto è fresco?", "Bu ürün taze mi?", Icons.Default.Eco),
                PhraseItem("Quali sono gli ingredienti?", "Bunun içinde hangi malzemeler var?", Icons.Default.Fastfood),
                PhraseItem("Ho un'allergia.", "Alerjim var.", Icons.Default.Warning),
                PhraseItem("Può farmi un pacchetto con gli avanzi?", "Kalan yemeği paket yapabilir misiniz?", Icons.Default.TakeoutDining),
                PhraseItem("L'acqua del rubinetto è potabile?", "Musluk suyu içilebiliyor mu?", Icons.Default.WaterDrop),
                PhraseItem("A che ora apre e chiude?", "Saat kaçta açılıyor ve kapanıyor?", Icons.Default.Schedule),
                PhraseItem("Dov'è l'ospedale più vicino?", "En yakın hastane nerede?", Icons.Default.LocalHospital),
                PhraseItem("Chiami un'ambulanza!", "Ambulans çağırın!", Icons.Default.MedicalServices),
                PhraseItem("Ho perso il mio portafoglio/passaporto.", "Cüzdanımı/pasaportumu kaybettim.", Icons.Default.ReportProblem),
                PhraseItem("Chiami la polizia.", "Polisi arayın.", Icons.Default.Security),
                PhraseItem("C'è una farmacia nelle vicinanze?", "Yakınlarda eczane var mı?", Icons.Default.MedicalServices),
                PhraseItem("Mi sento male.", "Kendimi kötü hissediyorum.", Icons.Default.Warning),
                PhraseItem("Ho bisogno di un medico.", "Doktora ihtiyacım var.", Icons.Default.LocalHospital),
                PhraseItem("Dov'è il centro città?", "Şehir merkezi nerede?", Icons.Default.Explore),
                PhraseItem("Cosa mi consiglia?", "Ne tavsiye edersiniz?", Icons.Default.Info),
                PhraseItem("C'è uno sconto?", "İndirim var mı?", Icons.Default.Percent),
                PhraseItem("Posso provarlo?", "Bunu deneyebilir miyim?", Icons.Default.FrontHand),
                PhraseItem("Lo prendo.", "Bunu alıyorum.", Icons.Default.Payments),
                PhraseItem("Sto solo guardando.", "Sadece bakıyorum.", Icons.Default.Search),
                PhraseItem("Ce l'ha in un'altra taglia?", "Bunun başka bedeni var mı?", Icons.Default.Info),
                PhraseItem("Dov'è l'entrata/l'uscita?", "Giriş/çıkış nerede?", Icons.AutoMirrored.Filled.ExitToApp),
                PhraseItem("Posso prenotare una camera?", "Oda rezerve edebilir miyim?", Icons.Default.Bed),
                PhraseItem("La colazione è inclusa?", "Kahvaltı dahil mi?", Icons.Default.FreeBreakfast)
            )),
            CountryItem("ÇEKYA", Locale("cs", "CZ"), listOf(
                PhraseItem("Ahoj / Dobrý den", "Merhaba / İyi günler", Icons.Default.WavingHand),
                PhraseItem("Účet, prosím", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Kde je nádraží?", "İstasyon nerede?", Icons.Default.DirectionsTransit),
                PhraseItem("Děkuji", "Teşekkür ederim", Icons.Default.RecordVoiceOver),
                PhraseItem("Kolik to stojí?", "Bu ne kadar?", Icons.Default.Payment),
                PhraseItem("Promiňte", "Affedersiniz", Icons.Default.RecordVoiceOver),
                PhraseItem("Kolik je hodin?", "Saat kaç?", Icons.Default.AccessTime),
                PhraseItem("Kde je toaleta?", "Tuvalet nerede?", Icons.Default.Wc),
                PhraseItem("Dobrý večer", "İyi akşamlar", Icons.Default.NightsStay),
                PhraseItem("Na shledanou", "Hoşçakalın", Icons.Default.WavingHand),
                PhraseItem("Pomozte mi", "Yardım edin", Icons.AutoMirrored.Filled.Help),
                PhraseItem("Chtěl bych si koupit lístek", "Bir bilet almak istiyorum", Icons.Default.ConfirmationNumber),
                PhraseItem("Jaké je heslo k Wi-Fi?", "Wi-Fi şifresi nedir?", Icons.Default.Wifi),
                PhraseItem("Můžete nás vyfotit?", "Fotoğrafımızı çekebilir misiniz?", Icons.Default.CameraAlt),
                PhraseItem("Mluvíte anglicky?", "İngilizce biliyor musunuz?", Icons.Default.Language),
                PhraseItem("Můžete mluvit pomaleji, prosím?", "Lütfen daha yavaş konuşur musunuz?", Icons.Default.RecordVoiceOver),
                PhraseItem("Kde přesně jsme na mapě?", "Haritada şu an tam olarak neredeyiz?", Icons.Default.Map),
                PhraseItem("Jak se dostanu na tuto adresu?", "Bu adrese nasıl gidebilirim?", Icons.Default.LocationOn),
                PhraseItem("Jaká je příští zastávka?", "Bir sonraki durak neresi?", Icons.Default.DirectionsBus),
                PhraseItem("Kde je stanoviště taxi?", "Taksi durağı nerede?", Icons.Default.LocalTaxi),
                PhraseItem("Můžete mi zavolat taxi?", "Bana bir taksi çağırabilir misiniz?", Icons.Default.LocalTaxi),
                PhraseItem("Přijímáte kreditní karty?", "Kredi kartı geçiyor mu?", Icons.Default.CreditCard),
                PhraseItem("Přijímáte pouze hotovost?", "Sadece nakit mi kabul ediyorsunuz?", Icons.Default.Payments),
                PhraseItem("Nemám drobné.", "Üzerimde bozuk para yok.", Icons.Default.Savings),
                PhraseItem("Mohu dostat účtenku?", "Fiş alabilir miyim?", Icons.Default.Receipt),
                PhraseItem("Je vše zahrnuto v ceně?", "Ücretin içine her şey dahil mi?", Icons.Default.AllInclusive),
                PhraseItem("Mohu vidět jídelní lístek?", "Menüyü görebilir miyim?", Icons.Default.RestaurantMenu),
                PhraseItem("Je tento produkt čerstvý?", "Bu ürün taze mi?", Icons.Default.Eco),
                PhraseItem("Jaké jsou ingredience?", "Bunun içinde hangi malzemeler var?", Icons.Default.Fastfood),
                PhraseItem("Mám alergii.", "Alerjim var.", Icons.Default.Warning),
                PhraseItem("Můžete mi zabalit zbytek jídla?", "Kalan yemeği paket yapabilir misiniz?", Icons.Default.TakeoutDining),
                PhraseItem("Je voda z kohoutku pitná?", "Musluk suyu içilebiliyor mu?", Icons.Default.WaterDrop),
                PhraseItem("V kolik hodin otevíráte a zavíráte?", "Saat kaçta açılıyor ve kapanıyor?", Icons.Default.Schedule),
                PhraseItem("Kde je nejbližší nemocnice?", "En yakın hastane nerede?", Icons.Default.LocalHospital),
                PhraseItem("Zavolejte sanitku!", "Ambulans çağırın!", Icons.Default.MedicalServices),
                PhraseItem("Ztratil jsem peněženku/pas.", "Cüzdanımı/pasaportumu kaybettim.", Icons.Default.ReportProblem),
                PhraseItem("Zavolejte policii.", "Polisi arayın.", Icons.Default.Security),
                PhraseItem("Je v blízkosti lékárna?", "Yakınlarda eczane var mı?", Icons.Default.MedicalServices),
                PhraseItem("Cítím se špatně.", "Kendimi kötü hissediyorum.", Icons.Default.Warning),
                PhraseItem("Potřebuji lékaře.", "Doktora ihtiyacım var.", Icons.Default.LocalHospital),
                PhraseItem("Kde je centrum města?", "Şehir merkezi nerede?", Icons.Default.Explore),
                PhraseItem("Co doporučujete?", "Ne tavsiye edersiniz?", Icons.Default.Info),
                PhraseItem("Je tu nějaká sleva?", "İndirim var mı?", Icons.Default.Percent),
                PhraseItem("Mohu si to vyzkoušet?", "Bunu deneyebilir miyim?", Icons.Default.FrontHand),
                PhraseItem("Vezmu si to.", "Bunu alıyorum.", Icons.Default.Payments),
                PhraseItem("Jen se dívám.", "Sadece bakıyorum.", Icons.Default.Search),
                PhraseItem("Máte to v jiné velikosti?", "Bunun başka bedeni var mı?", Icons.Default.Info),
                PhraseItem("Kde je vchod/východ?", "Giriş/çıkış nerede?", Icons.AutoMirrored.Filled.ExitToApp),
                PhraseItem("Mohu si rezervovat pokoj?", "Oda rezerve edebilir miyim?", Icons.Default.Bed),
                PhraseItem("Je snídaně v ceně?", "Kahvaltı dahil mi?", Icons.Default.FreeBreakfast)
            )),
            CountryItem("AVUSTURYA", Locale.GERMANY, listOf(
                PhraseItem("Servus / Hallo", "Merhaba", Icons.Default.WavingHand),
                PhraseItem("Die Rechnung, bitte", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Wo ist der Bahnhof?", "İstasyon nerede?", Icons.Default.DirectionsTransit),
                PhraseItem("Danke", "Teşekkür ederim", Icons.Default.RecordVoiceOver),
                PhraseItem("Was kostet das?", "Bu ne kadar?", Icons.Default.Payment),
                PhraseItem("Entschuldigung", "Affedersiniz", Icons.Default.RecordVoiceOver),
                PhraseItem("Wie spät ist es?", "Saat kaç?", Icons.Default.AccessTime),
                PhraseItem("Wo ist die Toilette?", "Tuvalet nerede?", Icons.Default.Wc),
                PhraseItem("Guten Abend", "İyi akşamlar", Icons.Default.NightsStay),
                PhraseItem("Auf Wiedersehen", "Hoşçakalın", Icons.Default.WavingHand),
                PhraseItem("Helfen Sie mir", "Yardım edin", Icons.AutoMirrored.Filled.Help),
                PhraseItem("Ich möchte eine Fahrkarte kaufen", "Bir bilet almak istiyorum", Icons.Default.ConfirmationNumber),
                PhraseItem("Wie lautet das Wi-Fi-Passwort?", "Wi-Fi şifresi nedir?", Icons.Default.Wifi),
                PhraseItem("Könnten Sie ein Foto von uns machen?", "Fotoğrafımızı çekebilir misiniz?", Icons.Default.CameraAlt),
                PhraseItem("Sprechen Sie Englisch?", "İngilizce biliyor musunuz?", Icons.Default.Language),
                PhraseItem("Könnten Sie bitte langsamer sprechen?", "Lütfen daha yavaş konuşur musunuz?", Icons.Default.RecordVoiceOver),
                PhraseItem("Wo genau sind wir auf der Karte?", "Haritada şu an tam olarak neredeyiz?", Icons.Default.Map),
                PhraseItem("Wie komme ich zu dieser Adresse?", "Bu adrese nasıl gidebilirim?", Icons.Default.LocationOn),
                PhraseItem("Was ist die nächste Haltestelle?", "Bir sonraki durak neresi?", Icons.Default.DirectionsBus),
                PhraseItem("Wo ist der Taxistand?", "Taksi durağı nerede?", Icons.Default.LocalTaxi),
                PhraseItem("Könnten Sie mir ein Taxi rufen?", "Bana bir taksi çağırabilir misiniz?", Icons.Default.LocalTaxi),
                PhraseItem("Akzeptieren Sie Kreditkarten?", "Kredi kartı geçiyor mu?", Icons.Default.CreditCard),
                PhraseItem("Nehmen Sie nur Bargeld?", "Sadece nakit mi kabul ediyorsunuz?", Icons.Default.Payments),
                PhraseItem("Ich habe kein Kleingeld dabei.", "Üzerimde bozuk para yok.", Icons.Default.Savings),
                PhraseItem("Kann ich eine Quittung haben?", "Fiş alabilir miyim?", Icons.Default.Receipt),
                PhraseItem("Ist alles im Preis inbegriffen?", "Ücretin içine her şey dahil mi?", Icons.Default.AllInclusive),
                PhraseItem("Kann ich die Speisekarte sehen?", "Menüyü görebilir miyim?", Icons.Default.RestaurantMenu),
                PhraseItem("Ist dieses Produkt frisch?", "Bu ürün taze mi?", Icons.Default.Eco),
                PhraseItem("Welche Zutaten sind darin enthalten?", "Bunun içinde hangi malzemeler var?", Icons.Default.Fastfood),
                PhraseItem("Ich habe eine Allergie.", "Alerjim var.", Icons.Default.Warning),
                PhraseItem("Könnten Sie das restliche Essen einpacken?", "Kalan yemeği paket yapabilir misiniz?", Icons.Default.TakeoutDining),
                PhraseItem("Kann man das Leitungswasser trinken?", "Musluk suyu içilebiliyor mu?", Icons.Default.WaterDrop),
                PhraseItem("Wann wird geöffnet und geschlossen?", "Saat kaçta açılıyor ve kapanıyor?", Icons.Default.Schedule),
                PhraseItem("Wo ist das nächste Krankenhaus?", "En yakın hastane nerede?", Icons.Default.LocalHospital),
                PhraseItem("Rufen Sie einen Krankenwagen!", "Ambulans çağırın!", Icons.Default.MedicalServices),
                PhraseItem("Ich habe meinen Geldbeutel/Reisepass verloren.", "Cüzdanımı/pasaportumu kaybettim.", Icons.Default.ReportProblem),
                PhraseItem("Rufen Sie die Polizei.", "Polisi arayın.", Icons.Default.Security),
                PhraseItem("Gibt es eine Apotheke in der Nähe?", "Yakınlarda eczane var mı?", Icons.Default.MedicalServices),
                PhraseItem("Ich fühle mich schlecht.", "Kendimi kötü hissediyorum.", Icons.Default.Warning),
                PhraseItem("Ich brauche einen Arzt.", "Doktora ihtiyacım var.", Icons.Default.LocalHospital),
                PhraseItem("Wo ist das Stadtzentrum?", "Şehir merkezi nerede?", Icons.Default.Explore),
                PhraseItem("Was empfehlen Sie?", "Ne tavsiye edersiniz?", Icons.Default.Info),
                PhraseItem("Gibt es einen Rabatt?", "İndirim var mı?", Icons.Default.Percent),
                PhraseItem("Kann ich das anprobieren?", "Bunu deneyebilir miyim?", Icons.Default.FrontHand),
                PhraseItem("Ich nehme das.", "Bunu alıyorum.", Icons.Default.Payments),
                PhraseItem("Ich schaue mich nur um.", "Sadece bakıyorum.", Icons.Default.Search),
                PhraseItem("Haben Sie das in einer anderen Größe?", "Bunun başka bedeni var mı?", Icons.Default.Info),
                PhraseItem("Wo ist der Eingang/Ausgang?", "Giriş/çıkış nerede?", Icons.AutoMirrored.Filled.ExitToApp),
                PhraseItem("Kann ich ein Zimmer reservieren?", "Oda rezerve edebilir miyim?", Icons.Default.Bed),
                PhraseItem("Ist das Frühstück inbegriffen?", "Kahvaltı dahil mi?", Icons.Default.FreeBreakfast)
            )),
            CountryItem("MACARİSTAN", Locale("hu", "HU"), listOf(
                PhraseItem("Szia / Jó napot", "Merhaba / İyi günler", Icons.Default.WavingHand),
                PhraseItem("A számlát, kérem", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Hol van az állomás?", "İstasyon nerede?", Icons.Default.DirectionsTransit),
                PhraseItem("Köszönöm", "Teşekkür ederim", Icons.Default.RecordVoiceOver),
                PhraseItem("Mennyibe kerül?", "Bu ne kadar?", Icons.Default.Payment),
                PhraseItem("Elnézést", "Affedersiniz", Icons.Default.RecordVoiceOver),
                PhraseItem("Hány óra van?", "Saat kaç?", Icons.Default.AccessTime),
                PhraseItem("Hol van a mosdó?", "Tuvalet nerede?", Icons.Default.Wc),
                PhraseItem("Jó estét", "İyi akşamlar", Icons.Default.NightsStay),
                PhraseItem("Viszontlátásra", "Hoşçakalın", Icons.Default.WavingHand),
                PhraseItem("Segítsen", "Yardım edin", Icons.AutoMirrored.Filled.Help),
                PhraseItem("Szeretnék venni egy jegyet", "Bir bilet almak istiyorum", Icons.Default.ConfirmationNumber),
                PhraseItem("Mi a Wi-Fi jelszó?", "Wi-Fi şifresi nedir?", Icons.Default.Wifi),
                PhraseItem("Készítene rólunk egy fényképet?", "Fotoğrafımızı çekebilir misiniz?", Icons.Default.CameraAlt),
                PhraseItem("Beszél angolul?", "İngilizce biliyor musunuz?", Icons.Default.Language),
                PhraseItem("Tudna lassabban beszélni, kérem?", "Lütfen daha yavaş konuşur musunuz?", Icons.Default.RecordVoiceOver),
                PhraseItem("Hol vagyunk pontosan a térképen?", "Haritada şu an tam olarak neredeyiz?", Icons.Default.Map),
                PhraseItem("Hogyan jutok el erre a címre?", "Bu adrese nasıl gidebilirim?", Icons.Default.LocationOn),
                PhraseItem("Mi a következő megálló?", "Bir sonraki durak neresi?", Icons.Default.DirectionsBus),
                PhraseItem("Hol van a taxiállomás?", "Taksi durağı nerede?", Icons.Default.LocalTaxi),
                PhraseItem("Tudna hívni nekem egy taxit?", "Bana bir taksi çağırabilir misiniz?", Icons.Default.LocalTaxi),
                PhraseItem("Elfogadnak hitelkártyát?", "Kredi kartı geçiyor mu?", Icons.Default.CreditCard),
                PhraseItem("Csak készpénzt fogadnak el?", "Sadece nakit mi kabul ediyorsunuz?", Icons.Default.Payments),
                PhraseItem("Nincs nálam aprópénz.", "Üzerimde bozuk para yok.", Icons.Default.Savings),
                PhraseItem("Kérhetnék nyugtát?", "Fiş alabilir miyim?", Icons.Default.Receipt),
                PhraseItem("Minden benne van az árban?", "Ücretin içine her şey dahil mi?", Icons.Default.AllInclusive),
                PhraseItem("Láthatnám az étlapot?", "Menüyü görebilir miyim?", Icons.Default.RestaurantMenu),
                PhraseItem("Friss ez a termék?", "Bu ürün taze mi?", Icons.Default.Eco),
                PhraseItem("Mik az összetevők?", "Bunun içinde hangi malzemeler var?", Icons.Default.Fastfood),
                PhraseItem("Allergiám van.", "Alerjim var.", Icons.Default.Warning),
                PhraseItem("Be tudná csomagolni a maradék ételt?", "Kalan yemeği paket yapabilir misiniz?", Icons.Default.TakeoutDining),
                PhraseItem("Iható a csapvíz?", "Musluk suyu içilebiliyor mu?", Icons.Default.WaterDrop),
                PhraseItem("Mikor nyitnak és zárnak?", "Saat kaçta açılıyor ve kapanıyor?", Icons.Default.Schedule),
                PhraseItem("Hol van a legközelebbi kórház?", "En yakın hastane nerede?", Icons.Default.LocalHospital),
                PhraseItem("Hívjon mentőt!", "Ambulans çağırın!", Icons.Default.MedicalServices),
                PhraseItem("Elvesztettem a pénztárcámat/útlevelemet.", "Cüzdanımı/pasaportumu kaybettim.", Icons.Default.ReportProblem),
                PhraseItem("Hívja a rendőrséget.", "Polisi arayın.", Icons.Default.Security),
                PhraseItem("Van gyógyszertár a közelben?", "Yakınlarda eczane var mı?", Icons.Default.MedicalServices),
                PhraseItem("Rosszul vagyok.", "Kendimi kötü hissediyorum.", Icons.Default.Warning),
                PhraseItem("Orvosra van szükségem.", "Doktora ihtiyacım var.", Icons.Default.LocalHospital),
                PhraseItem("Hol van a városközpont?", "Şehir merkezi nerede?", Icons.Default.Explore),
                PhraseItem("Mit javasol?", "Ne tavsiye edersiniz?", Icons.Default.Info),
                PhraseItem("Van kedvezmény?", "İndirim var mı?", Icons.Default.Percent),
                PhraseItem("Felpróbálhatom?", "Bunu deneyebilir miyim?", Icons.Default.FrontHand),
                PhraseItem("Megveszem.", "Bunu alıyorum.", Icons.Default.Payments),
                PhraseItem("Csak nézelődöm.", "Sadece bakıyorum.", Icons.Default.Search),
                PhraseItem("Van ebből más méret?", "Bunun başka bedeni var mı?", Icons.Default.Info),
                PhraseItem("Hol van a bejárat/kijárat?", "Giriş/çıkış nerede?", Icons.AutoMirrored.Filled.ExitToApp),
                PhraseItem("Tudok szobát foglalni?", "Oda rezerve edebilir miyim?", Icons.Default.Bed),
                PhraseItem("Benne van a reggeli?", "Kahvaltı dahil mi?", Icons.Default.FreeBreakfast)
            )),
            CountryItem("BELÇİKA", Locale.FRANCE, listOf(
                PhraseItem("Bonjour", "Merhaba / İyi günler", Icons.Default.WavingHand),
                PhraseItem("L'addition, s'il vous plaît", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Où est la gare ?", "İstasyon nerede?", Icons.Default.DirectionsTransit),
                PhraseItem("Merci", "Teşekkür ederim", Icons.Default.RecordVoiceOver),
                PhraseItem("C'est combien ?", "Bu ne kadar?", Icons.Default.Payment),
                PhraseItem("Pardon", "Affedersiniz", Icons.Default.RecordVoiceOver),
                PhraseItem("Quelle heure est-il ?", "Saat kaç?", Icons.Default.AccessTime),
                PhraseItem("Où sont les toilettes ?", "Tuvalet nerede?", Icons.Default.Wc),
                PhraseItem("Bonsoir", "İyi akşamlar", Icons.Default.NightsStay),
                PhraseItem("Au revoir", "Hoşçakalın", Icons.Default.WavingHand),
                PhraseItem("Aidez-moi", "Yardım edin", Icons.AutoMirrored.Filled.Help),
                PhraseItem("Je voudrais acheter un billet", "Bir bilet almak istiyorum", Icons.Default.ConfirmationNumber),
                PhraseItem("Quel est le mot de passe du Wi-Fi ?", "Wi-Fi şifresi nedir?", Icons.Default.Wifi),
                PhraseItem("Pourriez-vous nous prendre en photo ?", "Fotoğrafımızı çekebilir misiniz?", Icons.Default.CameraAlt),
                PhraseItem("Parlez-vous anglais ?", "İngilizce biliyor musunuz?", Icons.Default.Language),
                PhraseItem("Pourriez-vous parler plus lentement, s'il vous plaît ?", "Lütfen daha yavaş konuşur musunuz?", Icons.Default.RecordVoiceOver),
                PhraseItem("Où sommes-nous exactement sur la carte ?", "Haritada şu an tam olarak neredeyiz?", Icons.Default.Map),
                PhraseItem("Comment puis-je aller à cette adresse ?", "Bu adrese nasıl gidebilirim?", Icons.Default.LocationOn),
                PhraseItem("Quel est le prochain arrêt ?", "Bir sonraki durak neresi?", Icons.Default.DirectionsBus),
                PhraseItem("Où est la station de taxis ?", "Taksi durağı nerede?", Icons.Default.LocalTaxi),
                PhraseItem("Pourriez-vous m'appeler un taxi ?", "Bana bir taksi çağırabilir misiniz?", Icons.Default.LocalTaxi),
                PhraseItem("Est-ce que vous acceptez la carte de crédit ?", "Kredi kartı geçiyor mu?", Icons.Default.CreditCard),
                PhraseItem("Acceptez-vous uniquement les espèces ?", "Sadece nakit mi kabul ediyorsunuz?", Icons.Default.Payments),
                PhraseItem("Je n'ai pas de monnaie.", "Üzerimde bozuk para yok.", Icons.Default.Savings),
                PhraseItem("Puis-je avoir un reçu ?", "Fiş alabilir miyim?", Icons.Default.Receipt),
                PhraseItem("Est-ce que tout est inclus dans le prix ?", "Ücretin içine her şey dahil mi?", Icons.Default.AllInclusive),
                PhraseItem("Puis-je voir le menu ?", "Menüyü görebilir miyim?", Icons.Default.RestaurantMenu),
                PhraseItem("Est-ce que ce produit est frais ?", "Bu ürün taze mi?", Icons.Default.Eco),
                PhraseItem("Quels sont les ingrédients ?", "Bunun içinde hangi malzemeler var?", Icons.Default.Fastfood),
                PhraseItem("J'ai une allergie.", "Alerjim var.", Icons.Default.Warning),
                PhraseItem("Pouvez-vous emballer le reste de la nourriture ?", "Kalan yemeği paket yapabilir misiniz?", Icons.Default.TakeoutDining),
                PhraseItem("L'eau du robinet est-elle potable ?", "Musluk suyu içilebiliyor mu?", Icons.Default.WaterDrop),
                PhraseItem("À quelle heure ouvrez-vous et fermez-vous ?", "Saat kaçta açılıyor ve kapanıyor?", Icons.Default.Schedule),
                PhraseItem("Où est l'hôpital le plus proche ?", "En yakın hastane nerede?", Icons.Default.LocalHospital),
                PhraseItem("Appelez une ambulance !", "Ambulans çağırın!", Icons.Default.MedicalServices),
                PhraseItem("J'ai perdu mon portefeuille/passeport.", "Cüzdanımı/pasaportumu kaybettim.", Icons.Default.ReportProblem),
                PhraseItem("Appelez la police.", "Polisi arayın.", Icons.Default.Security),
                PhraseItem("Y a-t-il une pharmacie à proximité ?", "Yakınlarda eczane var mı?", Icons.Default.MedicalServices),
                PhraseItem("Je me sens mal.", "Kendimi kötü hissediyorum.", Icons.Default.Warning),
                PhraseItem("J'ai besoin d'un médecin.", "Doktora ihtiyacım var.", Icons.Default.LocalHospital),
                PhraseItem("Où est le centre-ville ?", "Şehir merkezi nerede?", Icons.Default.Explore),
                PhraseItem("Que recommandez-vous ?", "Ne tavsiye edersiniz?", Icons.Default.Info),
                PhraseItem("Y a-t-il une remise ?", "İndirim var mı?", Icons.Default.Percent),
                PhraseItem("Puis-je l'essayer ?", "Bunu deneyebilir miyim?", Icons.Default.FrontHand),
                PhraseItem("Je le prends.", "Bunu alıyorum.", Icons.Default.Payments),
                PhraseItem("Je regarde seulement.", "Sadece bakıyorum.", Icons.Default.Search),
                PhraseItem("Avez-vous cela dans une autre taille ?", "Bunun başka bedeni var mı?", Icons.Default.Info),
                PhraseItem("Où est l'entrée/la sortie ?", "Giriş/çıkış nerede?", Icons.AutoMirrored.Filled.ExitToApp),
                PhraseItem("Puis-je réserver une chambre ?", "Oda rezerve edebilir miyim?", Icons.Default.Bed),
                PhraseItem("Le petit-déjeuner est-il inclus ?", "Kahvaltı dahil mi?", Icons.Default.FreeBreakfast)
            ))
        )
    }

    val sortedCountries = remember(favorites, searchQuery) {
        countries
            .filter { it.name.contains(searchQuery, ignoreCase = true) }
            .sortedByDescending { it.name in favorites }
    }

    val sortedPhrases = remember(selectedCountry, phraseFavorites, phraseSearchQuery) {
        selectedCountry?.let { country ->
            country.phrases
                .filter {
                    it.localText.contains(phraseSearchQuery, ignoreCase = true) ||
                            it.translation.contains(phraseSearchQuery, ignoreCase = true)
                }
                .sortedByDescending {
                    "${country.name}_${it.localText}" in phraseFavorites
                }
        } ?: emptyList()
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        if (selectedCountry == null) stringResource(R.string.voice_default_title) else selectedCountry!!.name,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedCountry != null) {
                            selectedCountry = null
                            phraseSearchQuery = "" // Geri çıkarken aramayı temizle
                        } else {
                            onBack() // Ana ekrandayken aktiviteyi kapat
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.voice_back_desc))
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        if (selectedCountry == null) {
            // Ülke Listesi Ekranı
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text(stringResource(R.string.voice_search_country_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                items(sortedCountries) { country ->
                    val isFav = country.name in favorites
                    ElevatedCard(
                        onClick = {
                            selectedCountry = country
                            searchQuery = "" // Ülke seçince arama kutusunu temizle
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = country.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                favorites = if (isFav) favorites - country.name else favorites + country.name
                            }) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = stringResource(R.string.voice_fav_desc),
                                    tint = if (isFav) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Kelime Listesi Ekranı
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = phraseSearchQuery,
                        onValueChange = { phraseSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text(stringResource(R.string.voice_search_phrase_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                items(sortedPhrases) { phrase ->
                    val phraseKey = "${selectedCountry!!.name}_${phrase.localText}"
                    val isPhraseFav = phraseKey in phraseFavorites

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sol taraftaki ikon kutusu
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = phrase.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Metinler
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = phrase.localText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = phrase.translation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            // Kelime Favori Butonu
                            IconButton(onClick = {
                                phraseFavorites = if (isPhraseFav) phraseFavorites - phraseKey else phraseFavorites + phraseKey
                            }) {
                                Icon(
                                    imageVector = if (isPhraseFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = stringResource(R.string.voice_fav_desc),
                                    tint = if (isPhraseFav) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Dinleme Butonu (Daha dikkat çekici)
                            FilledIconButton(
                                onClick = {
                                    if (isTtsReady) {
                                        tts?.language = selectedCountry!!.locale
                                        tts?.speak(phrase.localText, TextToSpeech.QUEUE_FLUSH, null, null)
                                    }
                                },
                                enabled = isTtsReady,
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = stringResource(R.string.voice_speak_desc)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}