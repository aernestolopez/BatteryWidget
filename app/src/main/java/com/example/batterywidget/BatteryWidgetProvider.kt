package com.example.batterywidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
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

        fun updateAllWidgets(context: Context) {
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
        ){
            val views= RemoteViews(context.packageName, R.layout.widget_battery)

            val headsetBattery = BatteryStorage.loadLastHeadsetBattery(context)
            val phoneBattery= BatteryStorage.loadPhoneBattery(context)
            val charging = BatteryStorage.isPhoneCharging(context)

            views.setTextViewText(
                R.id.widgetHeadsetText,
                if(headsetBattery!= -1)"🎧 $headsetBattery%" else "🎧 --%"
            )
            views.setTextViewText(
                R.id.widgetPhoneText,
                if (charging)
                    "📱 ${phoneBattery}% ⚡"
                else
                    "📱 ${phoneBattery}%"
            )

            appWidgetManager.updateAppWidget(widgetId, views)

        }
    }
}