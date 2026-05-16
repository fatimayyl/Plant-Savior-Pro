package com.plantdisease.detector

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        findViewById<TextView>(R.id.tvWelcome).text =
            user?.displayName ?: user?.email?.substringBefore("@") ?: ""
        findViewById<TextView>(R.id.tvEmail).text = user?.email ?: ""

        // Geri butonu
        findViewById<LinearLayout>(R.id.btnBack).setOnClickListener { finish() }

        // İstatistikler
        findViewById<LinearLayout>(R.id.menuStats).setOnClickListener {
            startActivity(Intent(this, FeedbackStatsActivity::class.java))
        }

        // Bilgileri Düzenle
        findViewById<LinearLayout>(R.id.menuEditProfile).setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
            val etName  = view.findViewById<EditText>(R.id.etDialogName)
            val etEmail = view.findViewById<EditText>(R.id.etDialogEmail)
            etName.setText(user?.displayName ?: "")
            etEmail.setText(user?.email ?: "")

            AlertDialog.Builder(this)
                .setTitle("Bilgileri Düzenle")
                .setView(view)
                .setPositiveButton("Kaydet") { _, _ ->
                    val newName  = etName.text.toString().trim()
                    val newEmail = etEmail.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        val updates = UserProfileChangeRequest.Builder()
                            .setDisplayName(newName).build()
                        user?.updateProfile(updates)?.addOnSuccessListener {
                            findViewById<TextView>(R.id.tvWelcome).text = newName
                            Toast.makeText(this, "İsim güncellendi!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (newEmail.isNotEmpty() && newEmail != user?.email) {
                        user?.updateEmail(newEmail)?.addOnSuccessListener {
                            findViewById<TextView>(R.id.tvEmail).text = newEmail
                            Toast.makeText(this, "E-posta güncellendi!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        // Şifre Değiştir
        findViewById<LinearLayout>(R.id.menuChangePassword).setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)
            val etPassword = view.findViewById<EditText>(R.id.etDialogPassword)

            AlertDialog.Builder(this)
                .setTitle("Şifre Değiştir")
                .setView(view)
                .setPositiveButton("Kaydet") { _, _ ->
                    val newPass = etPassword.text.toString().trim()
                    if (newPass.length < 6) {
                        Toast.makeText(this, "En az 6 karakter!", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    user?.updatePassword(newPass)?.addOnSuccessListener {
                        Toast.makeText(this, "Şifre güncellendi!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        // Uygulama Hakkında
        findViewById<LinearLayout>(R.id.menuAbout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Uygulama Hakkında")
                .setMessage(
                    "Uygulama: Plant Savior Pro\n\n" +
                            "Versiyon: 1.0.0\n\n" +
                            "Geliştiriciler:\nFatıma Yaylı\nAmine Cemile Doğru\nZeynep Belemir Şuva\n" +
                            "Danışman: Doç. Dr. Selman Hızal\n\n" +
                            "Kurum: Sakarya Uygulamalı Bilimler Üniversitesi\n\n" +
                            "Proje: TÜBİTAK 2209-A"
                )
                .setPositiveButton("Tamam", null)
                .show()
        }

        // Destek
        findViewById<LinearLayout>(R.id.menuContact).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:plantsaviorpro@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Plant Savior Pro - Destek")
            }
            startActivity(Intent.createChooser(intent, "E-posta Gönder"))
        }

        // Çıkış Yap — XML'de LinearLayout olarak değişti
        findViewById<LinearLayout>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        // Bottom Nav
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        findViewById<LinearLayout>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            // Zaten profil sayfasındayız
        }
    }
}