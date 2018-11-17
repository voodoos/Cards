package fr.u31.cards

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import fr.u31.cards.lib.Cards
import fr.u31.cards.lib.audio.SamplingThread
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


class DiapoActivity : AppCompatActivity() {
    val PR_RECORD_AUDIO = 1

    var timer : Timer? = null
    var cards : Cards? = null
    val sthread = SamplingThread(this)

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


        timer = Timer()

        timer?.scheduleAtFixedRate(fadeOutTask, 4300, 5000)
        timer?.scheduleAtFixedRate(fadeInTask, 0, 5000)
    }

    fun startSampling() {
        // No permission, no gain
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, we ask for it
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PR_RECORD_AUDIO
                )
            return
        }
        // Permission is granted, we start the sampling thread :
        sthread.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PR_RECORD_AUDIO -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    // permission was granted, yay!
                    startSampling()
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diapo)

        startSampling()
    }

    override fun onStart() {
        super.onStart()

        /* Going full brignthness */
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
