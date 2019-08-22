package io.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupsCreateResponse(
    override val ok: Boolean,
    val group: SlackGroup
) : ResponseWithStatus,
    SlackChannelsCreateResponse {
    override val channel
        get() = group
}
