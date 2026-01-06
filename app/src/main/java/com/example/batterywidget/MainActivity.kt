package com.example.batterywidget

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var batteryText: TextView
    private lateinit var deviceText: TextView
    private lateinit var phoneBatteryText: TextView

    private var connectedDevice: BluetoothDevice? = null

    companion object {
        const val ACTION_BATTERY_LEVEL_CHANGED =
            "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"
        const val EXTRA_BATTERY_LEVEL =
            "android.bluetooth.device.extra.BATTERY_LEVEL"
    }
    
    private val bluetoothReceiver = object : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(context: Context?, intent: Intent?) {

            val device: BluetoothDevice? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }

            when (intent?.action) {

                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    if (device != null && isHeadset(device)) {
                        connectedDevice = device
                        BatteryStorage.setHeadsetConnected(this@MainActivity, true)
                        loadSavedBattery()
                        BatteryWidgetProvider.updateAllWidgets(this@MainActivity)
                    }
                }

                ACTION_BATTERY_LEVEL_CHANGED -> {
                    val level = intent.getIntExtra(EXTRA_BATTERY_LEVEL, -1)
                    if (device != null && device == connectedDevice && level != -1) {

                        BatteryStorage.saveHeadsetBattery(
                            this@MainActivity,
                            device,
                            level
                        )

                        batteryText.text = "Batería: $level%"
                        deviceText.text = device.name

                        BatteryWidgetProvider.updateAllWidgets(this@MainActivity)

                    }
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    if (device != null && device == connectedDevice) {
                        connectedDevice = null
                        BatteryStorage.setHeadsetConnected(this@MainActivity, false)
                        batteryText.text = "Cascos desconectados"
                        deviceText.text = ""
                        BatteryWidgetProvider.updateAllWidgets(this@MainActivity)
                    }
                }
            }
        }
    }


    private val phoneBatteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: return
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percent = (level * 100) / scale

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val charging =
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

            BatteryStorage.savePhoneBattery(this@MainActivity, percent)
            BatteryStorage.savePhoneCharging(this@MainActivity, charging)

            phoneBatteryText.text =
                if (charging)
                    "Batería móvil: $percent% ⚡"
                else
                    "Batería móvil: $percent%"

            BatteryWidgetProvider.updateAllWidgets(this@MainActivity)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        batteryText = findViewById(R.id.batteryText)
        deviceText = findViewById(R.id.batteryText2)
        phoneBatteryText = findViewById(R.id.batteryText3)

        batteryText.text = "Esperando dispositivo…"

        checkPermissions()

        registerReceiver(
            bluetoothReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(ACTION_BATTERY_LEVEL_CHANGED)
            }
        )

        registerReceiver(
            phoneBatteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        detectConnectedHeadset()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun detectConnectedHeadset() {
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter ?: return

        adapter.getProfileProxy(
            this,
            object : BluetoothProfile.ServiceListener {
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    for (device in proxy.connectedDevices) {
                        if (isHeadset(device)) {
                            connectedDevice = device
                            BatteryStorage.setHeadsetConnected(this@MainActivity, true)
                            loadSavedBattery()
                            BatteryWidgetProvider.updateAllWidgets(this@MainActivity)
                            break
                        }
                    }
                    adapter.closeProfileProxy(profile, proxy)
                }

                override fun onServiceDisconnected(profile: Int) {}
            },
            BluetoothProfile.A2DP
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun loadSavedBattery() {
        val saved = BatteryStorage.loadLastHeadsetBattery(this)
        if (saved != -1 && connectedDevice != null) {
            batteryText.text = "Batería: $saved% (última)"
            deviceText.text = connectedDevice!!.name
        } else {
            batteryText.text = "Conectado (esperando batería)"
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                100
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(bluetoothReceiver)
            unregisterReceiver(phoneBatteryReceiver)
        } catch (_: Exception) {}
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun isHeadset(device: BluetoothDevice): Boolean {
        if (device.bluetoothClass?.majorDeviceClass ==
            BluetoothClass.Device.Major.AUDIO_VIDEO
        ) return true

        val name = device.name?.lowercase() ?: return false
        return listOf("buds", "head", "ear", "air").any { name.contains(it) }
    }
}
