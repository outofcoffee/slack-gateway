package com.gatehill.slackgateway.service

/**
 * Handles outbound messages.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface OutboundMessageService {
    fun forward(raw: String)
    fun forward(message: Map<String, *>)
}
