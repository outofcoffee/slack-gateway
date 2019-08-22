package com.gatehill.slackgateway.backend.slack.model

import com.gatehill.slackgateway.model.ChannelType

interface SlackChannel {
    val channelType: ChannelType
    val id: String
    val name: String
    val members: List<String>
}
