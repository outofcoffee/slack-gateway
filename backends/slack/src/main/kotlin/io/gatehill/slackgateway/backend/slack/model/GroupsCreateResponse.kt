package io.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupsCreateResponse(
    override val ok: Boolean,
    val group: io.gatehill.slackgateway.backend.slack.model.SlackGroup
) : io.gatehill.slackgateway.backend.slack.model.ResponseWithStatus,
    io.gatehill.slackgateway.backend.slack.model.SlackChannelsCreateResponse {
    override val channel
        get() = group
}
