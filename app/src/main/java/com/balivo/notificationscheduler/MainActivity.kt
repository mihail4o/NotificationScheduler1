package com.balivo.notificationscheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*

class MainActivity : AppCompatActivity() {

    var mScheduler : JobScheduler? = null
    private val JOB_ID = 0

    lateinit var mDeviceIdle : Switch
    lateinit var mDeviceCharging : Switch
    var mSeekBar :SeekBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDeviceIdle = findViewById(R.id.idleSwitch) as Switch
        mDeviceCharging = findViewById(R.id.chargingSwitch) as Switch

        mSeekBar = findViewById(R.id.seekBar) as SeekBar

        var label = findViewById(R.id.seekBarLabel) as TextView
        var mSeekBarProgress = findViewById(R.id.seekBarProgress) as TextView

        mSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress > 0) {
                    mSeekBarProgress.text = progress.toString() + " s"
                }else {
                    mSeekBarProgress.text = "Not Set"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }

    fun scheduleJob(view:View) {

        mScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val seekBarInteger = mSeekBar!!.progress
        Log.i("BALIVO", seekBarInteger.toString()+" :mSeekBar value")
        val seekBarSet = seekBarInteger > 0

        val networkOptions = findViewById(R.id.networkOptions) as RadioGroup
        val selectedNetworkID = networkOptions.checkedRadioButtonId
        var selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE

        when(selectedNetworkID){
            R.id.noNetwork -> {
                selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE
            }
            R.id.anyNetwork -> {
                selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY
            }
            R.id.wifiNetwork -> {
                selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED
            }
        }

        val serviceName = ComponentName(packageName, NotificationJobService::class.java.name)
        val builder = JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(selectedNetworkOption)

        builder.setRequiresDeviceIdle(mDeviceIdle.isChecked)
        builder.setRequiresCharging(mDeviceCharging.isChecked)

        if (seekBarSet) {
            builder.setOverrideDeadline((seekBarInteger * 1000).toLong())
        }

        val constraintSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE || mDeviceCharging.isChecked
                || mDeviceIdle.isChecked || seekBarSet)

        if(constraintSet) {
            val myJobInfo = builder.build()
            mScheduler!!.schedule(myJobInfo)
            Toast.makeText(this, "Job was scheduled!", Toast.LENGTH_LONG).show()

        } else {
            Toast.makeText(this, "No constaints sets!", Toast.LENGTH_LONG).show()
        }

    }

    fun cancelJobs(view:View) {
        if (mScheduler !=null) {
            mScheduler!!.cancelAll()
            mScheduler = null
            Toast.makeText(this,"Job Canceled!", Toast.LENGTH_LONG).show()
        }
    }
}
