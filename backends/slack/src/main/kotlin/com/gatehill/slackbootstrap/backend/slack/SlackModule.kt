package com.gatehill.slackbootstrap.backend.slack

import com.gatehill.slackbootstrap.asSingleton
import com.gatehill.slackbootstrap.backend.slack.service.SlackApiService
import com.gatehill.slackbootstrap.backend.slack.service.SlackOutboundMessageService
import com.gatehill.slackbootstrap.service.OutboundMessageService
import com.google.inject.AbstractModule

class SlackModule : AbstractModule() {
    override fun configure() {
        bind(OutboundMessageService::class.java).to(SlackOutboundMessageService::class.java).asSingleton()
        bind(SlackApiService::class.java).asSingleton()
    }
}
