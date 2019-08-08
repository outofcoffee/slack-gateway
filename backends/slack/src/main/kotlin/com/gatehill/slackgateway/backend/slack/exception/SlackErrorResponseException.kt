package com.gatehill.slackgateway.backend.slack.exception

import com.gatehill.slackgateway.backend.slack.model.SlackErrorResponse

class SlackErrorResponseException : Throwable {
    val errorResponse: SlackErrorResponse?

    constructor(jsonResponse: String, cause: Throwable? = null) : super(
        message = "Received error response: $jsonResponse",
        cause = cause
    ) {
        this.errorResponse = null
    }

    constructor(errorResponse: SlackErrorResponse, jsonResponse: String, cause: Throwable? = null) : super(
        message = "Received error response [error=${errorResponse.error}]: $jsonResponse",
        cause = cause
    ) {
        this.errorResponse = errorResponse
    }
}
