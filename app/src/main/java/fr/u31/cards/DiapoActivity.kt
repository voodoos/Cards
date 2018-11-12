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
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.view.animation.Animation
import android.view.animation.AlphaAnimation
import fr.u31.cards.lib.ImageCard


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

        val i = ContextCompat.getDrawable(getApplicationContext(), R.mipmap.ic_audio_a)
        var c = ImageCard("test", i as Drawable)

        val cards = Cards(l.map { s -> Card(s) })


        val layoutInflater:LayoutInflater = LayoutInflater.from(applicationContext)

        Timer().scheduleAtFixedRate(4300, 5000) {
            this@DiapoActivity.runOnUiThread {
                diapoHere.getChildAt(0).startAnimation(fade_out(500))

            }
        }
        Timer().scheduleAtFixedRate(0, 5000) {
            this@DiapoActivity.runOnUiThread {
                diapoHere.removeAllViews();

                val diapo = c.getView(this@DiapoActivity)
                diapoHere.addView(diapo,0)

                diapo.startAnimation(fade_in(100))

            }
        }
    }
}
