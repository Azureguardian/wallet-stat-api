package com.anymindgroup.web.server.task.interfaces

import com.anymindgroup.web.server.task.entity.dto.BalanceByDateTimeDto
import com.anymindgroup.web.server.task.entity.dto.TransactionDto
import com.anymindgroup.web.server.task.public_.tables.pojos.Transactions
import org.jooq.DSLContext
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

interface WalletStorage {
    fun saveTransaction(dslContext: DSLContext, dto: TransactionDto): Mono<Int>
}