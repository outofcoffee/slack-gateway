package com.gatehill.slackbootstrap.service

/**
 * Handles outbound messages.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface OutboundMessageService {
    fun forward(channelName: String, message: String)
}
