package com.example.batterywidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.View
import android.widget.RemoteViews

class BatteryWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach {
            updateWidget(context, appWidgetManager, it)
        }
    }

    companion object {

        fun readPhoneBattery(context: Context): Pair<Int, Boolean> {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            val intent = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val charging =
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

            return Pair(level, charging)
        }

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, BatteryWidgetProvider::class.java)
            )
            ids.forEach { updateWidget(context, manager, it) }
        }

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_battery)

            // Usamos un tamaño de 200px para el bitmap para que tenga buena calidad
            val bitmapSize = 200

            // ===== MÓVIL =====
            val (phoneBattery, charging) = readPhoneBattery(context)

            BatteryStorage.savePhoneBattery(context, phoneBattery)
            BatteryStorage.savePhoneCharging(context, charging)

            val phoneArc = ArcBitmapGenerator.createSingleArcBitmap(
                percent = phoneBattery,
                charging = charging,
                enabled = true,
                size = bitmapSize
            )

            views.setImageViewBitmap(R.id.widgetArcPhone, phoneArc)

            val phoneText = if (phoneBattery != -1) {
                 "$phoneBattery"
            } else "--"

             if(charging){
                views.setViewVisibility(R.id.widgetChargingIcon, View.VISIBLE)
            }else{
                 views.setViewVisibility(R.id.widgetChargingIcon, View.GONE)
            }
            
            views.setTextViewText(R.id.widgetPhoneText, phoneText)

            // ===== CASCOS =====
            val headsetBattery = BatteryStorage.loadLastHeadsetBattery(context)
            val connected = BatteryStorage.isHeadsetConnected(context)

            val headsetArc = ArcBitmapGenerator.createSingleArcBitmap(
                percent = if (connected) headsetBattery else 0,
                charging = false,
                enabled = connected,
                size = bitmapSize
            )

            views.setImageViewBitmap(R.id.widgetArcHeadset, headsetArc)

            val headsetText = if (connected && headsetBattery != -1) {
                "$headsetBattery"
            } else "--"

            views.setTextViewText(R.id.widgetHeadsetText, headsetText)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
