package com.anymindgroup.web.server.task.config

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Component
class TransactionalUtils(private val databaseClient: DatabaseClient) {
    /**
     * Execute any [block] of code in a transaction
     */
    @Transactional
    fun <R> transaction(block: (dslContext: DSLContext) -> Mono<R>): Mono<R> {
        return databaseClient.inConnection {
            val dslContext = DSL.using(it).dsl()
            block(dslContext)
        }
    }
}