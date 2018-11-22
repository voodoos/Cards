package fr.u31.cards

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import fr.u31.cards.lib.audio.SamplingThread

open class SamplingActivity : AppCompatActivity() {
    private val PR_RECORD_AUDIO = 1
    private val sthread = SamplingThread(this)

    protected fun getThread() : SamplingThread {
        return sthread
    }

    private fun startSampling() {
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
        startSampling()
    }

    override fun onStart() {
        super.onStart()


        getThread().pleaseStart()
    }

    override fun onStop() {
        super.onStop()

        getThread().pleasePause()
    }

    override fun onDestroy() {
        super.onDestroy()
        getThread().pleaseStop()
    }


}