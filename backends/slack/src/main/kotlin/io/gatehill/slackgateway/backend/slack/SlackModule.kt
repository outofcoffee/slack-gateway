package io.gatehill.slackgateway.backend.slack

import com.google.inject.AbstractModule
import io.gatehill.slackgateway.asSingleton
import io.gatehill.slackgateway.service.OutboundMessageService

class SlackModule : AbstractModule() {
    override fun configure() {
        bind(OutboundMessageService::class.java)
            .to(io.gatehill.slackgateway.backend.slack.service.SlackOutboundMessageService::class.java).asSingleton()
        bind(io.gatehill.slackgateway.backend.slack.service.SlackOperationsService::class.java).asSingleton()
        bind(io.gatehill.slackgateway.backend.slack.service.SlackApiService::class.java).asSingleton()
    }
}
