package com.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * This is actually a private channel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackGroup(
    val id: String,
    val name: String,
    val members: List<String>
)
