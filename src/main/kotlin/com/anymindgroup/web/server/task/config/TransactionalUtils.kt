package com.anymindgroup.web.server.task.config

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Component
class TransactionalUtils {
    /**
     * Execute any [block] of code (even private methods)
     * as if it was effectively [Transactional]
     */
    @Transactional
    fun <R> executeAsTransactional(block: () -> Mono<R>): Mono<R> {
        return block()
    }
}