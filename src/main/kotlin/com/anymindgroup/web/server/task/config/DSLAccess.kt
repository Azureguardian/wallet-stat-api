package com.anymindgroup.web.server.task.config

import io.r2dbc.spi.Connection
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.reactivestreams.Publisher
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

//@Configuration
class DSLAccess(private val databaseClient: DatabaseClient) {

    private val settings = Settings()
        .withBindOffsetDateTimeType(true)
        .withBindOffsetTimeType(true)

    private fun Connection.dsl() = DSL.using(this, SQLDialect.POSTGRES, settings)

    fun <T : Any> withDSLContextMany(block: (DSLContext) -> Publisher<T>): Flux<T> =
        databaseClient.inConnectionMany { con -> block(con.dsl()).toFlux() }

    fun <T : Any> withDSLContext(block: (DSLContext) -> Mono<T>): Mono<T> =
        databaseClient.inConnection { con -> block(con.dsl()) }
}