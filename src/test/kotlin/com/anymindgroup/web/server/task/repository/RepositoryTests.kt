package com.anymindgroup.web.server.task.repository

import com.anymindgroup.web.server.task.config.TransactionalUtils
import com.anymindgroup.web.server.task.entity.dto.TransactionDto
import com.anymindgroup.web.server.task.interfaces.WalletStorage
import com.anymindgroup.web.server.task.repository.WalletBalanceDateTimeAggregateRepository.Companion.balanceAggregate
import com.anymindgroup.web.server.task.repository.WalletRepository.Companion.transactions
import com.anymindgroup.web.server.task.util.atBangkok
import com.anymindgroup.web.server.task.util.truncateToHourEnd
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Duration
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors

@SpringBootTest
@Testcontainers
class RepositoryTests {

    @Autowired
    lateinit var dslContext: DSLContext

    @Autowired
    lateinit var transactionalUtils: TransactionalUtils

    @Autowired
    lateinit var walletRepository: WalletStorage

    @Autowired
    lateinit var aggregateRepository: WalletBalanceDateTimeAggregateRepository

    companion object {
        @Container
        private val container = PostgreSQLContainer(
            DockerImageName.parse(
                "test-web-api"
            )
                .asCompatibleSubstituteFor("postgres")
        ).apply {
            addExposedPort(5432)
            setWaitStrategy(
                LogMessageWaitStrategy()
                    .withRegEx(".*database system is ready to accept connections.*\\s")
                    .withTimes(1)
                    .withStartupTimeout(Duration.of(5, ChronoUnit.SECONDS))
            )
        }.withDatabaseName("wallet")

        @DynamicPropertySource
        @JvmStatic
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") {
                ("r2dbc:postgresql://" + container.host) + ":" + container.firstMappedPort
                    .toString() + "/" + container.databaseName
            }
            registry.add("spring.r2dbc.username") { "postgres" }
            registry.add("spring.r2dbc.password") { "postgres" }
        }
    }

    @BeforeEach
    fun cleanUp() {
        Mono.from(
            dslContext.deleteFrom(transactions)
        ).then(
            Mono.from(dslContext.deleteFrom(balanceAggregate))
        ).block()
    }

    @Test
    fun testSaveTransaction() {
        val id = walletRepository.saveTransaction(dslContext, transactionDto).block()
        Mono.from(
            dslContext.select(
                transactions.DATETIME,
                transactions.AMOUNT,
                transactions.ID
            ).from(transactions)
        ).map {
            assertEquals(id, it[transactions.ID])
            assertEquals(transactionDto.amount, it[transactions.AMOUNT])
            assertEquals(transactionDto.datetime.atBangkok(), it[transactions.DATETIME].atBangkok())
        }
            .block()
    }

    @Test
    fun `hourly wallet balance should change correctly`() {
        // Aggregate is empty
        // 2011-10-05T11:00:00+00:00 - 1
        val dateTime = OffsetDateTime.parse("2011-10-05T10:48:01+00:00")
        aggregateRepository.processAmount(dslContext, dateTime, BigDecimal.ONE).block()
        aggregateRepository.processAmount(dslContext, dateTime, BigDecimal.ONE).block()
        val result = aggregateRepository.getBalanceHistoryHourly(dateTime.minusHours(1), dateTime.plusHours(1))
            .collect(Collectors.toList())
            .block()!!
        assertEquals(1, result.size)
        assertEquals(BigDecimal("2"), result.first().balance)
        assertTrue(dateTime.truncateToHourEnd().isEqual(result.first().dateTime))

        // If we receive a "late" transaction, where dateTime < current hour
        // We update aggregate by amount for all records where dateTime > transaction dateTime
        // 2011-10-05T10:00:00+00:00 - 1
        // 2011-10-05T11:00:00+00:00 - 3
        val dateTimeLate = OffsetDateTime.parse("2011-10-05T09:48:01+00:00")
        aggregateRepository.processAmount(dslContext, dateTimeLate, BigDecimal.ONE).block()
        val resultWithLateTransaction = aggregateRepository.getBalanceHistoryHourly(
            dateTimeLate.minusHours(2),
            dateTimeLate.plusHours(2)
        )
            .collect(Collectors.toList())
            .block()!!
            .sortedBy { it.dateTime }
        assertEquals(2, resultWithLateTransaction.size)
        assertEquals(BigDecimal.ONE, resultWithLateTransaction.first().balance)
        assertTrue(dateTimeLate.truncateToHourEnd().isEqual(resultWithLateTransaction.first().dateTime))
        assertEquals(BigDecimal("3"), resultWithLateTransaction[1].balance)
        assertTrue(dateTime.truncateToHourEnd().isEqual(resultWithLateTransaction[1].dateTime))

        // 2011-10-05T10:00:00+00:00 - 1
        // 2011-10-05T11:00:00+00:00 - 3
        // 2011-10-05T12:00:00+00:00 - 4
        val dateTimeLast = OffsetDateTime.parse("2011-10-05T11:48:01+00:00")
        aggregateRepository.processAmount(dslContext, dateTimeLast, BigDecimal.ONE).block()
        val resultLast = aggregateRepository.getBalanceHistoryHourly(
            dateTimeLast.minusHours(3),
            dateTimeLast.plusHours(1)
        )
            .collect(Collectors.toList())
            .block()!!
            .sortedBy { it.dateTime }
        assertEquals(3, resultLast.size)
        assertEquals(BigDecimal.ONE, resultLast.first().balance)
        assertTrue(dateTimeLate.truncateToHourEnd().isEqual(resultLast.first().dateTime))
        assertEquals(BigDecimal("3"), resultLast[1].balance)
        assertTrue(dateTime.truncateToHourEnd().isEqual(resultLast[1].dateTime))
        assertEquals(BigDecimal("4"), resultLast[2].balance)
        assertTrue(dateTimeLast.truncateToHourEnd().isEqual(resultLast[2].dateTime))
    }

    @Test
    fun `transactions should work`() {
        val dateTime = OffsetDateTime.now()
        assertThrows<DataAccessException> {
            transactionalUtils.transaction {
                insertBalanceAggregateReport(it, transactionDto.copy(datetime = dateTime))
                    .then(insertBalanceAggregateReport(it, transactionDto.copy(datetime = dateTime)))
            }
                .block()
        }
        val result = Flux.from(
            dslContext.select(
                balanceAggregate.BALANCE,
                balanceAggregate.DATETIME
            ).from(balanceAggregate)
        )
            .collect(Collectors.toList())
            .block()!!
        assertTrue(result.isEmpty())
    }

    private fun insertBalanceAggregateReport(it: DSLContext, dto: TransactionDto) = Mono.from(
        it.insertInto(balanceAggregate)
            .columns(
                balanceAggregate.DATETIME,
                balanceAggregate.BALANCE
            ).values(
                dto.datetime,
                dto.amount
            )
            .returningResult()
    )

    private val transactionDto = TransactionDto(
        datetime = OffsetDateTime.parse("2011-10-05T10:48:01+00:00"),
        amount = BigDecimal("100.1")
    )
}
