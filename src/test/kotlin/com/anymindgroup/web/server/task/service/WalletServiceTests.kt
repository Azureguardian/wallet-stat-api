package com.anymindgroup.web.server.task.service

import com.anymindgroup.web.server.task.entity.dto.TransactionDto
import com.anymindgroup.web.server.task.interfaces.WalletBalanceDateTimeAggregateStorage
import com.anymindgroup.web.server.task.interfaces.WalletStorage
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.runs
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.OffsetDateTime

@SpringBootTest(classes = [WalletService::class])
class WalletServiceTests {
    @Autowired
    lateinit var walletService: WalletService

    @MockkBean
    lateinit var walletRepository: WalletStorage

    @Autowired
    lateinit var connectionFactory: ConnectionFactory

    @MockkBean
    lateinit var walletAggregateRepository: WalletBalanceDateTimeAggregateStorage


//    private val walletService = WalletService(walletRepository)

    @Test
    fun topUpWallet() {
        val transactionDto = TransactionDto(
            datetime = OffsetDateTime.parse("2019-10-05T14:48:01+01:00"),
            amount = BigDecimal("1.1")
        )
        // Save transaction into transaction log
//        coEvery { walletRepository.saveTransaction(any<TransactionDto>()) } just runs
        // Find data for current hour
        coEvery { walletAggregateRepository.update(any(), any(), any()) } returns Mono.empty()
        // If not found - find last known balance
        // Update/insert balance value in an aggregated report
        // If we get value with old dateTime - update all aggregates since dateTime
        runBlocking { walletService.topUp(transactionDto) }
        // Find current wallet balance
        // Save transaction into transaction log
        // Upsert new hourly balance into prepared aggregate

        coVerify(exactly = 1) { walletRepository.saveTransaction(any(), any<TransactionDto>()) }
        coVerify(exactly = 1) { walletAggregateRepository.update(any(), any(), any()) }
    }
}