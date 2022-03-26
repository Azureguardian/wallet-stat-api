package com.anymindgroup.web.server.task.model

import java.math.BigDecimal
import java.time.OffsetDateTime

data class TransactionPayload(
    val datetime: OffsetDateTime,
    val amount: BigDecimal
)
