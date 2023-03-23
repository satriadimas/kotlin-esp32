package com.example.kmenkesarduino

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.NonCancellable.message
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity-1"
        private const val ESP32_DEVICE_ADDRESS = "08:B6:1F:34:86:4E"
        private val SERVICE_UUID = UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CB7")
        private val CHARACTERISTIC_UUID = UUID.fromString("0783B03E-8535-B5A0-7140-A304D2495CBB")
        private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
        private const val RUNTIME_PERMISSION_REQUEST_CODE = 2
        private const val REQUEST_ENABLE_BT = 1
        private const val SCAN_PERIOD: Long = 10000
    }

    // Request permissions if they have not been granted yet
    private val permissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)
    private lateinit var bluetoothManager: BluetoothManager
    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private var bluetoothGattService: BluetoothGattService? = null
    private var characteristic: BluetoothGattCharacteristic? = null
    private lateinit var scanCallback: ScanCallback

    private lateinit var context: Context
    private lateinit var activity: Activity

    // SPP UUID service
    private val macTimbangan: String = "08:B6:1F:34:86:4E"
    private val macStadiometer: String = "08:B6:1F:3B:57:2A"
    private val macLila: String = "94:B5:55:2E:45:E6"

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        ActivityCompat.requestPermissions(activity, permissions, ENABLE_BLUETOOTH_REQUEST_CODE)
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }

    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }
    fun Context.hasRequiredRuntimePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                    hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this
        activity = this

        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, "Sorry this device is not supported", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

//        var nik: String? = intent.getStringExtra("nik")
//        var fullName: String? = intent.getStringExtra("full_name")
//        var age: String? = intent.getStringExtra("age")

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvNik = findViewById<TextView>(R.id.tvNik)
        val tvAge = findViewById<TextView>(R.id.tvAge)
        val tvBB = findViewById<TextView>(R.id.tvBB)
        val tvTB = findViewById<TextView>(R.id.tvTB)
        val tvLB = findViewById<TextView>(R.id.tvLB)
        val btnBB = findViewById<Button>(R.id.btnBB)
        val btnTB = findViewById<Button>(R.id.btnTB)
        val btnLB = findViewById<Button>(R.id.btnLB)

        val etTetsUUID = findViewById<EditText>(R.id.etTestUUID)
        val etTestAddress = findViewById<EditText>(R.id.etTestAddress)
        val btnTest = findViewById<Button>(R.id.btnTest)
        val tvTest = findViewById<TextView>(R.id.tvTest)

        btnTest.setOnClickListener {
            val uuids = etTetsUUID.text.toString()
            val address = etTestAddress.text.toString()
        }

//        tvNik.text = nik
//        tvName.text = fullName
//        tvAge.text = age

        btnBB.setOnClickListener {
            tvBB.text = macTimbangan
            startBleScan()
            Log.i(TAG, "btn BB clicked")
        }

        btnTB.setOnClickListener {
            tvTB.text = macStadiometer
        }

        btnLB.setOnClickListener {
            tvLB.text = macLila
        }
    }

    private fun startBleScan() {
        if (!hasRequiredRuntimePermissions()) {
            requestRelevantRuntimePermissions()
        } else { /* TODO: Actually perform scan */ }
    }
    private fun Activity.requestRelevantRuntimePermissions() {
        if (hasRequiredRuntimePermissions()) { return }
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                requestLocationPermission()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                requestBluetoothPermissions()
            }
        }
    }

    private fun requestLocationPermission() {
        runOnUiThread {
            alert {
                title = "Location permission required"
                message = "Starting from Android M (6.0), the system requires apps to be granted " +
                        "location access in order to scan for BLE devices."
                isCancelable = false
                positiveButton(android.R.string.ok) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        RUNTIME_PERMISSION_REQUEST_CODE
                    )
                }
            }.show()
        }
    }
    private fun requestBluetoothPermissions() {
        runOnUiThread {
            alert {
                title = "Bluetooth permissions required"
                message = "Starting from Android 12, the system requires apps to be granted " +
                        "Bluetooth access in order to scan for and connect to BLE devices."
                isCancelable = false
                positiveButton(android.R.string.ok) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ),
                        RUNTIME_PERMISSION_REQUEST_CODE
                    )
                }
            }.show()
        }
    }
}