package fr.u31.cards

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater

import fr.u31.cards.lib.Card
import fr.u31.cards.lib.Cards

import kotlinx.android.synthetic.main.activity_diapo.*
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate
import android.view.WindowManager
import android.R.attr.button
import android.view.animation.Animation
import android.view.animation.AlphaAnimation






class DiapoActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diapo)

        /* Going full brignthness */
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val params = window.attributes
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window.attributes = params



        val l = listOf<String>(
            "do", "ré", "mi", "fa", "sol", "la", "si",
            "do♯", "ré♯", "mi♯", "fa♯", "sol♯", "la♯", "si♯",
            "do♭", "ré♭", "mi♭", "fa♭", "sol♭", "la♭", "si♭"
            )


        val cards = Cards(l.map { s -> Card(s) })


        val layoutInflater:LayoutInflater = LayoutInflater.from(applicationContext)

        Timer().scheduleAtFixedRate(4300, 5000) {
            this@DiapoActivity.runOnUiThread {
                val anim = AlphaAnimation(1.0f, 0.0f)
                anim.duration = 500
                anim.fillAfter = true
                diapoHere.getChildAt(0).startAnimation(anim)

            }
        }
        Timer().scheduleAtFixedRate(0, 5000) {
            this@DiapoActivity.runOnUiThread {
                diapoHere.removeAllViews();
                diapoHere.addView(cards.getRandom().getView(this@DiapoActivity),0)
            }
        }
    }
}
