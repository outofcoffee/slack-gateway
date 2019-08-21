package com.gatehill.slackgateway.config

import com.gatehill.slackgateway.model.ChannelType

/**
 * Core application settings.
 */
object Settings : EnvironmentSettings() {
    val defaultChannelType: ChannelType by lazy {
        getenv("DEFAULT_CHANNEL_TYPE")?.let { ChannelType.parse(it) } ?: ChannelType.PRIVATE
    }
}
