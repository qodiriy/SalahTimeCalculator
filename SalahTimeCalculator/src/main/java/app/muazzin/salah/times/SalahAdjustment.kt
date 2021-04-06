package app.muazzin.salah.times

data class SalahAdjustment(
    val fajr: Double = .0,
    val shuroq: Double = .0,
    val dhuhr: Double = .0,
    val asr: Double = .0,
    val maghrib: Double = .0,
    val isha: Double = .0,
    val qiyam: Double = .0
)