package com.plantdisease.detector

import android.content.Context
import android.graphics.Bitmap
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class Classifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private val IMG_SIZE = 224
    private val NUM_CLASSES = 10

    // Yaprak olmayan görsel için eşik
    private val LEAF_CONFIDENCE_THRESHOLD = 0.30f

    data class Result(
        val label: String,
        val confidence: Float,
        val allScores: Map<String, Float>,
        val isLeafDetected: Boolean
    )

    init {
        loadLabels()
        loadModelFromFirebase()
    }

    private fun loadModelFromFirebase() {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()

        FirebaseModelDownloader.getInstance()
            .getModel(
                "tomato_disease_model",
                DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
                conditions
            )
            .addOnSuccessListener { model: CustomModel ->
                val modelFile = model.file
                if (modelFile != null) {
                    val options = Interpreter.Options().apply {
                        setNumThreads(4)
                    }
                    interpreter = Interpreter(modelFile, options)
                } else {
                    loadLocalModel()
                }
            }
            .addOnFailureListener {
                loadLocalModel()
            }
    }

    private fun loadLocalModel() {
        try {
            val assetManager = context.assets
            val fileDescriptor = assetManager.openFd("tomato_efficientnet_lite_quant.tflite")
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val modelBuffer = fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelBuffer, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadLabels() {
        labels = context.assets.open("labels.txt")
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }
    }

    fun classify(bitmap: Bitmap): Result {
        val resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true)
        val inputBuffer = ByteBuffer.allocateDirect(1 * IMG_SIZE * IMG_SIZE * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(IMG_SIZE * IMG_SIZE)
        resized.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE)

        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF).toFloat()
            val g = ((pixel shr 8)  and 0xFF).toFloat()
            val b = ( pixel         and 0xFF).toFloat()
            // EfficientNet preprocess_input: 0-255 olduğu gibi
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        val output = Array(1) { FloatArray(NUM_CLASSES) }
        interpreter?.run(inputBuffer, output)

        val scores = output[0]
        val maxIdx = scores.indices.maxByOrNull { scores[it] } ?: 0
        val maxScore = scores[maxIdx]

        val allScores = labels.mapIndexed { i, label ->
            label to (if (i < scores.size) scores[i] else 0f)
        }.toMap()

        // Güven skoru çok düşükse yaprak değil say
        val isLeafDetected = maxScore >= LEAF_CONFIDENCE_THRESHOLD

        return Result(
            label = labels.getOrElse(maxIdx) { "Bilinmiyor" },
            confidence = maxScore,
            allScores = allScores,
            isLeafDetected = isLeafDetected
        )
    }

    fun close() {
        interpreter?.close()
    }
}