package com.example.batterywidget

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothReceiver : BroadcastReceiver() {
    companion object {
    const val ACTION_BATTERY_LEVEL_CHANGED =
        "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"
        const val EXTRA_BATTERY_LEVEL =
            "android.bluetooth.device.extra.BATTERY_LEVEL"
        }
    override fun onReceive(context: Context, intent: Intent?) {
        android.util.Log.d("DEBUG_BT", "Recibido: ${intent?.action}")
        val device: BluetoothDevice? = if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.TIRAMISU){
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        }else{
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        }
        when(intent?.action){
            BluetoothDevice.ACTION_ACL_CONNECTED->{
                android.util.Log.d("DEBUG_BT", "Recibidoaaaaaaaaaaaaaaa: ${intent?.action}")
                BatteryStorage.setHeadsetConnected(context,true)
                BatteryWidgetProvider.updateAllWidgets(context)
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED->{
                android.util.Log.d("DEBUG_BT", "Recibidoooooooooooo: ${intent.action}")
                BatteryStorage.setHeadsetConnected(context,false)
                BatteryWidgetProvider.updateAllWidgets(context)
            }

            ACTION_BATTERY_LEVEL_CHANGED->{
                val level= intent.getIntExtra(EXTRA_BATTERY_LEVEL, -1)
                if(level!=-1 && device!=null){
                    BatteryStorage.saveHeadsetBattery(context, device, level)
                    BatteryWidgetProvider.updateAllWidgets(context)
                }
            }
        }

        val updateIntent= Intent("ACTUALIZAR_INTERFAZ_APP")
        context.sendBroadcast(updateIntent)
    }

}


