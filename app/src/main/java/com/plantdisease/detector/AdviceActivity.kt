package com.plantdisease.detector

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdviceActivity : AppCompatActivity() {

    private lateinit var tvDiseaseName: TextView
    private lateinit var tvConfidence: TextView
    private lateinit var tvCultural: TextView
    private lateinit var tvBiological: TextView
    private lateinit var tvChemical: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advice)
        supportActionBar?.hide()

        tvDiseaseName = findViewById(R.id.tvAdviceDiseaseName)
        tvConfidence  = findViewById(R.id.tvAdviceConfidence)
        tvCultural    = findViewById(R.id.tvCultural)
        tvBiological  = findViewById(R.id.tvBiological)
        tvChemical    = findViewById(R.id.tvChemical)

        // Geri butonu
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val label      = intent.getStringExtra("disease_label") ?: ""
        val confidence = intent.getFloatExtra("confidence", 0f)

        val advice = DiseaseAdvice.getAdvice(label)

        tvDiseaseName.text = advice.displayName
        tvConfidence.text  = "Güven Oranı: %${"%.1f".format(confidence * 100)}"
        tvCultural.text    = advice.cultural
        tvBiological.text  = advice.biological
        tvChemical.text    = advice.chemical

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