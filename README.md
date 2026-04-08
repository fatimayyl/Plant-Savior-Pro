# 🌿 Plant Savior Pro

<p align="center">
  <img src="app/src/main/res/drawable/ic_plant_logo.xml" width="80"/>
</p>

<p align="center">
  <b>Anlık Bitki Hastalığı Teşhis Uygulaması</b><br/>
  TÜBİTAK 2209-A Üniversite Öğrencileri Araştırma Projeleri Destekleme Programı
</p>

---

## 📱 Uygulama Hakkında

Plant Savior Pro, domates bitkilerindeki yaprak hastalıklarını yapay zeka ile anlık olarak teşhis eden bir Android mobil uygulamasıdır. Kullanıcılar bitkilerinin fotoğrafını çekerek veya galeriden seçerek saniyeler içinde hastalık tespiti yaptırabilir ve tedavi önerilerine ulaşabilir.

---

## ✨ Özellikler

- 📷 Kamera veya galeriden fotoğraf yükleme
- 🤖 TensorFlow Lite ile cihaz üzerinde AI analizi
- 🦠 10 farklı domates hastalığı teşhisi
- 📊 Güven skoru ve dairesel görselleştirme
- 💊 Kültürel, biyolojik ve kimyasal tedavi önerileri
- 📁 Firebase ile tarama geçmişi kaydı
- 👤 Kullanıcı hesabı (E-posta & Google ile giriş)
- 🔒 Firebase Authentication ile güvenli kimlik doğrulama

---

## 🦠 Teşhis Edilen Hastalıklar

| Hastalık | Etiket |
|---|---|
| Bakteriyel Leke | Tomato_Bacterial_spot |
| Erken Yanıklık | Tomato_Early_blight |
| Geç Yanıklık | Tomato_Late_blight |
| Yaprak Küfü | Tomato_Leaf_Mold |
| Septoria Yaprak Lekesi | Tomato_Septoria_leaf_spot |
| Kırmızı Örümcek | Tomato_Spider_mites |
| Hedef Leke | Tomato_Target_Spot |
| Sarı Yaprak Kıvırcıklık Virüsü | Tomato_YellowLeaf_Curl_Virus |
| Mozaik Virüsü | Tomato_mosaic_virus |
| Sağlıklı Yaprak | Tomato_healthy |

---

## 🛠️ Kullanılan Teknolojiler

| Teknoloji | Kullanım Amacı |
|---|---|
| Kotlin | Ana programlama dili |
| TensorFlow Lite | Cihaz üzerinde AI modeli |
| Firebase Authentication | Kullanıcı girişi |
| Firebase Firestore | Tarama geçmişi veritabanı |
| Google Sign-In | Google ile giriş |
| RecyclerView | Geçmiş listesi |
| Material Design | UI bileşenleri |

---

## 📲 İndirme

<p align="center">
  <a href="https://github.com/fatimayyl/Plant-Savior-Pro/releases/download/v1.0.0/app-debug.apk">
    <img src="https://img.shields.io/badge/APK%20İndir-v1.0.0-green?style=for-the-badge&logo=android" />
  </a>
</p>

> Android 7.0 (API 24) ve üzeri cihazlarda çalışır.  
> Kurulum sırasında **"Bilinmeyen kaynaklardan yüklemeye izin ver"** seçeneğini etkinleştirmeniz gerekebilir.

## 🚀 Kurulum

1. Repoyu klonlayın:
```bash
git clone https://github.com/fatimayyl/Plant-Savior-Pro.git
```

2. Android Studio'da açın

3. Firebase Console'dan `google-services.json` dosyasını indirip `app/` klasörüne ekleyin

4. Firebase Console'da şu servisleri etkinleştirin:
   - Authentication (E-posta ve Google)
   - Firestore Database

5. Projeyi derleyip çalıştırın

---

## 👩‍💻 Geliştiriciler

| İsim | Rol |
|---|---|
| Fatıma Yaylı | Geliştirici |
| Amine Cemile Doğru | Geliştirici |
| Zeynep Belemir Şuva | Geliştirici |

**Danışman:** Doç. Dr. Selman Hızal  
**Kurum:** Sakarya Uygulamalı Bilimler Üniversitesi  
**Proje:** Bitirme Çalışması

---
