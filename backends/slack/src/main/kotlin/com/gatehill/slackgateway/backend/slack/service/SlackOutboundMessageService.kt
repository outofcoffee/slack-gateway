package com.gatehill.slackgateway.backend.slack.service

import com.fasterxml.jackson.module.kotlin.readValue
import com.gatehill.slackgateway.backend.slack.config.SlackSettings
import com.gatehill.slackgateway.backend.slack.model.SlackChannel
import com.gatehill.slackgateway.config.Settings
import com.gatehill.slackgateway.exception.HttpCodeException
import com.gatehill.slackgateway.model.ChannelType
import com.gatehill.slackgateway.service.OutboundMessageService
import com.gatehill.slackgateway.util.jsonMapper
import com.google.common.cache.CacheBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * A Slack implementation of an outbound message service.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackOutboundMessageService @Inject constructor(
    private val slackOperationsService: SlackOperationsService
) : OutboundMessageService {

    private val logger: Logger = LogManager.getLogger(SlackOutboundMessageService::class.java)

    private val channelCache = CacheBuilder.newBuilder()
        .expireAfterWrite(SlackSettings.cacheSeconds, TimeUnit.SECONDS)
        .build<ChannelType, List<SlackChannel>>()

    override fun forward(raw: String, channelType: ChannelType?) {
        val message = jsonMapper.readValue<Map<String, *>>(raw).toMutableMap()
        forward(message, channelType)
    }

    override fun forward(message: Map<String, *>, channelType: ChannelType?) {
        val channelName = message["channel"] as String? ?: throw HttpCodeException(400, "No channel in message")
        val channel = ensureChannelExists(channelName, channelType)
        checkParticipants(channel)
        slackOperationsService.sendMessage(message)
    }

    private fun ensureChannelExists(channelName: String, channelType: ChannelType?): SlackChannel =
        searchForChannel(channelName, channelType)?.let { channel ->
            // channel already exists
            logger.debug("Channel $channelName (${channel.channelType}) already exists")
            channel

        } ?: run {
            // create the channel
            val createChannelType = channelType ?: Settings.defaultCreateChannelType
            logger.debug("Channel $channelName ($createChannelType) does not exist - creating")
            slackOperationsService.createChannel(channelName, createChannelType)
        }

    private fun searchForChannel(channelName: String, channelType: ChannelType?): SlackChannel? =
        channelType?.let {
            // search for a channel of a particular type
            searchCacheForChannel(channelName, channelType)
                ?: searchSlackApiForChannel(channelName, channelType)

        } ?: run {
            // search caches first, then call APIs
            searchCacheForChannel(channelName, ChannelType.PRIVATE)
                ?: searchCacheForChannel(channelName, ChannelType.PUBLIC)
                ?: searchSlackApiForChannel(channelName, ChannelType.PRIVATE)
                ?: searchSlackApiForChannel(channelName, ChannelType.PUBLIC)
        }

    private fun searchCacheForChannel(channelName: String, channelType: ChannelType): SlackChannel? =
        channelCache.getIfPresent(channelType)?.firstOrNull { it.name == channelName }

    private fun searchSlackApiForChannel(channelName: String, channelType: ChannelType): SlackChannel? {
        val channels = slackOperationsService.listChannels(channelType)
        channelCache.put(channelType, channels)
        return channels.firstOrNull { it.name == channelName }
    }

    private fun checkParticipants(channel: SlackChannel) {
        if (SlackSettings.inviteGroups.isEmpty() && SlackSettings.inviteMembers.isEmpty()) {
            logger.debug("Skipping check for participants of channel: ${channel.name} (${channel.channelType}) - no participants are configured")
            return
        }

        logger.debug("Checking participants of channel: ${channel.name} (${channel.channelType})")

        val memberIds = mutableListOf<String>()
        SlackSettings.inviteGroups.forEach { userGroupName ->
            try {
                slackOperationsService.fetchUserGroup(userGroupName)?.let { userGroup ->
                    memberIds += slackOperationsService.listUserGroupUserIds(userGroup)
                }
            } catch (e: Exception) {
                logger.warn("Error inviting user group: $userGroupName - continuing", e)
            }
        }

        memberIds += SlackSettings.inviteMembers.mapNotNull { memberUsername ->
            slackOperationsService.users.firstOrNull { user -> user.name == memberUsername }?.id
        }

        logger.info("Inviting ${memberIds.size} members to channel: ${channel.name} (${channel.channelType})")

        memberIds
            .filterNot { memberId: String -> channel.members.contains(memberId) }
            .forEach { memberId ->
                logger.info("Inviting member $memberId to channel ${channel.name} (${channel.channelType})")
                try {
                    slackOperationsService.inviteToChannel(channel, channel.channelType, memberId)
                } catch (e: Exception) {
                    logger.warn("Error inviting member $memberId to channel ${channel.name} (${channel.channelType}) - continuing", e)
                }
            }
    }
}
