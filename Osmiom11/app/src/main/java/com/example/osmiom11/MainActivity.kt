package com.example.osmiom11


import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.telephony.CellInfoLte
import android.telephony.CellSignalStrengthLte
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.*

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var eciTextView: TextView
    private lateinit var signalPowerTextView: TextView
    private lateinit var taTextView: TextView
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var showEciButton: Button
    private lateinit var showLocationButton: Button
    private lateinit var searchEciButton: Button
    private lateinit var saveInfoButton: Button
    private lateinit var displayInfoButton: Button
    private lateinit var clearDatabaseButton: Button
    private lateinit var calculateEciLocationButton: Button
    private lateinit var eciInput: TextInputEditText

    private lateinit var locationManager: LocationManager
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eciTextView = findViewById(R.id.eciTextView)
        signalPowerTextView = findViewById(R.id.signalPowerTextView)
        taTextView = findViewById(R.id.taTextView)
        latitudeTextView = findViewById(R.id.latitudeTextView)
        longitudeTextView = findViewById(R.id.longitudeTextView)
        showEciButton = findViewById(R.id.showEciButton)
        showLocationButton = findViewById(R.id.showLocationButton)
        searchEciButton = findViewById(R.id.searchEciButton)
        saveInfoButton = findViewById(R.id.saveInfoButton)
        displayInfoButton = findViewById(R.id.displayInfoButton)
        clearDatabaseButton = findViewById(R.id.clearDatabaseButton)
        calculateEciLocationButton = findViewById(R.id.calculateEciLocationButton)
        eciInput = findViewById(R.id.eciInput)

        dbHelper = DatabaseHelper(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            setButtonListeners()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                setButtonListeners()
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setButtonListeners() {
        showEciButton.setOnClickListener {
            getCellInfo()
        }

        showLocationButton.setOnClickListener {
            getLocation()
        }

        searchEciButton.setOnClickListener {
            searchEci()
        }

        saveInfoButton.setOnClickListener {
            saveInfo()
        }

        displayInfoButton.setOnClickListener {
            displayInfo()
        }

        clearDatabaseButton.setOnClickListener {
            clearDatabase()
        }

        calculateEciLocationButton.setOnClickListener {
            calculateEciLocation()
        }
    }

    private fun getCellInfo() {
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val cellInfoList = telephonyManager.allCellInfo

            for (cellInfo in cellInfoList) {
                if (cellInfo is CellInfoLte) {
                    val cellIdentityLte = cellInfo.cellIdentity
                    val eci = cellIdentityLte.ci
                    val cellSignalStrengthLte = cellInfo.cellSignalStrength
                    val rsrp = cellSignalStrengthLte.rsrp
                    val timingAdvance = cellSignalStrengthLte.timingAdvance

                    eciTextView.text = eci.toString()
                    signalPowerTextView.text = "$rsrp dBm"
                    taTextView.text = timingAdvance.toString()
                    break
                }
            }
        }
    }

    private fun getLocation() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
        }
    }

    private fun searchEci() {
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        val inputEci = eciInput.text.toString().toIntOrNull()
        if (inputEci == null) {
            Toast.makeText(this, "Please enter a valid ECI number", Toast.LENGTH_SHORT).show()
            return
        }

        var found = false

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val cellInfoList = telephonyManager.allCellInfo

            for (cellInfo in cellInfoList) {
                if (cellInfo is CellInfoLte) {
                    val cellIdentityLte = cellInfo.cellIdentity
                    val eci = cellIdentityLte.ci
                    if (eci == inputEci) {
                        val cellSignalStrengthLte = cellInfo.cellSignalStrength
                        val rsrp = cellSignalStrengthLte.rsrp
                        val timingAdvance = cellSignalStrengthLte.timingAdvance

                        eciTextView.text = eci.toString()
                        signalPowerTextView.text = "$rsrp dBm"
                        taTextView.text = timingAdvance.toString()
                        found = true
                        break
                    }
                }
            }
        }

        if (!found) {
            Toast.makeText(this, "Cell with ECI $inputEci not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveInfo() {
        val latitude = latitudeTextView.text.toString()
        val longitude = longitudeTextView.text.toString()
        val eci = eciTextView.text.toString()
        val signalPower = signalPowerTextView.text.toString()
        val timingAdvance = taTextView.text.toString()

        val isInserted = dbHelper.insertData(latitude, longitude, eci, signalPower, timingAdvance)
        if (isInserted) {
            Toast.makeText(this, "Data Inserted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Data Insertion Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayInfo() {
        val cursor: Cursor = dbHelper.getAllData()
        if (cursor.count == 0) {
            showMessage("Error", "No data found")
            return
        }

        val buffer = StringBuffer()
        while (cursor.moveToNext()) {
            buffer.append("ID: ${cursor.getString(0)}\n")
            buffer.append("Latitude: ${cursor.getString(1)}\n")
            buffer.append("Longitude: ${cursor.getString(2)}\n")
            buffer.append("ECI: ${cursor.getString(3)}\n")
            buffer.append("Signal Power: ${cursor.getString(4)}\n")
            buffer.append("Timing Advance: ${cursor.getString(5)}\n\n")
        }

        showMessage("Data", buffer.toString())
    }

    private fun clearDatabase() {
        dbHelper.clearData()
        Toast.makeText(this, "Database Cleared", Toast.LENGTH_SHORT).show()
    }

    private fun calculateEciLocation() {
        val inputEci = eciInput.text.toString()
        if (inputEci.isEmpty()) {
            Toast.makeText(this, "Please enter a valid ECI number", Toast.LENGTH_SHORT).show()
            return
        }

        val cursor: Cursor = dbHelper.getDataByEci(inputEci)
        if (cursor.count < 3) {
            Toast.makeText(this, "Not enough data to calculate the cell location", Toast.LENGTH_SHORT).show()
            return
        }


        val locations = mutableListOf<Triple<Double, Double, Int>>()
        while (cursor.moveToNext()) {
            val latitude = cursor.getString(1).toDouble()
            val longitude = cursor.getString(2).toDouble()
            val ta = cursor.getString(5).toInt()
            locations.add(Triple(latitude, longitude, ta))
        }

        if (locations.size >= 3) {
            val cellLocation = trilateration(locations)
            if (cellLocation != null) {
                showLocationDialog(cellLocation.first, cellLocation.second)
            } else {
                Toast.makeText(this, "Error calculating cell location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun trilateration(locations: List<Triple<Double, Double, Int>>): Pair<Double, Double>? {
        val distances = locations.map { it.third * 39.0 }

        val x1 = locations[0].first
        val y1 = locations[0].second
        val d1 = distances[0]

        val x2 = locations[1].first
        val y2 = locations[1].second
        val d2 = distances[1]

        val x3 = locations[2].first
        val y3 = locations[2].second
        val d3 = distances[2]

        val a = 2 * (x2 - x1)
        val b = 2 * (y2 - y1)
        val c = 2 * (x3 - x1)
        val d = 2 * (y3 - y1)
        val e = d1.pow(2) - d2.pow(2) - x1.pow(2) + x2.pow(2) - y1.pow(2) + y2.pow(2)
        val f = d1.pow(2) - d3.pow(2) - x1.pow(2) + x3.pow(2) - y1.pow(2) + y3.pow(2)

        val y = (f - e * c / a) / (d - b * c / a)
        val x = (e - b * y) / a

        return Pair(x, y)
    }

    private fun showLocationDialog(latitude: Double, longitude: Double) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Cell Tower Location")
        builder.setMessage("Latitude: $latitude\nLongitude: $longitude")
        builder.setPositiveButton("OK", null)
        builder.show()
    }

    private fun showMessage(title: String, message: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.show()
    }

    override fun onLocationChanged(location: Location) {
        latitudeTextView.text = location.latitude.toString()
        longitudeTextView.text = location.longitude.toString()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}
}

