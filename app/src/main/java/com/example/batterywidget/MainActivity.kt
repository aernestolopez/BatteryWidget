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
import android.provider.Settings
import android.widget.TextView
import androidx.annotation.RequiresApi
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
                        loadSavedBattery()
                    }
                }

                ACTION_BATTERY_LEVEL_CHANGED -> {
                    val level = intent.getIntExtra(EXTRA_BATTERY_LEVEL, -1)
                    if (device != null && device == connectedDevice && level != -1) {
                        BatteryStorage.save(this@MainActivity, device, level)
                        batteryText.text =
                            getString(R.string.headset_battery, level)
                        deviceText.text = device.name
                    }
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    if (device != null && device == connectedDevice) {
                        connectedDevice = null
                        batteryText.text = getString(R.string.headset_disconnected)
                        deviceText.text = ""
                    }
                }
            }
        }
    }

    private val phoneBatteryReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.N_MR1)
        override fun onReceive(context: Context?, intent: Intent?) {

            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: return
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percent = (level * 100) / scale

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val plug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

            val chargingType = when (plug) {
                BatteryManager.BATTERY_PLUGGED_AC -> " (carga rápida)"
                BatteryManager.BATTERY_PLUGGED_USB -> " (carga normal)"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> " (carga inalámbrica)"
                else -> ""
            }

            val deviceName =
                Settings.Global.getString(contentResolver, Settings.Global.DEVICE_NAME)

            phoneBatteryText.text =
                if (charging)
                    getString(
                        R.string.phone_battery_charging_type,
                        deviceName,
                        percent,
                        chargingType
                    )
                else
                    getString(R.string.phone_battery, deviceName, percent)
        }
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        batteryText = findViewById(R.id.batteryText)
        deviceText = findViewById(R.id.batteryText2)
        phoneBatteryText = findViewById(R.id.batteryText3)

        batteryText.text = getString(R.string.waiting_device)
        deviceText.text = ""

        checkPermissions()

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(ACTION_BATTERY_LEVEL_CHANGED)
        }

        registerReceiver(bluetoothReceiver, filter)
        registerReceiver(
            phoneBatteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        detectConnectedHeadset()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun detectConnectedHeadset() {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter ?: return

        adapter.getProfileProxy(
            this,
            object : BluetoothProfile.ServiceListener {

                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onServiceConnected(
                    profile: Int,
                    proxy: BluetoothProfile
                ) {
                    for (device in proxy.connectedDevices) {
                        if (isHeadset(device)) {
                            connectedDevice = device
                            loadSavedBattery()
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
        val device = connectedDevice ?: run {
            batteryText.text = getString(R.string.waiting_device)
            deviceText.text = ""
            return
        }

        val saved = BatteryStorage.load(this, device)
        if (saved != -1) {
            batteryText.text =
                getString(R.string.last_battery, device.name, saved)
            deviceText.text = device.name
        } else {
            batteryText.text = getString(R.string.connected_waiting_battery)
            deviceText.text = device.name
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            detectConnectedHeadset()
        } else {
            batteryText.text = getString(R.string.permission_error)
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
        return name.contains("buds") ||
                name.contains("head") ||
                name.contains("ear") ||
                name.contains("air")
    }
}
