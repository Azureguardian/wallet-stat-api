package com.anymindgroup.web.server.task.interfaces

import com.anymindgroup.web.server.task.entity.dto.TransactionDto
import org.jooq.DSLContext
import reactor.core.publisher.Mono

interface WalletStorage {
    fun saveTransaction(dslContext: DSLContext, dto: TransactionDto): Mono<Int>
}
