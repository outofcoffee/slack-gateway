package io.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class UsersListResponse(
    override val ok: Boolean,

    val members: MutableList<SlackUser>,

    @JsonProperty("response_metadata")
    override val responseMetadata: ResponseMetadata?

) : ResponseWithStatus,
    PaginatedResponse<SlackUser> {
    override val innerResults
        get() = members
}
