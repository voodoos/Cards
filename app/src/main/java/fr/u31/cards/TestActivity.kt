package fr.u31.cards

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_test.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class TestActivity : SamplingActivity() {
    private var timer : Timer? = null
    private var data : ArrayList<Double> = ArrayList()
    private var adapter : ArrayAdapter<Double>? = null

    fun startTimer() {
        val fetchData = timerTask {
            if(getThread().lastPeakFrequencies != null) {
                data.clear()
                getThread().lastPeakFrequencies?.onEach { elt : Double -> data.add(elt) }
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

        adapter = ArrayAdapter<Double>(this, android.R.layout.simple_list_item_1, data)

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