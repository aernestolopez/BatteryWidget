package com.example.batterywidget

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.content.edit

object BatteryStorage {

    private const val PREFS = "bluetooth_battery"
    private const val PREFIX = "battery_"

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun save(context: Context, device: BluetoothDevice, level: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(PREFIX + device.address, level)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun load(context: Context, device: BluetoothDevice): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(PREFIX + device.address, -1)
    }
}
