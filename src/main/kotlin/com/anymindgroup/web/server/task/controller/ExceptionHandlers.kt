package com.anymindgroup.web.server.task.controller

import com.anymindgroup.web.server.task.entity.BaseError
import com.anymindgroup.web.server.task.exceptions.IncorrectDateTimeException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import java.lang.Exception

@RestControllerAdvice
class ExceptionHandlers {

    @ExceptionHandler(IncorrectDateTimeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handler(e: IncorrectDateTimeException): ResponseEntity<BaseError> {
        return ResponseEntity
            .badRequest()
            .body(BaseError(e.message!!))
    }

    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handler(e: WebExchangeBindException): ResponseEntity<BaseError> {
        return ResponseEntity
            .badRequest()
            .body(BaseError(e.localizedMessage))
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handler(e: Exception): ResponseEntity<BaseError> {
        return ResponseEntity
            .internalServerError()
            .body(BaseError(e.localizedMessage))
    }
}
