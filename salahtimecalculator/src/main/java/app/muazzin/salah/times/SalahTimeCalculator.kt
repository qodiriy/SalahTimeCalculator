package app.muazzin.salah.times

import kotlin.math.*

object SalahTimeCalculator {

    fun calculateSalahTimes(
        timeZoneDiffInHour: Double,
        time: Time,
        location: Location,
        calcMethod: CalculationMethod,
        angleMethod: AngleMethod,
        asrShadowRatio: AsrRatio,
        adjustment: SalahAdjustment
    ): ArrayList<SalahTime> {

        val jd = gregorian2Julian(time.year, time.month, time.day)

        val isMidNightMethod = calcMethod == CalculationMethod.MIDNIGHT
        val isOneSevenMethod = calcMethod == CalculationMethod.ONE_SEVEN

        val fajrAngle = angleMethod.fajr
        val ishaAngle = angleMethod.isha

        val times = calculateSalahTimes(
            jd,
            timeZoneDiffInHour,
            location.lat,
            location.lng,
            location.alt,
            isMidNightMethod,
            isOneSevenMethod,
            fajrAngle,
            asrShadowRatio.value,
            ishaAngle,
            adjustment
        )

        return arrayListOf(
            SalahTime(Salah.FAJR, times[0].toTime().copy(year = time.year, month = time.month, day = time.day)),
            SalahTime(Salah.SHUROQ, times[1].toTime().copy(year = time.year, month = time.month, day = time.day)),
            SalahTime(Salah.DHUHR, times[2].toTime().copy(year = time.year, month = time.month, day = time.day)),
            SalahTime(Salah.ASR, times[3].toTime().copy(year = time.year, month = time.month, day = time.day)),
            SalahTime(Salah.MAGRIB, times[4].toTime().copy(year = time.year, month = time.month, day = time.day)),
            SalahTime(Salah.ISHA, times[5].toTime().copy(year = time.year, month = time.month, day = time.day)),
            SalahTime(Salah.QIYAM, times[6].toTime().copy(year = time.year, month = time.month, day = time.day))
        )

    }

    private fun calculateSalahTimes(
        julianDate: Double,
        timeZoneDiffInHour: Double,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        isMidNightMethod: Boolean,
        isOneSevenMethod: Boolean,
        fajrAngle: Double,
        asrShadowRatio: Double,
        ishaAngle: Double,
        adjustment: SalahAdjustment
    ): Array<Double> {

        val todaySunPosition =
            calculateSunPositions(julianDate, timeZoneDiffInHour, latitude, longitude, altitude)
        val tomorrowSunPosition =
            calculateSunPositions(julianDate + 1, timeZoneDiffInHour, latitude, longitude, altitude)
        val yesterdaySunPosition =
            calculateSunPositions(julianDate - 1, timeZoneDiffInHour, latitude, longitude, altitude)

        val sunriseToday = todaySunPosition[0]
        val middayToday = todaySunPosition[1]
        val sunsetToday = todaySunPosition[2]
        val todayD = todaySunPosition[3]

        val sunriseTomorrow = tomorrowSunPosition[0]
        val sunsetYesterday = yesterdaySunPosition[2]

        val fajr = when {
            isMidNightMethod -> {
                fixHour(sunriseToday - fixHour(sunriseToday - sunsetYesterday) / 4)
            }
            isOneSevenMethod -> {
                fixHour(sunriseToday - fixHour(sunriseToday - sunsetYesterday) / 7)
            }
            else -> {
                fixHour(
                    middayToday - toDegrees(
                        acos(
                            (-sin(toRadians(fajrAngle)) - sin(
                                toRadians(
                                    latitude
                                )
                            ) * sin(toRadians(todayD))) / (cos(toRadians(latitude)) * cos(
                                toRadians(
                                    todayD
                                )
                            ))
                        )
                    ) / 15
                )
            }
        }

        val shuroq = fixHour(adjustment.shuroq + sunriseToday)

        val dhuhr = fixHour(adjustment.dhuhr + middayToday)

        val asr = fixHour(
            adjustment.asr +
                    middayToday + toDegrees(
                acos(
                    (sin(atan(1 / (asrShadowRatio + tan(toRadians(latitude - todayD))))) - sin(
                        toRadians(
                            latitude
                        )
                    ) * sin(toRadians(todayD))) / (cos(toRadians(latitude)) * cos(toRadians(todayD)))
                )
            ) / 15
        )

        val maghrib = fixHour(adjustment.maghrib + sunriseToday)

        val isha = when {
            isMidNightMethod -> {
                fixHour(adjustment.isha + sunsetToday + fixHour(sunriseTomorrow - sunsetToday) / 4)
            }
            isOneSevenMethod -> {
                fixHour(adjustment.isha + sunsetToday + fixHour(sunriseTomorrow - sunsetToday) / 7)
            }
            else -> {
                fixHour(
                    adjustment.isha +
                            middayToday + toDegrees(
                        acos(
                            (-sin(toRadians(ishaAngle)) - sin(
                                toRadians(
                                    latitude
                                )
                            ) * sin(toRadians(todayD))) / (cos(toRadians(latitude)) * cos(
                                toRadians(
                                    todayD
                                )
                            ))
                        )
                    ) / 15
                )
            }
        }

        val qiyam = fixHour(adjustment.qiyam + sunsetToday + fixHour(sunriseTomorrow - sunsetToday) / 2)

        return arrayOf(
            fajr,
            shuroq,
            dhuhr,
            asr,
            maghrib,
            isha,
            qiyam
        )
    }

