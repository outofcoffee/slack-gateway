package io.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.gatehill.slackgateway.model.ChannelType

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackPublicChannel(
    override val id: String,
    override val name: String,
    override val members: List<String>
) : SlackChannel {
    override val channelType = ChannelType.PUBLIC
}
