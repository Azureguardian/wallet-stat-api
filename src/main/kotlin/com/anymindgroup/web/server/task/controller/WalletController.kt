package com.anymindgroup.web.server.task.controller

import com.anymindgroup.web.server.task.entity.payload.TransactionPayload
import com.anymindgroup.web.server.task.entity.view.BalanceByDateTimeView
import com.anymindgroup.web.server.task.service.WalletService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.OffsetDateTime
import javax.validation.Valid

@RestController
@RequestMapping("/wallet")
class WalletController(
    private val walletService: WalletService
) {

    @PostMapping(
        "/top_up",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun topUp(@RequestBody @Valid payload: TransactionPayload): Mono<ResponseEntity<Unit>> {
        return walletService.topUp(payload.toDto()).map {
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .build()
        }
    }

    @GetMapping("/history", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun balanceStat(
        @RequestParam("start_datetime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDateTime: OffsetDateTime,
        @RequestParam("end_datetime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDateTime: OffsetDateTime
    ): Flux<BalanceByDateTimeView> {
        return walletService.getBalanceByDateTimes(startDateTime, endDateTime)
            .map { dto ->
                BalanceByDateTimeView(
                    datetime = dto.dateTime,
                    amount = dto.balance
                )
            }
    }
}
