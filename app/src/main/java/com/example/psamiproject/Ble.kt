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
    private lateinit var accelerometerTextView2 : TextView
    private lateinit var gyroscopeTextView2: TextView

    private lateinit var connectedGatt: BluetoothGatt
    private lateinit var connectedGattReka: BluetoothGatt

    private var noga = false
    private var reka = false

    private val addressNoga = "24:6F:28:15:EA:8E"
    private val addressReka = "0C:B8:15:D4:C9:DA"

    private var lastAccXReka = 0.0
    private var accXRekaUpCount = 0 // > 5
    private var accXRekaSkipCount = 0 // < 3
    private var accXRekaDownCount = 0 // > 5
    private var przysiadCount = 0

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
        accelerometerTextView2 = findViewById(R.id.accelerometer2_textView)
        gyroscopeTextView2 = findViewById(R.id.gyroscope2_textView)

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
            connectedGattReka.disconnect()
            isConnected = false
        }
        else
        {
            for(dev in scanResults)
            {
                if(dev.device.address == addressReka)
                {
                    dev.device.connectGatt(this, false, bluetoothCallbackReka)
                }
                else if(dev.device.address == addressNoga)
                {
                    dev.device.connectGatt(this, false, bluetoothCallbackNoga)
                }
            }
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
            if(result.device.address == addressNoga)
            {
                if(!noga)
                {
                    noga = true;
                    scanResults.add(result)
                    if (noga && reka) {
                        connectButton.isEnabled = true
                        stopBleScan()
                    }
                }
            }
            if(result.device.address == addressReka)
            {
                if(!reka) {
                    reka = true;
                    scanResults.add(result)
                    if (noga && reka) {
                        connectButton.isEnabled = true
                        stopBleScan()
                    }
                }
            }
        }
    }

    private val bluetoothCallbackReka: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if(gatt != null) {
                connectedGattReka = gatt
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
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val data = characteristic.uuid.toString()
            if (data == "9b3da85c-ea06-4c41-98fb-929038069269") {
                val value = characteristic.getStringValue(0)
                val value2 = value.split(";")
                val X = value2[0]
                val Y = value2[1]
                val Z = value2[2]

                runOnUiThread {
                    gyroscopeTextView.text = "Żyroskop:\n X=$X Y=$Y Z=$Z"
                }
            }
            else if (data == "cba1d466-344c-4be3-ab3f-189f80dd7518") {
                val val2 = characteristic.getStringValue(0)
                val value = val2.split(";")

                    val X = value[0].toFloat()
                    val Y = value[1].toFloat()
                    val Z = value[2].toFloat()

                        val temp = X - lastAccXReka
                        if(temp > 0.5)
                        {
                            accXRekaUpCount++
                        }
                        else if(temp < -0.5)
                        {
                            accXRekaDownCount++
                        }
                        else
                        {
                            accXRekaSkipCount++
                        }

                        if(accXRekaUpCount > 5 && accXRekaDownCount > 5 && accXRekaSkipCount < 4)
                        {
                            przysiadCount++
                        }

                    runOnUiThread {
                        accelerometerTextView.text = "Akcelerometr:\n X=" + X + " Y=" + Y + " Z=" + Z + "\n Przysiady = " + przysiadCount
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

    private val bluetoothCallbackNoga: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val deviceAddress = gatt?.device?.address

            if(gatt != null)
            {
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
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val data = characteristic.uuid.toString()
            if (data == "9b3da85c-ea06-4c41-98fb-929038069269") {
                val value = characteristic.getStringValue(0).split(";")
                val X = value[0]
                val Y = value[1]
                val Z = value[2]

                runOnUiThread {
                    gyroscopeTextView2.text = "Żyroskop:\n X=$X Y=$Y Z=$Z"
                }
            }
            else if (data == "cba1d466-344c-4be3-ab3f-189f80dd7518") {
                val value = characteristic.getStringValue(0).split(";")
                val X = value[0]
                val Y = value[1]
                val Z = value[2]

                runOnUiThread {
                    accelerometerTextView2.text = "Akcelerometr:\n X=$X Y=$Y Z=$Z"
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
        noga = false
        reka = false
        bleScanner.startScan(scanCallback)
        isScanning = true
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }
}