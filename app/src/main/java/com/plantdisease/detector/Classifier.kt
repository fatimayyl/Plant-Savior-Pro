package com.plantdisease.detector

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class Classifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var binaryInterpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    private val IMG_SIZE    = 224
    private val NUM_CLASSES = 10
    private val BINARY_THRESHOLD = 0.75f // binary_threshold.png'den kontrol et

    data class Result(
        val label: String,
        val confidence: Float,
        val allScores: Map<String, Float>,
        val isLeafDetected: Boolean = true
    )

    init {
        loadModel()
        loadBinaryModel()
        loadLabels()
    }

    // ── Ana model (EfficientNet — [0,255]) ──────────────────
    private fun loadModel() {
        val fd     = context.assets.openFd("tomato_efficientnet_lite_quant.tflite")
        val buffer = FileInputStream(fd.fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength
        )
        interpreter = Interpreter(buffer, Interpreter.Options().apply { setNumThreads(4) })
    }

    // ── Binary model (MobileNetV2 — [0,1]) ──────────────────
    private fun loadBinaryModel() {
        val fd     = context.assets.openFd("binary_classifier.tflite")
        val buffer = FileInputStream(fd.fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength
        )
        binaryInterpreter = Interpreter(buffer, Interpreter.Options().apply { setNumThreads(4) })
    }

    private fun loadLabels() {
        labels = context.assets.open("labels.txt")
            .bufferedReader()
            .readLines()
            .filter { it.isNotBlank() }
    }

    // ── Bitmap → ByteBuffer [0,1] — Binary model için ───────
    private fun toByteBuffer01(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true)
        val buf     = ByteBuffer.allocateDirect(1 * IMG_SIZE * IMG_SIZE * 3 * 4)
        buf.order(ByteOrder.nativeOrder())
        val pixels  = IntArray(IMG_SIZE * IMG_SIZE)
        resized.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE)
        for (pixel in pixels) {
            buf.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            buf.putFloat(((pixel shr 8)  and 0xFF) / 255.0f)
            buf.putFloat(( pixel         and 0xFF) / 255.0f)
        }
        return buf
    }

    // ── Yaprak kontrolü ─────────────────────────────────────
    private fun isLeaf(bitmap: Bitmap): Boolean {
        val input  = toByteBuffer01(bitmap)
        val output = Array(1) { FloatArray(1) }
        binaryInterpreter?.run(input, output)
        val score = output[0][0]

        // GEÇİCİ LOG — binary skoru görüntüle
        android.util.Log.d("BINARY_MODEL", "Binary score: $score | threshold: $BINARY_THRESHOLD | isLeaf: ${score >= BINARY_THRESHOLD}")

        return score >= BINARY_THRESHOLD
    }

    // ── Ana sınıflandırma ───────────────────────────────────
    fun classify(bitmap: Bitmap): Result {

        // Önce yaprak kontrolü
        if (!isLeaf(bitmap)) {
            return Result(
                label          = "Domates yaprağı tespit edilemedi",
                confidence     = 0f,
                allScores      = emptyMap(),
                isLeafDetected = false
            )
        }

        // Yaprak varsa hastalık tahmini (EfficientNet — [0,255])
        val resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true)
        val buf     = ByteBuffer.allocateDirect(1 * IMG_SIZE * IMG_SIZE * 3 * 4)
        buf.order(ByteOrder.nativeOrder())
        val pixels  = IntArray(IMG_SIZE * IMG_SIZE)
        resized.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE)
        for (pixel in pixels) {
            buf.putFloat(((pixel shr 16) and 0xFF).toFloat())
            buf.putFloat(((pixel shr 8)  and 0xFF).toFloat())
            buf.putFloat(( pixel         and 0xFF).toFloat())
        }

        val output = Array(1) { FloatArray(NUM_CLASSES) }
        interpreter?.run(buf, output)

        val scores = output[0]
        val maxIdx = scores.indices.maxByOrNull { scores[it] } ?: 0
        val allScores = labels.mapIndexed { i, lbl ->
            lbl to (if (i < scores.size) scores[i] else 0f)
        }.toMap()

        return Result(
            label          = labels.getOrElse(maxIdx) { "Bilinmiyor" },
            confidence     = scores[maxIdx],
            allScores      = allScores,
            isLeafDetected = true
        )
    }

    fun close() {
        interpreter?.close()
        binaryInterpreter?.close()
    }
}