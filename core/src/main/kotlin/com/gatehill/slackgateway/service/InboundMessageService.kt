package com.gatehill.slackgateway.service

/**
 * Handles inbound messages.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface InboundMessageService {
    fun listenForEvents()
    fun stopListening()
}
