package com.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupsListResponse(
    override val ok: Boolean,

    val groups: MutableList<SlackGroup>?,

    @JsonProperty("response_metadata")
    override val responseMetadata: ResponseMetadata?

) : ResponseWithStatus, PaginatedResponse<SlackGroup> {
    override val innerResults
        get() = groups ?: mutableListOf()
}
