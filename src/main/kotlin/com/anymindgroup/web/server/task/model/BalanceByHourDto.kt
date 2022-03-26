package com.anymindgroup.web.server.task.model

import java.math.BigDecimal
import java.time.OffsetDateTime

data class BalanceByHourDto(
    val dateTime: OffsetDateTime,
    val amount: BigDecimal
)
