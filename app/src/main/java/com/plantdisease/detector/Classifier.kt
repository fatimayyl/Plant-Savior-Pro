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
    private var labels: List<String> = emptyList()

    private val IMG_SIZE = 224
    private val NUM_CLASSES = 10

    data class Result(
        val label: String,
        val confidence: Float,
        val allScores: Map<String, Float>
    )

    init {
        loadModel()
        loadLabels()
    }

    private fun loadModel() {
        val assetManager = context.assets
        val fileDescriptor = assetManager.openFd("tomato_efficientnet_lite_quant.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val modelBuffer = fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )

        val options = Interpreter.Options()
        options.setNumThreads(4)
        interpreter = Interpreter(modelBuffer, options)
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

            // EfficientNet preprocessing: (x / 255 - mean) / std
            inputBuffer.putFloat((r / 255.0f - 0.485f) / 0.229f)
            inputBuffer.putFloat((g / 255.0f - 0.456f) / 0.224f)
            inputBuffer.putFloat((b / 255.0f - 0.406f) / 0.225f)
        }

        val output = Array(1) { FloatArray(NUM_CLASSES) }
        interpreter?.run(inputBuffer, output)

        val scores = output[0]
        val maxIdx = scores.indices.maxByOrNull { scores[it] } ?: 0
        val allScores = labels.mapIndexed { i, label ->
            label to (if (i < scores.size) scores[i] else 0f)
        }.toMap()

        return Result(
            label = labels.getOrElse(maxIdx) { "Bilinmiyor" },
            confidence = scores[maxIdx],
            allScores = allScores
        )
    }

    fun close() {
        interpreter?.close()
    }
}