package fr.u31.cards

import android.os.Bundle
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
            //debug(getThread().lastPeakFrequencies?.deepToString())

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
        timer?.scheduleAtFixedRate(checkSuccess, 0, 250)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diapo)
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
