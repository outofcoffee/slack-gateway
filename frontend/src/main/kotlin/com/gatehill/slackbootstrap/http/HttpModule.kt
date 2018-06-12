package com.gatehill.slackbootstrap.http

import com.gatehill.slackbootstrap.asSingleton
import com.gatehill.slackbootstrap.http.chat.HttpInboundMessageServiceImpl
import com.gatehill.slackbootstrap.service.InboundMessageService
import com.google.inject.AbstractModule

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class HttpModule : AbstractModule() {
    override fun configure() {
        bind(InboundMessageService::class.java).to(HttpInboundMessageServiceImpl::class.java).asSingleton()
    }
}
