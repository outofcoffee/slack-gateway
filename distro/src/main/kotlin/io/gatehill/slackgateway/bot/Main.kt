package io.gatehill.slackgateway.bot

import io.gatehill.slackgateway.config.Settings
import io.gatehill.slackgateway.http.HttpModule
import io.gatehill.slackgateway.util.VersionUtil

fun main(args: Array<String>) {
    println("Starting Slack Gateway [version ${VersionUtil.version}]")
    println("Default channel type: ${Settings.defaultCreateChannelType}")
    Bootstrap.build(HttpModule(), io.gatehill.slackgateway.backend.slack.SlackModule()).start()
}
