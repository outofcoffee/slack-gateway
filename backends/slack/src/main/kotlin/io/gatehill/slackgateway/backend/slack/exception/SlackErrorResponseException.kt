package io.gatehill.slackgateway.backend.slack.exception

class SlackErrorResponseException : Throwable {
    val errorResponse: io.gatehill.slackgateway.backend.slack.model.SlackErrorResponse?

    constructor(jsonResponse: String, cause: Throwable? = null) : super(
        message = "Received error response: $jsonResponse",
        cause = cause
    ) {
        this.errorResponse = null
    }

    constructor(
        errorResponse: io.gatehill.slackgateway.backend.slack.model.SlackErrorResponse,
        jsonResponse: String,
        cause: Throwable? = null
    ) : super(
        message = "Received error response [error=${errorResponse.error}]: $jsonResponse",
        cause = cause
    ) {
        this.errorResponse = errorResponse
    }
}
