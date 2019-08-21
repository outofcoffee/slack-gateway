package com.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChannelsCreateResponse(
    override val ok: Boolean,
    override val channel: SlackPublicChannel
) : ResponseWithStatus, SlackChannelsCreateResponse
