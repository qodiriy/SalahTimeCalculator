package app.example

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.muazzin.salah.times.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val timeZoneDiff = TimeZone.getDefault().rawOffset.toDouble() / (60 * 60 * 1000)
        val today = Calendar.getInstance()
        val time = Time(today[Calendar.YEAR], today[Calendar.MONTH], today[Calendar.DATE])
        val location = Location(lat = 39.6270, lng = 66.9750)
        val result = SalahTimeCalculator.calculateSalahTimes(
            timeZoneDiff, time, location, CalculationMethod.ANGLE, AngleMethod.UZB, AsrRatio.TWO,
            SalahAdjustment()
        )

        Log.e("salahTime", result.toString())

        findViewById<TextView>(R.id.textView)?.text = result.map {
            "${it.salah.name} -> ${it.time.hour}:${it.time.minute}"
        }.joinToString("\n")


    }
}