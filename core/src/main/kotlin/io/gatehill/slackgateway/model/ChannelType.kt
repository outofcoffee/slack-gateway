package io.gatehill.slackgateway.model

/**
 * Represents the types of channel to which a message can be sent.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
enum class ChannelType {
    PUBLIC,
    PRIVATE;

    companion object {
        fun parse(channelType: String): ChannelType = values()
            .firstOrNull { it.name.equals(channelType, ignoreCase = true) }
            ?: throw IllegalStateException("Unsupported channel type: $channelType")
    }

    override fun toString(): String {
        return name.toLowerCase()
    }
}
