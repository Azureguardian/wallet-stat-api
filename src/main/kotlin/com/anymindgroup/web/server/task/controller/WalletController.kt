package com.anymindgroup.web.server.task.controller

import com.anymindgroup.web.server.task.model.BalanceByHourDto
import com.anymindgroup.web.server.task.model.TransactionPayload
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.OffsetDateTime

@RestController
@RequestMapping("/wallet")
class WalletController {

    @PostMapping("/top_up")
    fun topUp(@RequestBody payload: TransactionPayload): Mono<Unit> {
        println("Hello world")
        return Mono.empty()
    }

    @GetMapping("/balance_stat", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun balanceStat(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDateTime: OffsetDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDateTime: OffsetDateTime
    ): Flux<BalanceByHourDto> {
        return Flux.fromArray(arrayOf(BalanceByHourDto(OffsetDateTime.now(), BigDecimal("1.0"))))
    }
}