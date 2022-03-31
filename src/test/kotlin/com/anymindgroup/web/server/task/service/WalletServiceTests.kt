package com.anymindgroup.web.server.task.service

import com.anymindgroup.web.server.task.config.TransactionalUtils
import com.anymindgroup.web.server.task.entity.dto.TransactionDto
import com.anymindgroup.web.server.task.interfaces.WalletBalanceDateTimeAggregateStorage
import com.anymindgroup.web.server.task.interfaces.WalletStorage
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.invoke
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.jooq.impl.DefaultConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.OffsetDateTime

@Suppress("ReactiveStreamsUnusedPublisher")
@SpringBootTest(classes = [WalletService::class])
class WalletServiceTests {

    @Autowired
    lateinit var walletService: WalletService

    @MockkBean
    lateinit var walletRepository: WalletStorage

    @MockkBean
    lateinit var transactionalUtils: TransactionalUtils

    @MockkBean
    lateinit var databaseClient: DatabaseClient

    @MockkBean
    lateinit var walletAggregateRepository: WalletBalanceDateTimeAggregateStorage

    @Test
    fun topUpWallet() {
        val transactionDto = TransactionDto(
            datetime = OffsetDateTime.parse("2019-10-05T14:48:01+01:00"),
            amount = BigDecimal("1.1")
        )
        every { transactionalUtils.transaction(captureLambda<(DSLContext) -> Mono<Any>>()) } answers
                { lambda<(DSLContext) -> Mono<Any>>().invoke(DefaultConfiguration().dsl()) }

        // Save transaction into transaction log
        every { walletRepository.saveTransaction(any(), any()) } returns Mono.just(1)
        // Upsert balance value in an aggregated report
        coEvery { walletAggregateRepository.update(any(), any(), any()) } returns Mono.empty()

        runBlocking { walletService.topUp(transactionDto) }
        coVerify(exactly = 1) { walletRepository.saveTransaction(any(), any<TransactionDto>()) }
        coVerify(exactly = 1) { walletAggregateRepository.update(any(), any(), any()) }
    }
}