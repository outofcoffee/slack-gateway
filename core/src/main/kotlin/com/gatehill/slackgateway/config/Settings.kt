package com.gatehill.slackgateway.config

import com.gatehill.slackgateway.model.ChannelType

/**
 * Core application settings.
 */
object Settings : EnvironmentSettings() {
    val defaultCreateChannelType: ChannelType by lazy {
        getenv("DEFAULT_CREATE_CHANNEL_TYPE")?.let { ChannelType.parse(it) } ?: ChannelType.PRIVATE
    }
}
