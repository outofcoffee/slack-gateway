package io.gatehill.slackgateway.exception

class HttpCodeException(val code: Int, message: String) : Throwable(
    "HTTP $code: $message"
)
