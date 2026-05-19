package com.bbangbat.common

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode
import com.bbangbat.common.exception.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BbangbatException::class)
    fun handleBbangbatException(e: BbangbatException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(code = e.errorCode.name, message = e.errorCode.message)

        return ResponseEntity.status(e.errorCode.httpStatus).body(error)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message =
            e.bindingResult.fieldErrors
                .firstOrNull()
                ?.defaultMessage
                ?: ErrorCode.INVALID_INPUT.message

        return ResponseEntity.badRequest().body(ErrorResponse(code = ErrorCode.INVALID_INPUT.name, message = message))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(code = ErrorCode.INVALID_INPUT.name, message = "${e.parameterName} 파라미터가 필요합니다.")

        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(code = ErrorCode.INVALID_INPUT.name, message = "${e.name} 파라미터의 타입이 올바르지 않습니다.")

        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(code = ErrorCode.INVALID_INPUT.name, message = e.message ?: ErrorCode.INVALID_INPUT.message)

        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception", e)

        return ResponseEntity.internalServerError().body(
            ErrorResponse(
                code = ErrorCode.INTERNAL_SERVER_ERROR.name,
                message = ErrorCode.INTERNAL_SERVER_ERROR.message,
            ),
        )
    }
}
