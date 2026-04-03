package com.plantdisease.detector

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class HistoryItem(
    val disease: String = "",
    val confidence: Float = 0f,
    val timestamp: Long = 0L
)

class HistoryAdapter(private val items: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDisease: TextView = view.findViewById(R.id.tvHistoryDisease)
        val tvDate: TextView = view.findViewById(R.id.tvHistoryDate)
        val tvConfidence: TextView = view.findViewById(R.id.tvHistoryConfidence)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val advice = DiseaseAdvice.getAdvice(item.disease)
        holder.tvDisease.text = advice.displayName
        holder.tvConfidence.text = "${"%.0f".format(item.confidence * 100)}%"
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(item.timestamp))
    }

    override fun getItemCount() = items.size
}

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        supportActionBar?.hide()

        // Geri butonu
        findViewById<LinearLayout>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rvHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(uid)
            .collection("history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { documents ->
                val items = documents.map { doc ->
                    HistoryItem(
                        disease = doc.getString("disease") ?: "",
                        confidence = (doc.getDouble("confidence") ?: 0.0).toFloat(),
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
                recyclerView.adapter = HistoryAdapter(items)
                if (items.isEmpty()) {
                    Toast.makeText(this, "Henüz tarama geçmişi yok!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Geçmiş yüklenemedi!", Toast.LENGTH_SHORT).show()
            }

        // Bottom Nav
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        findViewById<LinearLayout>(R.id.navHistory).setOnClickListener {
            // Zaten geçmiş sayfasındayız
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}