package com.gatehill.slackbootstrap.backend.slack.config

import com.gatehill.slackbootstrap.config.EnvironmentSettings

/**
 * Slack driver settings.
 */
object SlackSettings : EnvironmentSettings() {
    val slackUserToken by lazy { getenv("SLACK_USER_TOKEN") ?: throw IllegalStateException("Slack user token missing") }

    val members: List<String> by lazy {
        getenv("SLACK_CHANNEL_MEMBERS")?.let { members ->
            members.split(",").map(String::trim)
        } ?: emptyList()
    }
}
