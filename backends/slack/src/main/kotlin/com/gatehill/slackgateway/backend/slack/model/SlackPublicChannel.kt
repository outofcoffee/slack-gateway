package com.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackPublicChannel(
    override val id: String,
    override val name: String,
    override val members: List<String>
) : SlackChannel
