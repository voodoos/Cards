/**
 * While this is a new implementation (for the sake of learning),
 * this work is greatly inspired by @bewantbe/audio-analyzer-for-android
 *
 * Source : https://github.com/bewantbe/audio-analyzer-for-android/tree/master/audioSpectrumAnalyzer/src/main/java/github/bewantbe/audio_analyzer_for_android
 */

package fr.u31.cards.lib.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.support.v4.content.ContextCompat
import java.lang.Exception

class SamplingThread(val ctx : Context) : Thread() {
    override fun run() {
        val audioSource = MediaRecorder.AudioSource.MIC
        val sampleRateInHz = 48000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat  =AudioFormat.ENCODING_PCM_16BIT
        val bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)

        val record = AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes)

       if(record.state == AudioRecord.STATE_UNINITIALIZED){
           return
       }
    }
}
