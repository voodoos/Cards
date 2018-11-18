/**
 * While this is a new implementation (for the sake of learning),
 * this work is greatly inspired by @bewantbe/audio-analyzer-for-android
 *
 * Source : https://github.com/bewantbe/audio-analyzer-for-android/tree/master/audioSpectrumAnalyzer/src/main/java/github/bewantbe/audio_analyzer_for_android
 */

package fr.u31.cards.lib.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.RECORDSTATE_RECORDING
import android.media.MediaRecorder
import fr.u31.cards.lib.debug
import kotlin.concurrent.thread

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

        var audioData = ShortArray(10)
        record.startRecording()

        val recordingThread = thread {
            val audioData = ShortArray(bufferSizeInBytes)

            while(record.recordingState == RECORDSTATE_RECORDING) {
                record.read(audioData, 0, bufferSizeInBytes)

                debug(audioData.fold(""){ acc, s -> acc + " " + s.toString() })
            }

        }

        debug("Start recording thread")
        recordingThread.start()

        debug("Before sleep")
        sleep(1000000)
        record.stop()


        /*
        debug("Starting recording")
        record.startRecording()

        sleep(1000)

        debug("Stoping recording")
        record.stop()
        */

    }
}