    private fun calculateSunPositions(
        julianDate: Double,
        timeZoneDiffInHour: Double,
        latitude: Double,
        longitude: Double,
        altitude: Double
    ): List<Double> {

        val january2000 = julianDate - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * january2000)
        val q = fixAngle(280.459 + 0.98564736 * january2000)
        val l = fixAngle(q + 1.915 * sin(toRadians(g)) + 0.020 * sin(toRadians(2 * g)))
        val e = fixHour(23.439 - 0.00000036 * january2000)
        val ra =
            fixHour(toDegrees(atan2(cos(toRadians(e)) * sin(toRadians(l)), cos(toRadians(l)))) / 15)
        val d = toDegrees(asin(sin(toRadians(e)) * sin(toRadians(l))))
        val eqt = q / 15 - ra

        val o = 0.833 + 0.0347 * sqrt(altitude).toDouble()

        val midday = fixHour(12 + timeZoneDiffInHour - longitude / 15 - eqt)
        val sunrise = fixHour(
            midday - toDegrees(
                acos(
                    (-sin(toRadians(o)) - sin(toRadians(latitude)) * sin(toRadians(d))) / (cos(
                        toRadians(
                            latitude
                        )
                    ) * cos(toRadians(d)))
                )
            ) / 15
        )
        val sunset = fixHour(
            midday + toDegrees(
                acos(
                    (-sin(toRadians(o)) - sin(toRadians(latitude)) * sin(toRadians(d))) / (cos(
                        toRadians(
                            latitude
                        )
                    ) * cos(toRadians(d)))
                )
            ) / 15
        )

        return arrayListOf(sunrise, midday, sunset, d)
    }


    private fun gregorian2Julian(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month

        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)

        return (floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b) - 1524.5
    }

    private fun toDegrees(value: Double): Double {
        return value * 180 / PI
    }

    private fun toRadians(value: Double): Double {
        return value * PI / 180
    }

    private fun fixAngle(a: Double): Double {
        var b = a
        b -= 360 * floor(b / 360.0)
        b = if (b < 0) b + 360 else b
        return b
    }

    private fun fixHour(h: Double): Double {
        var a = h
        a -= 24.0 * floor(a / 24.0)
        a = if (a < 0) a + 24 else a
        return a
    }

    private fun Double.toTime(): Time {
        val s = (this * 3600).toInt()
        val hour = s / 3600
        val minute = (s - hour * 3600) / 60
        val second = (s - hour * 3600 - minute * 60)

        return Time(0, 0, 0, hour, minute, second)

    }
}

