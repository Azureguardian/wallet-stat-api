package com.anymindgroup.web.server.task.entity.view

import java.math.BigDecimal
import java.time.OffsetDateTime

data class BalanceByDateTimeView(
    val datetime: OffsetDateTime,
    val amount: BigDecimal
)
