package com.gatehill.slackgateway.backend.slack

import com.gatehill.slackgateway.asSingleton
import com.gatehill.slackgateway.backend.slack.service.SlackApiService
import com.gatehill.slackgateway.backend.slack.service.SlackOperationsService
import com.gatehill.slackgateway.backend.slack.service.SlackOutboundMessageService
import com.gatehill.slackgateway.service.OutboundMessageService
import com.google.inject.AbstractModule

class SlackModule : AbstractModule() {
    override fun configure() {
        bind(OutboundMessageService::class.java).to(SlackOutboundMessageService::class.java).asSingleton()
        bind(SlackOperationsService::class.java).asSingleton()
        bind(SlackApiService::class.java).asSingleton()
    }
}
