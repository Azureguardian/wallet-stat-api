package com.anymindgroup.web.server.task.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.CloseableDSLContext
import org.jooq.impl.DSL
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration
class PostgresContainerTestConfiguration {

    @Bean
	fun container(): PostgreSQLContainer<*> = PostgreSQLContainer(
		DockerImageName.parse(System.getenv("IMAGE_NAME") ?: "db_db").apply {
		}
			.asCompatibleSubstituteFor("postgres")
	)

//	@Bean
//	fun getDataSource() {
//		val hikariConfig = HikariConfig()
//		hikariConfig.jdbcUrl = container.jdbcUrl
//		hikariConfig.username = container.username
//		hikariConfig.password = container.password
//		val dataSource = HikariDataSource(hikariConfig)
//	}

	@DependsOn("container")
	@Bean
	fun dslContext(container: PostgreSQLContainer<*>): CloseableDSLContext {
		val hikariConfig = HikariConfig()
		hikariConfig.jdbcUrl = container.jdbcUrl
		hikariConfig.username = container.username
		hikariConfig.password = container.password
		return DSL.using(container.jdbcUrl, container.username, container.password)
	}
}