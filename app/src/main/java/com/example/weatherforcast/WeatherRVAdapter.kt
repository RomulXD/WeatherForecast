package com.example.weatherforcast

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WeatherRVAdapter(
    private var context: Context,
    private var weatherRVModelArrayList: MutableList<WeatherRVModel>,
) : RecyclerView.Adapter<WeatherRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return weatherRVModelArrayList.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = weatherRVModelArrayList[position]
        holder.temperatureTV.text = model.temperature + "°c"
        Picasso.get().load("https:${model.icon}").into(holder.conditionIV)
        holder.windTV.text = model.windSpeed + "Km/h"
        val input = SimpleDateFormat("yyyy-MM-dd hh:mm")
        val output = SimpleDateFormat("hh:mm aa")
        try {
            val t = input.parse(model.time)
            holder.timeTV.text = output.format(t as Date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var windTV: TextView
        internal var temperatureTV: TextView
        internal var timeTV: TextView
        internal var conditionIV: ImageView

        init {
            windTV = itemView.findViewById(R.id.idTVWindSpeed)
            temperatureTV = itemView.findViewById(R.id.idTVTemperature)
            timeTV = itemView.findViewById(R.id.idTVTime)
            conditionIV = itemView.findViewById(R.id.idTVCondition)
        }


    }


}
