package fr.u31.cards

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import fr.u31.cards.lib.Cards
import fr.u31.cards.lib.audio.*
import fr.u31.cards.lib.debug
import fr.u31.cards.lib.deepToString
import fr.u31.cards.lib.make_cards
import kotlinx.android.synthetic.main.activity_diapo.*
import java.util.*
import kotlin.concurrent.timerTask
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.ValueDependentColor
import com.jjoe64.graphview.series.BarGraphSeries




fun fade_out(time : Long) : AlphaAnimation {
    val anim = AlphaAnimation(1.0f, 0.0f)
    anim.duration = time
    anim.fillAfter = true
    return anim
}
fun fade_in(time : Long) : AlphaAnimation {
    val anim = AlphaAnimation(0.0f, 1.0f)
    anim.duration = time
    anim.fillAfter = true
    return anim
}


class DiapoActivity : SamplingActivity() {
    private var timer : Timer? = null
    private var cards : Cards? = null
    private val series = BarGraphSeries(arrayOf())
    private val viewportX = 1500

    fun startTimer() {
        val fadeOutTask = timerTask {
            this@DiapoActivity.runOnUiThread {
                diapoHere.getChildAt(0).startAnimation(fade_out(500))

            }
        }

        val fadeInTask = timerTask {
            this@DiapoActivity.runOnUiThread {
                diapoHere.removeAllViews()

                if(cards != null) {
                    val diapo = (cards as Cards).getRandom().getView(this@DiapoActivity)
                    diapoHere.addView(diapo, 0)

                    diapo.startAnimation(fade_in(100))
                }
            }}


        val checkSuccess = timerTask {
            val peaks = getThread().lastPeakFrequencies

            this@DiapoActivity.runOnUiThread {
                val spectrum = getThread().lastSpectrum?.slice(0..viewportX)

                // Show spectrum:
                if (spectrum != null)
                    series.resetData(
                        spectrum.mapIndexed { i, d ->
                            DataPoint(i.toDouble(), d)
                        }.toTypedArray()
                    )
            }


            if(peaks != null && peaks.size > 0) {
                val peakNotes = peaks.map {
                    val (arr, delta) = Note.nearestNote(it)
                    if (delta > 10) arrayOf() else arr
                }.toTypedArray()
                val nbDos =  peakNotes.countNote2(Note(BaseNote.C, Alteration.None))
                val nbMis =  peakNotes.countNote2(Note(BaseNote.E, Alteration.None))
                val nbSols =  peakNotes.countNote2(Note(BaseNote.G, Alteration.None))

                if(nbDos > 0 && nbMis > 0 && nbSols > 0 && peakNotes.size - (nbDos + nbMis + nbSols) == 0) {
                    this@DiapoActivity.runOnUiThread {
                        diapoInfo.setText("SUCCES")
                    }
                }
                /*
                debug("nbDos", nbDos)
                debug("nbMis", nbMis)
                debug("nbSols", nbSols)
                debug("totalAccord", nbDos + nbMis + nbSols)
                debug("totalTotal", peakNotes.size)
                debug("difference", peakNotes.size - (nbDos + nbMis + nbSols))
                */
            }
        }

        timer = Timer()

        timer?.scheduleAtFixedRate(fadeOutTask, 4300, 5000)
        timer?.scheduleAtFixedRate(fadeInTask, 0, 5000)
        timer?.scheduleAtFixedRate(checkSuccess, 0, 30)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diapo)

        val graph = findViewById<View>(R.id.graph) as GraphView
        // set manual X bounds
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(viewportX.toDouble())
        debug(getThread().bufferSizeInBytes.toDouble())
        // set manual Y bounds
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMinY(1.0)
        graph.viewport.setMaxY(100.0)

        graph.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.NONE
        graph.gridLabelRenderer.isHorizontalLabelsVisible = false
        graph.gridLabelRenderer.isVerticalLabelsVisible = false
        graph.gridLabelRenderer.padding = 0
        graph.addSeries(series)

        // styling
        series.setValueDependentColor { data ->
            Color.rgb(
                50,
                150,
                150
            )/*
            Color.rgb(
                data.x.toInt() * 255 / 4,
                Math.abs(data.y * 255 / 6).toInt(),
                100
            )*/
        }

        series.spacing = 100
        series.isDrawValuesOnTop = false
        series.dataWidth = 500.0
    }

    override fun onStart() {
        super.onStart()

        /* Going full brightness */
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val params = window.attributes
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window.attributes = params

        cards = make_cards(this)

        startTimer()

    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()
    }
}
