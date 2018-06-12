package com.gatehill.slackbootstrap.backend.slack.service

import com.fasterxml.jackson.module.kotlin.readValue
import com.gatehill.slackbootstrap.backend.slack.config.SlackSettings
import com.gatehill.slackbootstrap.backend.slack.model.GroupsCreateResponse
import com.gatehill.slackbootstrap.backend.slack.model.GroupsListResponse
import com.gatehill.slackbootstrap.backend.slack.model.SlackGroup
import com.gatehill.slackbootstrap.backend.slack.model.UsersListResponse
import com.gatehill.slackbootstrap.service.OutboundMessageService
import com.gatehill.slackbootstrap.util.jsonMapper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import javax.inject.Inject

/**
 * Allows an item to be locked or unlocked by a user.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class SlackOutboundMessageService @Inject constructor(private val slackApiService: SlackApiService) : OutboundMessageService {
    private val logger: Logger = LogManager.getLogger(SlackOutboundMessageService::class.java)

    private val users by lazy {
        val reply = slackApiService.invokeSlackCommand<UsersListResponse>("users.list")
        slackApiService.checkReplyOk(reply.ok)
        reply.members
    }

    init {
        // force evaluation of token on startup
        SlackSettings.slackUserToken
    }

    override fun forward(channelName: String, message: String) {
        val channel = ensureChannelExists(channelName)
        checkParticipants(channel)
        sendMessage(channelName, message)
    }

    private fun ensureChannelExists(channelName: String): SlackGroup {
        val reply = slackApiService.invokeSlackCommand<GroupsListResponse>("groups.list")
        slackApiService.checkReplyOk(reply.ok)

        reply.groups.firstOrNull { it.name == channelName }?.let {
            // channel already exists
            logger.debug("Channel $channelName already exists")
            return it

        } ?: run {
            // create the channel
            logger.debug("Channel $channelName does not exist - creating")
            return createChannel(channelName)
        }
    }

    private fun createChannel(channelName: String): SlackGroup {
        val reply = slackApiService.invokeSlackCommand<GroupsCreateResponse>("groups.create", mapOf(
                "name" to channelName,
                "validate" to "true"
        ))

        logger.debug("Create channel response: $reply")

        try {
            slackApiService.checkReplyOk(reply.ok)
            logger.debug("Channel $channelName created")
            return reply.group

        } catch (e: Exception) {
            throw RuntimeException("Error parsing channel creation response", e)
        }
    }

    private fun checkParticipants(channel: SlackGroup) {
        logger.debug("Checking participants of channel: ${channel.name}")

        val memberIds = SlackSettings.members.mapNotNull { memberUsername ->
            users.firstOrNull { user -> user.name == memberUsername }?.id
        }

        memberIds
                .filterNot { memberId: String -> channel.members.contains(memberId) }
                .forEach { memberId ->
                    logger.info("Inviting member $memberId to channel ${channel.name}")
                    inviteToChannel(channel, memberId)
                }
    }

    private fun inviteToChannel(channel: SlackGroup, memberId: String) {
        val reply = slackApiService.invokeSlackCommand<Map<String, Any>>("groups.invite", mapOf(
                "channel" to channel.id,
                "user" to memberId
        ))
        slackApiService.checkReplyOk(reply)
    }

    private fun sendMessage(channelName: String, message: String) {
        val params = jsonMapper.readValue<Map<String, *>>(message).toMutableMap()
        params += "channel" to channelName

        logger.info("Forwarding message to channel '$channelName': $message")

        val reply = slackApiService.invokeSlackCommand<Map<String, Any>>(
                commandName = "chat.postMessage",
                params = params,
                bodyMode = SlackApiService.BodyMode.JSON)

        slackApiService.checkReplyOk(reply)
    }
}
