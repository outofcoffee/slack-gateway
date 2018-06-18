package com.gatehill.slackbootstrap.backend.slack.config

import com.gatehill.slackbootstrap.config.EnvironmentSettings

/**
 * Slack driver settings.
 */
object SlackSettings : EnvironmentSettings() {
    val slackUserToken by lazy { getenv("SLACK_USER_TOKEN") ?: throw IllegalStateException("Slack user token missing") }

    val inviteMembers: List<String> by lazy {
        getenv("SLACK_CHANNEL_MEMBERS")?.split(",")?.map(String::trim) ?: emptyList()
    }

    val inviteGroups: List<String> by lazy {
        getenv("SLACK_CHANNEL_GROUPS")?.split(",")?.map(String::trim) ?: emptyList()
    }
}
