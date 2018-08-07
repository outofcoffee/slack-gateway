package com.gatehill.slackgateway.backend.slack.service

import com.fasterxml.jackson.module.kotlin.readValue
import com.gatehill.slackgateway.backend.slack.config.SlackSettings
import com.gatehill.slackgateway.backend.slack.model.SlackGroup
import com.gatehill.slackgateway.exception.HttpCodeException
import com.gatehill.slackgateway.service.OutboundMessageService
import com.gatehill.slackgateway.util.jsonMapper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * A Slack implementation of an outbound message service.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackOutboundMessageService
@Inject constructor(private val slackOperationsService: SlackOperationsService) : OutboundMessageService {
    private val logger: Logger = LogManager.getLogger(SlackOutboundMessageService::class.java)

    override fun forward(raw: String) {
        val message = jsonMapper.readValue<Map<String, *>>(raw).toMutableMap()
        forward(message)
    }

    override fun forward(message: Map<String, *>) {
        val channelName = message["channel"] as String? ?: throw HttpCodeException(400, "No channel in message")

        val channel = ensureChannelExists(channelName)
        checkParticipants(channel)
        slackOperationsService.sendMessage(message)
    }

    private fun ensureChannelExists(channelName: String): SlackGroup {
        slackOperationsService.listPrivateChannels().firstOrNull { it.name == channelName }?.let {
            // channel already exists
            logger.debug("Channel $channelName already exists")
            return it
        } ?: run {
            // create the channel
            logger.debug("Channel $channelName does not exist - creating")
            return slackOperationsService.createPrivateChannel(channelName)
        }
    }

    private fun checkParticipants(channel: SlackGroup) {
        if (SlackSettings.inviteGroups.isEmpty() && SlackSettings.inviteMembers.isEmpty()) {
            logger.debug("Skipping check for participants of channel: ${channel.name} (no participants are configured)")
            return
        }

        logger.debug("Checking participants of channel: ${channel.name}")

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

        logger.info("Inviting ${memberIds.size} members to channel: ${channel.name}")

        memberIds
            .filterNot { memberId: String -> channel.members.contains(memberId) }
            .forEach { memberId ->
                logger.info("Inviting member $memberId to channel ${channel.name}")
                try {
                    slackOperationsService.inviteToPrivateChannel(channel, memberId)
                } catch (e: Exception) {
                    logger.warn("Error inviting member $memberId to channel ${channel.name} - continuing", e)
                }
            }
    }
}
