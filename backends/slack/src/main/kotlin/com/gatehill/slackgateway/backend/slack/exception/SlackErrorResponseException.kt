package com.gatehill.slackgateway.backend.slack.exception

import com.gatehill.slackgateway.backend.slack.model.SlackErrorResponse

class SlackErrorResponseException : Throwable {
    val errorResponse: SlackErrorResponse?

    constructor(jsonResponse: String) : super(
        message = "Received error response: $jsonResponse"
    ) {
        this.errorResponse = null
    }

    constructor(errorResponse: SlackErrorResponse, jsonResponse: String) : super(
        message = "Received error response [error=${errorResponse.error}]: $jsonResponse"
    ) {
        this.errorResponse = errorResponse
    }
}
