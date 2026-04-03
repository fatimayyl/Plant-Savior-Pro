package com.plantdisease.detector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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

        // Circular progress
        val circularProgress = findViewById<CircularProgressView>(R.id.circularProgress)
        circularProgress.setProgress(confidencePct, "")

        // Teşhis adı
        findViewById<TextView>(R.id.tvResultDiseaseName).text = advice.displayName

        // Güven etiketi
        findViewById<TextView>(R.id.tvConfidenceLabel).text = when {
            confidencePct >= 90 -> "✅ Çok Yüksek Güven"
            confidencePct >= 70 -> "👍 Yüksek Güven"
            confidencePct >= 50 -> "⚠️ Orta Güven"
            else                -> "❗ Düşük Güven"
        }

        // Güven düşükse uyarı
        if (confidencePct < 50) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Düşük Güven")
                .setMessage("Model bu görselden emin değil (%${"%.0f".format(confidencePct)}). Lütfen daha net bir yaprak fotoğrafı çekin.")
                .setPositiveButton("Tamam", null)
                .show()
        }

        // Diğer olası hastalıklar
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
        findViewById<LinearLayout>(R.id.btnBack).setOnClickListener {
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
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        findViewById<LinearLayout>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}