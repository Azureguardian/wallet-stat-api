package com.azureguardian.wallet.stat.api.interfaces

import com.azureguardian.wallet.stat.api.entity.dto.TransactionDto
import org.jooq.DSLContext
import reactor.core.publisher.Mono

interface WalletStorage {
    fun saveTransaction(dslContext: DSLContext, dto: TransactionDto): Mono<Int>
}
