package com.gatehill.slackgateway.service

import com.gatehill.slackgateway.model.ChannelType

/**
 * Handles outbound messages.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface OutboundMessageService {
    fun forward(raw: String, channelType: ChannelType?)
    fun forward(message: Map<String, *>, channelType: ChannelType?)
}
