package io.gatehill.slackgateway.backend.slack.model

import io.gatehill.slackgateway.model.ChannelType

interface SlackChannel {
    val channelType: ChannelType
    val id: String
    val name: String
    val members: List<String>
}
