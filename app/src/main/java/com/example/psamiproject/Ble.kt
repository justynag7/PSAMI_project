package com.example.psamiproject

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*


class Ble : AppCompatActivity() {

    private lateinit var bluetoothAdapter : BluetoothAdapter

    private lateinit var bleScanner : BluetoothLeScanner

    private lateinit var connectButton : Button

    private lateinit var accelerometerTextView : TextView
    private lateinit var gyroscopeTextView: TextView

    private lateinit var connectedGatt: BluetoothGatt

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { findViewById<Button>(R.id.scan_button).text = if (value) "Zatrzymaj skanowanie" else "Rozpocznij skanowanie" }
        }

    private var isConnected = false
        set(value) {
            field = value
            runOnUiThread { findViewById<Button>(R.id.connect_button).text = if (value) "Rozłącz" else "Połącz" }
        }

    private val scanResults = mutableListOf<ScanResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble)

        accelerometerTextView = findViewById(R.id.accelerometer_textView)
        gyroscopeTextView = findViewById(R.id.gyroscope_textView)

        connectButton = findViewById(R.id.connect_button)

        connectButton.setOnClickListener {
            connectClick()
        }

        findViewById<Button>(R.id.scan_button).setOnClickListener {
            scanClick()
        }
    }

    private fun connectClick() {
        if(isConnected)
        {
            connectedGatt.disconnect()
            isConnected = false
        }
        else
        {
            scanResults.elementAt(0).device.connectGatt(this, false, bluetoothCallback)
            isConnected = true
        }
    }

    private fun scanClick() {
        if(isScanning)
        {
            stopBleScan()
        }
        else
        {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                if (areLocationServicesEnabled(this)) {
                    if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                        Toast.makeText(this, "BLE nieobsługiwany", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                    val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
                    bluetoothAdapter = bluetoothManager.adapter
                    bleScanner = bluetoothAdapter.bluetoothLeScanner
                    startBleScan()
                }
            }
        }
    }

    private fun areLocationServicesEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if(result.device.address == "24:6F:28:15:EA:8E")
            {
                scanResults.add(result)
                connectButton.isEnabled = true
                stopBleScan()
            }
        }
    }

    private val bluetoothCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val deviceAddress = gatt?.device?.address

            if (gatt != null) {
                connectedGatt = gatt
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt?.close()
                }
            } else {
                gatt?.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            gatt?.services?.forEach { bluetoothGattService ->
                if(bluetoothGattService.uuid.toString() == "64627710-c1bb-41ca-b18e-ba04dd708937")
                {
                    gatt.setCharacteristicNotification(bluetoothGattService.getCharacteristic(UUID.fromString("cba1d466-344c-4be3-ab3f-189f80dd7518")), true)
                    val descriptor: BluetoothGattDescriptor = bluetoothGattService.getCharacteristic(UUID.fromString("cba1d466-344c-4be3-ab3f-189f80dd7518")).getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                    )
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                    return
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            if(characteristic?.uuid.toString() == "9b3da85c-ea06-4c41-98fb-929038069269")
            {
                runOnUiThread {
                    gyroscopeTextView.text =
                        "Żyroskop:\n " + (characteristic?.getStringValue(0))
                }
            }
            if(characteristic?.uuid.toString() == "cba1d466-344c-4be3-ab3f-189f80dd7518")
            {
                runOnUiThread {
                    accelerometerTextView.text =
                        "Akcelerometr:\n " + (characteristic?.getStringValue(0))
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            gatt?.services?.forEach { bluetoothGattService ->
                if (bluetoothGattService.uuid.toString() == "64627710-c1bb-41ca-b18e-ba04dd708937") {
                    gatt.setCharacteristicNotification(
                        bluetoothGattService.getCharacteristic(
                            UUID.fromString(
                                "9b3da85c-ea06-4c41-98fb-929038069269"
                            )
                        ), true
                    )
                    val descriptor2: BluetoothGattDescriptor =
                        bluetoothGattService.getCharacteristic(UUID.fromString("9b3da85c-ea06-4c41-98fb-929038069269"))
                            .getDescriptor(
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                            )
                    descriptor2.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor2)
                }
            }
        }
    }

    private fun startBleScan() {
        scanResults.clear()
        bleScanner.startScan(scanCallback)
        isScanning = true
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }
}