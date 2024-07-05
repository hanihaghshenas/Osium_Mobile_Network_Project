package com.example.osmiom11



import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.telephony.CellInfoLte
import android.telephony.CellInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var eciTextView: TextView
    private lateinit var signalPowerTextView: TextView
    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var showEciButton: Button
    private lateinit var showLocationButton: Button
    private lateinit var searchEciButton: Button
    private lateinit var eciInput: TextInputEditText

    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eciTextView = findViewById(R.id.eciTextView)
        signalPowerTextView = findViewById(R.id.signalPowerTextView)
        latitudeTextView = findViewById(R.id.latitudeTextView)
        longitudeTextView = findViewById(R.id.longitudeTextView)
        showEciButton = findViewById(R.id.showEciButton)
        showLocationButton = findViewById(R.id.showLocationButton)
        searchEciButton = findViewById(R.id.searchEciButton)
        eciInput = findViewById(R.id.eciInput)

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

                    eciTextView.text = "ECI: $eci"
                    signalPowerTextView.text = "Signal Power: $rsrp dBm"
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

                        eciTextView.text = "ECI: $eci"
                        signalPowerTextView.text = "Signal Power: $rsrp dBm"
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

    override fun onLocationChanged(location: Location) {
        latitudeTextView.text = "Latitude: ${location.latitude}"
        longitudeTextView.text = "Longitude: ${location.longitude}"
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}
}
