package me.nanova.subspace.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.ln
import kotlin.math.pow

fun String.toCamelCase(): String {
    return this.split('_')
        .joinToString("") { it.replaceFirstChar { char -> char.uppercase() } }
}

fun Float.percentage(decimalPlaces: Int = 2): String {
    return when {
        this < 1 -> "%.${decimalPlaces}f%%".format(this * 100)
        this == 1F -> "100%"
        else -> "%.0f%%".format(this * 100)
    }
}

fun Float.round(decimals: Int = 2): String {
    return "%.${decimals}f".format(this)
}

fun Long.sec2Time(): String {
    if (this >= 8640000) return "∞"

    val days = TimeUnit.SECONDS.toDays(this)
    val hours = TimeUnit.SECONDS.toHours(this) % 24
    val minutes = TimeUnit.SECONDS.toMinutes(this) % 60
    val secs = this % 60

    return buildString {
        if (days > 0) append("$days ")

        if (days > 0 || hours > 0) append("%02d:".format(hours))

        if (days > 0 || hours > 0 || minutes > 0) append("%02d:".format(minutes))
        else append("0:") // Ensure minutes are shown if only seconds are non-zero

        append("%02d".format(secs))
    }
}

fun Long.unix2DateTime(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    if (this <= 0) return "Unknown"
    val instant = Instant.ofEpochSecond(this)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return localDateTime.format(formatter)
}

fun Long.formatBytesPerSec(si: Boolean = false, toBit: Boolean = false): String {
    val sec = if (toBit) "ps" else "/s"
    return "${this.formatBytes(si, toBit)}${sec}"
}

/**
 * @param si SI(decimal) or IEC(binary)
 * @link https://en.wikipedia.org/wiki/Data-rate_units
 * @param toBit convert bytes to bit or not
 */
fun Long.formatBytes(si: Boolean = false, toBit: Boolean = false): String {
    val unit = if (si) 1000 else 1024
    val size = if (toBit) this * 8 else this
    val suffix = if (toBit) "b" else "B"
    if (size < unit) return "$size $suffix"

    val exp = (ln(size.toDouble()) / ln(unit.toDouble())).toInt()
    val prefix = if (si) "kMGTPE"[exp - 1] else arrayOf("Ki", "Mi", "Gi", "Ti", "Pi", "Ei")[exp - 1]
    val formattedNumber = size / unit.toDouble().pow(exp.toDouble())

    return "${"%.1f".format(formattedNumber)} ${prefix}${suffix}"
}
