package com.anymindgroup.web.server.task.interfaces

import com.anymindgroup.web.server.task.entity.dto.BalanceByDateTimeDto
import org.jooq.DSLContext
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.OffsetDateTime

interface WalletBalanceDateTimeAggregateStorage {

    fun getBalanceHistoryHourly(start: OffsetDateTime, end: OffsetDateTime): Flux<BalanceByDateTimeDto>

    fun processAmount(dslContext: DSLContext, dateTime: OffsetDateTime, amount: BigDecimal): Mono<Unit>
}
