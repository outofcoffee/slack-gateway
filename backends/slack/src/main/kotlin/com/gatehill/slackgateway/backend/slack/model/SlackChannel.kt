package com.gatehill.slackgateway.backend.slack.model

interface SlackChannel {
    val id: String
    val name: String
    val members: List<String>
}
