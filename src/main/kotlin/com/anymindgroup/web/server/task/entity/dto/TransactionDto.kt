package com.anymindgroup.web.server.task.entity.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class TransactionDto(
    val datetime: OffsetDateTime,
    val amount: BigDecimal
)
