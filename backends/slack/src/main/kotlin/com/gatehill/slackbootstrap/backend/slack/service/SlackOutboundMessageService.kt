package com.gatehill.slackbootstrap.backend.slack.service

import com.gatehill.slackbootstrap.backend.slack.config.SlackSettings
import com.gatehill.slackbootstrap.backend.slack.model.SlackGroup
import com.gatehill.slackbootstrap.service.OutboundMessageService
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

    override fun forward(channelName: String, message: String) {
        val channel = ensureChannelExists(channelName)
        checkParticipants(channel)
        slackOperationsService.sendMessage(channelName, message)
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
