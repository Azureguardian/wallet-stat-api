package com.anymindgroup.web.server.task.service

import com.anymindgroup.web.server.task.config.DSLAccess
import com.anymindgroup.web.server.task.config.TransactionalUtils
import com.anymindgroup.web.server.task.interfaces.WalletStorage
import com.anymindgroup.web.server.task.entity.dto.BalanceByDateTimeDto
import com.anymindgroup.web.server.task.entity.dto.TransactionDto
import com.anymindgroup.web.server.task.interfaces.WalletBalanceDateTimeAggregateStorage
import com.anymindgroup.web.server.task.util.withDSLContext
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.OffsetDateTime

@Service
class WalletService(
    private val connectionFactory: ConnectionFactory,
    private val dslContext: DSLContext,
    private val transactionalUtils: TransactionalUtils,
//    private val dslAccess: DSLAccess,
    private val operator: TransactionalOperator,
    private val databaseClient: DatabaseClient,
//    private val operations: R2dbcOperations,
    private val walletRepository: WalletStorage,
    private val walletBalanceDateTimeAggregateStorage: WalletBalanceDateTimeAggregateStorage
) {

    fun getBalanceByDateTimes(start: OffsetDateTime, end: OffsetDateTime): Flux<BalanceByDateTimeDto> {
        return walletBalanceDateTimeAggregateStorage.getBalanceHistoryHourly(start, end)
    }

//    @Transactional
//    fun topUp(dto: TransactionDto): Mono<Unit> {
//        return walletRepository.saveTransaction(dslContext, dto)
//            .then(
//                walletBalanceDateTimeAggregateStorage.update(dslContext, dto.datetime, dto.amount)
//            )
//    }

    @Transactional
    fun topUp(dto: TransactionDto): Mono<Unit> {
        return databaseClient.inConnection {
            val dslContext = DSL.using(it).dsl()
            walletRepository.saveTransaction(dslContext, dto)
                .then(
                    walletBalanceDateTimeAggregateStorage.update(dslContext, dto.datetime, dto.amount)
                )
        }
//        return databaseClient.withDSLContext {
//            walletRepository.saveTransaction(dslContext, dto)
//                .then(
//                    walletBalanceDateTimeAggregateStorage.update(dslContext, dto.datetime, dto.amount)
//                )
//        }
    }

//    @Transactional
//    fun topUp(dto: TransactionDto): Mono<Unit> {
//    return transactionalUtils.executeAsTransactional {
//        databaseClient.withDSLContext {
//            walletRepository.saveTransaction(dslContext, dto)
//                .then(
//                    walletBalanceDateTimeAggregateStorage.update(dslContext, dto.datetime, dto.amount)
//                )
//        }
//    }
//}
//
//    fun <T : Any> DatabaseClient.withDSLContext(block: (DSLContext) -> Mono<T>): Mono<T> {
//        return inConnection {
//            block(DSL.using(it).dsl())
//        }
//    }
}