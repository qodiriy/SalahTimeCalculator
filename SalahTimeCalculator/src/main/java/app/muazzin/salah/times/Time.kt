package app.muazzin.salah.times

data class Time(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0
) {

    fun getTime(): Int {
        return 1000 * (second * 60 + minute * 60 * 60 + hour * 60 * 60 * 60)
    }

}