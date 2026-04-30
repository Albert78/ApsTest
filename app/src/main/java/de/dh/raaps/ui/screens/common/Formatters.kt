package de.dh.raaps.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import de.dh.raaps.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AppFormatters(
    val shortDateTime: DateTimeFormatter,
    val shortDate: DateTimeFormatter,
    val longDateTime: DateTimeFormatter,
    val longDate: DateTimeFormatter,
)

// Provider for the formatters
val LocalAppFormatters = staticCompositionLocalOf<AppFormatters> {
    error("No AppFormatters provided")
}

@Composable
fun rememberAppFormatters(): AppFormatters {
    val locale = LocalLocale.current.platformLocale

    val shortDateTimePattern = stringResource(R.string.short_date_time_format)
    val shortDatePattern = stringResource(R.string.short_date_format)
    val longDateTimePattern = stringResource(R.string.long_date_time_format)
    val longDatePattern = stringResource(R.string.long_date_format)

    return remember(locale, shortDateTimePattern, shortDatePattern, longDateTimePattern, longDatePattern) {
        AppFormatters(
            shortDateTime = DateTimeFormatter.ofPattern(shortDateTimePattern, locale),
            shortDate = DateTimeFormatter.ofPattern(shortDatePattern, locale),
            longDateTime = DateTimeFormatter.ofPattern(longDateTimePattern, locale),
            longDate = DateTimeFormatter.ofPattern(longDatePattern, locale)
        )
    }
}

/////////////////////////////////////////////// Time ///////////////////////////////////////////////

fun time(time: LocalTime): String {
    return String.format(Locale.getDefault(), "%02d:%02d", time.hour, time.minute)
}

/////////////////////////////////////////////// Long date time //////////////////////////////////////

@Composable
fun longDateTime(dateTime: LocalDateTime): String {
    return dateTime.format(LocalAppFormatters.current.longDateTime)
}

@Composable
fun longDateTime(dateTime: LocalDateTime?, default: String = "-"): String {
    return dateTime?.let {
        longDateTime(dateTime)
    } ?: default
}

/////////////////////////////////////////////// Short date time //////////////////////////////////////

@Composable
fun shortDateTime(dateTime: LocalDateTime): String {
    return dateTime.format(LocalAppFormatters.current.shortDateTime)
}

@Composable
fun shortDateTime(dateTime: LocalDateTime?, default: String = "-"): String {
    return dateTime?.let {
        shortDateTime(dateTime)
    } ?: default
}

/////////////////////////////////////////////// Long date //////////////////////////////////////

@Composable
fun longDate(date: LocalDate): String {
    return date.format(LocalAppFormatters.current.longDate)
}

@Composable
fun longDate(date: LocalDate?, default: String = "-"): String {
    return date?.let {
        longDate(date)
    } ?: default
}

@Composable
fun longDate(dateTime: LocalDateTime?, default: String = "-"): String {
    return longDate(dateTime?.toLocalDate(), default)
}

/////////////////////////////////////////////// Short date //////////////////////////////////////

@Composable
fun shortDate(date: LocalDate): String {
    return date.format(LocalAppFormatters.current.shortDate)
}

@Composable
fun shortDate(date: LocalDate?, default: String = "-"): String {
    return date?.let {
        shortDate(date)
    } ?: default
}

