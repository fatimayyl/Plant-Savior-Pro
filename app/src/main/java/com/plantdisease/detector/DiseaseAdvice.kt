package com.plantdisease.detector

data class Advice(
    val displayName: String,
    val description: String,
    val cultural: String,
    val biological: String,
    val chemical: String
)

object DiseaseAdvice {

    fun getAdvice(label: String): Advice {
        return when (label) {

            "Tomato_Bacterial_spot" -> Advice(
                displayName = "🦠 Bakteriyel Leke",
                description = "Xanthomonas bakterisinin neden olduğu hastalık. Yapraklarda küçük, kahverengi, su ile ıslanmış görünümlü lekeler oluşur. Sıcak ve nemli havalarda hızla yayılır.",
                cultural = """
                    • Sertifikalı, hastalıksız tohum kullanın
                    • Damla sulama tercih edin, yaprakları ıslatmaktan kaçının
                    • Hasta bitki artıklarını tarladan uzaklaştırın ve yakın
                    • En az 2 yıl domates dışı ürünlerle münavebe yapın
                    • Bitkileri gereğinden sık dikmekten kaçının
                """.trimIndent(),
                biological = """
                    • Bakır içerikli biyofungisitler uygulayın
                    • Bacillus subtilis içeren biyopestisitler kullanın
                    • Sabah saatlerinde uygulama yapın
                """.trimIndent(),
                chemical = """
                    • Bakır oksiklorit veya bakır hidroksit içeren preparatlar
                    • Mancozeb + bakır kombinasyonu
                    ⚠️ Kesin tanı ve ilaç dozu için mutlaka bir ziraat mühendisine danışın
                """.trimIndent()
            )

            "Tomato_Early_blight" -> Advice(
                displayName = "🍂 Erken Yanıklık",
                description = "Alternaria solani fungusunun neden olduğu hastalık. Yapraklarda önce alt yapraklarda, halka şeklinde koyu kahverengi lekeler oluşur. Lekeler büyüyerek yaprak sararmaya ve dökülmeye başlar.",
                cultural = """
                    • Alt yaprakları budayarak hava sirkülasyonunu artırın
                    • Aşırı azot gübrelemesinden kaçının
                    • Sulama suyunun yapraklara değmemesine dikkat edin
                    • Hasta yaprak ve bitki artıklarını imha edin
                    • Dayanıklı çeşitler tercih edin
                """.trimIndent(),
                biological = """
                    • Trichoderma harzianum içeren preparatlar uygulayın
                    • Bacillus subtilis bazlı biyofungisitler kullanın
                    • Neem yağı karışımı püskürtün
                """.trimIndent(),
                chemical = """
                    • Mancozeb, Chlorothalonil veya Azoxystrobin içeren fungisitler
                    • 7-10 günlük aralıklarla koruyucu uygulama yapın
                    ⚠️ Kesin tanı ve ilaç dozu için mutlaka bir ziraat mühendisine danışın
                """.trimIndent()
            )

            "Tomato_Late_blight" -> Advice(
                displayName = "🌧️ Geç Yanıklık",
                description = "Phytophthora infestans'ın neden olduğu, çok hızlı yayılan tehlikeli bir hastalık. Yapraklarda koyu yeşil-kahverengi, yağlı görünümlü lekeler oluşur. Serin ve yağışlı havalarda salgın yapabilir.",
                cultural = """
                    • Erken uyarı sistemlerini takip edin
                    • Yüksek nemden kaçının, seralarda havalandırmayı artırın
                    • Hasta bitkileri hemen tarladan uzaklaştırın
                    • Dayanıklı domates çeşitleri tercih edin
                    • Münavebe uygulayın
                """.trimIndent(),
                biological = """
                    • Bakır bazlı biyopestisitler koruyucu amaçlı kullanılabilir
                    • Bacillus amyloliquefaciens içeren preparatlar
                """.trimIndent(),
                chemical = """
                    • Metalaxyl + Mancozeb kombinasyonu
                    • Cymoxanil veya Dimethomorph içeren fungisitler
                    • Hastalık görüldüğünde hemen müdahale edin
                    ⚠️ Kesin tanı ve ilaç dozu için mutlaka bir ziraat mühendisine danışın
                """.trimIndent()
            )

            "Tomato_Leaf_Mold" -> Advice(
                displayName = "🍃 Yaprak Küfü",
                description = "Passalora fulva fungusunun neden olduğu hastalık. Yaprakların üst yüzeyinde sarı lekeler, alt yüzeyinde ise kadifemsi, zeytin yeşili küf tabakası oluşur. Özellikle seralarda sorun yaratır.",
                cultural = """
                    • Sera nem oranını %85'in altında tutun
                    • İyi havalandırma sağlayın
                    • Damla sulama kullanın
                    • Hasta yaprakları erkenden toplayıp imha edin
                    • Sezon sonunda sera yüzeylerini dezenfekte edin
                """.trimIndent(),
                biological = """
                    • Trichoderma türleri içeren preparatlar
                    • Bacillus subtilis uygulaması
                """.trimIndent(),
                chemical = """
                    • Chlorothalonil, Mancozeb veya Difenoconazole içeren fungisitler
                    • Koruyucu uygulamalar daha etkilidir
                    ⚠️ Kesin tanı ve ilaç dozu için mutlaka bir ziraat mühendisine danışın
                """.trimIndent()
            )

            "Tomato_Septoria_leaf_spot" -> Advice(
                displayName = "🔵 Septoria Yaprak Lekesi",
                description = "Septoria lycopersici fungusunun neden olduğu hastalık. Küçük, yuvarlak, koyu kenarlı ve açık gri merkezli çok sayıda leke oluşur. Alt yapraklardan başlayarak yukarı doğru ilerler.",
                cultural = """
                    • Alt yaprakları budayın ve imha edin
                    • Hava sirkülasyonunu iyileştirin
                    • Yaprak ıslanmasını önleyin
                    • Toprak sıçramasını önlemek için malç kullanın
                    • Münavebe uygulayın
                """.trimIndent(),
                biological = """
                    • Bakır bazlı preparatlar koruyucu olarak kullanılabilir
                    • Bacillus subtilis uygulaması
                """.trimIndent(),
                chemical = """
                    • Mancozeb, Chlorothalonil veya Trifloxystrobin içeren fungisitler
                    • 10-14 günlük aralıklarla uygulama yapın
                    ⚠️ Kesin tanı ve ilaç dozu için mutlaka bir ziraat mühendisine danışın
                """.trimIndent()
            )

            "Tomato_Spider_mites_Two_spotted_spider_mite" -> Advice(
                displayName = "🕷️ Kırmızı Örümcek",
                description = "Tetranychus urticae'nin neden olduğu zararlı. Yapraklarda sarımsı beneklenme, ince örümcek ağları ve bronzlaşma görülür. Sıcak ve kuru havalarda hızla çoğalır.",
                cultural = """
                    • Düzenli sulama yaparak kuru koşulları önleyin
                    • Bitkileri su spreyi ile yıkayın
                    • Yabancı otları temizleyin
                    • Aşırı azot gübrelemesinden kaçının
                """.trimIndent(),
                biological = """
                    • Phytoseiulus persimilis gibi yırtıcı akarlar bırakın
                    • Neoseiulus californicus kullanın
                    • Neem yağı uygulaması yapın
                """.trimIndent(),
                chemical = """
                    • Abamectin, Bifenazate veya Spirodiclofen içeren akarisitler
                    • İlaç direncini önlemek için farklı etki mekanizmalı ilaçları dönüşümlü kullanın
                    ⚠️ Kesin tanı ve ilaç dozu için mutlaka bir ziraat mühendisine danışın
                """.trimIndent()
            )

            "Tomato__Target_Spot" -> Advice(
                displayName = "🎯 Hedef Leke",
                description = "Corynespora cassiicola fungusunun neden olduğu hastalık. Yapraklarda büyük, yuvarlak, halka şeklinde kahverengi lekeler oluşur. Hedef tahtasına benzer görünümüyle tanınır.",
                cultural = """
                    • Hava sirkülasyonunu iyileştirin
                    • Hasta yaprak ve bitki artıklarını imha edin
                    • Yaprak ıslanmasını minimize edin
                    • Münavebe uygulayın
                """.trimIndent(),
                biological = """
                    • Trichoderma harzianum içeren preparatlar
                    • Bacillus subtilis uygulaması
                """.trimIndent(),
                chemical = """
                    • Azoxystrobin, Boscalid veya Chlorothalonil içeren fungisitler
                    ⚠️ Kesin tanı ve ilaç dozu için mutlaka bir ziraat mühendisine danışın
                """.trimIndent()
            )

            "Tomato__Tomato_YellowLeaf__Curl_Virus" -> Advice(
                displayName = "🟡 Sarı Yaprak Kıvırcıklık Virüsü",
                description = "Beyazsinek (Bemisia tabaci) tarafından taşınan virüs hastalığı. Yapraklarda sararma, kıvırcıklaşma ve yukarı doğru kıvrılma görülür. Bitkinin büyümesi durur, meyve tutumu azalır.",
                cultural = """
                    • Beyazsinek popülasyonunu sarı yapışkan tuzaklarla izleyin
                    • Virüslü bitkileri hemen söküp imha edin
                    • Dayanıklı/toleranslı çeşitler tercih edin
                    • Fide döneminde bitkileri sinekten koruyun
                    • Yabancı otları temizleyin
                """.trimIndent(),
                biological = """
                    • Encarsia formosa gibi beyazsinek parazitoitleri bırakın
                    • Sarı yapışkan tuzaklar kullanın
                    • Neem yağı ile vektörü kontrol edin
                """.trimIndent(),
                chemical = """
                    • Vektör beyazsineğe karşı: İmidacloprid, Thiamethoxam veya Pyriproxyfen
                    • Virüse karşı doğrudan ilaç yoktur, vektör kontrolü esastır
                    ⚠️ Kesin tanı ve ilaç dozu için mutlaka bir ziraat mühendisine danışın
                """.trimIndent()
            )

            "Tomato__Tomato_mosaic_virus" -> Advice(
                displayName = "🦠 Mozaik Virüsü",
                description = "Tütün mozaik virüsü (TMV) veya Domates mozaik virüsü (ToMV) kaynaklı hastalık. Yapraklarda açık ve koyu yeşil mozaik renk deseni, şekil bozukluğu görülür. Temas ve aletler yoluyla kolayca yayılır.",
                cultural = """
                    • Çalışma aletlerini %10 çamaşır suyu ile dezenfekte edin
                    • Sigara içen kişiler bitkiye dokunmadan önce ellerini yıkamalı
                    • Hasta bitkileri hemen söküp imha edin
                    • Dayanıklı çeşitler tercih edin
                    • Yaprak budama sonrası aletleri sterilize edin
                """.trimIndent(),
                biological = """
                    • Virüse karşı doğrudan biyolojik ajan yoktur
                    • Bitkinin bağışıklığını artırmak için silisyum uygulaması yapılabilir
                """.trimIndent(),
                chemical = """
                    • Virüse karşı doğrudan kimyasal ilaç bulunmamaktadır
                    • Yaprak biti gibi vektörlere karşı insektisit uygulanabilir
                    ⚠️ Kesin tanı için mutlaka bir ziraat mühendisine danışın
                """.trimIndent()
            )

            "Tomato_healthy" -> Advice(
                displayName = "✅ Sağlıklı Yaprak",
                description = "Bitkinin yaprakları sağlıklı görünmektedir. Hastalık belirtisi tespit edilmedi.",
                cultural = """
                    • Düzenli sulama ve gübreleme programına devam edin
                    • Bitkileri haftada bir hastalık belirtileri açısından kontrol edin
                    • Hava sirkülasyonunu iyi tutun
                    • Yabancı otları düzenli olarak temizleyin
                """.trimIndent(),
                biological = """
                    • Koruyucu amaçlı Bacillus subtilis uygulaması yapılabilir
                    • Faydalı böcekleri destekleyin
                """.trimIndent(),
                chemical = """
                    • Şu an ilaç uygulamasına gerek yoktur
                    • Koruyucu bakır uygulaması yağışlı dönemlerde değerlendirilebilir
                """.trimIndent()
            )

            else -> Advice(
                displayName = label,
                description = "Bu hastalık için bilgi bulunamadı.",
                cultural = "Bir ziraat mühendisine danışın.",
                biological = "Bir ziraat mühendisine danışın.",
                chemical = "Bir ziraat mühendisine danışın."
            )
        }
    }
}