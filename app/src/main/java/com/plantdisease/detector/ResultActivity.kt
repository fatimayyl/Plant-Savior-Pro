package com.plantdisease.detector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

        val circularProgress = findViewById<CircularProgressView>(R.id.circularProgress)
        circularProgress.setProgress(confidencePct, "")

        findViewById<TextView>(R.id.tvResultDiseaseName).text = advice.displayName

        findViewById<TextView>(R.id.tvConfidenceLabel).text = when {
            confidencePct >= 90 -> "✅ Çok Yüksek Güven"
            confidencePct >= 70 -> "👍 Yüksek Güven"
            confidencePct >= 50 -> "⚠️ Orta Güven"
            else                -> "❗ Düşük Güven"
        }

        if (confidencePct < 50) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Düşük Güven")
                .setMessage("Model bu görselden emin değil (%${"%.0f".format(confidencePct)}). Lütfen daha net bir yaprak fotoğrafı çekin.")
                .setPositiveButton("Tamam", null)
                .show()
        }

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

        findViewById<LinearLayout>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnGoAdvice).setOnClickListener {
            val intent = Intent(this, AdviceActivity::class.java)
            intent.putExtra("disease_label", label)
            intent.putExtra("confidence", confidence)
            startActivity(intent)
        }

        // Geri Bildirim
        val btnFeedbackYes = findViewById<Button>(R.id.btnFeedbackYes)
        val btnFeedbackNo  = findViewById<Button>(R.id.btnFeedbackNo)

        btnFeedbackYes.setOnClickListener {
            saveFeedback(label, confidence, true, null, null)
            btnFeedbackYes.backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF4CAF50.toInt())
            btnFeedbackNo.isEnabled = false
            Toast.makeText(this, "Teşekkürler! 🎉", Toast.LENGTH_SHORT).show()
        }

        btnFeedbackNo.setOnClickListener {
            showDetailedFeedbackDialog(label, confidence, btnFeedbackYes, btnFeedbackNo)
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

    private fun showDetailedFeedbackDialog(
        label: String,
        confidence: Float,
        btnYes: Button,
        btnNo: Button
    ) {
        val diseases = listOf(
            "Tomato_Bacterial_spot",
            "Tomato_Early_blight",
            "Tomato_Late_blight",
            "Tomato_Leaf_Mold",
            "Tomato_Septoria_leaf_spot",
            "Tomato_Spider_mites_Two_spotted_spider_mite",
            "Tomato__Target_Spot",
            "Tomato__Tomato_YellowLeaf__Curl_Virus",
            "Tomato__Tomato_mosaic_virus",
            "Tomato_healthy"
        )

        val displayNames = diseases.map { DiseaseAdvice.getAdvice(it).displayName }.toTypedArray()
        var selectedIndex = -1

        // Önce doğru hastalığı sor
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔍 Doğru hastalık hangisi?")
            .setSingleChoiceItems(displayNames, -1) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Devam") { _, _ ->
                val correctLabel = if (selectedIndex >= 0) diseases[selectedIndex] else null
                // Sonra yorum sor
                showCommentDialog(label, confidence, correctLabel, btnYes, btnNo)
            }
            .setNegativeButton("Atla") { _, _ ->
                showCommentDialog(label, confidence, null, btnYes, btnNo)
            }
            .show()
    }

    private fun showCommentDialog(
        label: String,
        confidence: Float,
        correctLabel: String?,
        btnYes: Button,
        btnNo: Button
    ) {
        val editText = android.widget.EditText(this).apply {
            hint = "Yorumunuzu yazın (isteğe bağlı)"
            setPadding(48, 24, 48, 24)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("💬 Yorum eklemek ister misiniz?")
            .setView(editText)
            .setPositiveButton("Gönder") { _, _ ->
                val comment = editText.text.toString().trim()
                saveFeedback(label, confidence, false, correctLabel, comment.ifEmpty { null })
                btnNo.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFFB71C1C.toInt())
                btnYes.isEnabled = false
                Toast.makeText(this, "Geri bildiriminiz kaydedildi! Teşekkürler 🙏", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Atla") { _, _ ->
                saveFeedback(label, confidence, false, correctLabel, null)
                btnNo.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFFB71C1C.toInt())
                btnYes.isEnabled = false
                Toast.makeText(this, "Geri bildiriminiz kaydedildi!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun saveFeedback(
        label: String,
        confidence: Float,
        isCorrect: Boolean,
        correctLabel: String?,
        comment: String?
    ) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        val feedback = hashMapOf(
            "disease"      to label,
            "confidence"   to confidence,
            "isCorrect"    to isCorrect,
            "correctLabel" to (correctLabel ?: ""),
            "comment"      to (comment ?: ""),
            "userId"       to uid,
            "timestamp"    to System.currentTimeMillis()
        )

        db.collection("feedback").add(feedback)

        // Firebase Analytics
        logFeedbackAnalytics(label, confidence, isCorrect, correctLabel)
    }

    private fun logFeedbackAnalytics(
        label: String,
        confidence: Float,
        isCorrect: Boolean,
        correctLabel: String?
    ) {
        try {
            val analytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(this)
            val bundle = android.os.Bundle().apply {
                putString("disease_label", label)
                putFloat("confidence", confidence)
                putBoolean("is_correct", isCorrect)
                putString("correct_label", correctLabel ?: "unknown")
            }
            analytics.logEvent("model_feedback", bundle)
        } catch (e: Exception) {
            // Analytics yoksa sessizce geç
        }
    }
}