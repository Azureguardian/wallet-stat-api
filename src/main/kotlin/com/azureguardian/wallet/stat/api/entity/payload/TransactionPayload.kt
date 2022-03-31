package com.azureguardian.wallet.stat.api.entity.payload

import com.azureguardian.wallet.stat.api.entity.dto.TransactionDto
import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotNull

class TransactionPayload(
    @field: NotNull
    val datetime: OffsetDateTime?,
    @field: NotNull
    @field: DecimalMin(value = "0.0", inclusive = false)
    val amount: BigDecimal?
) {
    // We want to have null safety
    // Payload class used for validation only
    fun toDto(): TransactionDto {
        return TransactionDto(datetime!!, amount!!)
    }
}
