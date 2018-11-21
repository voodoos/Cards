package fr.u31.cards

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import fr.u31.cards.lib.audio.Note
import kotlinx.android.synthetic.main.activity_test.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class TestActivity : SamplingActivity() {
    private var timer : Timer? = null
    private var data : ArrayList<String> = ArrayList()
    private var adapter : ArrayAdapter<String>? = null

    fun startTimer() {
        val fetchData = timerTask {
            if(getThread().lastPeakFrequencies != null) {
                data.clear()
                getThread().lastPeakFrequencies?.onEach {
                    val (notes, delta) = Note.nearestNote(it)
                    val str = Math.round(it).toString() + "hz " +
                            notes.contentDeepToString() +
                            " (∆: " + Math.round(delta) + ")"
                    data.add(str)
                }
            }


            /* Adapter view must be modified from main thread */
            Handler(Looper.getMainLooper()).post {
                adapter?.notifyDataSetChanged()
            }
        }

        timer = Timer()

        timer?.scheduleAtFixedRate(fetchData, 0, 250)
    }

    fun notif() {
        adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)

        val list = resultList

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)

        list.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        startTimer()
    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()
    }
}