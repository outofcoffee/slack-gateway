package io.gatehill.slackgateway.backend.slack

import com.google.inject.AbstractModule
import io.gatehill.slackgateway.asSingleton
import io.gatehill.slackgateway.backend.slack.service.SlackApiService
import io.gatehill.slackgateway.backend.slack.service.SlackOperationsService
import io.gatehill.slackgateway.backend.slack.service.SlackOutboundMessageService
import io.gatehill.slackgateway.service.OutboundMessageService

class SlackModule : AbstractModule() {
    override fun configure() {
        bind(OutboundMessageService::class.java).to(SlackOutboundMessageService::class.java).asSingleton()
        bind(SlackOperationsService::class.java).asSingleton()
        bind(SlackApiService::class.java).asSingleton()
    }
}
