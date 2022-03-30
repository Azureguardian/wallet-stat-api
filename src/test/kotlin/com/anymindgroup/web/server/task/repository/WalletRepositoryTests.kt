package com.anymindgroup.web.server.task.repository

import com.anymindgroup.web.server.task.entity.dto.TransactionDto
import com.anymindgroup.web.server.task.interfaces.WalletStorage
import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.math.BigDecimal
import java.time.Duration
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit


@SpringBootTest
@Testcontainers
class WalletRepositoryTests {

    @Autowired
    lateinit var dslContext: DSLContext

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

    @Autowired
    lateinit var walletRepository: WalletStorage


//	@BeforeEach
//	fun setUp() {
//		val hikariConfig = HikariConfig()
//		hikariConfig.jdbcUrl = container.jdbcUrl
//		hikariConfig.username = container.username
//		hikariConfig.password = container.password
//		val dataSource = HikariDataSource(hikariConfig)
//		repository = SpringJdbcFrameworkRepository(
//			FrameworkSimpleJdbcInsert(dataSource),
//			JdbcTemplate(dataSource), FrameworkRowMapper.getInstance()
//		)
//	}

    @Test
    fun testSaveTransaction() {
        val result = walletRepository.saveTransaction(dslContext, TransactionDto(OffsetDateTime.now(), BigDecimal("100.1"))).block()
        print(result)
    }
}