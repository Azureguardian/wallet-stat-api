package com.anymindgroup.web.server.task.util

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

fun <T : Any> DatabaseClient.withDSLContext(block: (DSLContext) -> Mono<T>): Mono<T> {
        return inConnection {
            block(DSL.using(it).dsl())
        }
    }