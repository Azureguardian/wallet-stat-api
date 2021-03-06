package com.azureguardian.wallet.stat.api.controller

import com.azureguardian.wallet.stat.api.entity.dto.BalanceByDateTimeDto
import com.azureguardian.wallet.stat.api.entity.payload.TransactionPayload
import com.azureguardian.wallet.stat.api.service.WalletService
import com.azureguardian.wallet.stat.api.util.truncateToHourEnd
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.OffsetDateTime

@Suppress("ReactiveStreamsUnusedPublisher")
@WebFluxTest(controllers = [WalletController::class])
class WalletControllerTests {

    @MockkBean
    lateinit var walletService: WalletService

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun getWalletBalance() {
        val dateStart = OffsetDateTime.parse("2011-10-05T10:48:01+00:00")
        val dateEnd = OffsetDateTime.parse("2011-10-05T18:48:02+00:00")
        val balanceByDateTimeDto = BalanceByDateTimeDto(
            dateTime = OffsetDateTime.now().truncateToHourEnd(),
            balance = BigDecimal("1001.1")
        )
        every { walletService.getBalanceByDateTimes(dateStart, dateEnd) } returns Flux.just(balanceByDateTimeDto)
        val uri = UriComponentsBuilder.newInstance()
            .path("/wallet/history")
            .queryParam("start_datetime", dateStart)
            .queryParam("end_datetime", dateEnd)
            .build()

        client.get()
            .uri(uri.toUriString())
            .exchange()
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].amount").isEqualTo(balanceByDateTimeDto.balance)
    }

    @Nested
    inner class TopUpWallet {
        @Test
        fun topUpWallet() {
            every { walletService.topUp(any()) } returns Mono.empty()
            client.post()
                .uri("/wallet/top_up")
                .bodyValue(topUpPayload)
                .exchange()
                .expectStatus()
                .isOk
        }

        @Test
        fun `transaction amount should be positive`() {
            client.post()
                .uri("/wallet/top_up")
                .bodyValue(
                    TransactionPayload(
                        datetime = OffsetDateTime.now(),
                        amount = BigDecimal("-10.1")
                    )
                )
                .exchange()
                .expectStatus()
                .isBadRequest
        }

        @Test
        fun `transaction date should not be null`() {
            client.post()
                .uri("/wallet/top_up")
                .bodyValue(
                    TransactionPayload(
                        datetime = null,
                        amount = BigDecimal("1.1")
                    )
                )
                .exchange()
                .expectStatus()
                .isBadRequest
        }

        private val topUpPayload = TransactionPayload(
            datetime = OffsetDateTime.now(),
            amount = BigDecimal("1.1")
        )
    }
}
