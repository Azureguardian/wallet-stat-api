package com.anymindgroup.web.server.task.repository

import com.anymindgroup.web.server.task.aggregates.Aggregates.AGGREGATES
import com.anymindgroup.web.server.task.aggregates.tables.BalanceHourly
import com.anymindgroup.web.server.task.entity.dto.BalanceByDateTimeDto
import com.anymindgroup.web.server.task.interfaces.WalletBalanceDateTimeAggregateStorage
import com.anymindgroup.web.server.task.util.truncateToHourEnd
import org.jooq.DSLContext
import org.jooq.Record1
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.OffsetDateTime

@Repository
class WalletBalanceDateTimeAggregateRepository(
    private val dslContext: DSLContext
) : WalletBalanceDateTimeAggregateStorage {

    companion object {
        val balanceAggregate: BalanceHourly = AGGREGATES.BALANCE_HOURLY
    }

    override fun getBalanceHistoryHourly(
        start: OffsetDateTime,
        end: OffsetDateTime
    ): Flux<BalanceByDateTimeDto> {
        return Flux.from(
            dslContext.select(
                balanceAggregate.BALANCE,
                balanceAggregate.DATETIME
            ).from(balanceAggregate)
                .where(balanceAggregate.DATETIME.le(end))
                .and(balanceAggregate.DATETIME.ge(start))
        ).map {
            BalanceByDateTimeDto(
                dateTime = it[balanceAggregate.DATETIME],
                balance = it[balanceAggregate.BALANCE]
            )
        }.sort(
            Comparator.comparing(BalanceByDateTimeDto::dateTime)
        )
    }

    @Transactional
    override fun processAmount(dslContext: DSLContext, dateTime: OffsetDateTime, amount: BigDecimal): Mono<Unit> {
        val dateTimeHourly = dateTime.truncateToHourEnd()
        val existingAggregate = findExistingAggregateRecord(dslContext, dateTimeHourly)
        return existingAggregate.hasElement().flatMap { hasElement ->
            if (hasElement) {
                updateBalance(dslContext, amount, dateTimeHourly)
            } else {
                findLastKnownBalance(dslContext, dateTimeHourly).flatMap { balance ->
                    insertRecordWithUpdatedBalance(dslContext, dateTimeHourly, balance, amount)
                        .then(
                            updateLaterAggregates(dslContext, amount, dateTimeHourly)
                        )
                }
            }
        }.map {
        }
    }

    private fun updateLaterAggregates(
        dslContext: DSLContext,
        amount: BigDecimal,
        dateTimeHourly: OffsetDateTime
    ) = Mono.from(
        dslContext.update(balanceAggregate)
            .set(balanceAggregate.BALANCE, balanceAggregate.BALANCE.plus(amount))
            .where(balanceAggregate.DATETIME.gt(dateTimeHourly))
    )

    private fun insertRecordWithUpdatedBalance(
        dslContext: DSLContext,
        dateTimeHourly: OffsetDateTime,
        balance: BigDecimal,
        amount: BigDecimal
    ) = Mono.from(
        dslContext.insertInto(balanceAggregate)
            .columns(
                balanceAggregate.DATETIME,
                balanceAggregate.BALANCE
            ).values(
                dateTimeHourly,
                balance + amount
            )
            .returningResult()
    )

    private fun findLastKnownBalance(
        dslContext: DSLContext,
        dateTimeHourly: OffsetDateTime
    ) = Mono.from(
        dslContext.select(
            DSL.coalesce(
                DSL.select(balanceAggregate.BALANCE)
                    .from(balanceAggregate)
                    .where(balanceAggregate.DATETIME.le(dateTimeHourly))
                    .orderBy(balanceAggregate.DATETIME.desc())
                    .limit(1),
                BigDecimal.ZERO
            )
        )
    ).map {
        it.value1() as BigDecimal
    }

    private fun updateBalance(
        dslContext: DSLContext,
        amount: BigDecimal,
        dateTimeHourly: OffsetDateTime
    ) = Mono.from(
        dslContext.update(balanceAggregate)
            .set(balanceAggregate.BALANCE, balanceAggregate.BALANCE.plus(amount))
            .where(balanceAggregate.DATETIME.ge(dateTimeHourly))
    )

    private fun findExistingAggregateRecord(
        dslContext: DSLContext,
        dateTimeHourly: OffsetDateTime
    ): Mono<Record1<OffsetDateTime>> {
        return Mono.from(
            dslContext.select(balanceAggregate.DATETIME)
                .from(balanceAggregate)
                .where(balanceAggregate.DATETIME.eq(dateTimeHourly))
        )
    }
}
