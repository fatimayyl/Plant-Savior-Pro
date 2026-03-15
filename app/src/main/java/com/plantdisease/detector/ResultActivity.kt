package com.plantdisease.detector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        supportActionBar?.hide()

        val label      = intent.getStringExtra("disease_label") ?: ""
        val confidence = intent.getFloatExtra("confidence", 0f)
        val allScores  = intent.getSerializableExtra("all_scores") as? HashMap<String, Float> ?: hashMapOf()

        val advice = DiseaseAdvice.getAdvice(label)
        val confidencePct = confidence * 100

        // Eski progress bar satırlarını sil, bunu ekle:
        val circularProgress = findViewById<CircularProgressView>(R.id.circularProgress)
        circularProgress.setProgress(confidencePct, "")

        findViewById<TextView>(R.id.tvResultDiseaseName).text = advice.displayName


        findViewById<TextView>(R.id.tvConfidenceLabel).text = when {
            confidencePct >= 90 -> "✅ Çok Yüksek Güven"
            confidencePct >= 70 -> "👍 Yüksek Güven"
            confidencePct >= 50 -> "⚠️ Orta Güven"
            else -> "❗ Düşük Güven"
        }

        // Güven düşükse uyarı göster
        if (confidencePct < 50) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Düşük Güven")
                .setMessage("Model bu görselden emin değil (%${"%.0f".format(confidencePct)}). Lütfen daha net bir yaprak fotoğrafı çekin.")
                .setPositiveButton("Tamam", null)
                .show()
        }

        // Diğer hastalıklar
        val layoutOther = findViewById<LinearLayout>(R.id.layoutOtherDiseases)
        allScores.entries
            .filter { it.key != label }
            .sortedByDescending { it.value }
            .take(2)
            .forEach { entry ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 8 }
                }
                val nameView = TextView(this).apply {
                    text = DiseaseAdvice.getAdvice(entry.key).displayName
                    textSize = 14f
                    setTextColor(0xFF555555.toInt())
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                val pctView = TextView(this).apply {
                    text = "${"%.1f".format(entry.value * 100)}%"
                    textSize = 14f
                    setTextColor(0xFF888888.toInt())
                }
                row.addView(nameView)
                row.addView(pctView)
                layoutOther.addView(row)
            }

        // Geri butonu
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Tavsiye Al
        findViewById<Button>(R.id.btnGoAdvice).setOnClickListener {
            val intent = Intent(this, AdviceActivity::class.java)
            intent.putExtra("disease_label", label)
            intent.putExtra("confidence", confidence)
            startActivity(intent)
        }

        // Bottom Nav
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
