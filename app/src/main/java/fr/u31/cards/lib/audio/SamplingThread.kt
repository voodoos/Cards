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
import android.media.MediaRecorder
import com.google.corp.productivity.specialprojects.android.fft.RealDoubleFFT
import fr.u31.cards.lib.debug
import kotlin.math.log10


class SamplingThread(val ctx : Context) : Thread() {
    private var shouldStop = false
    private var isPaused = true
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val sampleRateInHz = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat  = AudioFormat.ENCODING_PCM_16BIT
    val bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat) * 3 /* todo: choosen arbitralrly... */

    var lastPeakFrequencies : ArrayList<Double>? = null
    var lastSpectrum : DoubleArray? = null

    private val fft = RealDoubleFFT(bufferSizeInBytes) // Maybe a bad value

    private fun fftCompute(audioData: ShortArray) : DoubleArray {

        var data = DoubleArray(bufferSizeInBytes) { i -> audioData[i].toDouble() }

        // The call to the FFT library:
        fft.ft(data)

        /*
            Decomplexification
            Code from : https://github.com/bewantbe/audio-analyzer-for-android
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

        // Log scale
        for(i in 0..(dataOut.size - 1))
            dataOut[i] = 10.0 * log10(dataOut[i]);

        lastSpectrum = dataOut

        return dataOut
    }

    private fun indexToFreq(idx : Int) : Double {
        return idx.toDouble() * sampleRateInHz.toDouble() / bufferSizeInBytes.toDouble()
    }

    private fun peakAnalysis(spectrum : DoubleArray) : ArrayList<Double> {
        val i_max = spectrum.indices.maxBy { spectrum[it] } ?: -1
        val v_max = spectrum[i_max]

        /* Normalization */
        //val norm_spectrum = spectrum.map { d -> d * 1.0 / (if (v_max == 0.0) 1.0 else v_max) }
        val peakFrequencies = ArrayList<Double>()


        val threshold = (20.0 / 100.0) * v_max
        for (i in 1..(spectrum.size - 2)) {
            val prev = spectrum[i] - spectrum[i-1]
            val next = spectrum[i + 1] - spectrum[i]

            if(prev >= 0 && next <= 0 && spectrum[i] > threshold)
                peakFrequencies.add(
                    indexToFreq(i)
                )
        }
        //debug("threshold", threshold)
        //debug("vmax", v_max)
        //debug(peakFrequencies)
        return peakFrequencies
    }

    override fun run() {

        val record = AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes)

        if(record.state == AudioRecord.STATE_UNINITIALIZED){
            return
        }

        record.startRecording()
        debug("ST", "Start recording loop with buffersize = $bufferSizeInBytes")
        val audioData = ShortArray(bufferSizeInBytes)

        while(!shouldStop) {
            if(isPaused) sleep(500)
            else {
                record.read(audioData, 0, bufferSizeInBytes)

                val da = fftCompute(audioData)

                lastPeakFrequencies = peakAnalysis(da)

                //debug(peakFrequencies.deepToString())
                //debug(lastPeakFrequencies.toString())
/*
            (ctx as Activity).runOnUiThread {
                ctx.diapoInfo.text = peakFrequencies.map{
                        f -> val (arr, dist) = Note.nearestNote(f)
                    Pair(arr.contentDeepToString(), dist)
                }.deepToString()
            }*/
            }
        }

        record.stop()
        record.release()

        debug("ST", "SamplingThread stopping")
    }

    fun pleaseStop() {
        shouldStop = true;
    }

    fun pleaseStart() {
        isPaused = false;
    }

    fun pleasePause() {
        isPaused = true;
    }
}
