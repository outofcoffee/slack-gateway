package io.gatehill.slackgateway.http

import com.google.inject.AbstractModule
import io.gatehill.slackgateway.asSingleton
import io.gatehill.slackgateway.http.chat.HttpInboundMessageServiceImpl
import io.gatehill.slackgateway.service.InboundMessageService

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class HttpModule : AbstractModule() {
    override fun configure() {
        bind(InboundMessageService::class.java).to(HttpInboundMessageServiceImpl::class.java).asSingleton()
    }
}
