package com.example.batterywidget

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
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


    private val ACTION_BATTERY_LEVEL_CHANGED =
        "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"
    private val EXTRA_BATTERY_LEVEL =
        "android.bluetooth.device.extra.BATTERY_LEVEL"

    private val ACTION_ACL_CONNECTED =
        "android.bluetooth.device.action.ACL_CONNECTED"
    private val ACTION_ACL_DISCONNECTED =
        "android.bluetooth.device.action.ACL_DISCONNECTED"




    private val bluetoothReceiver = object : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(context: Context?, intent: Intent?) {

            val device =
                intent?.getParcelableExtra<BluetoothDevice>(
                    BluetoothDevice.EXTRA_DEVICE
                )

            when (intent?.action) {


                ACTION_ACL_CONNECTED -> {
                    if (device != null && isHeadset(device)) {
                        connectedDevice = device
                        loadSavedBattery()
                    }
                }


                ACTION_BATTERY_LEVEL_CHANGED -> {
                    val level = intent.getIntExtra(EXTRA_BATTERY_LEVEL, -1)
                    if (
                        device != null &&
                        device == connectedDevice &&
                        level != -1
                    ) {
                        BatteryStorage.save(this@MainActivity, device, level)
                        batteryText.text = "Batería: $level%"
                        deviceText.text = device.name
                    }
                }


                ACTION_ACL_DISCONNECTED -> {
                    if (device != null && device == connectedDevice) {
                        connectedDevice = null
                        batteryText.text = "Cascos desconectados"
                        deviceText.text = ""
                    }
                }
            }
        }
    }

    private val phoneBatteryReceiver = object : BroadcastReceiver(){
        @RequiresApi(Build.VERSION_CODES.N_MR1)
        override fun onReceive(context: Context?, intent: Intent?) {
            val level= intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale= intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percent = scale?.let { (level?.times(100))?.div(it) }

            val status= intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status== BatteryManager.BATTERY_STATUS_FULL
            val deviceName = Settings.Global.getString(contentResolver, Settings.Global.DEVICE_NAME)
            phoneBatteryText.text=
                if(charging)
                    "Bateria de $deviceName: $percent% (cargando)"
            else
                "Bateria de $deviceName: $percent%"
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
        deviceText.text = ""

        checkPermissions()

        val filter = IntentFilter().apply {
            addAction(ACTION_ACL_CONNECTED)
            addAction(ACTION_ACL_DISCONNECTED)
            addAction(ACTION_BATTERY_LEVEL_CHANGED)
        }
        registerReceiver(bluetoothReceiver, filter)
        registerReceiver(phoneBatteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))


        detectConnectedHeadset()
    }

    @SuppressLint("SetTextI18n")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun detectConnectedHeadset() {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return

        adapter.getProfileProxy(
            this,
            object : BluetoothProfile.ServiceListener {

                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    val devices = proxy.connectedDevices
                    for (device in devices) {
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
    @SuppressLint("SetTextI18n")
    private fun loadSavedBattery() {
        val device = connectedDevice ?: run {
            batteryText.text = "Esperando dispositivo…"
            deviceText.text = ""
            return
        }

        val saved = BatteryStorage.load(this, device)
        if (saved != -1) {
            batteryText.text = device.name + " Batería: $saved% (última)"
            deviceText.text = device.name
        } else {
            batteryText.text = "Conectado (esperando batería)"
            deviceText.text = device.name
        }
    }


    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
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
            batteryText.text = "Error: sin permisos"
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
