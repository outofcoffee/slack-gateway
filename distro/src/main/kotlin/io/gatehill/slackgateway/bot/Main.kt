package io.gatehill.slackgateway.bot

import io.gatehill.slackgateway.backend.slack.SlackModule
import io.gatehill.slackgateway.config.Settings
import io.gatehill.slackgateway.http.HttpModule
import io.gatehill.slackgateway.util.VersionUtil

fun main() {
    println("Starting Slack Gateway [version ${VersionUtil.version}]")
    println("Default channel type: ${Settings.defaultCreateChannelType}")
    Bootstrap.build(HttpModule(), SlackModule()).start()
}
