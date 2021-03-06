package io.gatehill.slackgateway.backend.slack.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.gatehill.slackgateway.model.ChannelType

/**
 * This is actually a private channel.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SlackGroup(
    override val id: String,
    override val name: String,
    override val members: List<String> = emptyList()
) : SlackChannel {
    override val channelType = ChannelType.PRIVATE
}
