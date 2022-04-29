package com.example.ryangallagher


import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class NotificationService: Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    private val timer = Timer()

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    private open inner class TimeTask(private var time: Int) : TimerTask() {
        override fun run() {
            val intent = Intent(TIMER_UPDATED)
            intent.putExtra(ELAPSED_TIME, time)
            sendBroadcast(intent)
        }
    }

    companion object {
        const val TIMER_UPDATED = "TimerUpdated"
        const val ELAPSED_TIME = "ElapseTime"
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = Notification.Builder(this,
            NotificationCompat.PRIORITY_DEFAULT.toString()
        )
            .setContentTitle("Notification Title")
            .setContentText("notContentText")
            .setSmallIcon(R.drawable.sun)
            .setContentIntent(pendingIntent)
            .setTicker("ticker text")
            .build()

        startForeground(1111, notification)
        return super.onStartCommand(intent, flags, startId)
    }

}