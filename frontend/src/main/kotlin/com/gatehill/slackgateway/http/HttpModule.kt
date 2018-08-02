package com.gatehill.slackgateway.http

import com.gatehill.slackgateway.asSingleton
import com.gatehill.slackgateway.http.chat.HttpInboundMessageServiceImpl
import com.gatehill.slackgateway.service.InboundMessageService
import com.google.inject.AbstractModule

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class HttpModule : AbstractModule() {
    override fun configure() {
        bind(InboundMessageService::class.java).to(HttpInboundMessageServiceImpl::class.java).asSingleton()
    }
}
