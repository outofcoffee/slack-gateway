package com.gatehill.slackgateway.bot

import com.gatehill.slackgateway.backend.slack.SlackModule
import com.gatehill.slackgateway.http.HttpModule
import com.gatehill.slackgateway.util.VersionUtil

fun main(args: Array<String>) {
    println("Starting Slack Gateway [version ${VersionUtil.version}]")
    Bootstrap.build(HttpModule(), SlackModule()).start()
}
