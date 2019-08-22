package io.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelsCreateResponse(
    override val ok: Boolean,
    override val channel: io.gatehill.slackgateway.backend.slack.model.SlackPublicChannel
) : io.gatehill.slackgateway.backend.slack.model.ResponseWithStatus,
    io.gatehill.slackgateway.backend.slack.model.SlackChannelsCreateResponse
