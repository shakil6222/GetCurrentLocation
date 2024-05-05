package com.example.getcurrentlocation

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.dynamic.ObjectWrapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.io.IOException
import java.io.ObjectInput
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    // Variables declaration
    private var googleMap: GoogleMap? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var searchItem: androidx.appcompat.widget.SearchView
    val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                enableMyLocation()
            }
        }

    // onCreate method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val voiceSearch = findViewById<ImageButton>(R.id.voiceSearch_imageView)
        voiceSearch.setOnClickListener {
            searchVoice()
        }

        // Initialize variables
        searchItem = findViewById(R.id.searchItem_searchView)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Get the map fragment and set up map
        val mapsFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragement) as SupportMapFragment
        mapsFragment.getMapAsync(this)

        // Set up search functionality
        searchItem.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Perform location search

                val location = query ?: ""
                if (location.isNotEmpty()) {
                    val geoCoder = Geocoder(this@MainActivity)
                    try {
                        val addressList = geoCoder.getFromLocationName(location, 1)
                        if (addressList!!.isNotEmpty()) {
                            val address = addressList[0]
                            val latLang = LatLng(address.latitude, address.longitude)
                            googleMap?.addMarker(MarkerOptions().position(latLang).title(location))
                            googleMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLang,
                                    12f
                                )
                            )
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                return true

            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        // Set up edge-to-edge display
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Set up map options button
        val mapOptionButtonManu: ImageButton = findViewById(R.id.mapOptionButton)
        val pupUpMenu = PopupMenu(this, mapOptionButtonManu)
        pupUpMenu.menuInflater.inflate(R.menu.map_option, pupUpMenu.menu)
        pupUpMenu.setOnMenuItemClickListener { menuItem ->
            changeMap(menuItem.itemId)
            true
        }
        mapOptionButtonManu.setOnClickListener {
            pupUpMenu.show()
        }
    }

    // onMapReady method
    override fun onMapReady(Maps: GoogleMap) {
        googleMap = Maps

        // Configure map settings
        googleMap!!.uiSettings.isMyLocationButtonEnabled = true
        googleMap!!.uiSettings.isZoomControlsEnabled = true

        // Set a default location and zoom
        val defaultLocation = LatLng(25.9952247, 84.9835813)  // Sydney, Australia
        googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))


        // Add marker and polyline
        val location = enableMyLocation()
        val amnour = LatLng(25.9748985,84.9251597)
        val hajarpur = LatLng(25.9949647,84.9933598)
        googleMap?.addMarker(
            MarkerOptions()
                .position(amnour)
                .title("Amnour")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        googleMap?.addPolyline(
            PolylineOptions().add(amnour, hajarpur)
                .width(10f)
                .color(Color.BLUE)
                .geodesic(true)
        )
        googleMap?.addMarker(
            MarkerOptions()
                .position(hajarpur)
                .title("HajarPur")
        )

        // Check and request location permission
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            enableMyLocation()
        }

        // Set up location button and click listeners
        googleMap?.setOnMyLocationButtonClickListener {
            enableMyLocation()
            true
        }

        googleMap?.setOnMyLocationClickListener {
            enableMyLocation()
        }
        googleMap?.setPadding(0,1060,0,180)
    }

    // Function to enable "My Location" layer on the map
    fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        googleMap?.isMyLocationEnabled = true

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLocation = LatLng(location.latitude, location.longitude)
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLocation, 12f
                    )
                )
            }
        }
    }
    // voiceSearch function
    private fun searchVoice(){
        val voiceSearch = findViewById<ImageButton>(R.id.voiceSearch_imageView)

        voiceSearch.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak Now")
            try {
                startActivityForResult(intent,100)
            }catch (e:Exception){

                Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                onActivityResult(100,100,intent)

            }
        }
    }

    // Function to change map type
    private fun changeMap(itemId: Int) {
        when (itemId) {
            R.id.normalMap_menu -> googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybridMap_menu -> googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.sateliteMap_menu -> googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.trainMap_menu -> googleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }
}
