package io.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelsListResponse(
    override val ok: Boolean,

    val channels: MutableList<io.gatehill.slackgateway.backend.slack.model.SlackPublicChannel>?,

    @JsonProperty("response_metadata")
    override val responseMetadata: io.gatehill.slackgateway.backend.slack.model.ResponseMetadata?

) : io.gatehill.slackgateway.backend.slack.model.ResponseWithStatus,
    io.gatehill.slackgateway.backend.slack.model.PaginatedResponse<io.gatehill.slackgateway.backend.slack.model.SlackPublicChannel> {
    override val innerResults
        get() = channels ?: mutableListOf()
}
