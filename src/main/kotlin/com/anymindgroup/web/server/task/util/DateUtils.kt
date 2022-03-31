package com.anymindgroup.web.server.task.util

import com.anymindgroup.web.server.task.util.DateUtils.bangkokTimeZone
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object DateUtils {
    val bangkokTimeZone: ZoneId = ZoneId.of( "Asia/Bangkok")
}

fun OffsetDateTime.atBangkok(): ZonedDateTime = atZoneSameInstant(bangkokTimeZone)
fun OffsetDateTime.truncateToHourEnd(): OffsetDateTime = truncatedTo(ChronoUnit.HOURS).plusHours(1)