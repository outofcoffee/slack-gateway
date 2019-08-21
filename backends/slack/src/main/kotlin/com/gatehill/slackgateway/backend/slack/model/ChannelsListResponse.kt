package com.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelsListResponse(
    override val ok: Boolean,

    val channels: MutableList<SlackPublicChannel>?,

    @JsonProperty("response_metadata")
    override val responseMetadata: ResponseMetadata?

) : ResponseWithStatus, PaginatedResponse<SlackPublicChannel> {
    override val innerResults
        get() = channels ?: mutableListOf()
}
