package com.example.batterywidget

import android.Manifest
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var batteryText: TextView
    private lateinit var deviceText: TextView
    private lateinit var phoneBatteryText: TextView

    private var connectedDevice: BluetoothDevice? = null

    private val uiUpdateReceiver=object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshUi()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
        registerReceiver(uiUpdateReceiver, IntentFilter("REFRESH_UI_APP"), RECEIVER_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(uiUpdateReceiver)
    }
    private fun refreshUi() {
        val level= BatteryStorage.loadLastHeadsetBattery(this)
        val connected= BatteryStorage.isHeadsetConnected(this)

        if(connected && level!=-1){
            batteryText.text = "Batería: $level%"
        }else{
            batteryText.text = "Cascos desconectados"
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        val serviceIntent = Intent(this, BatteryMonitorService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        if (hasPermits()) {
            detectConnectedHeadset()
        }
    }

    private fun hasPermits(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            detectConnectedHeadset()
        }
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
