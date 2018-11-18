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
import com.google.corp.productivity.specialprojects.android.fft.RealDoubleFFT
import fr.u31.cards.lib.debug


class SamplingThread(val ctx : Context) : Thread() {
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val sampleRateInHz = 48000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat  = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)

    private val fft = RealDoubleFFT(bufferSizeInBytes) // Maybe a bad value

    private fun decomplexification(data: DoubleArray) : DoubleArray {
        /*
            Code from : https://github.com/bewantbe/audio-analyzer-for-android/blob/master/audioSpectrumAnalyzer/src/main/java/github/bewantbe/audio_analyzer_for_android/STFT.java
         */
        val size = data.size
        val dataOut = DoubleArray(size / 2 + 1)

        // data.length should be a even number
        val scaler = 2.0 * 2.0 / (size * size)  // *2 since there are positive and negative frequency part
        dataOut[0] = data[0] * data[0] * scaler / 4.0

        var j = 1
        var i = 1
        while (i < size - 1) {
            dataOut[j] = (data[i] * data[i] + data[i + 1] * data[i + 1]) * scaler
            i += 2
            j++
        }
        dataOut[j] = data[size - 1] * data[size - 1] * scaler / 4.0

        return dataOut
    }

    override fun run() {

        val record = AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes)

        if(record.state == AudioRecord.STATE_UNINITIALIZED){
            return
        }

        record.startRecording()
        debug("Start recording loop")
        val audioData = ShortArray(bufferSizeInBytes)

        while(record.recordingState == RECORDSTATE_RECORDING) {
            record.read(audioData, 0, bufferSizeInBytes)

            //debug("   " + audioData.fold(""){ acc, s -> acc + " " + s.toString() })
            //debug("   " + audioData.fold(""){ acc, s -> acc + " " + s.toString() })

            var da = DoubleArray(bufferSizeInBytes) { i -> audioData[i].toDouble()}
            fft.ft(da)

            //debug("ft " + audioData.fold(""){ acc, s -> acc + " " + s.toString() })
            da = decomplexification(da)
            val maxIdx = da.indices.maxBy { da[it] } ?: -1
            debug( maxIdx * sampleRateInHz / bufferSizeInBytes)
        }

        record.stop()
        record.release()


        /*
        debug("Starting recording")
        record.startRecording()

        sleep(1000)

        debug("Stoping recording")
        record.stop()
        */

    }
}
