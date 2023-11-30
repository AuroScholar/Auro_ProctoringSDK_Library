package com.example.auroproctoringsdk.voiceDetector

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.auroproctoringsdk.detector.FaceDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoiseDetector {

    private val SAMPLE_RATE = 44100
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

    private var isRunning = false
    fun startNoiseDetector(context: Context, listener: FaceDetector.OnProctoringResultListener) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch{
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            )

            audioRecord.startRecording()
            var isRunning = true

            val buffer = ShortArray(BUFFER_SIZE)
            var sumAmplitude = 0L

            while (isRunning) {
                val readSize = audioRecord.read(buffer, 0, BUFFER_SIZE)
                if (readSize != AudioRecord.ERROR_INVALID_OPERATION) {
                    val amplitude = calculateAmplitude(buffer, readSize!!)

                    for (i in 0 until readSize) {
                        sumAmplitude += Math.abs(buffer[i].toLong())
                    }

                    val averageAmplitude = sumAmplitude / readSize
                    val humanVoiceThreshold = 5000 // 5000
                    val nonHumanVoiceThreshold = 2000
                    var typeOfVoiceDetected: String = ""
                    if (averageAmplitude > humanVoiceThreshold) {
                        typeOfVoiceDetected = "Human Voice Detected"
                    } else if (averageAmplitude <= nonHumanVoiceThreshold) {
                        typeOfVoiceDetected = "Non-Human Voice Detected"
                    } else {
                        typeOfVoiceDetected = "Unknown Voice Detected"
                    }

                    Log.e("TAG", "start: voice  $amplitude")

                    // max voice detected 350
//                    if (amplitude > 350) {
//                    if (amplitude > 700) {
                    if (amplitude > 1500) {
                        listener.onVoiceDetected(amplitude, true, isRunning, typeOfVoiceDetected)
                    } else {
                        listener.onVoiceDetected(amplitude, false, isRunning, typeOfVoiceDetected)
                    }
                }
            }

            audioRecord.stop()
            audioRecord.release()
        }

    }

    fun stop() {
        isRunning = false
    }

    private fun calculateAmplitude(buffer: ShortArray, readSize: Int): Double {
        var sum = 0.0
        for (i in 0 until readSize) {
            sum += buffer[i].toDouble() * buffer[i].toDouble()
        }
        val amplitude = Math.sqrt(sum / readSize)
        return amplitude
    }
}

