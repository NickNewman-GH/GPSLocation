package com.example.gpslocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.annotation.NonNull




class MainActivity : AppCompatActivity(), LocationListener {
    val LOCATION_PERM_CODE = 2
    lateinit var locationManager: LocationManager
    var is_provider_enabled = false
    lateinit var adapter: ArrayAdapter<*>

    fun update_loc(view: View?){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {return}
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)

        val prv = locationManager.getBestProvider(Criteria(), true)
        Log.d("my", locationManager.allProviders.toString())
        if (prv != null) {
            val location = locationManager.getLastKnownLocation(prv)
            if (location != null) {
                Log.d("lat", location.latitude.toString())
                Log.d("long", location.longitude.toString())
                displayCoord(location.latitude, location.longitude)
            }
            Log.d("mytag", "location set")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // запрашиваем разрешения на доступ к геопозиции
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            // переход в запрос разрешений
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERM_CODE)
        }

        update_loc(null)
        var allProv = locationManager.allProviders
        adapter=object : ArrayAdapter<Any?>(this, android.R.layout.simple_list_item_1, allProv as List<Any?>)
        {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val place_for_text = view.findViewById<View>(android.R.id.text1) as TextView
                val text_for_text = allProv[position]
                place_for_text.text=text_for_text
                if(locationManager.isProviderEnabled(allProv[position]))
                    place_for_text.setTextColor(Color.GREEN)
                else
                    place_for_text.setTextColor(Color.RED)
                return view
            }
        }
        findViewById<ListView>(R.id.list).adapter=adapter
        findViewById<Button>(R.id.updButton).setOnClickListener(this::update_loc)
    }

    override fun onLocationChanged(loc: Location) {
        val lat = loc.latitude
        val lng = loc.longitude
        displayCoord(lat, lng)
        Log.d("my", "lat " + lat + " long " + lng)
    }

    fun displayCoord(latitude: Double, longtitude: Double) {
        findViewById<TextView>(R.id.lat).text = String.format("%.5f", latitude)
        findViewById<TextView>(R.id.lng).text = String.format("%.5f", longtitude)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERM_CODE
            -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d("access", "granted")
                    is_provider_enabled = true
                    findViewById<TextView>(R.id.message).text = ""
                    update_loc(null)
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d("access", "denied")
                    findViewById<TextView>(R.id.message).text = "Access denied"
                    adapter.notifyDataSetChanged();
                }
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                return
            }
        }
    }

//    override fun onProviderEnabled(provider: String) {
//        Log.d("mytag", "provider is enabled")
//        //val textView=findViewById<TextView>(R.id.message)
//        //textView.text=""
//        //is_providers_enabled = true
//    }
//
//    override fun onProviderDisabled(provider: String) {
//        Log.d("mytag", "provider is not enabled")
//        val textView=findViewById<TextView>(R.id.message)
//        textView.text="permissions denied"
//        //is_providers_enabled = false
//    }

    override fun onProviderEnabled(provider: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            is_provider_enabled = true
            findViewById<TextView>(R.id.message).text = ""
        }
        adapter.notifyDataSetChanged();
    }

    override fun onProviderDisabled(provider: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            is_provider_enabled = false
            findViewById<TextView>(R.id.message).text = "Providers disabled"
        }
        adapter.notifyDataSetChanged();
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    // TODO: обработать случай отключения GPS (геолокации) пользователем
    // onProviderDisabled + onProviderEnabled

    // TODO: обработать возврат в активность onRequestPermissionsResult
}