package com.plantdisease.detector

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent

class FeedbackStatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_stats)
        supportActionBar?.hide()

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        loadStats()

        // Bottom Nav
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
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

    private fun loadStats() {
        val db = FirebaseFirestore.getInstance()

        db.collection("feedback")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "Henüz geri bildirim yok!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val total = documents.size()
                var correct = 0

                // Hastalık bazlı istatistik
                val diseaseTotal   = mutableMapOf<String, Int>()
                val diseaseCorrect = mutableMapOf<String, Int>()

                // Karışıklık matrisi: predicted -> actual
                val confusionMap = mutableMapOf<String, MutableMap<String, Int>>()

                // Yorumlar
                val comments = mutableListOf<Pair<String, String>>() // disease, comment

                documents.forEach { doc ->
                    val disease      = doc.getString("disease") ?: return@forEach
                    val isCorrect    = doc.getBoolean("isCorrect") ?: return@forEach
                    val correctLabel = doc.getString("correctLabel") ?: ""
                    val comment      = doc.getString("comment") ?: ""

                    diseaseTotal[disease]   = (diseaseTotal[disease] ?: 0) + 1
                    if (isCorrect) {
                        correct++
                        diseaseCorrect[disease] = (diseaseCorrect[disease] ?: 0) + 1
                    } else if (correctLabel.isNotEmpty()) {
                        // Karışıklık matrisi güncelle
                        val inner = confusionMap.getOrPut(disease) { mutableMapOf() }
                        inner[correctLabel] = (inner[correctLabel] ?: 0) + 1
                    }

                    if (comment.isNotEmpty()) {
                        comments.add(Pair(disease, comment))
                    }
                }

                // Genel istatistikler
                val accuracyRate = if (total > 0) (correct * 100 / total) else 0
                findViewById<TextView>(R.id.tvTotalFeedback).text = total.toString()
                findViewById<TextView>(R.id.tvAccuracyRate).text  = "%$accuracyRate"

                // Hastalık bazlı doğruluk
                val layoutDiseaseStats = findViewById<LinearLayout>(R.id.layoutDiseaseStats)
                layoutDiseaseStats.removeAllViews()

                diseaseTotal.entries
                    .sortedByDescending { it.value }
                    .forEach { (disease, count) ->
                        val correctCount = diseaseCorrect[disease] ?: 0
                        val rate = correctCount * 100 / count
                        val displayName = DiseaseAdvice.getAdvice(disease).displayName

                        // Satır
                        val row = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { bottomMargin = 12 }
                        }

                        // İsim + oran
                        val headerRow = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                        }
                        val nameText = TextView(this).apply {
                            text = displayName
                            textSize = 13f
                            setTextColor(0xFF333333.toInt())
                            layoutParams = LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        }
                        val rateText = TextView(this).apply {
                            text = "%$rate ($correctCount/$count)"
                            textSize = 13f
                            setTextColor(if (rate >= 70) 0xFF2E7D32.toInt() else 0xFFB71C1C.toInt())
                            textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                        }
                        headerRow.addView(nameText)
                        headerRow.addView(rateText)

                        // Progress bar
                        val progressBar = ProgressBar(
                            this, null,
                            android.R.attr.progressBarStyleHorizontal
                        ).apply {
                            max = 100
                            progress = rate
                            progressTintList = android.content.res.ColorStateList.valueOf(
                                if (rate >= 70) 0xFF4CAF50.toInt() else 0xFFB71C1C.toInt()
                            )
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 16
                            ).apply { topMargin = 4 }
                        }

                        row.addView(headerRow)
                        row.addView(progressBar)
                        layoutDiseaseStats.addView(row)
                    }

                // En çok karıştırılan hastalıklar
                val layoutConfused = findViewById<LinearLayout>(R.id.layoutConfusedDiseases)
                layoutConfused.removeAllViews()

                if (confusionMap.isEmpty()) {
                    val emptyText = TextView(this).apply {
                        text = "Henüz karışıklık verisi yok 🎉"
                        textSize = 13f
                        setTextColor(0xFF888888.toInt())
                    }
                    layoutConfused.addView(emptyText)
                } else {
                    confusionMap.entries
                        .sortedByDescending { it.value.values.sum() }
                        .take(5)
                        .forEach { (predicted, actualMap) ->
                            val mostConfused = actualMap.maxByOrNull { it.value }
                            if (mostConfused != null) {
                                val row = TextView(this).apply {
                                    text = "• ${DiseaseAdvice.getAdvice(predicted).displayName} " +
                                            "→ ${DiseaseAdvice.getAdvice(mostConfused.key).displayName} " +
                                            "(${mostConfused.value} kez)"
                                    textSize = 13f
                                    setTextColor(0xFF333333.toInt())
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    ).apply { bottomMargin = 8 }
                                }
                                layoutConfused.addView(row)
                            }
                        }
                }

                // Son yorumlar
                val layoutComments = findViewById<LinearLayout>(R.id.layoutComments)
                layoutComments.removeAllViews()

                if (comments.isEmpty()) {
                    val emptyText = TextView(this).apply {
                        text = "Henüz yorum yok"
                        textSize = 13f
                        setTextColor(0xFF888888.toInt())
                    }
                    layoutComments.addView(emptyText)
                } else {
                    comments.takeLast(5).reversed().forEach { (disease, comment) ->
                        val card = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            setBackgroundColor(0xFFF9F9F9.toInt())
                            setPadding(16, 12, 16, 12)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { bottomMargin = 8 }
                        }
                        val diseaseText = TextView(this).apply {
                            text = DiseaseAdvice.getAdvice(disease).displayName
                            textSize = 11f
                            setTextColor(0xFF2E7D32.toInt())
                        }
                        val commentText = TextView(this).apply {
                            text = "\"$comment\""
                            textSize = 13f
                            setTextColor(0xFF333333.toInt())
                        }
                        card.addView(diseaseText)
                        card.addView(commentText)
                        layoutComments.addView(card)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Veriler yüklenemedi!", Toast.LENGTH_SHORT).show()
            }
    }
}