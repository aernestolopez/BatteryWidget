package com.example.batterywidget

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.content.edit

object BatteryStorage {

    private const val PREFS = "battery_storage"

    private const val KEY_HEADSET_BATTERY = "headset_battery_"
    private const val KEY_LAST_HEADSET = "last_headset"
    private const val KEY_HEADSET_CONNECTED = "headset_connected"

    private const val KEY_PHONE_BATTERY = "phone_battery"
    private const val KEY_PHONE_CHARGING = "phone_charging"


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun saveHeadsetBattery(
        context: Context,
        device: BluetoothDevice,
        level: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(KEY_HEADSET_BATTERY + device.address, level)
            putString(KEY_LAST_HEADSET, device.address)
            putBoolean(KEY_HEADSET_CONNECTED, true)
        }
    }

    fun setHeadsetConnected(context: Context, connected: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_HEADSET_CONNECTED, connected)
        }
    }

    fun loadLastHeadsetBattery(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val address = prefs.getString(KEY_LAST_HEADSET, null) ?: return -1
        return prefs.getInt(KEY_HEADSET_BATTERY + address, -1)
    }

    fun isHeadsetConnected(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_HEADSET_CONNECTED, false)
    }



    fun savePhoneBattery(context: Context, level: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(KEY_PHONE_BATTERY, level)
        }
    }

    fun savePhoneCharging(context: Context, charging: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_PHONE_CHARGING, charging)
        }
    }

    fun loadPhoneBattery(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_PHONE_BATTERY, -1)
    }

    fun isPhoneCharging(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PHONE_CHARGING, false)
    }
}
