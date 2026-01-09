package com.example.batterywidget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat

class BatteryMonitorService : Service() {
    private val batteryReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent?) {
            BatteryWidgetProvider.updateAllWidgets(context)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    override fun onCreate() {
        super.onCreate()
        createNotification()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(batteryReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(batteryReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    private fun createNotification() {
        val channelId= "battery_monitor_channel"
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId, "Monitor de Bateria", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .setContentTitle("Monitoreo de Bateria")
            .setContentText("Actualizando Información en tiempo real")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(Color.RED)
            .setColorized(true)
            .setShowWhen(false)



        val notification = builder.build()
        startForeground(1, notification)
    }


    override fun onBind(intent: Intent?)=null

}
