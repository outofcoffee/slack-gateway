package com.gatehill.slackbootstrap.bot

import com.gatehill.slackbootstrap.backend.slack.SlackModule
import com.gatehill.slackbootstrap.http.HttpModule
import com.gatehill.slackbootstrap.util.VersionUtil

fun main(args: Array<String>) {
    println("Starting Slack Bootstrap [version ${VersionUtil.version}]")
    Bootstrap.build(HttpModule(), SlackModule()).start()
}
