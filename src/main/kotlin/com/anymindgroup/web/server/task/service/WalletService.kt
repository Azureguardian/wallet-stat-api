package com.anymindgroup.web.server.task.service

import com.anymindgroup.web.server.task.config.TransactionalUtils
import com.anymindgroup.web.server.task.entity.dto.BalanceByDateTimeDto
import com.anymindgroup.web.server.task.entity.dto.TransactionDto
import com.anymindgroup.web.server.task.exceptions.IncorrectDateTimeException
import com.anymindgroup.web.server.task.interfaces.WalletBalanceDateTimeAggregateStorage
import com.anymindgroup.web.server.task.interfaces.WalletStorage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

@Service
class WalletService(
    private val transactionalUtils: TransactionalUtils,
    private val walletRepository: WalletStorage,
    private val walletBalanceDateTimeAggregateStorage: WalletBalanceDateTimeAggregateStorage
) {

    fun getBalanceByDateTimes(start: OffsetDateTime, end: OffsetDateTime): Flux<BalanceByDateTimeDto> {
        // TODO: Validate input dates, maybe set max duration window for query
        return walletBalanceDateTimeAggregateStorage.getBalanceHistoryHourly(start, end)
    }

    @Transactional
    fun topUp(dto: TransactionDto): Mono<Unit> {
        if (dto.datetime >= OffsetDateTime.now())
            throw IncorrectDateTimeException("Transaction cannot come from the future! Sorry.")
        return transactionalUtils.transaction { dslContext ->
            walletRepository.saveTransaction(dslContext, dto)
                .then(
                    walletBalanceDateTimeAggregateStorage.processAmount(dslContext, dto.datetime, dto.amount)
                )
        }
    }
}
