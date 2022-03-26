package com.anymindgroup.web.server.task.controller

import com.anymindgroup.web.server.task.model.TransactionPayload
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriUtils
import java.math.BigDecimal
import java.net.URI
import java.net.URLEncoder
import java.time.OffsetDateTime

class WalletControllerTests {
    private val client = WebTestClient.bindToController(WalletController()).build()

    @Test
    fun getWalletBalance() {
        val uri = UriComponentsBuilder.newInstance()
            .path("/wallet/balance_stat")
            .queryParam("startDateTime", OffsetDateTime.parse("2011-10-05T10:48:01+00:00"))
            .queryParam("endDateTime", OffsetDateTime.parse("2011-10-05T18:48:02+00:00"))
            .build()

        client.get()
        .uri(uri.toUriString())
            .exchange()
            .expectStatus()
            .is2xxSuccessful
    }

    @Nested
    inner class TopUpWallet {
        @Test
        fun topUpWallet() {
            client.post()
                .uri("/wallet/top_up")
                .bodyValue(topUpPayload)
                .exchange()
                .expectStatus()
                .is2xxSuccessful
        }

        private val topUpPayload = TransactionPayload(
            datetime = OffsetDateTime.now(),
            amount = BigDecimal("1.1")
        )
    }
}