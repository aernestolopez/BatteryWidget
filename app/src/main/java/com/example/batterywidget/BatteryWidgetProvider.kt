package com.example.batterywidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.RemoteViews


class BatteryWidgetProvider: AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for(widgetId in appWidgetIds){
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object{

        fun readPhoneBattery(context: Context): Pair<Int, Boolean>{
            val bm =context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            val status= intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)?:-1

            val charging=
                status== BatteryManager.BATTERY_STATUS_CHARGING || status== BatteryManager.BATTERY_STATUS_FULL
            return Pair(level, charging)
        }

        fun updateAllWidgets(context: Context) {
            android.util.Log.d("DEBUG_BT", "Recibido: se recibe")
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, BatteryWidgetProvider::class.java)
            )

            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_battery)
            val (phoneBattery, charging)= readPhoneBattery(context)
            BatteryStorage.savePhoneBattery(context, phoneBattery)
            BatteryStorage.savePhoneCharging(context, charging)

            val headsetBattery = BatteryStorage.loadLastHeadsetBattery(context)
            val connected = BatteryStorage.isHeadsetConnected(context)

            val percentText =
                if (headsetBattery != -1 && connected) "$headsetBattery%"
                else "--%"

            val arcBitmap = ArcBitmapGenerator.createDoubleArcBitmap(
                phonePercent = phoneBattery,
                headsetPercent = headsetBattery,
                phoneCharging = charging,
                headsetConnected = connected,
                size = 256
            )

            views.setImageViewBitmap(R.id.widgetArc, arcBitmap)

// Texto central → cascos si existen, si no móvil
            val centerText =
                when {
                    connected && headsetBattery != -1 -> "$headsetBattery%"
                    else -> "--%"
                }

            views.setTextViewText(R.id.widgetCenterText, centerText)

// Icono ⚡
            val phoneBatteryText=
                if(phoneBattery != -1)
                    if(charging)
                        "$phoneBattery% ⚡"
                    else
                        "$phoneBattery%"
                else
                    "--%"

            views.setTextViewText(
                R.id.widgetCharge,
                phoneBatteryText
            )

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}