package com.azureguardian.wallet.stat.api.entity.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class BalanceByDateTimeDto(
    val dateTime: OffsetDateTime,
    val balance: BigDecimal
)
