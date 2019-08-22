package io.gatehill.slackgateway.backend.slack.model

interface PaginatedResponse<T> {
    val responseMetadata: io.gatehill.slackgateway.backend.slack.model.ResponseMetadata?
    val innerResults: MutableList<T>
}
