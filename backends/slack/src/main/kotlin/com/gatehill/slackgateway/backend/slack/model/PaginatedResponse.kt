package com.gatehill.slackgateway.backend.slack.model

interface PaginatedResponse<T> {
    val responseMetadata: ResponseMetadata?
    val innerResults: MutableList<T>
}
