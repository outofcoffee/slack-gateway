package io.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupsListResponse(
    override val ok: Boolean,

    val groups: MutableList<io.gatehill.slackgateway.backend.slack.model.SlackGroup>?,

    @JsonProperty("response_metadata")
    override val responseMetadata: io.gatehill.slackgateway.backend.slack.model.ResponseMetadata?

) : io.gatehill.slackgateway.backend.slack.model.ResponseWithStatus,
    io.gatehill.slackgateway.backend.slack.model.PaginatedResponse<io.gatehill.slackgateway.backend.slack.model.SlackGroup> {
    override val innerResults
        get() = groups ?: mutableListOf()
}
