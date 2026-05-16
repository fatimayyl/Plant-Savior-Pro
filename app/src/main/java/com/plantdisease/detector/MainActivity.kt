package com.plantdisease.detector

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnGallery: Button
    private lateinit var btnCamera: Button
    private lateinit var btnAnalyze: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutResult: LinearLayout
    private lateinit var tvDiseaseName: TextView
    private lateinit var tvConfidence: TextView
    private lateinit var btnAdvice: Button
    private lateinit var layoutPlaceholder: LinearLayout

    private var selectedBitmap: Bitmap? = null
    private lateinit var classifier: Classifier
    private var lastResult: Classifier.Result? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                val stream = contentResolver.openInputStream(it)
                selectedBitmap = BitmapFactory.decodeStream(stream)
                imgPreview.setImageBitmap(selectedBitmap)
                imgPreview.visibility = View.VISIBLE
                layoutPlaceholder.visibility = View.GONE
                btnAnalyze.visibility = View.VISIBLE
                layoutResult.visibility = View.GONE
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                selectedBitmap = it
                imgPreview.setImageBitmap(it)
                imgPreview.visibility = View.VISIBLE
                layoutPlaceholder.visibility = View.GONE
                btnAnalyze.visibility = View.VISIBLE
                layoutResult.visibility = View.GONE
            }
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        } else {
            Toast.makeText(this, "Kamera izni gerekli!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        // View'ları bağla
        imgPreview       = findViewById(R.id.imgPreview)
        btnGallery       = findViewById(R.id.btnGallery)
        btnCamera        = findViewById(R.id.btnCamera)
        btnAnalyze       = findViewById(R.id.btnAnalyze)
        progressBar      = findViewById(R.id.progressBar)
        layoutResult     = findViewById(R.id.layoutResult)
        tvDiseaseName    = findViewById(R.id.tvDiseaseName)
        tvConfidence     = findViewById(R.id.tvConfidence)
        btnAdvice        = findViewById(R.id.btnAdvice)
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder)

        classifier = Classifier(this)

        // Bottom Navigation
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            // Zaten ana sayfadayız
        }
        findViewById<LinearLayout>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Galeri
        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        }

        // Kamera
        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intent)
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        // Analiz Et
        btnAnalyze.setOnClickListener {
            selectedBitmap?.let { bitmap ->
                analyzeImage(bitmap)
            }
        }

        // Tavsiye
        btnAdvice.setOnClickListener {
            lastResult?.let { result ->
                val intent = Intent(this, AdviceActivity::class.java)
                intent.putExtra("disease_label", result.label)
                intent.putExtra("confidence", result.confidence)
                startActivity(intent)
            }
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        progressBar.visibility = View.VISIBLE
        btnAnalyze.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            val result = classifier.classify(bitmap)

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                btnAnalyze.isEnabled = true

                if (!result.isLeafDetected) {
                    Toast.makeText(
                        this@MainActivity,
                        "⚠️ Lütfen domates yaprağı fotoğrafı çekin",
                        Toast.LENGTH_LONG
                    ).show()
                    return@withContext
                }

                lastResult = result

                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val record = hashMapOf(
                        "disease"    to result.label,
                        "confidence" to result.confidence,
                        "timestamp"  to System.currentTimeMillis()
                    )
                    db.collection("users").document(uid)
                        .collection("history")
                        .add(record)
                }

                // Feedback sayısını kontrol et
                checkFeedbackCountAndNotify()

                val intent = Intent(this@MainActivity, ResultActivity::class.java)
                intent.putExtra("disease_label", result.label)
                intent.putExtra("confidence", result.confidence)
                intent.putExtra("all_scores", HashMap(result.allScores))
                startActivity(intent)
            }
        }
    } 


    private fun checkFeedbackCountAndNotify() {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("feedback").get()
            .addOnSuccessListener { docs ->
                val count = docs.size()
                if (count > 0 && count % 1 == 0) {
                    sendAdminEmail(count)
                }
            }
    }

    private fun sendAdminEmail(count: Int) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val notification = hashMapOf(
            "type"      to "feedback_milestone",
            "count"     to count,
            "message"   to "$count geri bildirim birikti! Model güncelleme zamanı.",
            "timestamp" to System.currentTimeMillis(),
            "isRead"    to false
        )
        db.collection("admin_notifications").add(notification)

        // EmailJS ile e-posta gönder
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = okhttp3.OkHttpClient()
                val json = """
                {
                    "service_id": "service_0idw1uo",
                    "template_id": "template_qn6yu2h",
                    "user_id": "_f0LIgJRWB6z8Ant5",
                    "template_params": {
                        "count": "$count",
                        "message": "$count geri bildirim birikti! Model güncelleme zamanı.",
                        "date": "${java.util.Date()}",
                        "name": "Veridia Pro Sistem"
                    }
                }
            """.trimIndent()

                val mediaType = "application/json".toMediaType()
                val body = json.toRequestBody(mediaType)

                val request = okhttp3.Request.Builder()
                    .url("https://api.emailjs.com/api/v1.0/email/send")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    android.util.Log.d("EMAIL", "✅ Admin e-postası gönderildi!")
                } else {
                    android.util.Log.e("EMAIL", "❌ Hata: ${response.code} ${response.body?.string()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("EMAIL", "❌ Exception: ${e.message}")
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}