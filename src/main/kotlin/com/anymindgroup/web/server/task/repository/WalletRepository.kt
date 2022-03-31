package com.anymindgroup.web.server.task.repository

import com.anymindgroup.web.server.task.entity.dto.TransactionDto
import com.anymindgroup.web.server.task.interfaces.WalletStorage
import com.anymindgroup.web.server.task.public_.Public.PUBLIC
import com.anymindgroup.web.server.task.public_.tables.Transactions
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class WalletRepository : WalletStorage {

    companion object {
        val transactions: Transactions = PUBLIC.TRANSACTIONS
    }

    override fun saveTransaction(dslContext: DSLContext, dto: TransactionDto): Mono<Int> {
        return Mono.from(
            dslContext.insertInto(transactions)
                .columns(
                    transactions.DATETIME,
                    transactions.AMOUNT
                ).values(
                    dto.datetime,
                    dto.amount
                ).returningResult(transactions.ID)
        ).map {
            it.value1()
        }
    }
}
