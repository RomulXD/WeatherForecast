package com.example.weatherforcast

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private val weatherRVModelArrayList: MutableList<WeatherRVModel> = ArrayList()

    private val weatherRVAdapter: WeatherRVAdapter by lazy {
        WeatherRVAdapter(this@MainActivity, weatherRVModelArrayList)
    }

    private lateinit var locationManager: LocationManager

    private lateinit var cityName: String

    private val PERMISSION_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_main)
        idRVWeather.adapter = weatherRVAdapter

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this@MainActivity,
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@MainActivity,
                ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) ActivityCompat.requestPermissions(this@MainActivity,
            arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
            PERMISSION_CODE)

        val location =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location != null) {
            cityName = getCityName(location.longitude, location.latitude)
            getWeatherInfo(cityName)
        } else {
            Toast.makeText(this, "Invalid User's Location", Toast.LENGTH_SHORT).show()
            idBPLoading.visibility = View.GONE
            idRLHome.visibility = View.VISIBLE
        }



        idIVSearch.setOnClickListener {
            val city = idETCity.text.toString()
            if (city.isEmpty()) {
                Toast.makeText(this@MainActivity, "Please enter city name", Toast.LENGTH_SHORT)
                    .show()
            } else {
                idTVCityName.text = city
                getWeatherInfo(city)
            }
        }
    }

    /**
     * Handle user permission
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Permission granted ... ", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this@MainActivity,
                    "Please provide the permission",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun getCityName(longtitude: Double, latitude: Double): String {
        var cityName = "Not found"
        val gcd = Geocoder(baseContext, Locale.getDefault())
        try {
            val addresses = gcd.getFromLocation(latitude, longtitude, 10);

            for (addr in addresses) {
                if (addr != null) {
                    val city = addr.locality
                    if (city != null && city != "") {
                        cityName = city
                    } else {
                        Log.d("TAG", "CITY NOT FOUND")
                        Toast.makeText(this@MainActivity,
                            "User City Not Found...",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return cityName
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun getWeatherInfo(cityName: String) {
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=d520964eb8bb410ab51200726221206&q=$cityName&days=1&aqi=no&alerts=no"
        idTVCityName.text = cityName
        val requestQueue = Volley.newRequestQueue(this@MainActivity)

        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response ->
                idBPLoading.visibility = View.GONE
                idRLHome.visibility = View.VISIBLE
                weatherRVModelArrayList.clear()
                val temperature = response.getJSONObject("current").getString("temp_c")
                idTVTemperature.text = "$temperature°c"
                val isDay = response.getJSONObject("current").getInt("is_day")
                val condition =
                    response.getJSONObject("current").getJSONObject("condition").getString("text")
                val conditionIcon =
                    response.getJSONObject("current").getJSONObject("condition").getString("icon")
                Picasso.get().load("https:$conditionIcon").into(idIVIcon)
                idTVCondition.text = condition
                if (isDay == 1) {
//                    // morning
                    Picasso.get().load("https://wallpaperaccess.com/full/3162177.jpg")
                        .into(idIVBlack)
                } else {
                    //night
                    Picasso.get()
                        .load("https://images.unsplash.com/photo-1436891620584-47fd0e565afb?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MXx8cGhvbmUlMjBiYWNrZ3JvdW5kJTIwc3RhcnxlbnwwfHwwfHw%3D&w=1000&q=80")
                        .into(idIVBlack)
                }
//
                val forecastObj =
                    response.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0)
                val hourArray = forecastObj.getJSONArray("hour")
//
                for (i in 0 until hourArray.length()) {
                    val hourObj = hourArray.getJSONObject(i)
                    val time = hourObj.getString("time")
                    val temper = hourObj.getString("temp_c")
                    val img = hourObj.getJSONObject("condition").getString("icon")
                    val wind = hourObj.getString("wind_kph")
                    weatherRVModelArrayList.add(WeatherRVModel(time, temper, img, wind))
                }
                weatherRVAdapter.notifyDataSetChanged()
                Toast.makeText(this@MainActivity,
                    "Valid",
                    Toast.LENGTH_SHORT).show()

            }, {
                Toast.makeText(this@MainActivity,
                    "Please enter valid city name ... ",
                    Toast.LENGTH_SHORT).show()
                println(url)
            })

        requestQueue.add(jsonObjectRequest)
    }
}