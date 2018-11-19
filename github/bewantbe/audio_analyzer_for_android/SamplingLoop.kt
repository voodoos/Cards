/* Copyright 2014 Eddy Xiao <bewantbe@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package github.bewantbe.audio_analyzer_for_android

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.audiofx.AutomaticGainControl
import android.os.Build
import android.os.SystemClock
import android.util.Log

import java.util.Arrays

import android.provider.SyncStateContract.Helpers.update

/**
 * Read a snapshot of audio data at a regular interval, and compute the FFT
 * @author suhler@google.com
 * bewantbe@gmail.com
 * Ref:
 * https://developer.android.com/guide/topics/media/mediarecorder.html#example
 * https://developer.android.com/reference/android/media/audiofx/AutomaticGainControl.html
 *
 * TODO:
 * See also: High-Performance Audio
 * https://developer.android.com/ndk/guides/audio/index.html
 * https://developer.android.com/ndk/guides/audio/aaudio/aaudio.html
 */

internal class SamplingLoop(private val activity: AnalyzerActivity, private val analyzerParam: AnalyzerParameters) :
    Thread() {
    private val TAG = "SamplingLoop"
    @Volatile
    private var isRunning = true
    @Volatile
    var pause = false
    private var stft: STFT? = null   // use with care

    private var sineGen1: SineGenerator? = null
    private val sineGen2: SineGenerator
    private var spectrumDBcopy: DoubleArray? = null   // XXX, transfers data from SamplingLoop to AnalyzerGraphic

    @Volatile
    var wavSecRemain: Double = 0.toDouble()
    @Volatile
    var wavSec = 0.0

    private var baseTimeMs = SystemClock.uptimeMillis().toDouble()

    private var mdata: DoubleArray? = null

    init {

        pause = (activity.findViewById(R.id.run) as SelectorText).getValue().equals("stop")
        // Signal sources for testing
        val fq0 = java.lang.Double.parseDouble(activity.getString(R.string.test_signal_1_freq1))
        val amp0 =
            Math.pow(10.0, 1 / 20.0 * java.lang.Double.parseDouble(activity.getString(R.string.test_signal_1_db1)))
        val fq1 = java.lang.Double.parseDouble(activity.getString(R.string.test_signal_2_freq1))
        val amp1 =
            Math.pow(10.0, 1 / 20.0 * java.lang.Double.parseDouble(activity.getString(R.string.test_signal_2_db1)))
        val fq2 = java.lang.Double.parseDouble(activity.getString(R.string.test_signal_2_freq2))
        val amp2 =
            Math.pow(10.0, 1 / 20.0 * java.lang.Double.parseDouble(activity.getString(R.string.test_signal_2_db2)))
        if (analyzerParam.audioSourceId === 1000) {
            sineGen1 = SineGenerator(fq0, analyzerParam.sampleRate, analyzerParam.SAMPLE_VALUE_MAX * amp0)
        } else {
            sineGen1 = SineGenerator(fq1, analyzerParam.sampleRate, analyzerParam.SAMPLE_VALUE_MAX * amp1)
        }
        sineGen2 = SineGenerator(fq2, analyzerParam.sampleRate, analyzerParam.SAMPLE_VALUE_MAX * amp2)
    }

    private fun SleepWithoutInterrupt(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    private fun LimitFrameRate(updateMs: Double) {
        // Limit the frame rate by wait `delay' ms.
        baseTimeMs += updateMs
        val delay = (baseTimeMs - SystemClock.uptimeMillis()).toInt().toLong()
        //      Log.i(TAG, "delay = " + delay);
        if (delay > 0) {
            try {
                Thread.sleep(delay)
            } catch (e: InterruptedException) {
                Log.i(TAG, "Sleep interrupted")  // seems never reached
            }

        } else {
            baseTimeMs -= delay.toDouble()  // get current time
            // Log.i(TAG, "time: cmp t="+Long.toString(SystemClock.uptimeMillis())
            //            + " v.s. t'=" + Long.toString(baseTimeMs));
        }
    }

    // Generate test data.
    private fun readTestData(a: ShortArray, offsetInShorts: Int, sizeInShorts: Int, id: Int): Int {
        if (mdata == null || mdata!!.size != sizeInShorts) {
            mdata = DoubleArray(sizeInShorts)
        }
        Arrays.fill(mdata!!, 0.0)
        when (id - 1000) {
            1 -> {
                sineGen2.getSamples(mdata)
                sineGen1!!.addSamples(mdata)
                for (i in 0 until sizeInShorts) {
                    a[offsetInShorts + i] = Math.round(mdata!![i]).toShort()
                }
            }
            // No break, so values of mdata added.
            0 -> {
                sineGen1!!.addSamples(mdata)
                for (i in 0 until sizeInShorts) {
                    a[offsetInShorts + i] = Math.round(mdata!![i]).toShort()
                }
            }
            2 -> for (i in 0 until sizeInShorts) {
                a[i] = (analyzerParam.SAMPLE_VALUE_MAX * (2.0 * Math.random() - 1)) as Short
            }
            else -> Log.w(TAG, "readTestData(): No this source id = " + analyzerParam.audioSourceId)
        }
        // Block this thread, so that behave as if read from real device.
        LimitFrameRate(1000.0 * sizeInShorts / analyzerParam.sampleRate)
        return sizeInShorts
    }

    override fun run() {
        val record: AudioRecord

        val tStart = SystemClock.uptimeMillis()
        try {
            activity.graphInit.join()  // TODO: Seems not working as intended....
        } catch (e: InterruptedException) {
            Log.w(TAG, "run(): activity.graphInit.join() failed.")
        }

        val tEnd = SystemClock.uptimeMillis()
        if (tEnd - tStart < 500) {
            Log.i(TAG, "wait more.." + (500 - (tEnd - tStart)) + " ms")
            // Wait until previous instance of AudioRecord fully released.
            SleepWithoutInterrupt(500 - (tEnd - tStart))
        }

        val minBytes = AudioRecord.getMinBufferSize(
            analyzerParam.sampleRate, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBytes == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "SamplingLoop::run(): Invalid AudioRecord parameter.\n")
            return
        }

        /*
          Develop -> Reference -> AudioRecord
             Data should be read from the audio hardware in chunks of sizes
             inferior to the total recording buffer size.
         */
        // Determine size of buffers for AudioRecord and AudioRecord::read()
        var readChunkSize = analyzerParam.hopLen  // Every hopLen one fft result (overlapped analyze window)
        readChunkSize = Math.min(readChunkSize, 2048)  // read in a smaller chunk, hopefully smaller delay
        var bufferSampleSize = Math.max(minBytes / analyzerParam.BYTE_OF_SAMPLE, analyzerParam.fftLen / 2) * 2
        // tolerate up to about 1 sec.
        bufferSampleSize = Math.ceil(1.0 * analyzerParam.sampleRate / bufferSampleSize).toInt() * bufferSampleSize

        // Use the mic with AGC turned off. e.g. VOICE_RECOGNITION for measurement
        // The buffer size here seems not relate to the delay.
        // So choose a larger size (~1sec) so that overrun is unlikely.
        try {
            if (analyzerParam.audioSourceId < 1000) {
                record = AudioRecord(
                    analyzerParam.audioSourceId, analyzerParam.sampleRate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, analyzerParam.BYTE_OF_SAMPLE * bufferSampleSize
                )
            } else {
                record = AudioRecord(
                    analyzerParam.RECORDER_AGC_OFF, analyzerParam.sampleRate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, analyzerParam.BYTE_OF_SAMPLE * bufferSampleSize
                )
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Fail to initialize recorder.")
            activity.analyzerViews.notifyToast("Illegal recorder argument. (change source)")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Check Auto-Gain-Control status.
            if (AutomaticGainControl.isAvailable()) {
                val agc = AutomaticGainControl.create(
                    record.audioSessionId
                )
                if (agc.enabled)
                    Log.i(TAG, "SamplingLoop::Run(): AGC: enabled.")
                else
                    Log.i(TAG, "SamplingLoop::Run(): AGC: disabled.")
            } else {
                Log.i(TAG, "SamplingLoop::Run(): AGC: not available.")
            }
        }

        Log.i(
            TAG, "SamplingLoop::Run(): Starting recorder... \n" +
                    "  source          : " + analyzerParam.getAudioSourceName() + "\n" +
                    String.format(
                        "  sample rate     : %d Hz (request %d Hz)\n",
                        record.sampleRate,
                        analyzerParam.sampleRate
                    ) +
                    String.format(
                        "  min buffer size : %d samples, %d Bytes\n",
                        minBytes / analyzerParam.BYTE_OF_SAMPLE,
                        minBytes
                    ) +
                    String.format(
                        "  buffer size     : %d samples, %d Bytes\n",
                        bufferSampleSize,
                        analyzerParam.BYTE_OF_SAMPLE * bufferSampleSize
                    ) +
                    String.format(
                        "  read chunk size : %d samples, %d Bytes\n",
                        readChunkSize,
                        analyzerParam.BYTE_OF_SAMPLE * readChunkSize
                    ) +
                    String.format("  FFT length      : %d\n", analyzerParam.fftLen) +
                    String.format("  nFFTAverage     : %d\n", analyzerParam.nFFTAverage)
        )
        analyzerParam.sampleRate = record.sampleRate

        if (record.state == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "SamplingLoop::run(): Fail to initialize AudioRecord()")
            activity.analyzerViews.notifyToast("Fail to initialize recorder.")
            // If failed somehow, leave user a chance to change preference.
            return
        }

        val audioSamples = ShortArray(readChunkSize)
        var numOfReadShort: Int

        stft = STFT(analyzerParam)
        stft!!.setAWeighting(analyzerParam.isAWeighting)
        if (spectrumDBcopy == null || spectrumDBcopy!!.size != analyzerParam.fftLen / 2 + 1) {
            spectrumDBcopy = DoubleArray(analyzerParam.fftLen / 2 + 1)
        }

        val recorderMonitor = RecorderMonitor(analyzerParam.sampleRate, bufferSampleSize, "SamplingLoop::run()")
        recorderMonitor.start()

        //      FPSCounter fpsCounter = new FPSCounter("SamplingLoop::run()");

        val wavWriter = WavWriter(analyzerParam.sampleRate)
        val bSaveWavLoop = activity.bSaveWav  // change of bSaveWav during loop will only affect next enter.
        if (bSaveWavLoop) {
            wavWriter.start()
            wavSecRemain = wavWriter.secondsLeft()
            wavSec = 0.0
            Log.i(TAG, "PCM write to file " + wavWriter.getPath())
        }

        // Start recording
        try {
            record.startRecording()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Fail to start recording.")
            activity.analyzerViews.notifyToast("Fail to start recording.")
            return
        }

        // Main loop
        // When running in this loop (including when paused), you can not change properties
        // related to recorder: e.g. audioSourceId, sampleRate, bufferSampleSize
        // TODO: allow change of FFT length on the fly.
        while (isRunning) {
            // Read data
            if (analyzerParam.audioSourceId >= 1000) {
                numOfReadShort = readTestData(audioSamples, 0, readChunkSize, analyzerParam.audioSourceId)
            } else {
                numOfReadShort = record.read(audioSamples, 0, readChunkSize)   // pulling
            }
            if (recorderMonitor.updateState(numOfReadShort)) {  // performed a check
                if (recorderMonitor.getLastCheckOverrun())
                    activity.analyzerViews.notifyOverrun()
                if (bSaveWavLoop)
                    wavSecRemain = wavWriter.secondsLeft()
            }
            if (bSaveWavLoop) {
                wavWriter.pushAudioShort(audioSamples, numOfReadShort)  // Maybe move this to another thread?
                wavSec = wavWriter.secondsWritten()
                activity.analyzerViews.updateRec(wavSec)
            }
            if (pause) {
                //          fpsCounter.inc();
                // keep reading data, for overrun checker and for write wav data
                continue
            }

            stft!!.feedData(audioSamples, numOfReadShort)

            // If there is new spectrum data, do plot
            if (stft!!.nElemSpectrumAmp() >= analyzerParam.nFFTAverage) {
                // Update spectrum or spectrogram
                val spectrumDB = stft!!.getSpectrumAmpDB()
                System.arraycopy(spectrumDB, 0, spectrumDBcopy!!, 0, spectrumDB.size)
                activity.analyzerViews.update(spectrumDBcopy)
                //          fpsCounter.inc();

                stft!!.calculatePeak()
                activity.maxAmpFreq = stft!!.maxAmpFreq
                activity.maxAmpDB = stft!!.maxAmpDB

                // get RMS
                activity.dtRMS = stft!!.getRMS()
                activity.dtRMSFromFT = stft!!.getRMSFromFT()
            }
        }
        Log.i(TAG, "SamplingLoop::Run(): Actual sample rate: " + recorderMonitor.getSampleRate())
        Log.i(TAG, "SamplingLoop::Run(): Stopping and releasing recorder.")
        record.stop()
        record.release()
        if (bSaveWavLoop) {
            Log.i(TAG, "SamplingLoop::Run(): Ending saved wav.")
            wavWriter.stop()
            activity.analyzerViews.notifyWAVSaved(wavWriter.relativeDir)
        }
    }

    fun setAWeighting(isAWeighting: Boolean) {
        if (stft != null) {
            stft!!.setAWeighting(isAWeighting)
        }
    }

    fun finish() {
        isRunning = false
        interrupt()
    }
}